# Clarifications

Every question about the product, the answer proposed from the documents, and what was decided.
The questions and their IDs come from the skill's question bank (see *Sources*); the answers and
decisions are this project's.

Classes: A documented · B inferred · C conflict · D open, blocking · E open, cosmetic.
Status: approved → approved / corrected (A, B) · open → asked round n → decided / deferred (C, D) · assumed (E).

## How to read the IDs

A question ID is a group letter and a number, such as `P1` or `J3`.

| Prefix | Group |
|---|---|
| P | Product and scope |
| J | Journeys |
| N | Navigation and structure |
| S | States and data |
| A | Access and platform |
| T | Technical constraints that change the experience |
| C | Content |
| V | Visual direction |
| X | Accessibility and compliance |

- **The number** identifies the question within its group. It is not a priority: `P1` is the
  first Product and scope question in the question bank, not the most important one.
- **Grouped and ranged IDs**: `P1/P2` is questions P1 and P2, answered together; `J1–J3` is J1,
  J2 and J3.
- **Where they come from**: the IDs and the questions are the skill's question bank, the same in
  every project. A question this project added takes the next free number in its group (for
  example `S8`) and is marked *added* in the summary. The answers, classes and decisions are this
  project's.
- **Not to be confused with**:
  - the *Class* column (A–E), which says where an answer came from: class A is "documented",
    not the Access group;
  - frame numbers S1, S2, … on the sketch (`flow.drawio`) and in the handoff, which number screen
    states. In this file `S1` is always the States and data question; a frame is written
    "frame S1";
  - fact IDs F01, F02, …, which cite the fact register in `notes/INPUTS.md`.

## Summary

| ID | Question | Class | Status |
|---|---|---|---|
| P1/P2 | User and goal | A | approved |
| P3 | Prototype scope | B | approved |
| P4 | Success criterion | A | approved |
| P5 | Platform | A | approved |
| P6 | Sketch style | D | decided |
| J1–J3 | Primary journey and entry/return | A | approved |
| J4 | Secondary journeys | A | approved |
| J5 | First run | B | approved |
| N1/N2/N4 | Navigation structure | A | approved |
| N3 | Back with unsaved changes | D | decided |
| S1/S2/S6 | Visible content, empty/waiting/current states | A | approved |
| S3/T1 | Loading and slow actions | B | approved |
| S4 | Local read/save failure recovery | D | decided |
| S5/S7 | Offline and partial success | A | approved |
| S8 | Late-progress continuation (added) | D | decided |
| A1/A2 | Access and permissions | A | approved |
| A3/A4 | Form factor | E | assumed |
| A5 | Minimum OS | A | approved |
| T2/T4 | Background and freshness | A | approved |
| T3/T5 | Limits and validation | A | approved |
| C1/C2 | Copy and language | E | assumed |
| C3 | Formatting | A | approved |
| C4/X2 | Privacy and regulatory flows | A | approved |
| V1–V4 | Visual defaults | E | assumed |
| X1 | Accessibility | A | approved |
| T6 | Points and continuity (added) | A | approved |
| S9 | Retirement and restoration (added) | A | approved |

## Items

### P1/P2 — User and goal

Class: A

Evidence: F01/F02

Proposal: One person builds measurable personal habits by creating, tracking and reviewing them.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### P3 — Prototype scope

Class: B

Evidence: F01–F03/F29; inferred full scope from supplied V1 documents

Proposal: Cover full V1: create all cadences, record/correct progress, outcomes/history, target/name edit, notification/fallback, retirement/undo and retired history; include relevant recovery/restoration states. Exclude login/sync/export/iOS/extra badges and quiet-hours controls.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### P4 — Success criterion

Class: A

Evidence: F03

Proposal: Reviewer completes core loop, sees success/miss and target effects, undoes retirement and finds saved history after restart.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### P5 — Platform

Class: A

Evidence: F21; user Inputs approval

Proposal: Android only (user confirmed); Material 3, minimum API 24, API 33+ notification branch.

Status: approved

Decision: User confirmed Android only at Inputs stop, 2026-10-11.

### P6 — Sketch style

Class: D

Evidence: User preference; not a document question

Proposal: Choose wireflow, click-through, or both.

Status: decided

Decision: User explicitly chose both wireflow and click-through, 2026-10-11.

### J1–J3 — Primary journey and entry/return

Class: A

Evidence: F02/F09/F23

Proposal: Launch Overview → Create habit → Overview → Record progress → Overview → Habit review. Notification enters Review with Overview parent. Saves visibly update committed data.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### J4 — Secondary journeys

Class: A

Evidence: F09/F10/F16/F18/F19

Proposal: Edit name/target; correct total; acknowledge misses; cancel retirement, timely undo, late undo and retained retired review.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### J5 — First run

Class: B

Evidence: F12/F23; no onboarding destination exists

Proposal: Begin at no-habits Overview with Create habit; no separate onboarding.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### N1/N2/N4 — Navigation structure

Class: A

Evidence: F23/F24

Proposal: Overview is the start and hub; form, progress sheet, review, retirement dialog and retired list are nested destinations; notification Back goes to Overview. No added tab destinations.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### N3 — Back with unsaved changes

Class: D

Evidence: F25/F26; restoration defines retained destination input but not intentional navigation-away policy

Proposal: Recommend discard confirmation for dirty create/edit/progress drafts; untouched screens dismiss normally. Submitted saves continue independently. Needs user choice: discard confirmation versus retained draft on leaving.

Status: decided

Decision: User approved recommendation, 2026-10-11: Discard / Keep editing confirmation for changed create, edit and progress forms; untouched screens leave normally. Accepted saves continue independently.

### S1/S2/S6 — Visible content, empty/waiting/current states

Class: A

Evidence: F11/F12/F15/F16/F20/F32

Proposal: Show required Overview values and distinguish open, first-period waiting, Custom off-day and timezone gap. Review shows immutable chronological results, effective/superseded targets and badge; acknowledge on open. Empty Overview offers create/retired history. Refresh on catch-up/boundaries.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### S3/T1 — Loading and slow actions

Class: B

Evidence: F26; installed Android conventions § Feedback and overlays

Proposal: Show loading/saving if work exceeds 300 ms; disable relevant duplicate Save during transaction, preserve raw inputs. No promised duration; refresh after catch-up.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### S4 — Local read/save failure recovery

Class: D

Evidence: F28; recoverability required but exact controls unspecified

Proposal: Recommend inline error with Retry preserving fields; failed reads block dependent actions until recovered; committed saves remain successful if later notification delivery fails. Needs approval of recovery presentation.

Status: decided

Decision: User approved recommendation, 2026-10-11: Inline error and Retry preserving fields; failed reads block dependent actions until recovered. Notification effect failure does not invalidate a committed save.

### S5/S7 — Offline and partial success

Class: A

Evidence: F01/F17/F26/F28

Proposal: All tracking works without network; no offline/sync banner. Failed transaction saves no partial data; committed save persists despite notification effect failure, with miss fallback.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### S8 — Late-progress continuation (added)

Class: D

Evidence: F09/F25; deliberate action mandated but its interaction is unspecified

Proposal: Recommend explicit “Record for current period” action to rebind retained text only after showing the new period; if no current period, show next start and Close. No automatic transfer or submit. Alternative: close and reopen a blank progress sheet.

Status: decided

Decision: User approved recommendation, 2026-10-11: Explicit Record for current period shows the new period and retains text for review before a separate Save. With no current period, show next start and Close; never auto-transfer or auto-submit.

### A1/A2 — Access and permissions

Class: A

Evidence: F01/F17/F21/F22

Proposal: No sign-in; notification request once after first successful creation on API 33+; denial/channel blocking keeps tracking available and in-app miss counts; never auto-reprompt.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### A3/A4 — Form factor

Class: E

Evidence: Installed Android conventions; low-impact prototype scope default

Proposal: Portrait phone sketch; no separate tablet/foldable journeys.

Status: assumed

Decision: Agent default, flagged for review.

### A5 — Minimum OS

Class: A

Evidence: F21/F22

Proposal: API 24; notification channels API 26+, notification permission API 33+.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### T2/T4 — Background and freshness

Class: A

Evidence: F20/F29

Proposal: Automatic catch-up on use and foreground boundaries; best-effort closed-app notification delivery; no pull-to-refresh or exact-midnight promise.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### T3/T5 — Limits and validation

Class: A

Evidence: F04–F10/F18/F20/F33

Proposal: Name/unit limits, unique active/pending names, locale decimals, five cadence starts, replacement totals, cadence-specific target effects, immutable history, one current occurrence and 10-second retirement deadline. No product habit-count cap specified.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### C1/C2 — Copy and language

Class: E

Evidence: No approved copy/localization source supplied

Proposal: Draft English copy and realistic examples (e.g. Meditation, minutes); plain, nonjudgmental status wording; exact copy subject to later review.

Status: assumed

Decision: Agent default, flagged for review.

### C3 — Formatting

Class: A

Evidence: F06/F10/F20

Proposal: Device locale decimal separator and local calendar/timezone; show target/progress with fixed unit and effective period.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### C4/X2 — Privacy and regulatory flows

Class: A

Evidence: F01; PRD § Deliberately deferred decisions

Proposal: Personal on-device data; backup/transfer disabled; no supplied requirement for PIN, consent, regulated workflows or export. Add no new privacy gates.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### V1–V4 — Visual defaults

Class: E

Evidence: No visual design supplied; skill sketch contract

Proposal: Plain hand-drawn frames without branding, clear hierarchy and scrollable content; optional later design considers light/dark. No existing screen design is an authority.

Status: assumed

Decision: Agent default, flagged for review.

### X1 — Accessibility

Class: A

Evidence: F30; installed Android conventions § Touch and accessibility

Proposal: System text scaling, screen-reader identifiable controls/status and textual success/miss labels; Android baseline 48dp targets and no essential color-only meaning.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### T6 — Points and continuity (added)

Class: A

Evidence: F13/F14

Proposal: Success +10; miss rounded-up 1% of global balance with floor zero; started-period Active run distinct from success streak; Daily badge once for 18-of-21.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

### S9 — Retirement and restoration (added)

Class: A

Evidence: F18/F19/F25–F27

Proposal: Confirmation → durable pending row with 10-second Undo → active if timely, read-only retired history if expired; frozen period resumes on Undo or cancels without scoring at expiry. Retained-task drafts restore; no save/effect replay; committed-create re-save can report duplicate.

Status: approved

Decision: User approved gate A proposals, 2026-10-11.

## Sources

The conventions in this file are the skill's; the answers and decisions are this project's.

| Convention | Defined in |
|---|---|
| Question IDs, groups and the full questions | [Question bank](../../../.agents/skills/mobile-prototype/references/question-bank.md) (`references/question-bank.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/question-bank.md) |
| Classes A–E, status values, how a decision is recorded | [Clarification protocol](../../../.agents/skills/mobile-prototype/references/clarification-protocol.md) (`references/clarification-protocol.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/clarification-protocol.md) |
| Fact IDs (F01, …) | [`notes/INPUTS.md`](INPUTS.md), this project's fact register |
