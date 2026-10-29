"""Behavioral fixtures for offline policy and runtime routing checks; no model calls."""
import contextlib
import io
import json
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.dont_write_bytecode = True
SCRIPTS = Path(__file__).resolve().parents[1] / 'scripts'
sys.path.insert(0, str(SCRIPTS))
from check_harness import Skill, Validator
from harness_runtime import Unavailable, run_probes, verify_report


class HarnessFixtures(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name).resolve()
        self.name = 'ht-demo'
        self.skill = '.agents/skills/ht-demo/SKILL.md'
        self.wrapper = '.claude/skills/ht-demo/SKILL.md'
        front = '---\nname: ht-demo\ndescription: Review an example when requested.\n---\n\n'
        self.put(self.skill, front + '# Example\n')
        self.put(self.wrapper, front + '[Method](../../../.agents/skills/ht-demo/SKILL.md)\n')
        self.put('.agents/authoring.md', '# Authoring\n')
        for router in ['AGENTS.md', 'CLAUDE.md']:
            self.put(router, '[Authoring](.agents/authoring.md)\n')
        self.put('.agents/README.md', '# Index\n[Authoring](authoring.md)\n\n## Example\n[Method](skills/ht-demo/SKILL.md)\n`$ht-demo` `/ht-demo`\n')
        self.exceptions = {'unprefixed_skills': [], 'generated_links': []}
        self.save_exceptions()

    def put(self, name, text):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
        return path

    def append(self, name, text):
        path = self.root / name
        path.write_text(path.read_text() + text)

    def save_exceptions(self):
        self.put('.agents/harness-exceptions.json', json.dumps(self.exceptions))

    def check(self):
        return Validator(self.root).validate()

    def fails(self, message):
        result = self.check()
        self.assertTrue(result.errors)
        self.assertIn(message, '\n'.join(result.errors))

    def snapshot(self):
        return {str(p.relative_to(self.root)): p.read_bytes() for p in self.root.rglob('*') if p.is_file()}

    def test_valid_optional_role_template_and_metadata(self):
        before = self.snapshot()
        self.assertEqual([], self.check().errors)
        self.assertEqual(before, self.snapshot())

    def test_missing_claude_entry_point(self):
        shutil.rmtree(self.root / '.claude/skills/ht-demo')
        self.fails('missing matching Claude entry point')

    def test_frontmatter_directory_name_mismatch(self):
        path = self.root / self.skill
        path.write_text(path.read_text().replace('name: ht-demo', 'name: ht-other'))
        self.fails('name must match its directory')

    def test_duplicate_yaml_identity_fails(self):
        path = self.root / self.skill
        path.write_text(path.read_text().replace('name: ht-demo', 'name: ht-demo\nname: ht-other'))
        self.fails('duplicate key')

    def test_blank_description_fails(self):
        path = self.root / self.skill
        path.write_text(path.read_text().replace('description: Review an example when requested.', 'description: ""'))
        self.fails('description must be a nonempty string')

    def test_wrapper_description_drift_fails(self):
        path = self.root / self.wrapper
        path.write_text(path.read_text().replace('Review an example when requested.', 'Changed behavior.'))
        self.fails('description must match')

    def test_orphaned_claude_entry_point_fails(self):
        self.put('.claude/skills/ht-orphan/SKILL.md', '---\nname: ht-orphan\ndescription: Orphan\n---\n')
        self.fails('orphaned Claude entry point')

    def test_wrong_canonical_target_fails(self):
        self.put('.agents/wrong.md', '# Not the method\n')
        path = self.root / self.wrapper
        path.write_text(path.read_text().replace('skills/ht-demo/SKILL.md', 'wrong.md'))
        self.fails('must link its canonical SKILL.md')

    def test_missing_index_registration_fails(self):
        path = self.root / '.agents/README.md'
        path.write_text(path.read_text().replace('[Method](skills/ht-demo/SKILL.md)', 'No entry'))
        self.fails('unregistered canonical skill')

    def test_old_invocation_cannot_satisfy_index(self):
        path = self.root / '.agents/README.md'
        path.write_text(path.read_text().replace('`$ht-demo`', '`$ht-demo-old`'))
        self.fails('missing invocation example: $ht-demo')

    def test_extra_stale_invocation_is_not_hidden_by_valid_commands(self):
        self.append('.agents/README.md', 'Old command: `$ht-retired with arguments`.\n')
        self.fails('stale invocation name: ht-retired')

    def test_unprefixed_project_skill_requires_an_exact_exception(self):
        for base in ['.agents/skills', '.claude/skills']:
            (self.root / base / 'ht-demo').rename(self.root / base / 'demo')
        for p in self.root.rglob('*.md'):
            p.write_text(p.read_text().replace('ht-demo', 'demo'))
        self.fails('require the ht- prefix')
        self.exceptions['unprefixed_skills'].append({'name': 'demo', 'reason': 'Exact creator-approved legacy name.'})
        self.save_exceptions()
        self.assertEqual([], self.check().errors)

    def test_empty_catalog_does_not_pass(self):
        shutil.rmtree(self.root / '.agents/skills/ht-demo')
        shutil.rmtree(self.root / '.claude/skills/ht-demo')
        self.fails('no canonical workflows found')

    def test_broken_link_after_move_fails(self):
        self.append(self.skill, '[Old](references/deleted.md)\n')
        self.fails('missing local reference: references/deleted.md')

    def test_broken_anchor_fails(self):
        self.put('.agents/skills/ht-demo/references/guide.md', '# Guide\n')
        self.append(self.skill, '[Guide](references/guide.md#missing)\n')
        self.fails('missing anchor')

    def test_reference_links_and_duplicate_heading_anchors(self):
        self.put('.agents/skills/ht-demo/references/guide.md', '# Guide\n## State\n## State\n')
        self.append(self.skill, '[guide][ref]\n[ref]: references/guide.md#state-1\n')
        self.assertEqual([], self.check().errors)

    def test_fenced_example_paths_are_not_dependencies(self):
        self.append(self.skill, '```markdown\n[Example](missing.md)\n`references/missing.md`\n```\n')
        self.assertEqual([], self.check().errors)

    def test_literal_support_file_is_resolved_from_bundle(self):
        self.put('.agents/skills/ht-demo/references/nested.md', 'Use `scripts/missing.py`.\n')
        self.fails('scripts/missing.py')
        self.put('.agents/skills/ht-demo/scripts/missing.py', '# helper\n')
        self.assertEqual([], self.check().errors)

    def test_broken_javascript_import_fails(self):
        self.put('.agents/skills/ht-demo/scripts/run.mjs', "import { run } from './missing.mjs';\n")
        self.fails('./missing.mjs')

    def test_broken_html_asset_fails(self):
        self.put('.agents/skills/ht-demo/assets/index.html', '<script src="missing.js"></script>\n')
        self.fails('missing.js')

    def test_machine_specific_reference_fails(self):
        outside = Path(self.temp.name).parent / 'outside-harness-fixture.md'
        self.append(self.skill, f'[Outside]({outside})\n')
        self.fails('references must stay inside the workspace')

    def test_file_uri_cannot_bypass_portable_reference_check(self):
        self.append(self.skill, '[Machine file](file:///Users/example/private-guide.md)\n')
        self.fails('nonportable machine-local reference')

    def test_stale_metadata_prompt_fails(self):
        self.put('.agents/skills/ht-demo/agents/openai.yaml', 'interface:\n  default_prompt: "Use $demo"\n')
        self.fails('default_prompt must invoke the current skill name')

    def test_metadata_icon_resolves_from_canonical_bundle(self):
        self.put('.agents/skills/ht-demo/agents/openai.yaml', 'interface:\n  icon_small: ./assets/icon.svg\n')
        self.fails('assets/icon.svg')
        self.put('.agents/skills/ht-demo/assets/icon.svg', '<svg/>\n')
        self.assertEqual([], self.check().errors)

    def test_valid_role_template_wiring(self):
        self.add_role()
        self.assertEqual([], self.check().errors)
        workflow = self.check().skills[self.name]
        self.assertEqual({self.root / '.agents/roles/demo.md'}, workflow.roles)
        self.assertEqual({self.root / '.agents/templates/demo.md'}, workflow.templates)

    def add_role(self):
        self.put('.agents/roles/demo.md', '[Method](../skills/ht-demo/SKILL.md)\n[Template](../templates/demo.md)\n')
        self.put('.agents/templates/demo.md', '# Template\n')
        self.append(self.skill, '[Role](../../roles/demo.md)\n')
        self.append(self.wrapper, '[Role](../../../.agents/roles/demo.md)\n')
        self.append('.agents/README.md', '[Role](roles/demo.md)\n')

    def test_orphaned_role_and_template_fail(self):
        self.put('.agents/roles/unused.md', '# Unused\n')
        self.put('.agents/templates/unused.md', '# Unused\n')
        self.fails('orphaned role')
        self.fails('orphaned shared template')

    def test_missing_role_from_wrapper_fails(self):
        self.add_role()
        path = self.root / self.wrapper
        path.write_text(path.read_text().replace('[Role](../../../.agents/roles/demo.md)', ''))
        self.fails('missing applicable role link')

    def test_root_must_link_contract(self):
        self.put('CLAUDE.md', '# No authoring route\n')
        self.fails('must link the shared authoring contract')

    def test_exact_generated_output_exception(self):
        source = '.agents/skills/ht-demo/assets/templates/README.md'
        self.put(source, '[Output](generated.drawio)\n')
        self.exceptions['generated_links'].append({'source': source, 'target': 'generated.drawio', 'reason': 'Created in the output workspace by the documented generator.'})
        self.save_exceptions()
        self.assertEqual([], self.check().errors)

    def test_wildcard_exception_fails(self):
        self.exceptions['generated_links'].append({'source': '.agents/**', 'target': '*', 'reason': 'Ignore everything'})
        self.save_exceptions()
        self.fails('exact relative paths')

    def test_name_wildcard_exception_fails(self):
        self.exceptions['unprefixed_skills'].append({'name': '*', 'reason': 'Ignore names'})
        self.save_exceptions()
        self.fails('name exception')

    def test_unused_exception_fails(self):
        source = '.agents/skills/ht-demo/assets/templates/README.md'
        self.put(source, '# Output no longer linked\n')
        self.exceptions['generated_links'].append({'source': source, 'target': 'generated.drawio', 'reason': 'Previously generated output.'})
        self.save_exceptions()
        self.fails('unused generated-link exception')

    def test_exact_vendored_reference_exception(self):
        source = '.agents/skills/ht-demo/references/vendored.md'
        self.put(source, '[Upstream](#missing) and [example](path/to/file)\n')
        self.fails('missing anchor: #missing')
        self.exceptions['vendored_references'] = [
            {'source': source, 'reference': '#missing', 'reason': 'Broken in the unmodified upstream file.'},
            {'source': source, 'reference': 'path/to/file', 'reason': 'Example text, not a dependency.'}]
        self.save_exceptions()
        self.assertEqual([], self.check().errors)

    def test_vendored_reference_exception_is_exact_and_must_be_used(self):
        source = '.agents/skills/ht-demo/references/vendored.md'
        self.put(source, '[Upstream](#missing) and [other](#also-missing)\n')
        self.exceptions['vendored_references'] = [{'source': source, 'reference': '#missing', 'reason': 'Broken upstream.'}]
        self.save_exceptions()
        self.fails('missing anchor: #also-missing')
        self.put(source, '# Fixed upstream\n')
        self.fails('unused vendored-reference exception')

    def test_vendored_reference_wildcard_exception_fails(self):
        self.exceptions['vendored_references'] = [{'source': '.agents/skills/**/*.md', 'reference': '*', 'reason': 'Ignore all'}]
        self.save_exceptions()
        self.fails('exact existing Markdown file')

    def test_cli_offline_has_no_artifacts_or_model_calls(self):
        before = self.snapshot()
        result = subprocess.run([sys.executable, '-B', str(SCRIPTS / 'check_harness.py'), '--root', str(self.root)], capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        self.assertIn('offline check only', result.stdout)
        self.assertEqual(before, self.snapshot())

    def test_cli_runtime_requires_explicit_skill(self):
        result = subprocess.run([sys.executable, '-B', str(SCRIPTS / 'check_harness.py'), '--root', str(self.root), '--runtime'], capture_output=True, text=True)
        self.assertEqual(2, result.returncode)
        self.assertIn('explicit --skill', result.stderr)

    def test_runtime_missing_tools_is_not_a_pass(self):
        with patch('harness_runtime.shutil.which', return_value=None), contextlib.redirect_stdout(io.StringIO()) as output:
            code = run_probes(self.check(), [self.name])
        self.assertEqual(2, code)
        self.assertIn('NOT RUN:', output.getvalue())
        self.assertNotIn('PASS:', output.getvalue())

    def report(self, tool):
        return {'skill_name': self.name, 'verification_only': True, 'canonical_skill_path': str(self.root / self.skill),
                'loaded_skill_path': str(self.root / (self.skill if tool == 'codex' else self.wrapper)),
                'role_path': None, 'template_paths': []}

    def successful_invoke(self, command, root):
        tool = Path(command[0]).name
        report = json.dumps(self.report(tool))
        if tool == 'codex':
            return 0, json.dumps({'item': {'type': 'agent_message', 'text': report}}), ''
        self.assertIn('plan', command)
        self.assertIn('Read,Glob,Grep,Skill', command)
        return 0, '\n'.join([json.dumps({'type': 'system', 'subtype': 'init', 'slash_commands': [self.name]}),
                             json.dumps({'type': 'result', 'result': report, 'is_error': False})]), ''

    def mocked_runtime(self, invoke):
        catalog = {self.name: {'enabled': True, 'path': str(self.root / self.skill)}}
        with patch('harness_runtime.shutil.which', side_effect=lambda tool: '/fake/' + tool), patch('harness_runtime.codex_discovery', return_value=catalog), patch('harness_runtime.invoke', side_effect=invoke), contextlib.redirect_stdout(io.StringIO()) as output:
            code = run_probes(self.check(), [self.name])
        return code, output.getvalue()

    def test_runtime_verifies_both_sources_without_changes(self):
        before = self.snapshot()
        code, output = self.mocked_runtime(self.successful_invoke)
        self.assertEqual(0, code, output)
        self.assertEqual(2, output.count('PASS:'))
        self.assertEqual(before, self.snapshot())

    def test_runtime_timeout_is_not_a_pass(self):
        def timeout(*args):
            raise Unavailable('test timeout')
        code, output = self.mocked_runtime(timeout)
        self.assertEqual(2, code)
        self.assertNotIn('PASS:', output)

    def test_runtime_invalid_report_fails(self):
        code, output = self.mocked_runtime(lambda *args: (0, '{}', ''))
        self.assertEqual(1, code)
        self.assertIn('FAIL:', output)

    def test_runtime_role_and_template_evidence_must_match(self):
        self.add_role()
        skill = self.check().skills[self.name]
        report = self.report('codex')
        with self.assertRaisesRegex(ValueError, 'applicable role'):
            verify_report('codex', skill, report, self.root)
        report['role_path'] = str(self.root / '.agents/roles/demo.md')
        with self.assertRaisesRegex(ValueError, 'template paths'):
            verify_report('codex', skill, report, self.root)
        report['template_paths'] = [str(self.root / '.agents/templates/demo.md')]
        self.assertEqual(str(skill.path), verify_report('codex', skill, report, self.root))

    def test_runtime_can_verify_multiple_applicable_roles(self):
        self.add_role()
        second = self.put('.agents/roles/helper.md', '# Helper\n')
        skill = self.check().skills[self.name]
        skill.roles.add(second)
        report = self.report('codex')
        report['role_paths'] = [str(p) for p in skill.roles]
        report['template_paths'] = [str(p) for p in skill.templates]
        self.assertEqual(str(skill.path), verify_report('codex', skill, report, self.root))

    def test_runtime_detects_workspace_writes(self):
        def mutate(command, root):
            self.put('docs/unexpected.md', '# Unexpected workflow artifact\n')
            return self.successful_invoke(command, root)
        code, output = self.mocked_runtime(mutate)
        self.assertEqual(1, code)
        self.assertIn('changed workspace source files', output)

    def test_personal_mobile_exception_requires_full_bundle_parity(self):
        canonical = self.put('.agents/skills/mobile-prototype/SKILL.md', '# Original\n')
        personal = self.put('home/.claude/skills/mobile-prototype/SKILL.md', '# Original\n')
        skill = Skill('mobile-prototype', canonical, 'Prototype')
        report = {'skill_name': skill.name, 'verification_only': True, 'canonical_skill_path': str(canonical),
                  'loaded_skill_path': str(personal), 'role_path': None, 'template_paths': []}
        with patch('harness_runtime.Path.home', return_value=self.root / 'home'):
            self.assertIn('parity verified', verify_report('claude', skill, report, self.root))
            self.put('home/.claude/skills/mobile-prototype/extra.mjs', '// divergent support file\n')
            with self.assertRaisesRegex(ValueError, 'diverged'):
                verify_report('claude', skill, report, self.root)

    def test_personal_mobile_template_paths_map_to_equal_canonical_assets(self):
        canonical = self.put('.agents/skills/mobile-prototype/SKILL.md', '# Original\n')
        personal = self.put('home/.claude/skills/mobile-prototype/SKILL.md', '# Original\n')
        expected = self.put('.agents/skills/mobile-prototype/assets/templates/flow.json', '{}\n')
        inspected = self.put('home/.claude/skills/mobile-prototype/assets/templates/flow.json', '{}\n')
        skill = Skill('mobile-prototype', canonical, 'Prototype', templates={expected})
        report = {'skill_name': skill.name, 'verification_only': True, 'canonical_skill_path': str(canonical),
                  'loaded_skill_path': str(personal), 'role_path': None, 'template_paths': [str(inspected)]}
        with patch('harness_runtime.Path.home', return_value=self.root / 'home'):
            self.assertIn('parity verified', verify_report('claude', skill, report, self.root))


if __name__ == '__main__':
    unittest.main()
