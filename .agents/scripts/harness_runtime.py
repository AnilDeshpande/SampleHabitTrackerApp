"""Opt-in, bounded local invocation checks. No model credentials are used by static CI."""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import selectors
import shutil
import signal
import subprocess
import tempfile
import time


class Unavailable(Exception):
    pass


def stop(process):
    if process.poll() is None:
        if os.name == 'posix':
            os.killpg(process.pid, signal.SIGTERM)
        else:
            process.terminate()
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            if os.name == 'posix':
                os.killpg(process.pid, signal.SIGKILL)
            else:
                process.kill()
            process.wait()


def invoke(command, root, timeout=150):
    process = subprocess.Popen(command, cwd=root, text=True, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, start_new_session=True)
    try:
        stdout, stderr = process.communicate(timeout=timeout)
        return process.returncode, stdout, stderr
    except subprocess.TimeoutExpired:
        stop(process)
        raise Unavailable(f'local invocation timed out after {timeout}s')


def codex_discovery(executable, root):
    """Read the actual fresh app-server skill catalog, without a model call."""
    with tempfile.TemporaryFile() as log:
        process = subprocess.Popen([executable, 'app-server', '--stdio'], cwd=root,
                                   stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=log, start_new_session=True)
        selector = selectors.DefaultSelector()
        selector.register(process.stdout, selectors.EVENT_READ)
        buffer = b''

        def send(message):
            process.stdin.write((json.dumps(message) + '\n').encode())
            process.stdin.flush()

        def receive(identifier):
            nonlocal buffer
            deadline = time.monotonic() + 30
            while time.monotonic() < deadline:
                while b'\n' in buffer:
                    line, buffer = buffer.split(b'\n', 1)
                    try:
                        event = json.loads(line)
                    except ValueError:
                        continue
                    if event.get('id') == identifier:
                        if 'error' in event:
                            raise ValueError('app-server rejected skill discovery')
                        return event['result']
                if process.poll() is not None:
                    raise Unavailable('local app-server exited before returning discovery')
                for key, _ in selector.select(1):
                    chunk = os.read(key.fileobj.fileno(), 65536)
                    if chunk:
                        buffer += chunk
            raise Unavailable('local app-server discovery timed out after 30s')

        try:
            send({'id': 1, 'method': 'initialize', 'params': {'clientInfo': {'name': 'ht-harness-check', 'version': '1.0'}}})
            receive(1)
            send({'method': 'initialized'})
            send({'id': 2, 'method': 'skills/list', 'params': {'cwds': [str(root)], 'forceReload': True}})
            result = receive(2)
            return {skill['name']: skill for entry in result['data'] for skill in entry['skills']
                    if skill['scope'] == 'repo'}
        finally:
            stop(process)
            selector.close()
            process.stdin.close()
            process.stdout.close()


def parse_events(text):
    events = []
    for line in text.splitlines():
        try:
            value = json.loads(line)
            if isinstance(value, dict):
                events.append(value)
        except ValueError:
            pass
    return events


def final_report(tool, events):
    if tool == 'codex':
        messages = [e.get('item', {}).get('text', '') for e in events
                    if e.get('item', {}).get('type') == 'agent_message']
        if not messages:
            raise ValueError('no final Codex verification report')
        text = messages[-1]
    else:
        final = next((e for e in reversed(events) if e.get('type') == 'result'), {})
        if final.get('is_error') or not isinstance(final.get('result'), str):
            raise ValueError('no successful Claude verification report')
        text = final['result']
    try:
        report, _ = json.JSONDecoder().raw_decode(text[text.index('{'):])
        if not isinstance(report, dict):
            raise ValueError()
        return report
    except (ValueError, TypeError):
        raise ValueError('model did not return the requested JSON verification report')


def same_bundle(left, right):
    def hashes(folder):
        return {p.relative_to(folder).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
                for p in folder.rglob('*') if p.is_file() and '__pycache__' not in p.parts}
    return hashes(left) == hashes(right)


def verify_report(tool, skill, report, root):
    if report.get('skill_name') != skill.name or report.get('verification_only') is not True:
        raise ValueError('verification identity/verification_only mismatch')
    loaded = Path(report.get('loaded_skill_path', '')).resolve()
    expected = skill.path if tool == 'codex' else root / '.claude/skills' / skill.name / 'SKILL.md'
    source_note = ''
    personal_bundle = None
    if loaded not in {expected, expected.parent}:
        personal = Path.home() / '.claude/skills/mobile-prototype/SKILL.md'
        if tool == 'claude' and skill.name == 'mobile-prototype' and loaded in {personal, personal.parent}:
            if not same_bundle(personal.parent, skill.path.parent):
                raise ValueError('accepted personal mobile-prototype source has diverged from the canonical bundle')
            personal_bundle = personal.parent
            source_note = ' (accepted personal mobile-prototype source; bundle parity verified)'
        else:
            raise ValueError('unexpected loaded skill source')
    allowed_canonical = {skill.path}
    if personal_bundle:
        allowed_canonical.add(personal_bundle / 'SKILL.md')
    if Path(report.get('canonical_skill_path', '')).resolve() not in allowed_canonical:
        raise ValueError('canonical source does not match this repository or its verified personal exception')
    reported_role = report.get('role_path')
    if 'role_paths' in report:
        if not isinstance(report['role_paths'], list) or not all(isinstance(p, str) for p in report['role_paths']):
            raise ValueError('role_paths must be a list of inspected file paths')
        reported_roles = {Path(p).resolve() for p in report['role_paths']}
    else:
        reported_roles = {Path(reported_role).resolve()} if isinstance(reported_role, str) else set()
    if reported_roles != skill.roles:
        raise ValueError('applicable role does not match canonical links')
    templates = report.get('template_paths')
    if not isinstance(templates, list) or not all(isinstance(p, str) for p in templates):
        raise ValueError('template_paths must be a list of inspected file paths')
    inspected = {Path(p).resolve() for p in templates}
    corresponding = {skill.path.parent / p.relative_to(personal_bundle)
                     if personal_bundle and p.is_relative_to(personal_bundle) else p for p in inspected}
    missing = skill.templates - corresponding
    if missing or any(not p.is_file() for p in inspected):
        raise ValueError('applicable template paths were not verified: ' + ', '.join(str(p) for p in sorted(missing)))
    return str(loaded) + source_note


def source_snapshot(root):
    files = {root / 'AGENTS.md', root / 'CLAUDE.md'}
    for name in ['.agents', '.claude', '.github', 'docs', 'app', 'gradle']:
        files.update(p for p in (root / name).rglob('*') if p.is_file()
                     and not any(part in {'__pycache__', 'build', '.gradle'} for part in p.parts))
    return {p.relative_to(root).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in files if p.is_file()}


def run_probes(validator, names):
    root = validator.root
    snapshot = source_snapshot(root)
    statuses = []
    prompt = '''Invocation verification only. Do not begin, resume, or execute this workflow.
Do not create/edit files, ask product questions, run setup/update commands, or invoke
other workflows. Read the canonical instructions and applicable conversation role.
Inspect every template referenced by Markdown links or inline resource paths in
those instructions and in this workflow's shared-index section, including optional
templates. If the canonical bundle has assets/templates/, enumerate all its template
files too. Resolve canonical resource paths from the canonical skill directory.
Return a compact JSON object with skill_name, loaded_skill_path, canonical_skill_path,
role_path (null if none; use role_paths for multiple roles), template_paths, and verification_only=true. Use actual
absolute paths inspected. Report the actual entry-point source, including personal
skill resolution when applicable. This is a routing test, not product work.'''
    for tool in ['codex', 'claude']:
        executable = shutil.which(tool)
        if not executable:
            for name in names:
                print(f'NOT RUN: {tool} {name}: CLI unavailable', flush=True)
                statuses.append('NOT RUN')
            continue
        catalog = None
        if tool == 'codex':
            try:
                catalog = codex_discovery(executable, root)
            except (Unavailable, OSError, ValueError, KeyError) as exc:
                for name in names:
                    print(f'NOT RUN: {tool} {name}: discovery unavailable ({exc})', flush=True)
                    statuses.append('NOT RUN')
                continue
        for name in names:
            print(f'RUN: {tool} {name}: read-only discovery/invocation probe', flush=True)
            skill = validator.skills[name]
            try:
                if tool == 'codex':
                    found = catalog.get(name, {})
                    if not found.get('enabled') or Path(found.get('path', '')).resolve() != skill.path:
                        raise ValueError('fresh Codex catalog did not find the enabled canonical skill')
                    command = [executable, 'exec', '--ephemeral', '--sandbox', 'read-only', '--json', '--color', 'never', '$' + name + ' ' + prompt]
                else:
                    command = [executable, '-p', '--no-session-persistence', '--output-format', 'stream-json', '--verbose',
                               '--permission-mode', 'plan', '--tools', 'Read,Glob,Grep,Skill', '--allowedTools', 'Read,Glob,Grep,Skill',
                               '--settings', '{"disableAllHooks":true}', '--strict-mcp-config', '--mcp-config', '{"mcpServers":{}}',
                               '--max-budget-usd', '1.50', '/' + name + ' ' + prompt]
                code, stdout, stderr = invoke(command, root)
                events = parse_events(stdout)
                unavailable = re_unavailable(stdout + '\n' + stderr)
                if code or unavailable:
                    if unavailable:
                        raise Unavailable('authentication, connectivity, usage, or service availability prevented verification')
                    raise ValueError(f'CLI exited with status {code}')
                if tool == 'claude':
                    init = next((e for e in events if e.get('type') == 'system' and e.get('subtype') == 'init'), {})
                    if name not in init.get('slash_commands', []):
                        raise ValueError('fresh Claude catalog did not list the slash command')
                loaded = verify_report(tool, skill, final_report(tool, events), root)
                print(f'PASS: {tool} {name}: discovery/invocation/role/templates; source={loaded}', flush=True)
                statuses.append('PASS')
            except (Unavailable, FileNotFoundError) as exc:
                print(f'NOT RUN: {tool} {name}: {exc}', flush=True)
                statuses.append('NOT RUN')
            except (ValueError, OSError, KeyError) as exc:
                print(f'FAIL: {tool} {name}: {exc}', flush=True)
                statuses.append('FAIL')
            if source_snapshot(root) != snapshot:
                print('FAIL: runtime probe changed workspace source files; stopping without reverting user work.', flush=True)
                return 1
    print('NOT RUN: behavioral workflow testing (invocation probes only).', flush=True)
    return 1 if 'FAIL' in statuses else 2 if 'NOT RUN' in statuses else 0


def re_unavailable(text):
    import re
    return bool(re.search(r'(?i)(not logged in|authentication failed|unauthorized|login required|error sending request|connection (?:failed|refused|reset)|could not resolve|rate limit exceeded|quota exceeded|exceeded.*budget)', text))
