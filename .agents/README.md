# AI harness index

Shared starting point for Claude and Codex. Select the workflow below and read its
canonical skill, applicable conversation role, and templates. This index provides
navigation; the linked instructions own behavior, decision gates, and authorization.
Use the project-local files linked here. Paths in invocation examples are relative
to the repository root; examples work without a registered skill shortcut.

## Product ideation

Discover agreed product intent when an app or product request is underspecified,
before product definition or implementation.

- **Inputs:** Creator's idea and any existing creator decisions or session records.
- **Deliverables:** Current Idea Brief and dated session notes in `docs/idea-briefs/`.
- **Instructions:** [Product Ideation](skills/product-ideation/SKILL.md) · [Ideator Agent](roles/ideator-agent.md).
- **Templates:** [Idea Brief](templates/idea-brief-template.md) · [Session notes](templates/ideation-session-notes-template.md).
- **Invoke:** “Read `.agents/roles/ideator-agent.md` and run ideation for my app idea.”

## PRD creation

Define observable product behavior from an approved Idea Brief or other current
approved product-intent source; use for creator-led product definition.

- **Inputs:** Approved product-intent source, decision provenance, and any existing PRD.
- **Deliverable:** PRD after the method's decision lock and drafting authorization.
- **Instructions:** [PRD Creation](skills/prd-creation/SKILL.md) · [PRD-Creator Agent](roles/prd-creator-agent.md).
- **Template:** [PRD](templates/prd-template.md).
- **Invoke:** “Read `.agents/roles/prd-creator-agent.md` and create a PRD from my approved Idea Brief.”

## Architecture creation, refinement, or review

Design technical ownership and contracts when product behavior is sufficiently
defined, or review/refine an existing architecture.

- **Inputs:** Approved PRD/product source, applicable architecture and ADRs, and material implementation constraints.
- **Deliverables:** Architecture under `docs/architecture/` and applicable durable decision records under `docs/adr/`.
- **Instructions:** [Architecture Creation](skills/architecture-creation/SKILL.md) · [Architect Agent](roles/architect-agent.md).
- **Templates:** [Architecture](templates/architecture-template.md) · [ADR](templates/adr-template.md), when applicable.
- **Invoke:** “Read `.agents/roles/architect-agent.md` and review the architecture against my approved PRD.”

## Android low-level design

Create, refine, or review detailed Android design from approved architecture,
resolving concrete state, work lifetime, wiring, and test contracts.

- **Inputs:** Approved product source, architecture, applicable ADRs, existing LLD/history, and relevant Android/tooling constraints.
- **Deliverables:** LLD under `docs/design/` and applicable ADRs; review findings for review-only work.
- **Instructions:** [LLD Creation](skills/lld-creation/SKILL.md) · [LLD-Creator Agent](roles/lld-creator-agent.md) · [Android review guide](skills/lld-creation/references/android-design-review.md).
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
