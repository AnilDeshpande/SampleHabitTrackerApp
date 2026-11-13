---
name: ht-increment-planning
description: "Turn a set of approved prototype screens (S-numbers) into an increment plan before any code: a scope cut by journey, a bottom-up layer plan taken from the architecture and LLD, a screen-state map with the UI state models, a design-lock check, a register of interpretations, and the creator's decisions in one table. Use when asked to start implementing screens or a feature increment; not for foundation work and not for writing code."
---

# Increment Planning

Go from "implement these screens" to a plan the creator can approve in one answer. An increment is the smallest
vertical outcome a user can see (for example create a habit and see it on the overview), built on top of the
foundation and the approved documents. This method decides scope, order, state models and open decisions. It writes
no production code.

Use it after a [foundation](../ht-android-foundation/SKILL.md) exists and before
[implementing the increment](../ht-increment-implementation/SKILL.md). The
[Implementation Engineer Agent](../../roles/implementation-engineer-agent.md) guides the conversation. This is a
repository-local skill.

## Inputs

- The screens, named by prototype S-number or journey, and the prototype handoff (screen inventory, transitions, state
  model per screen, copy, component mapping, theme).
- The approved PRD, architecture, LLD and ADRs, the foundation record, and the creator's scope decisions.
- The checkout, the host environment and the creator's Android Studio version.

## Outputs

- An [increment plan](../../templates/increment-plan-template.md), saved under `docs/implementation/` after approval.
- A [screen-state map](../../templates/screen-state-map-template.md) covering every state of every included screen.
- A decisions table (interpretations and choices) approved by the creator.

## Procedure

### 0. Orient and preflight (read-only)

1. Read the [harness index](../../README.md), the foundation record's "next implementation order", and the handoff
   sections for the named screens. Use the graph skill when a current graph exists; verify claims against source.
2. Confirm the repository state: current branch (never plan to commit on `main`), clean tree, other branches that hold
   related work. Treat a side branch's claims as unverified.
3. Read the environment facts that bound the plan: devices and tiers available, the creator's IDE and its supported
   AGP range, and which device the creator owns. Ask only for what cannot be read.
4. State any spoken term whose meaning is not certain, and confirm it before planning on it.

### 1. Scope cut

1. Choose the cut by **journey, not by screen count**: the first screens of a journey plus the screen the journey
   returns to. Read the transitions table to find the nearest closed loop.
2. Include **every state** of an included screen that the handoff lists: loading, read failure, empty, validation,
   saving, save failure, discard confirmation, restoration. These are cheap once the state model exists and costly to
   retrofit.
3. List what is excluded and what each excluded neighbor becomes (a placeholder destination that holds no state), so
   the whole navigation graph can be wired once and later increments replace one placeholder each.
4. Build shared machinery to its **final contract** when an included command depends on it (for example the
   transaction gate and catch-up). A stub that silently skips a contract is a defect that surfaces later.

### 2. Screen-state map and state models

1. For each included screen state, fill the [screen-state map](../../templates/screen-state-map-template.md): the
   prototype frame, trigger, owner class, state type, data source, commands and the test that proves it.
2. Classify every value with the LLD's ownership rule: durable (database), restorable input (`SavedStateHandle` under the
   LLD's key register), derived (recomputed from durable data, restorable input, clock and locale), transient effect
   (never restored or replayed), or visual-only (cursor, scroll).
3. Sketch the UI state and effect types now. Decide what is derived from what, so the layers below can supply it.

### 3. Layer plan

Plan bottom-up and give each layer a checkpoint the architecture's verification obligations name:

| Layer | Builds | Checkpoint |
| --- | --- | --- |
| 1. Domain rules | Models and pure rule services, no Android imports | Host tests for the cases the architecture's traceability table names |
| 2. Database and repository | Schema, migration, guards, DAOs, repository commands, read models | Real in-memory tests; the same scenarios on a file-backed database on the mandatory minimum API |
| 3. Execution and coordination | Command runner, lifecycle observer, tickers, ports, DI modules | Tests for admission, cancellation ownership and scheduling under virtual time |
| 4. ViewModels and UI | Theme, navigation, ViewModels, screens | ViewModel tests, Compose tests on every tier including the smallest screen, and the app run on devices |

Say what is deferred. Record the schema decision explicitly (a real migration when a version already exists).

### 4. Design lock

The UI layer needs a look. When the handoff has no approved visual direction:

1. Render the first screens as pictures: export the sketch pages with the draw.io CLI (check the page index; this
   version numbers pages from 1), then build a Material 3 mockup in HTML and render it with headless Chrome, in light
   and dark. Use the real copy and the real theme tokens.
2. Ask for the palette and for dynamic colour **at planning time**, not after a default was shown. Do not present the
   template colours as a proposal.
3. Record the choice (tokens, the open items) in a design note beside the prototype notes, and mark the mockup as an
   interpretation, not a pixel specification.

### 5. Interpretations register

Whenever the documents are silent (number parsing, an extra control, a label, ordering), write the choice, its
location, the alternative and your recommendation into the plan's decisions table. Ask the creator once, in one answer.
These choices travel into the pull request's "decisions to confirm" table. Classify any document text the plan finds stale or
conflicting as a **sync correction** (made true to what will be built, no change to approved behavior; done inside the increment in
its own commit) or a **substantive correction** (changes behavior, a contract or an ADR decision; a separate document change through
the owning workflow, with the creator's approval).

### 6. Approve and save

1. Present the plan: scope table, state map summary, layer table, design lock, decisions with recommendations and the
   process (branch name, commit per layer, evidence). Wait for approval.
2. Save it as `docs/implementation/<increment>-plan.md`. Do not start building under this skill.

## Rules

- Plan first and wait. Carry earlier approvals forward; do not re-ask settled choices.
- Do not give time estimates.
- The plan names its branch; it is never `main`. Branch from an up-to-date `main`. Do not stack on an unmerged pull request: if the
  increment depends on one, wait for its review, approval and merge. Nothing is merged without the creator's approval.
- Keep native sub-agents out, except the read-only verifier that [emulator validation](../ht-emulator-validation/SKILL.md) permits;
  other sub-agents only when the creator asks.

## Stopping boundary

Stop when the creator has approved the saved plan. Building starts under
[the implementation skill](../ht-increment-implementation/SKILL.md).
