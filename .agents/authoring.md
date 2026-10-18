# Harness authoring standard

Read this before creating, editing, moving, or renaming a workspace skill,
conversation role, template, supporting resource, or harness entry point. Complete
the checks below before handoff. Both Codex and Claude use this same contract.

## Authoring checklist

1. Inspect the [shared index](README.md) and existing resources first. Identify the
   requested scope, inherited behavior, current source, and affected consumers.
   Reuse existing capabilities; create only the resources the requested work needs.
2. Define the purpose, when to use the capability, required inputs, outputs, and
   stopping boundary. Keep descriptions specific enough to select the right skill.
   Review these qualitatively; a passing parser cannot establish workflow quality.
3. Keep one canonical method in `.agents/skills/ht-<name>/SKILL.md`, with supporting
   files beside it. Match the directory and frontmatter names. Use lowercase words
   separated by hyphens. New project invocation names use the `ht-` prefix.
4. Add the matching Claude entry point under `.claude/skills/ht-<name>/SKILL.md`.
   Match its name and description to the canonical skill and link the canonical
   instructions. Resolve supporting resources from the canonical skill directory.
   Keep this entry point thin; review it for copied method rules and divergent gates.
5. When a conversation role is needed, keep it under `.agents/roles/`, link it from
   the canonical method, Claude entry point, and index, and link it back to the method.
   Keep shared document templates under `.agents/templates/`; skill-owned assets
   can remain bundled with the skill. Roles and templates are optional when the
   workflow does not need them. Do not add empty scaffolding to satisfy a checklist.
6. A conversation role guides the current collaborating agent. Create a native
   subagent or autonomous agent configuration only when explicitly required.
7. Update the index with purpose, triggers, inputs, outputs, canonical instructions,
   applicable role/templates, both invocation names, and a file-based fallback.
   Update existing invocation metadata when names change; metadata alone does not
   prove discovery. Check consumers when moving supporting resources.
8. Keep scope and authorization explicit. Packaging/naming work preserves method
   behavior and decision gates. Keep project documents and personal/global
   installations unchanged unless requested. Carry existing authorization forward;
   this standard adds no redundant permission or drafting gate.
9. Review exceptions in [the exact exception list](harness-exceptions.json).
   Every exception needs a concrete source/name, target where relevant, and reason.
   Do not replace failures with directory-wide ignores or wildcard exclusions.
10. Run validation, review the focused diff, and report the evidence below. Preserve
    unrelated work and remove obsolete references/aliases only within authorized scope.

## Minimal entry points

Canonical skill frontmatter (follow it with the actual method):

```yaml
---
name: ht-example
description: Explain the capability and when the creator should use it.
---
```

Claude entry point (add the applicable role link only when there is a role):

```markdown
---
name: ht-example
description: Explain the capability and when the creator should use it.
---

Read [the canonical skill](../../../.agents/skills/ht-example/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/ht-example/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
```

Do not create these example files unless that capability is actually requested.

## Validation and evidence

Use Python 3.11 with the pinned [development dependency](requirements-dev.txt).
From the repository root:

```bash
python3 -m pip install -r .agents/requirements-dev.txt
python3 -B -m unittest discover -s .agents/tests -p 'test_*.py'
python3 -B .agents/scripts/check_harness.py
git diff --check
```

The [offline checker](scripts/check_harness.py) discovers canonical skills and
Claude entry points from their directories. It checks frontmatter, naming,
registration, role wiring, existing invocation metadata, local Markdown links and
anchors, literal support paths, JavaScript imports, and HTML asset references.
It skips fenced examples and external URLs. It does not fetch external URLs,
execute skill scripts, validate arbitrary generated code, or judge method semantics.
Checks must pass on the existing harness as well as the changed resources.

For a new skill, or a change affecting names, routing, discovery, or behavior, run
local discovery/invocation checks for the affected skills:

```bash
python3 -B .agents/scripts/check_harness.py --runtime --skill ht-product-ideation
python3 -B .agents/scripts/check_harness.py --runtime --skill ht-prd-creation --skill ht-lld-creation
```

These are explicit, usage-consuming model calls through installed, authenticated
Codex and Claude CLIs. The [probe implementation](scripts/harness_runtime.py) uses
bounded read-only sessions with workflow execution prohibited. It verifies the
reported source, applicable role and templates and checks for workspace source
changes. Template evidence includes optional referenced templates and files in
the skill's bundled template directory. It never starts product sessions or runs setup/update commands. Missing
tools, authentication, connectivity, or usage availability are **NOT RUN**, with a
nonzero runtime exit status; they are never presented as a passing invocation.
Fix routing failures before claiming cross-tool readiness.

`mobile-prototype` is the existing naming exception. If Claude resolves its
personal copy, report that source; the probe also compares the complete personal
bundle against the repository bundle. Only after that comparison passes can
inspected personal template paths satisfy their corresponding canonical paths;
the evidence still identifies the personal source. Do not modify personal installations.
Generated-output exceptions name individual existing asset sources and exact
output links; remove an exception when its source/link disappears or it is no
longer needed.

Behavior changes additionally need a focused realistic scenario that exercises
the changed decision or outcome. Use an isolated temporary workspace and permitted
tools. A link check or read-only invocation does not prove behavioral correctness.
Do not run full workflows against project documents just to test invocation.

At handoff, distinguish **static checks**, **fresh discovery**, **invocation**, and
**behavioral testing**. For each, state passed/failed/not run, the affected scope,
actual source/exception observations, and limitations. Do not imply that tests
ran because they are listed as obligations. If a check cannot run, state the
concrete blocker and what readiness remains unverified.

CI runs fixture tests and offline validation for harness, routing, and relevant
Markdown changes. CI uses no model credentials and does not run runtime probes.
Its result catches mechanical regressions; qualitative checklist items remain
the author's and reviewer's responsibility. Required-merge status depends on
repository branch protection, which this mechanism does not change.
