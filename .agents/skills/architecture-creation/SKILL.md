---
name: architecture-creation
description: "Create or refine an implementation-ready architecture from approved product behavior, using a risk-triggered integrity review and durable decision records."
---

# Architecture Creation

Turn approved product behavior into a proportionate, implementation-ready
technical design. Use this skill for a new architecture baseline, an
architecture refinement, or a review that may change the design. Do not use it
to implement an approved architecture, to write a PRD, or to decide product
behavior that has not been approved.

## Authority and preparation

Before proposing technical decisions:

1. Read repository instructions, the current approved product source,
   chronological provenance, existing architecture documents, relevant ADRs,
   and the applicable templates.
2. Identify which source is current product truth and which records are only
   historical context. Do not let an earlier note silently replace an approved
   product decision.
3. Classify the work as a **new baseline** or an **architecture delta**.
   Inspect current implementation and build configuration only when they
   materially constrain the decision.
4. Stop and return to PRD refinement when product behavior is absent,
   ambiguous, or conflicting. Product-visible retention, privacy, scoring,
   lifecycle, and acceptance-criteria changes require product approval before
   architecture work continues.

Keep product behavior separate from technical realization. The architecture
selects ownership, contracts, persistence, platform integration, and
verification needed to deliver approved outcomes; it never quietly creates a
new user-visible rule.

## Visible architecture decision state

At the start of every material decision round, state these classifications:

- **Verified product truth** — approved behavior carried forward with source
  references.
- **Inherited architecture decisions** — applicable architecture and ADR
  decisions that remain in force.
- **Architecture decisions open now** — technical choices that materially
  affect the current design.
- **Product-definition blockers** — behavior that must return to PRD
  refinement.
- **Deliberately deferred** — technical decisions with a reason, later owner,
  and concrete reopening event.
- **Out of scope** — work excluded from this architecture pass.

Ask only the next one to three technical decisions that are material now.
Carry verified choices forward without asking again. Record alternatives only
where they help the creator understand a durable trade-off.

## Risk-triggered architecture integrity review

For every affected requirement, map the UI, domain, persistence, platform, and
verification responsibilities. Then apply only the review prompts that match
the requirement's risk; none of these patterns is mandatory for simple,
stateless behavior.

| Trigger | Architecture must make explicit |
| --- | --- |
| Time changes an outcome | The captured time snapshot, immutable stored boundary where later recomputation could differ, planner for future work, and late-command behavior. |
| Concurrent work can alter shared or balance-dependent state | Transaction owner/serialization scope, conditional writes, idempotency boundary, and a deterministic total order when result order affects an aggregate. |
| Exact quantity determines comparison or history | One storage representation, conversion boundary, validation, and historical snapshot policy; never leave incompatible exactness choices to separate layers. |
| A retained aggregate, score, badge, or entitlement needs explanation | The retained audit evidence, lifecycle/retention policy, and queryable link from aggregate to source records. |
| A lifecycle reaches a terminal state | State owner, transition guards, dependent open work, pending effects, retained records, and active versus historical UI visibility. |
| A committed result causes an external effect | Durable committed event, idempotency key or stable platform identity, dispatcher ownership, retry and terminal states, and in-app fallback. |
| Local-only or private data is promised | Every relevant storage domain and Android backup, export, transfer, or encryption configuration needed to honor the approved boundary. |
| A read-only or retired view differs from active work | Distinct read model/control policy so historical access cannot restore disallowed active actions. |

Use the smallest durable mechanism that satisfies the applicable outcome. Make
schema types, nullability, foreign keys, unique keys, indexes, valid state
combinations, and migration impact explicit whenever persistence is part of
the contract. State a focused verification strategy that can prove each
material invariant.

## Decision lock and deliverables

Before creating or materially revising architecture artifacts, present an
**Architecture Decision Lock** containing:

1. Source audit, work classification, and affected requirements.
2. Verified product truth and inherited architecture/ADR decisions.
3. Resolved architecture choices, material alternatives, and ownership.
4. Applicable integrity-review findings and the proportionate safeguards.
5. ADR impact, Mermaid diagram plan, requirement traceability, and focused
   verification.
6. Product blockers, deliberate deferrals, out-of-scope items, and an explicit
   `Unclassified architecture questions: None` result.

The lock passes only when no product blocker or unclassified architecture
question remains. Wait for explicit authorization before drafting or editing
architecture artifacts.

After authorization, use
[`architecture-template.md`](../../templates/architecture-template.md) for a
new baseline when appropriate, stored under `docs/architecture/`. Update only
affected sections for a delta.
Create a focused ADR from
[`adr-template.md`](../../templates/adr-template.md) only when a decision
establishes or supersedes a durable data, concurrency, lifecycle, privacy,
external-effect, or core-pattern contract. Small local design edits stay in
the architecture document. Store ADRs under `docs/adr/` as
`NNNN-kebab-case-title.md`, using the next unused four-digit number; create or
update `docs/adr/README.md` as their index when the first ADR is added.
Preserve accepted ADRs; supersede them with a new record rather than rewriting
their historical rationale.

Use Mermaid fenced blocks for behavioral diagrams. Choose a flowchart for
layers, dependencies, or navigation; a state diagram for lifecycles; and a
sequence diagram for transactions, races, retries, or external effects. Do
not use ASCII or raster diagrams. Keep package layouts as text trees and
schemas as tables when they are more precise.

## Validation and handoff

Before handoff, verify source traceability, internal links, ADR status and
linkage, applicable schema/lifecycle contracts, Mermaid fences and semantics,
and removal of template placeholders from authored decisions. Run
`git diff --check`; run the narrowest relevant project check only when the
change also affects implementation or build configuration.

Summarize the product source used, architecture decisions made, ADRs created
or intentionally omitted, validation performed, and deliberate deferrals.
Stop at the architecture boundary unless the creator separately requests an
implementation plan or code.
