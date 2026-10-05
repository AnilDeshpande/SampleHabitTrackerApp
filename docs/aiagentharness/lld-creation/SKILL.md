---
name: lld-creation
description: "Collaboratively create, refine, or review an Android low-level design from approved product, architecture, and ADR sources, resolving state ownership, work lifetime, framework wiring, and test evidence before formal drafting. Use for detailed design, not implementation alone."
---

# LLD Creation

Turn an approved architecture into a decision-complete Android low-level design
(LLD). The architecture supplies boundaries and invariants; the LLD closes the
platform and component contracts a coding agent would otherwise have to invent.
Do not redefine product behavior or silently repair accepted source documents.

Use this method for a new LLD, an affected-section refinement, or a review of an
existing LLD. The [LLD-Creator Agent](../lld-creator-agent.md) orchestrates the
conversation; the [LLD template](../../templates/lld-template.md) structures the
output. This is a repository-local skill, not an installed global entry point.

## 1. Audit sources and authorization

Before asking design questions:

1. Read repository instructions, the creator's current approved product source,
   architecture, applicable ADRs, the existing LLD, and relevant decision history.
   Inventory authority, status, scope, and protected artifacts. Historical notes
   explain decisions; they do not override current approved sources.
2. Inspect code, manifest, dependency versions, SDK levels, and test infrastructure
   only where they constrain the design. Implementation is evidence of current
   mechanics, not authority to change approved behavior.
3. Classify the work as **new LLD**, **refinement**, or **review** and identify the
   affected requirements and components. A review does not automatically authorize
   document edits; a refinement need not re-open the whole product.
4. Resolve discoverable facts through inspection and authoritative platform/library
   documentation. Check version and minimum-API support before recommending an API,
   database feature, or test runner. Ask about intent and trade-offs, not facts
   available from the repository.
5. Carry prior decisions and scoped drafting authorization forward. Record new
   behavior as a proposal until accepted. Missing/conflicting product outcomes go
   to product definition; contradictions in architecture/ADRs go to their owner
   with a concrete finding. Continue independent, unblocked design work.

## 2. Review breadth through four questions

Before drilling into one API, classify each area below as covered, needing a
decision, or not applicable with a reason. Read applicable sections of the
[Android design-review guide](references/android-design-review.md); use its
specific checks to fill the registers, not as a fixed questionnaire.

| Question | Required design result |
| --- | --- |
| Who keeps this state? | Per-screen ownership, saved-key/type/encoding register, initialization/restoration contract, target identity, route/ViewModel ownership, non-restored effects, and restoration checks. |
| How long does work live? | Scope/cancellation register, command input/result contracts, admission keys and release points, transaction order, after-commit recovery, foreground sequence, scheduled-work timing/budget, and lifecycle/race checks. |
| Who creates objects? | Construction decision and alternatives, instance lifetimes, framework-created-object/factory register, lazy/headless startup, clock/dispatcher wiring, callback cleanup, package placement, and test-graph entry paths. |
| What is real versus fake in tests? | Component seam register, actual enforcement mechanisms, database/executor/contention topology, scheduler coordination, test harness/API/engine matrix, and requirement-to-evidence mapping. |

Scale to the affected feature. A screen with no draft has no saved input keys;
a feature without notifications needs no outbox or dispatcher. State why a
register is inapplicable. Do not fill a template by inventing infrastructure.
Add feature-specific interfaces, exact representations, or persistence details
only where an inherited contract leaves a necessary implementation choice open.

## 3. Collaborate and keep visible decision state

At each material decision round, give a compact update using these categories:

- **Inherited and resolved:** current product/architecture/ADR choices and prior
  creator decisions, with sources.
- **Accepted here:** new accepted technical choices, rationale, consequence, and
  affected registers/verification.
- **Needs decision:** implementation-critical questions, clearly labeled proposals,
  or source conflicts still awaiting disposition.
- **External product/source questions:** exact issue, appropriate owner, and whether
  it blocks the affected LLD scope.
- **Deliberately deferred:** reason, owner, concrete reopening event, and why the
  choice is not needed to implement this scope.
- **Out of scope:** excluded behavior, implementation, and protected artifacts.

Ask the next one to three related material questions. For each, explain the gap,
recommendation, reasoning, meaningful alternatives, and downstream consequence.
When evidence cannot support a recommendation, explain the trade-off neutrally.
Do not re-ask inherited choices or use a long checklist as a questionnaire.

After a response, record the decision and its provenance, update affected
contracts, retire superseded questions, and expose newly relevant consequences.
Keep proposals separate from accepted decisions. A fix can need a second pass:
restoring an ID is incomplete until the command consumes it, and application
scope is incomplete until admission and effect lifetimes are specified.

Keep pre-lock reasoning in the conversation. Create temporary session notes only
when authorized, with their temporary status explicit. Do not persist a formal
LLD with implementation-critical holes just to defer them to the coding agent.

## 4. Pass the LLD Decision Lock before formal drafting

Present a lock containing:

1. Source audit, work classification, affected requirements, constraints, and
   protected documents.
2. Inherited and newly accepted choices, alternatives where material, ownership,
   rationale, and provenance.
3. Coverage of all four questions and each applicable register; justified
   not-applicable areas.
4. The cross-section consistency review: route → saved target identity → immutable
   command request → transaction check → durable result; admission versus deadlines
   and effects; shared dependencies across foreground/headless paths; tests versus
   the actual enforcement mechanism.
5. Source findings and accepted dispositions, ADR impact, focused verification,
   harness/API compatibility, and a Mermaid diagram plan only where useful.
6. External questions, deliberate deferrals, exclusions, and explicit
   `Implementation-critical questions: None` and
   `Unclassified LLD questions: None` results.

The lock fails while a relevant product/source conflict, unapproved behavior,
missing applicable contract, or implementation-critical question remains.
Never re-label a blocker as a deferral to pass it. Legitimate later questions
may remain only with their owner/event and an explanation of why this scope can
be implemented without resolving them.

Verify drafting authority after the lock passes. An existing explicit request
to create/refine the LLD is sufficient for its scope; do not demand a second
approval merely because the lock has a name. Ask only for missing decisions or
authorization, and explain the exact source of the requirement. Stay within any
active planning-only mode regardless of drafting authorization.

## 5. Draft, refine, and manage artifacts

Use the [LLD template](../../templates/lld-template.md) under the repository's
design-document location (`docs/design/` here). For a refinement, preserve the
existing structure and update affected contracts rather than mechanically
rewriting an accepted document to fit the latest template.

- Keep all four core sections, even if a section states that it is inapplicable.
  End each with its focused verification obligations. Replace instructional
  placeholders with accepted decisions, sourced inherited contracts, or justified
  not-applicable entries.
- Link the architecture and applicable ADRs. Cite inherited schema/invariants
  rather than copying them into a competing source of truth. Use precise input,
  output, ordering, failure, identity, and lifecycle contracts where needed.
- New durable data, concurrency, lifecycle, privacy, external-effect, or core-pattern
  contracts may need a focused ADR using the [ADR template](../../templates/adr-template.md)
  and repository numbering/index conventions. Local key names and routine wiring
  details alone do not demand ADRs. Preserve accepted records; explain when no new
  ADR is needed.
- Do not amend PRD, architecture, or accepted ADRs as incidental cleanup. Record
  discrepancies and owner dispositions. A source amendment or successor ADR
  requires the corresponding authorized scope.
- Use Mermaid for behavioral diagrams when they clarify a material relationship.
  Registers and package tables often suffice; diagrams are not a completion quota.
- When a proposal is approved, update the lock, sequence, finding status, registers,
  and proposed tests together. When temporary notes are promoted, carry their
  decisions forward, remove obsolete links, and delete the notes only if authorized.

No Habit Tracker values are reusable defaults. Its manual container, occurrence
names, admission keys, undo duration, notification identity, worker interval,
attempt limit, and API matrix are worked examples. Resolve corresponding choices
from each project's sources and creator decisions.

## 6. Validate and hand off

Use the guide's [handoff review](references/android-design-review.md#handoff-review)
to test coherence and implementation readiness, not merely heading presence.
For each material path, walk through recreation, screen departure, process death
on both sides of commit, relevant boundaries/races, and real test evidence.

Check links/anchors, source traceability, ADR linkage/status, route/key/class/work
registers, platform/tooling compatibility, and template placeholder removal.
Search for deleted-note references, obsolete proposals, resolved questions still
marked open, and inconsistent verification status. Run `git diff --check` and
review protected/unrelated changes. Run project checks only if implementation or
build configuration changed within separately authorized scope.

Report changed artifacts, decisions closed, actual validation performed, and
classified remaining external/deferred items. Distinguish documented test
obligations from tests executed. Do not label the document Accepted without creator
acceptance; a complete handoff awaiting review can be Ready for review.
Stop at the design boundary unless implementation is separately requested.
