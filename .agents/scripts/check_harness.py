#!/usr/bin/env python3
"""Offline harness checks; model invocations require --runtime and explicit --skill names."""
from __future__ import annotations

import argparse
from dataclasses import dataclass, field
import json
from pathlib import Path, PurePosixPath
import re
import sys
from urllib.parse import unquote, urlsplit

sys.dont_write_bytecode = True
try:
    import yaml
except ImportError:
    raise SystemExit('Install validation dependencies: python3 -m pip install -r .agents/requirements-dev.txt')


class UniqueLoader(yaml.SafeLoader):
    pass


def unique_mapping(loader, node, deep=False):
    mapping = {}
    for key_node, value_node in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in mapping:
            raise yaml.constructor.ConstructorError(None, None, f'duplicate key: {key}', key_node.start_mark)
        mapping[key] = loader.construct_object(value_node, deep=deep)
    return mapping


UniqueLoader.add_constructor(yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, unique_mapping)


def prose(text):
    """Retain line numbers while excluding frontmatter and fenced examples."""
    lines = text.splitlines()
    front = bool(lines and lines[0] == '---')
    fence = None
    result = []
    for index, line in enumerate(lines):
        if front:
            result.append('')
            if index and line == '---':
                front = False
            continue
        match = re.match(r'^\s*(?:>\s*)?(`{3,}|~{3,})', line)
        if match:
            marker = match[1]
            if fence is None:
                fence = (marker[0], len(marker))
            elif marker[0] == fence[0] and len(marker) >= fence[1]:
                fence = None
            result.append('')
        else:
            result.append('' if fence else line)
    return '\n'.join(result)


def without_code(text):
    return re.sub(r'(`+)[^`\n]*?\1', '', text)


def markdown_links(text):
    """Inline/image and reference links in repository Markdown, excluding code."""
    text = without_code(prose(text))
    definitions = {}
    for match in re.finditer(r'^ {0,3}\[([^\]]+)\]:\s*(<[^>]+>|\S+)', text, re.M):
        definitions[' '.join(match[1].lower().split())] = match[2].strip('<>')
    text = re.sub(r'^ {0,3}\[[^\]]+\]:.*$', '', text, flags=re.M)
    inline = re.compile(r'!?\[[^\]\n]*\]\(\s*(<[^>\n]+>|[^\s)]+)(?:\s+["\x27][^\n]*?["\x27])?\s*\)')
    for match in inline.finditer(text):
        yield match[1].strip('<>')
    text = inline.sub('', text)
    explicit = re.compile(r'!?\[([^\]\n]+)\]\[([^\]\n]*)\]')
    for match in explicit.finditer(text):
        key = ' '.join((match[2] or match[1]).lower().split())
        yield definitions.get(key, 'undefined-reference:' + key)
    text = explicit.sub('', text)
    for match in re.finditer(r'!?\[([^\]\n]+)\]', text):
        key = ' '.join(match[1].lower().split())
        if key in definitions:
            yield definitions[key]


def markdown_anchors(text):
    text = prose(text)
    result = set(re.findall(r'(?:id|name)=["\x27]([^"\x27]+)', text))
    seen = {}
    for heading in re.findall(r'^ {0,3}#{1,6}\s+(.+?)\s*#*\s*$', text, re.M):
        heading = re.sub(r'\[([^]]+)\]\([^)]*\)', r'\1', heading)
        slug = re.sub(r'[^\w\-\s]', '', re.sub(r'<[^>]+>', '', heading).strip().lower()).replace(' ', '-')
        count = seen.get(slug, 0)
        seen[slug] = count + 1
        result.add(slug + (f'-{count}' if count else ''))
    return result


@dataclass
class Skill:
    name: str
    path: Path
    description: str
    roles: set[Path] = field(default_factory=set)
    templates: set[Path] = field(default_factory=set)


class Validator:
    def __init__(self, root):
        self.root = Path(root).resolve()
        self.errors = []
        self.skills = {}
        self.edges = {}
        self.links = self.anchors = 0
        self.unprefixed = set()
        self.generated = set()
        self.used_generated = set()
        self.vendored = set()
        self.used_vendored = set()

    def error(self, path, message):
        try:
            path = Path(path).relative_to(self.root)
        except ValueError:
            pass
        self.errors.append(f'{path}: {message}')

    def load_yaml(self, path, text=None):
        try:
            value = yaml.load(path.read_text() if text is None else text, Loader=UniqueLoader)
            if not isinstance(value, dict):
                raise ValueError('expected a YAML mapping')
            return value
        except (OSError, ValueError, yaml.YAMLError, TypeError) as exc:
            self.error(path, f'invalid YAML: {exc}')
            return {}

    def frontmatter(self, path):
        text = path.read_text()
        match = re.match(r'\A---\r?\n(.*?)\r?\n---(?:\r?\n|$)', text, re.S)
        if not match:
            self.error(path, 'missing or unclosed YAML frontmatter')
            return {}
        data = self.load_yaml(path, match[1])
        if data.get('name') != path.parent.name or not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', str(data.get('name', ''))) or len(str(data.get('name', ''))) > 64:
            self.error(path, 'name must match its directory and use lowercase hyphenated words (maximum 64 characters)')
        if not isinstance(data.get('description'), str) or not data['description'].strip():
            self.error(path, 'description must be a nonempty string')
        if re.search(r'^\s*\[TODO:[^\n]*\]\s*$', prose(text), re.M):
            self.error(path, 'unfinished authoring placeholder')
        return data

    def exceptions(self):
        path = self.root / '.agents/harness-exceptions.json'
        try:
            data = json.loads(path.read_text())
            required = {'unprefixed_skills', 'generated_links'}
            if not isinstance(data, dict) or not required <= set(data) <= required | {'vendored_references'}:
                raise ValueError('only unprefixed_skills, generated_links, and vendored_references are supported')
            kinds = [('unprefixed_skills', {'name', 'reason'}), ('generated_links', {'source', 'target', 'reason'})]
            if 'vendored_references' in data:
                kinds.append(('vendored_references', {'source', 'reference', 'reason'}))
            for kind, fields in kinds:
                if not isinstance(data[kind], list):
                    raise ValueError(f'{kind} must be a list')
                for entry in data[kind]:
                    if not isinstance(entry, dict) or set(entry) != fields or not all(isinstance(v, str) and v.strip() for v in entry.values()):
                        raise ValueError(f'{kind} requires exact fields {sorted(fields)} and a reason')
                    if kind == 'unprefixed_skills':
                        name = entry['name']
                        if not re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', name) or name.startswith('ht-') or name in self.unprefixed:
                            raise ValueError(f'invalid, broad, duplicate, or unnecessary name exception: {name}')
                        self.unprefixed.add(name)
                    elif kind == 'vendored_references':
                        source, reference = entry['source'], entry['reference']
                        parts = PurePosixPath(source)
                        if parts.is_absolute() or '..' in parts.parts or re.search(r'[*?\[\]{}<>\\]', source) or not re.fullmatch(r'\.agents/skills/[^/]+/.+\.md', source) or not (self.root / source).is_file():
                            raise ValueError('vendored-reference exceptions must name an exact existing Markdown file inside a skill bundle')
                        pair = (source, reference)
                        if pair in self.vendored:
                            raise ValueError('duplicate vendored-reference exception')
                        self.vendored.add(pair)
                    else:
                        source, target = entry['source'], entry['target']
                        for value in [source, target]:
                            parts = PurePosixPath(value)
                            if parts.is_absolute() or '..' in parts.parts or re.search(r'[*?\[\]{}<>\\]', value) or urlsplit(value).scheme or '#' in value:
                                raise ValueError('generated-link exceptions must be exact relative paths without globs or fragments')
                        if not re.fullmatch(r'\.agents/skills/[^/]+/assets/.+', source) or not (self.root / source).is_file():
                            raise ValueError('generated-link exceptions must name an existing skill asset source file')
                        pair = (source, target)
                        if pair in self.generated:
                            raise ValueError('duplicate generated-link exception')
                        self.generated.add(pair)
        except (OSError, ValueError, TypeError) as exc:
            self.error(path, f'invalid exceptions: {exc}')

    def reference(self, source, target):
        # Exact reference text in an unmodified vendored file that is not a real dependency or is broken upstream.
        resolved = source.resolve()
        if resolved.is_relative_to(self.root) and (resolved.relative_to(self.root).as_posix(), target) in self.vendored:
            self.used_vendored.add((resolved.relative_to(self.root).as_posix(), target))
            return None
        if target.startswith('undefined-reference:'):
            self.error(source, target)
            return None
        scheme = urlsplit(target).scheme
        if scheme == 'file' or (len(scheme) == 1 and target[1:3] in {':/', ':\\'}):
            self.error(source, f'nonportable machine-local reference: {target}')
            return None
        if scheme or target.startswith('//'):
            return None
        if any(token in target for token in ['{{', '${', '<', '>', '…']):
            return None
        filepart, separator, fragment = target.partition('#')
        filepart = filepart.split('?', 1)[0]
        destination = (source.parent / unquote(filepart)).resolve() if filepart else source.resolve()
        if not destination.is_relative_to(self.root):
            self.error(source, f'local references must stay inside the workspace: {target}')
            return None
        pair = (source.relative_to(self.root).as_posix(), target)
        if pair in self.generated and not destination.exists():
            self.used_generated.add(pair)
            return None
        self.links += 1
        if not destination.exists():
            self.error(source, f'missing local reference: {target}')
            return None
        self.edges.setdefault(source, set()).add(destination)
        if separator and fragment and destination.suffix.lower() == '.md':
            self.anchors += 1
            if unquote(fragment) not in markdown_anchors(destination.read_text()):
                self.error(source, f'missing anchor: {target}')
        return destination

    def scan_references(self):
        files = {self.root / 'AGENTS.md', self.root / 'CLAUDE.md'}
        for folder in ['.agents', '.claude', 'docs']:
            files.update((self.root / folder).rglob('*.md'))
        for source in sorted(files):
            if not source.is_file():
                self.error(source, 'required instruction file is missing')
                continue
            text = source.read_text()
            for target in markdown_links(text):
                self.reference(source, target)
            relative = source.relative_to(self.root)
            if relative.parts[:2] == ('.agents', 'skills') and len(relative.parts) > 3:
                bundle = self.root.joinpath(*relative.parts[:3])
                for target in re.findall(r'(?<!`)`((?:references|assets|scripts|agents|templates)/[^`\n\s]+)`(?!`)', prose(text)):
                    # Literal support paths in these bundles are relative to the skill directory.
                    self.reference(bundle / 'SKILL.md', target)
            for target in re.findall(r'(?<!`)`((?:\.agents|\.claude)/(?:skills/)?[^`\n\s]+)`(?!`)', prose(text)):
                if any(token in target for token in ['<', '>', '*', '…', '{{']):
                    continue
                self.reference(self.root / 'AGENTS.md', target)
        for source in sorted((self.root / '.agents/skills').rglob('*')):
            if not source.is_file():
                continue
            if source.suffix in {'.js', '.mjs'}:
                for target in re.findall(r'(?:\bfrom\s*|\bimport\s*\(?\s*)["\x27](\.[^"\x27]+)["\x27]', source.read_text()):
                    self.reference(source, target)
            elif source.suffix == '.html':
                for target in re.findall(r'\b(?:src|href)=["\x27]([^"\x27]+)["\x27]', source.read_text()):
                    self.reference(source, target)
        for pair in self.generated - self.used_generated:
            self.error(self.root / '.agents/harness-exceptions.json', f'unused generated-link exception: {pair}')
        for pair in self.vendored - self.used_vendored:
            self.error(self.root / '.agents/harness-exceptions.json', f'unused vendored-reference exception: {pair}')

    def inventory(self, folder):
        result = {}
        if not folder.is_dir():
            self.error(folder, 'skill directory is missing')
            return result
        for bundle in sorted(folder.iterdir()):
            if not bundle.is_dir() or bundle.name.startswith('.'):
                continue
            path = bundle / 'SKILL.md'
            if not path.is_file():
                self.error(bundle, 'missing SKILL.md')
                continue
            data = self.frontmatter(path)
            result[bundle.name] = (path, data)
        return result

    def validate(self):
        self.exceptions()
        canonical = self.inventory(self.root / '.agents/skills')
        wrappers = self.inventory(self.root / '.claude/skills')
        if not canonical:
            self.error(self.root / '.agents/skills', 'no canonical workflows found')
        self.scan_references()
        index = self.root / '.agents/README.md'
        index_text = index.read_text() if index.exists() else ''
        for invoked in re.findall(r'`[$/]([a-z0-9]+(?:-[a-z0-9]+)*)(?=[\s`])', prose(index_text)):
            if invoked not in canonical:
                self.error(index, f'stale invocation name: {invoked}')
        for router in [self.root / 'AGENTS.md', self.root / 'CLAUDE.md', index]:
            if self.root / '.agents/authoring.md' not in self.edges.get(router, set()):
                self.error(router, 'must link the shared authoring contract')
        for name in sorted(canonical.keys() | wrappers.keys()):
            if name not in canonical:
                self.error(wrappers[name][0], 'orphaned Claude entry point has no canonical skill')
                continue
            path, data = canonical[name]
            if not name.startswith('ht-') and name not in self.unprefixed:
                self.error(path, 'project skill names require the ht- prefix or an exact documented exception')
            if name not in wrappers:
                self.error(path, 'missing matching Claude entry point')
            else:
                wrapper, meta = wrappers[name]
                if meta.get('description') != data.get('description'):
                    self.error(wrapper, 'description must match the canonical skill')
                if meta.get('user-invocable') is False:
                    self.error(wrapper, 'entry point hides the required slash command')
                if path not in self.edges.get(wrapper, set()):
                    self.error(wrapper, 'must link its canonical SKILL.md')
            if path not in self.edges.get(index, set()):
                self.error(index, f'unregistered canonical skill: {name}')
            for prefix in ['$', '/']:
                if not re.search(re.escape(prefix + name) + r'(?![\w-])', index_text):
                    self.error(index, f'missing invocation example: {prefix}{name}')
            skill = Skill(name, path, data.get('description', ''))
            skill.roles = {p for p in self.edges.get(path, set()) if p.parent == self.root / '.agents/roles'}
            sources = {path, *skill.roles}
            # Include templates advertised by this workflow's index section.
            for section in re.split(r'(?m)^## ', index_text):
                linked = {(index.parent / link.split('#')[0]).resolve() for link in markdown_links(section) if not urlsplit(link).scheme}
                if path in linked:
                    sources.add(index)
                    skill.templates.update(p for p in linked if self.is_template(p))
            for source in sources - {index}:
                skill.templates.update(p for p in self.edges.get(source, set()) if self.is_template(p))
            if name in wrappers:
                for role in skill.roles:
                    if role not in self.edges.get(wrappers[name][0], set()):
                        self.error(wrappers[name][0], f'missing applicable role link: {role.name}')
            metadata = path.parent / 'agents/openai.yaml'
            if metadata.exists():
                interface = self.load_yaml(metadata).get('interface', {})
                if not isinstance(interface, dict):
                    self.error(metadata, 'interface must be a mapping')
                elif 'default_prompt' in interface and not re.search(re.escape('$' + name) + r'(?![\w-])', str(interface['default_prompt'])):
                    self.error(metadata, 'default_prompt must invoke the current skill name')
                if isinstance(interface, dict):
                    for key in ['icon_small', 'icon_large']:
                        if key in interface:
                            if not isinstance(interface[key], str) or not interface[key].strip():
                                self.error(metadata, f'{key} must name an asset')
                            else:
                                self.reference(path, interface[key])
            self.skills[name] = skill
        for name in self.unprefixed - canonical.keys():
            self.error(self.root / '.agents/harness-exceptions.json', f'unused name exception: {name}')
        for role in (self.root / '.agents/roles').glob('*.md'):
            owners = [s for s in self.skills.values() if role in s.roles]
            if not owners or role not in self.edges.get(index, set()):
                self.error(role, 'orphaned role: link it from the index and its canonical skill')
            elif not any(owner.path in self.edges.get(role, set()) for owner in owners):
                self.error(role, 'role must link back to its canonical method')
        inbound = set().union(*self.edges.values()) if self.edges else set()
        for template in (self.root / '.agents/templates').glob('*.md'):
            if template not in inbound:
                self.error(template, 'orphaned shared template')
        return self

    def is_template(self, path):
        return path.is_relative_to(self.root / '.agents/templates') or (
            path.is_relative_to(self.root / '.agents/skills') and '/assets/templates/' in path.as_posix())


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[2], help='repository root (defaults to this script\'s repository)')
    parser.add_argument('--runtime', action='store_true', help='also run local read-only Codex and Claude probes; may consume model usage')
    parser.add_argument('--skill', action='append', default=[], help='explicit runtime skill name; repeat for multiple skills')
    args = parser.parse_args(argv)
    if args.runtime != bool(args.skill):
        parser.error('--runtime and at least one explicit --skill must be supplied together')
    validator = Validator(args.root).validate()
    if validator.errors:
        for error in validator.errors:
            print('FAIL:', error)
        return 1
    print(f'PASS: static validation — {len(validator.skills)} workflows, {validator.links} local references, {validator.anchors} anchors.', flush=True)
    if not args.runtime:
        print('NOT RUN: runtime discovery/invocation and behavioral testing (offline check only).')
        return 0
    if any(name not in validator.skills for name in args.skill):
        parser.error('--skill must name a registered canonical skill')
    from harness_runtime import run_probes
    return run_probes(validator, list(dict.fromkeys(args.skill)))


if __name__ == '__main__':
    raise SystemExit(main())
