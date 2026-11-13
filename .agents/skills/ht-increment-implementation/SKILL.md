---
name: ht-increment-implementation
description: "Build an approved increment bottom-up in gated layers (domain rules, database and repository, execution and coordination, ViewModels and UI), with one commit per layer on a feature branch, real tests with mutation checks, device-tier evidence, an interpretations register and a written increment record. Use after an increment plan is approved; it hands the UI layer to the screen skill and ends at pull request handoff."
---

# Increment Implementation

Carry an approved [increment plan](../ht-increment-planning/SKILL.md) to a verified, reviewable result. The
approved documents decide behavior; this method decides the order, the gates and the evidence, and it is the owner of
the whole flow from approved screens to a pull request.

Use it when an increment plan is approved. Do not use it to plan (that is
[planning](../ht-increment-planning/SKILL.md)), to lay a foundation
([foundation](../ht-android-foundation/SKILL.md)), or to change approved behavior. The
[Implementation Engineer Agent](../../roles/implementation-engineer-agent.md) guides the conversation. This is a
repository-local skill.

## The flow from screens to pull request

| Phase | Skill | Gate to leave it |
| --- | --- | --- |
| 0. Intake and preflight | [planning](../ht-increment-planning/SKILL.md), with [audit](../ht-foundation-audit/SKILL.md) for an unfamiliar repository | Screens named; documents read; environment and IDE state known; branch is not `main` |
| 1. Plan | [planning](../ht-increment-planning/SKILL.md) | Plan, state map, design lock and decisions approved and saved |
| 2. Layers 1 to 3 | this skill | Per layer: named tests green, a mutation check, device evidence where SQL is involved, one commit |
| 3. Layer 4 | [screens](../ht-screen-implementation/SKILL.md) | ViewModel and Compose tests green on every tier, including the smallest screen |
| 4. Look at it | [emulator validation](../ht-emulator-validation/SKILL.md) | Every included state seen on a device; process death, dark mode and large font checked |
| 5. Record | this skill | [Increment record](../../templates/increment-record-template.md) written |
| 6. Handoff | [pull request handoff](../ht-pr-handoff/SKILL.md) | Pull request opened with verified numbers; CI state reported |

## Inputs

- The approved plan, the screen-state map, the design note, and the creator's decisions.
- The foundation record, the architecture, the LLD and the ADRs.
- The branch the plan names, a clean working tree, and the devices the plan assigns to each tier.

## Outputs

- One commit per layer on the named branch, each with a message that says what the layer adds and what evidence backs it.
- Tests that fail when the rule they guard is removed, and shared scenarios that run identically on every tier.
- An [increment record](../../templates/increment-record-template.md) with verification, decisions, interpretations and
  what is not covered.
- A list of document corrections the work found (stale text, silent decisions) for the document owner.

## Operating boundary

- Never commit to `main`. Create or switch to the planned branch first. Do not push or open a pull request here; that is
  [pull request handoff](../ht-pr-handoff/SKILL.md), and a pull request request authorizes its own push.
- Carry earlier approvals forward. Native sub-agents only when the creator asks, with one standing exception: a read-only verifier
  sub-agent may run the independent check described in [emulator validation](../ht-emulator-validation/SKILL.md), which owns its
  role and rules. It never edits, and it drives only devices you name and started.
- Never merge, enable auto-merge or push to `main`. A pull request is merged only after review and the creator's approval.
- Never touch a device the creator started. Restore every setting you change on a device you started.
- Do not change approved behavior to make a test pass; report the conflict.

## Procedure

### Preconditions

Confirm the approved plan, the branch (not `main`), a clean tree, a current graph when one exists, and the devices.
Run the harness checker if documents were touched.

### The layer loop

For each layer in order, do all of the following before the next layer starts:

1. **Design note.** Say in a few lines what the layer builds, what it reuses and what it will not do. No separate approval
   unless the layer departs from the plan.
2. **Build** from the documents' specifications. Read the architecture's tables (schema, commands, planner, outbox, ticker)
   before writing; do not reconstruct them from memory.
3. **Tests, named from the documents.** Take the cases from the architecture's traceability and verification tables and the
   LLD's verification obligations. Share fixtures between the host and device tiers so both assert the same things.
4. **Mutation check.** Remove or break the one rule that is easiest to get subtly wrong, confirm the intended tests fail,
   restore it. A suite that passes first time is not yet evidence.
5. **Device evidence** when the layer is SQL-facing or UI-facing, through [device tiers](../ht-device-tiers/SKILL.md). Evidence for
   the minimum API counts only from that API; record API level and platform SQLite version.
6. **Commit.** Update the graph first when one exists. Use the attribution lines the session provides.
7. **Report** to the creator in one short table: what was built, tests and counts, evidence, anything surprising.

### Layer rules

| Layer | Rules specific to it |
| --- | --- |
| 1. Domain rules | Pure Kotlin, no Android imports. Cover each cadence's first period, timezone overlap, gap and date-line skip, daylight saving, decimal parsing, integer scoring, window boundaries. Assert zone ids exactly (`ZoneOffset.UTC.id` is `Z`). |
| 2. Database and repository | Take the schema, constraints and orders verbatim from the architecture. Generate the migration from the exported schema, not by hand. Make every trigger condition NULL-safe and test a NULL input. Commands acquire the gate first, then capture one time snapshot. Run the same scenarios on an in-memory and a file-backed database on the minimum API. Add a contention test with two database instances on real threads. Read the overview in one transaction and re-emit after each commit. |
| 3. Execution and coordination | A command's accepted job lives in the application scope; leaving a screen cancels only the wait. Release the admission key before effects run. Exactly one process-lifecycle observer. A ticker must not spin. Use fakes only for scheduling-only tests and say so; test rules against the real repository. Add a graph test that the real wiring resolves. |
| 4. ViewModels and UI | Follow [screens](../ht-screen-implementation/SKILL.md). |

### Cross-layer rules

- **Real versus fake.** Follow the LLD's seam register. Mock or fake only the documented external-effect ports, plus UI-mapping
  or scheduling-only tests. Never mock `Context`, Room DAOs or the repository in a rule test.
- **Read the failing artifact first** (test result XML, `dependencyInsight`, logcat) before editing. Separate **test-design
  faults** from **product defects** and report both honestly.
- **No virtual-time timeouts around real I/O.** Database work runs on real threads; wait for it in real time.
- **Dependencies.** Add the smallest set the layer needs, with a `because` note on any constraint. After any version or
  catalog change, read the resolved tree for surprises (consistent resolution can pin a transitive library).
- **IDE gate.** When Gradle files change, the creator's Android Studio needs a sync. Say so in the report, and verify its
  state with [the validation skill](../ht-emulator-validation/SKILL.md) if it is reported broken.
- **Interpretations register.** Each time a document is silent, add a row (choice, location, alternative) to the plan's
  decisions table and carry it into the record and the pull request.
- **Spec drift.** Keep a list of stale or conflicting document text found while building, and classify each. A **sync correction**
  makes a document true to what was built or to a decision the creator approved, without changing approved behavior (a stale name or
  mechanism, a missing cross-reference, an interpretation promoted to a recorded decision): make it in this increment, in its own commit
  ("Sync documents with the implementation"), and list it in the pull request. A **substantive correction** changes approved behavior,
  a contract or an ADR decision: do not edit it here; propose a separate document change through the owning workflow, with the
  creator's approval, and link it.

## Close-out

1. Run every build, test and lint command from the final state on every tier; record counts and accepted warnings.
2. Run [emulator validation](../ht-emulator-validation/SKILL.md) and curate the evidence outside the repository. Optionally start the
   read-only independent check. Confirm that the CI workflow covers what the increment adds (a new tier or module needs a CI job).
3. Write the [increment record](../../templates/increment-record-template.md) under `docs/implementation/`.
4. Report to the creator: what passed, what is held back, what is unverified, the decisions needing confirmation, and
   the next increment. Offer [pull request handoff](../ht-pr-handoff/SKILL.md).

## Stopping boundary

Stop when the layers are committed, the record is written and the creator has the report. Opening the pull request
is [a separate request](../ht-pr-handoff/SKILL.md).
