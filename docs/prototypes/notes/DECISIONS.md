# Decisions

Source: `document` (approved from a document), `assumption-approved` (inferred and approved), `user` (user choice), `assumption` (agent default, review before build).

| ID | Decision | Source | Evidence / reason | Date |
|---|---|---|---|---|
| Inputs | Use docs/prototypes/ and supplied PRD, architecture, LLD and ADR directory. PRD governs behavior, architecture/current ADRs constraints, LLD detailed state/navigation. ADR 0001 remains historical. | user | Inputs “ok”; F31 | 2026-10-11 |
| P5 | Android only | user | Inputs “ok”; F21 | 2026-10-11 |
| A3/A4 | Portrait phone sketch; no separate tablet/foldable journeys | assumption | Installed Android conventions; prototype default | 2026-10-11 |
| C1/C2 | Draft English copy and realistic habit examples | assumption | No approved copy/localization supplied | 2026-10-11 |
| V1–V4 | Plain hand-drawn frames, clear hierarchy, scrollable content; optional later light/dark design | assumption | No supplied visual design; skill sketch contract | 2026-10-11 |
| P1/P2 | One person builds measurable personal habits by creating, tracking and reviewing them. | document | User gate A approval; F01/F02 | 2026-10-11 |
| P3 | Cover full V1: create all cadences, record/correct progress, outcomes/history, target/name edit, notification/fallback, retirement/undo and retired history; include relevant recovery/restoration states. Exclude login/sync/export/iOS/extra badges and quiet-hours controls. | assumption-approved | User gate A approval; F01–F03/F29; inferred full scope from supplied V1 documents | 2026-10-11 |
| P4 | Reviewer completes core loop, sees success/miss and target effects, undoes retirement and finds saved history after restart. | document | User gate A approval; F03 | 2026-10-11 |
| J1–J3 | Launch Overview → Create habit → Overview → Record progress → Overview → Habit review. Notification enters Review with Overview parent. Saves visibly update committed data. | document | User gate A approval; F02/F09/F23 | 2026-10-11 |
| J4 | Edit name/target; correct total; acknowledge misses; cancel retirement, timely undo, late undo and retained retired review. | document | User gate A approval; F09/F10/F16/F18/F19 | 2026-10-11 |
| J5 | Begin at no-habits Overview with Create habit; no separate onboarding. | assumption-approved | User gate A approval; F12/F23; no onboarding destination exists | 2026-10-11 |
| N1/N2/N4 | Overview is the start and hub; form, progress sheet, review, retirement dialog and retired list are nested destinations; notification Back goes to Overview. No added tab destinations. | document | User gate A approval; F23/F24 | 2026-10-11 |
| S1/S2/S6 | Show required Overview values and distinguish open, first-period waiting, Custom off-day and timezone gap. Review shows immutable chronological results, effective/superseded targets and badge; acknowledge on open. Empty Overview offers create/retired history. Refresh on catch-up/boundaries. | document | User gate A approval; F11/F12/F15/F16/F20/F32 | 2026-10-11 |
| S3/T1 | Show loading/saving if work exceeds 300 ms; disable relevant duplicate Save during transaction, preserve raw inputs. No promised duration; refresh after catch-up. | assumption-approved | User gate A approval; F26; installed Android conventions § Feedback and overlays | 2026-10-11 |
| S5/S7 | All tracking works without network; no offline/sync banner. Failed transaction saves no partial data; committed save persists despite notification effect failure, with miss fallback. | document | User gate A approval; F01/F17/F26/F28 | 2026-10-11 |
| A1/A2 | No sign-in; notification request once after first successful creation on API 33+; denial/channel blocking keeps tracking available and in-app miss counts; never auto-reprompt. | document | User gate A approval; F01/F17/F21/F22 | 2026-10-11 |
| A5 | API 24; notification channels API 26+, notification permission API 33+. | document | User gate A approval; F21/F22 | 2026-10-11 |
| T2/T4 | Automatic catch-up on use and foreground boundaries; best-effort closed-app notification delivery; no pull-to-refresh or exact-midnight promise. | document | User gate A approval; F20/F29 | 2026-10-11 |
| T3/T5 | Name/unit limits, unique active/pending names, locale decimals, five cadence starts, replacement totals, cadence-specific target effects, immutable history, one current occurrence and 10-second retirement deadline. No product habit-count cap specified. | document | User gate A approval; F04–F10/F18/F20/F33 | 2026-10-11 |
| C3 | Device locale decimal separator and local calendar/timezone; show target/progress with fixed unit and effective period. | document | User gate A approval; F06/F10/F20 | 2026-10-11 |
| C4/X2 | Personal on-device data; backup/transfer disabled; no supplied requirement for PIN, consent, regulated workflows or export. Add no new privacy gates. | document | User gate A approval; F01; PRD § Deliberately deferred decisions | 2026-10-11 |
| X1 | System text scaling, screen-reader identifiable controls/status and textual success/miss labels; Android baseline 48dp targets and no essential color-only meaning. | document | User gate A approval; F30; installed Android conventions § Touch and accessibility | 2026-10-11 |
| T6 | Success +10; miss rounded-up 1% of global balance with floor zero; started-period Active run distinct from success streak; Daily badge once for 18-of-21. | document | User gate A approval; F13/F14 | 2026-10-11 |
| S9 | Confirmation → durable pending row with 10-second Undo → active if timely, read-only retired history if expired; frozen period resumes on Undo or cancels without scoring at expiry. Retained-task drafts restore; no save/effect replay; committed-create re-save can report duplicate. | document | User gate A approval; F18/F19/F25–F27 | 2026-10-11 |
| P6 | Generate both wireflow and click-through from one flow.json | user | Explicit user choice at gate A | 2026-10-11 |
| N3 | Discard / Keep editing confirmation for changed create, edit and progress forms; untouched screens leave normally. Accepted saves continue independently. | user | User: use all recommendations; gate B round 1 | 2026-10-11 |
| S4 | Inline error and Retry preserving fields; failed reads block dependent actions until recovered. Notification effect failure does not invalidate a committed save. | user | User: use all recommendations; gate B round 1 | 2026-10-11 |
| S8 | Explicit Record for current period shows the new period and retains text for review before a separate Save. With no current period, show next start and Close; never auto-transfer or auto-submit. | user | User: use all recommendations; gate B round 1 | 2026-10-11 |

| Gate C | Lock the 26 separate journey pages and their 121 numbered screen states, with both wireflow and click-through views | user | Explicit user: lock the flow and create the handoff document; separate journeys preferred | 2026-10-11 |
| Design | Proceed directly to handoff; optional visual design phase skipped | user | Explicit request for handoff after flow lock | 2026-10-11 |

## Assumptions to confirm

| ID | Assumption | Why |
|---|---|---|
| A3/A4 | Portrait phone focus | Prototype default; flag at handoff |
| C1/C2 | Draft English copy and examples | Review copy/localization before build |
| V1–V4 | Plain sketch without brand styling | Optional design phase after flow lock |

All gate A proposals approved by the user. All gate B interaction choices approved by the user.

## Sources

The IDs are the question IDs from [`notes/CLARIFICATIONS.md`](CLARIFICATIONS.md) (its *How to read
the IDs* explains them). The conventions are the skill's; the decisions are this project's.

| Convention | Defined in |
|---|---|
| Question IDs and the full questions | [Question bank](../../../.agents/skills/mobile-prototype/references/question-bank.md) (`references/question-bank.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/question-bank.md) |
| Source values, and how an answer becomes a decision | [Clarification protocol](../../../.agents/skills/mobile-prototype/references/clarification-protocol.md) (`references/clarification-protocol.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/clarification-protocol.md) |
