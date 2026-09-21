---
name: prd-creation
description: "Facilitate creator-led product definition from verified product-intent sources, resolve PRD-level behavior with provenance, and create a trustworthy PRD only after a zero-unclassified-question decision lock."
---

# PRD Creation

Convert verified product intent into a Product Requirements Document through a
creator-led product-definition session. Maintain the decision state; never
silently become the product owner or treat common product conventions as
requirements.

## Authority and preparation

Before asking a question:

1. Read repository instructions and every source the creator identifies as
   authoritative.
2. Inventory the sources and identify which one is the current approved
   product truth, which records chronological provenance or superseded
   decisions, and which instructions or templates constrain the method.
3. Check that required sources exist and that the current approved source is
   materially consistent with the provenance record. If a required artifact is
   absent or a material conflict cannot be resolved from source authority,
   identify the exact problem and stop. Do not reconstruct intent from a
   generic product, existing implementation, placeholder, or agent default.

Source-authority rules:

- Carry an explicit decision in the current approved product source forward
  without asking for redundant approval, and cite its source.
- Use chronological notes to explain provenance and replaced decisions, never
  to silently override the current approved source.
- When sources genuinely conflict, classify the item as **Needs
  clarification**, show the conflict, and ask the creator to resolve it.
- Record only inherited decisions or decisions the creator explicitly approves.
- When the creator rejects a recommendation or supplies counter-intent, retain
  the recommendation as discussion evidence and record the creator's final
  decision, rationale, consequence, and source as product truth.

## Visible decision state

Begin every question round with a compact state summary using exactly these
classifications. Do not let an item disappear between rounds; state why when
an item changes classification.

- **Inherited and resolved** — explicit, current approved source decisions
  carried forward with provenance.
- **Needs clarification** — PRD-level behavior that is ambiguous, incomplete,
  or conflicting.
- **Deliberately deferred** — a later decision, including its reason, named
  owner, and concrete reopening event.
- **Out of scope** — behavior explicitly excluded from the product or from
  product definition.

Separate product behavior from technical realization. Persistence-library
selection, database/schema design, generated-code tool selection, navigation,
architecture, implementation sequencing, code, and tests are normally
technical deferrals unless the creator explicitly makes one a product
requirement. Do not hide a product ambiguity inside such a deferral.

## Clarify breadth before depth

Audit the following coverage areas before drilling into details. Skip an area
when the verified source already resolves it; do not turn the audit into a
static questionnaire.

1. Product goal, V1 boundary, users, assumptions, and non-goals.
2. End-to-end user journeys and observable success journey.
3. Functional requirements and stable requirement identifiers.
4. Domain vocabulary, calculations, temporal rules, and boundary conditions.
5. Product states, transitions, errors, empty states, and recovery.
6. Interaction outcomes for creation, editing, recording progress, review,
   notifications, deletion, undo, and final deletion when applicable.
7. Local data lifecycle and historical-integrity requirements.
8. Meaningful privacy, accessibility, reliability, restart-survival, and
   observable-responsiveness expectations.
9. Requirement-level acceptance criteria and traceability to source decisions.
10. Explicit exclusions, technical deferrals, and conditional future questions.

### Risk-triggered exceptional-path audit

Apply the following prompts only when the approved product behavior makes them
relevant. They resolve user-visible outcomes; they do not select a database,
transaction pattern, background scheduler, or other technical mechanism.

- **Time-sensitive behavior:** What outcome applies at a period or deadline
  boundary, after a timezone change where relevant, and when an action arrives
  late?
- **Competing actions:** When undo, expiry, edits, or other opposite actions
  contend, which observable outcome wins and what recovery is available?
- **Retained outcomes:** When the product retains a balance, score, badge,
  entitlement, or history, what explanatory history must remain available?
- **External effects:** If a notification, reminder, export, or similar effect
  cannot happen immediately or the app restarts, what durable product outcome
  and later user-visible fallback apply?
- **Local-data promises:** If data is local or private, does that promise
  include cloud backup, export, and device-to-device transfer?

Record the creator-approved answers as requirements, domain boundaries,
state/recovery behavior, lifecycle rules, or non-functional expectations. The
later architecture step decides whether a conditional update, immutable time
boundary, transactional outbox, or platform configuration is needed to honor
that answer.

Ask only the next one to three related questions that are meaningful now. For
every **Needs clarification** item, provide:

- **Decision required** — the ambiguity preventing a complete PRD.
- **Recommendation** — one clearly labelled proposed resolution when evidence
  supports one.
- **Reasoning** — why it fits the inherited intent.
- **Trade-offs** — what it enables or constrains.
- **Downstream consequence** — what the PRD will say or require if approved.
- **Creator response requested** — ask the creator to approve, reject, or give
  counter-intent.

When evidence does not support a responsible recommendation, explain the
decision dimensions neutrally and ask for the creator's intent instead.

## Record each material decision

After every material creator response:

1. Record the decision, the creator's rationale in their words or a faithful
   concise summary, its immediate consequence, and a source reference to the
   creator turn.
2. Preserve prior decisions and their replacement trail rather than silently
   overwriting them.
3. Recalculate the four-state view, retire invalidated questions, and expose
   only newly meaningful questions.
4. Keep an auditable decision record in the conversation and in any
   creator-authorized session artifact. Do not claim persistence if no artifact
   was created.

Conditional topics reopen only when their recorded condition occurs. Do not
re-litigate them merely because they are familiar product features.

## PRD Decision Lock

When every coverage area is classified, do not draft or edit PRD artifacts
yet. Present a **PRD Decision Lock** containing:

1. Inherited decisions carried forward, with source references.
2. Every new creator-approved decision, rationale, consequence, and source
   turn.
3. Every deliberately deferred item, why it is deferred, its later owner, and
   its exact reopening event.
4. Every out-of-scope item.
5. A coverage audit across all ten areas, including each applicable
   exceptional-path prompt.
6. An explicit `Unclassified product questions` result.

The lock passes only when `Unclassified product questions: None`. Technical
deferrals are not hidden open product questions only when their reason, owner,
and concrete reopening event are present.

After a passing lock, stop and wait for the creator's explicit authorization to
create the PRD artifacts. If authorization arrives before the lock passes,
refuse the creation request and show the unresolved classifications.

## Create and validate a PRD after authorization

Use a creator-approved reusable PRD template when one exists. Before replacing
or materially restructuring a target artifact, inspect it and obtain creator
approval. The resulting PRD may contain only:

- current inherited decisions with source traceability;
- creator-approved decisions from this product-definition session;
- deliberate deferrals with reason, owner, and reopening event; and
- explicit out-of-scope items.

The PRD must give functional requirements stable identifiers; state observable
acceptance criteria rather than an implementation plan; trace every
requirement to an inherited or creator-approved decision; and report `Open
questions: None` truthfully.

Before handoff, verify that:

1. Every inherited requirement traces to the current approved source.
2. Every new requirement traces to an explicit creator decision.
3. Counter-intent replaced recommendations without losing discussion evidence.
4. The PRD contains no unapproved assumptions or product-specific information
   from a reusable artifact.
5. Every technical deferral includes reason, owner, and reopening event.
6. Explicit exclusions remain excluded.
7. `Open questions: None` and `Unclassified product questions: None` are
   truthful.

Stop at the product-definition boundary. Do not create architecture,
implementation plans, application code, tests, content packages, or pull
requests unless the creator separately asks for them.
