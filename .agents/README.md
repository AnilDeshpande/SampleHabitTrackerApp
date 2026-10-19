# AI harness index

Shared starting point for Claude and Codex. Select the workflow below and read its
canonical skill, applicable conversation role, and templates. This index provides
navigation; the linked instructions own behavior, decision gates, and authorization.
Use the project-local files linked here. The four `ht-` names distinguish this
repository's harness from personal/global skills. Paths in file-based invocation
examples are relative to the repository root.

## Creating or changing the harness

Before creating, editing, moving, or renaming skills, roles, templates, supporting
resources, or entry points, read the [shared authoring standard](authoring.md) and
complete its validation and evidence checklist before handoff. It applies to both
tools. Run the [offline checker](scripts/check_harness.py) for every harness change;
use its explicit runtime mode for affected discovery/invocation paths.

## Direct invocation

| Workflow | Codex | Claude Code |
| --- | --- | --- |
| Product ideation | `$ht-product-ideation` | `/ht-product-ideation` |
| PRD creation | `$ht-prd-creation` | `/ht-prd-creation` |
| Architecture | `$ht-architecture-creation` | `/ht-architecture-creation` |
| Android LLD | `$ht-lld-creation` | `/ht-lld-creation` |
| Repository knowledge graph | `$ht-graphify` | `/ht-graphify` |
| Mobile prototype | `$mobile-prototype` | `/mobile-prototype` |

Append your request or source documents, for example:

- Codex: `$ht-prd-creation Create a PRD from docs/idea-briefs/habit-tracker-v1-idea-brief.md`.
- Claude Code: `/ht-prd-creation Create a PRD from docs/idea-briefs/habit-tracker-v1-idea-brief.md`.

Codex reads the canonical bundles in `.agents/skills/`. Claude Code reads the
[project entry points](../.claude/skills/ht-product-ideation/SKILL.md) under
`.claude/skills/`, which load the same canonical bundles and conversation roles.
The role guides the current agent; it does not start a separate subagent.

If a command is missing, open a fresh session in this repository and check the
skill selector (`$` in Codex, `/` in Claude Code). Confirm the corresponding
`SKILL.md` exists in the tool's project skill directory and that skills are enabled.
The four renamed harness skills have no unprefixed project aliases. The file-based
examples below remain available as fallbacks.

`mobile-prototype` intentionally keeps its unprefixed name. Claude Code may select
`~/.claude/skills/mobile-prototype/` ahead of this repository's entry point. That
personal bundle matched the project bundle when this setup was introduced. The
read-only invocation check on 2026-10-18 loaded that personal copy; future changes
can diverge. Use the canonical file-based invocation below when
repository-local resolution is required. Personal/global installations are not
modified by this setup.

## Repository knowledge graph

Build, refresh, or query the repository knowledge graph for cross-file
orientation, planning, implementation impact, debugging, reviews, test
discovery, documentation, architecture, or handoffs. Use it to locate evidence,
then verify material claims against current source. Approved project documents
remain authoritative.

- **Inputs:** A concrete question, affected file/symbol/document/workflow, the
  current checkout, and an existing graph when querying.
- **Outputs:** A source-verified evidence map or impact trace; an explicit build
  produces local ignored graph artifacts.
- **Instructions:** [Repository knowledge graph](skills/ht-graphify/SKILL.md) ·
  [Operations runbook](skills/ht-graphify/references/operations.md).
- **Invoke:** Codex: `$ht-graphify Trace the Habit feature from PRD through
  implementation and tests.` Claude Code: `/ht-graphify What is affected by a
  change to MainActivity?`
- **File fallback:** “Read `.agents/skills/ht-graphify/SKILL.md` and use it to
  answer this repository question.”

When the graph is unavailable or stale, explain the limitation and use focused
direct source inspection. The workflow does not install hooks, watchers,
services, global configuration, or automatic model calls.

## Product ideation

Discover agreed product intent when an app or product request is underspecified,
before product definition or implementation.

- **Inputs:** Creator's idea and any existing creator decisions or session records.
- **Deliverables:** Current Idea Brief and dated session notes in `docs/idea-briefs/`.
- **Instructions:** [Product Ideation](skills/ht-product-ideation/SKILL.md) · [Ideator Agent](roles/ideator-agent.md).
- **Templates:** [Idea Brief](templates/idea-brief-template.md) · [Session notes](templates/ideation-session-notes-template.md).
- **Invoke:** “Read `.agents/roles/ideator-agent.md` and run ideation for my app idea.”

## PRD creation

Define observable product behavior from an approved Idea Brief or other current
approved product-intent source; use for creator-led product definition.

- **Inputs:** Approved product-intent source, decision provenance, and any existing PRD.
- **Deliverable:** PRD after the method's decision lock and drafting authorization.
- **Instructions:** [PRD Creation](skills/ht-prd-creation/SKILL.md) · [PRD-Creator Agent](roles/prd-creator-agent.md).
- **Template:** [PRD](templates/prd-template.md).
- **Invoke:** “Read `.agents/roles/prd-creator-agent.md` and create a PRD from my approved Idea Brief.”

## Architecture creation, refinement, or review

Design technical ownership and contracts when product behavior is sufficiently
defined, or review/refine an existing architecture.

- **Inputs:** Approved PRD/product source, applicable architecture and ADRs, and material implementation constraints.
- **Deliverables:** Architecture under `docs/architecture/` and applicable durable decision records under `docs/adr/`.
- **Instructions:** [Architecture Creation](skills/ht-architecture-creation/SKILL.md) · [Architect Agent](roles/architect-agent.md).
- **Templates:** [Architecture](templates/architecture-template.md) · [ADR](templates/adr-template.md), when applicable.
- **Invoke:** “Read `.agents/roles/architect-agent.md` and review the architecture against my approved PRD.”

## Android low-level design

Create, refine, or review detailed Android design from approved architecture,
resolving concrete state, work lifetime, wiring, and test contracts.

- **Inputs:** Approved product source, architecture, applicable ADRs, existing LLD/history, and relevant Android/tooling constraints.
- **Deliverables:** LLD under `docs/design/` and applicable ADRs; review findings for review-only work.
- **Instructions:** [LLD Creation](skills/ht-lld-creation/SKILL.md) · [LLD-Creator Agent](roles/lld-creator-agent.md) · [Android review guide](skills/ht-lld-creation/references/android-design-review.md).
- **Templates:** [LLD](templates/lld-template.md) · [ADR](templates/adr-template.md), when applicable.
- **Invoke:** “Read `.agents/roles/lld-creator-agent.md` and refine my LLD from the approved architecture.”

## Mobile prototyping

Map native Android/iOS screens, states, and navigation before design or building;
also resume an existing prototype. Final-look design is optional within this skill.

- **Inputs:** Available spec, architecture/design documents, platform scope, and any existing prototype with `notes/STATE.md`.
- **Deliverables:** Flow specification, draw.io sketch, decision records, and handoff in the chosen project prototype folder; optional design artifacts.
- **Instructions:** [Mobile Prototype](skills/mobile-prototype/SKILL.md). Separate conversation role: not applicable.
- **Templates:** Existing [workspace templates](skills/mobile-prototype/assets/templates/README.md) and [flow template](skills/mobile-prototype/assets/templates/flow.json), kept inside the skill bundle.
- **Invoke:** “Read `.agents/skills/mobile-prototype/SKILL.md` and prototype the screens from my product documents.”
