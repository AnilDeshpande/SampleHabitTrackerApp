# Product Requirements Document — Habit Tracker V1

> This PRD defines creator-approved V1 product behavior. It is not an
> architecture document, data-model design, implementation plan, code plan, or
> test suite.

## Metadata and authoritative sources

| Field | Value |
| --- | --- |
| Product | Habit Tracker V1 |
| Status | Product definition complete; ready for later technical design |
| Owner | Creator |
| PRD date | 2026-08-19 |
| Current approved product source | `docs/idea-briefs/habit-tracker-v1-idea-brief.md` (IB), as amended by this PRD's 2026-09-21 creator-approved retirement, time-boundary, and privacy decisions and its 2026-09-23 creator-approved boundary, notification, and input-contract decisions (PD) |
| Provenance and session sources | `docs/idea-briefs/habit-tracker-v1-ideation-session-notes-2026-08-15.md` (IN); locked creator product-definition session, 2026-08-19; creator-approved PRD amendments, 2026-09-21 and 2026-09-23 (PD) |
| Refinement record | [`docs/prds/habit-tracker-v1-prd-refinement-learnings.md`](habit-tracker-v1-prd-refinement-learnings.md) |

IB is the original creator-approved product truth. IN preserves earlier
provenance and replacements. PD records creator-approved PRD-level
clarifications and amendments; the 2026-09-21 amendment supersedes earlier
IB/PD deletion, timezone, and local-data-retention decisions where they
conflict. The 2026-09-23 amendment supersedes earlier first-period,
missed-notification, target-change, and retirement-boundary wording where they
conflict.

## Product goal and boundary

### Goal

Help one person build measurable habits by defining schedule-based targets,
recording progress, preserving trustworthy history, and seeing motivating,
deterministic consistency outcomes. [IB: Product intent]

### V1 boundary

Habit Tracker V1 is a private, single-user Android application whose data stays
entirely on the device. It supports measurable habits with user-defined units;
Daily, Weekly, Fortnightly, Monthly, and Custom-weekday schedules; progress
and target-change history; points; the 21-Day Consistency badge; missed-target
handling and notification; and safe editing and retirement with undo. [IB: V1
boundary; PD]

### Assumptions

- The device's current local calendar and timezone define future tracking
  periods. An occurrence already created retains its own period boundary if
  the timezone later changes. A habit has at most one open occurrence at a
  time, and an occurrence never begins before the previous one ends. [PD;
  PD, 2026-09-23]
- The device clock is the only time authority. V1 does not detect manual clock
  changes: moving the clock forward finalizes the periods it passes, and they
  stay final; moving it back never reopens, duplicates, or re-creates a
  period. [PD, 2026-09-23]
- A device may be unable to display a notification; an in-app missed-result
  fallback is therefore part of the product outcome. [PD]
- User data must remain on the original device: Android cloud backup and
  device-to-device transfer are disabled for all V1 user data. [PD,
  2026-09-21]
- The product is an observable AI-agent-led Android-development demonstration,
  so success is demonstrated by a reviewer journey rather than commercial
  adoption metrics. [IB; IN]

### Non-goals

- Backend services, REST APIs, accounts or login, network or cloud sync,
  multi-user, social, or collaborative behavior, and non-Android clients.
- Export and export formats.
- Badges other than the 21-Day Consistency badge.
- Architecture, persistence technology, database/schema design, generated-code
  tooling, navigation design, implementation phases, Android code, and tests.
  [IB; IN; PD]

## Users and observable journeys

### Intended users

One individual who manages personal measurable habits rather than shared or
organizational activity. [IB: Target user]

### Primary journey

1. The user creates an identifiable measurable habit, selects its schedule,
   target, and unit, and sees it in the active overview.
2. During an open applicable period, the user records or corrects the current
   total and sees the saved result immediately.
3. When the period finalizes, the user sees success or a missed-target result,
   its points effect, and the appropriate continuity values.
4. The user reviews finalized periods and target changes without completed
   history being altered.

### Observable success journey

A reviewer can create a measurable habit, record progress, see successful and
missed-target consequences, change a target without changing completed history,
review progress, undo retirement, restart the app, and find retained data. [IB:
Success signal]

## Functional requirements

| ID | Requirement | Source decision | Acceptance criteria |
| --- | --- | --- | --- |
| FR-001 | The app shall create an active habit with a required name, unit, positive decimal-capable target, and selected frequency that satisfy the approved input contract. A Custom frequency shall require one or more weekdays. | IB: Habit, Unit, V1 boundary; PD; PD, 2026-09-23 | A valid habit is saved and visible; an invalid, empty, duplicate-name, or out-of-range value is not saved. |
| FR-002 | The app shall assign tracking occurrences using the approved cadence, first-period, and one-open-occurrence rules. | IB: Tracking period; PD; PD, 2026-09-23 | The first applicable period and every later occurrence follow the selected cadence; a habit never has more than one open occurrence. |
| FR-003 | The app shall let the user set the current total for an open applicable period and replace that total before finalization. A total submitted after its period closes is not saved. | IB: Progress; PD; PD, 2026-09-23 | The displayed and stored open-period total equals the last valid entered amount; a late submission is rejected, the period's final result is shown, and the typed value is retained. |
| FR-004 | The app shall finalize elapsed periods once, evaluate their target attainment, and keep finalized records immutable. | IB: Target attainment, Missed target, Points; PD | A finalized result is success when progress meets target, otherwise a miss; it cannot be altered later. |
| FR-005 | The app shall allow name and target edits only; target effects shall follow the approved cadence-specific rules, including a change saved when no occurrence is open or after an occurrence closed. Unit and frequency remain fixed after creation. | IB: Target change, Historical progress; PD; PD, 2026-09-23 | Eligible edits preserve completed records, apply to open or future periods as specified, and identify their effective period. |
| FR-006 | The app shall show an active overview with the app-wide points balance, each active habit's current required status, and each active habit's unacknowledged missed results. | IB: Product promise, Points, Active run, Success streak; PD; PD, 2026-09-23 | A user can inspect current target/progress or the next occurrence start, schedule, outcome state, Active run, Success streak, and unacknowledged misses for every active habit. |
| FR-007 | The app shall provide per-habit chronological review of finalized period outcomes and target changes, including a retired-habits view. | IB: Historical progress; PD | Each finalized period shows its effective target, progress, result, and points effect; target changes show their effective period; retired-habit history remains available outside the active overview. |
| FR-008 | The app shall apply the approved points, Active run, Success streak, and badge rules exactly once per eligible occurrence, and show an earned badge on its qualifying active habit's overview and review. | IB: Points, Active run, Success streak, 21-Day Consistency badge; PD | Scoring, run/streak transitions, and badge eligibility match the defined rules without duplicate effects; an awarded badge is visible in both required locations. |
| FR-009 | The app shall make each missed finalized period observable through one per-habit notification that counts that habit's unacknowledged misses, and through an in-app indicator that remains until the user opens that habit's review. | IB: Missed target; PD; PD, 2026-09-23 | Selecting the notification opens that habit's review and acknowledges its misses; unavailable notification delivery still exposes each miss in-app until acknowledged. |
| FR-010 | The app shall use recoverable retirement with confirmation and a 10-second visible undo, then retain the habit and its history as retired. Retirement stops tracking at confirmation. | IB: Delete recovery, Active run; PD; PD, 2026-09-23 | Undo restores the habit, its Active run, and the occurrence open at confirmation; final retirement cancels that occurrence, ends active tracking, and retains the complete audit history in the retired-habits view; an undo attempted after the deadline is not applied. |
| FR-011 | The app shall retain active and retired data and finalized history across restart while keeping all V1 data on the original device only. | IB: V1 boundary, Success signal; IN; PD | After restart, active/retired habits, history, and the app-wide points balance remain available; backup and device-to-device transfer do not copy user data elsewhere. |
| FR-012 | The app shall provide the approved accessibility, validation-recovery, no-habits, and saved-action feedback outcomes. | PD | The app supports system text scaling, screen-reader-identifiable controls/status, non-color-only outcomes, form correction without losing other values, and a clear create path when no active habits exist. |

## Domain vocabulary and rules

| Term or rule | Product definition | Boundary or exception | Source decision |
| --- | --- | --- | --- |
| Habit | A user-defined measurable activity with a name, target, non-empty unit, and selected frequency. | Name and target are editable; unit and frequency are fixed after creation. The name is unique, ignoring case, among active and pending-retirement habits; a retired habit does not reserve its name. | IB; PD; PD, 2026-09-23 |
| Input contract | Name: trimmed, 1–50 characters. Unit: trimmed, 1–20 characters. Target: greater than zero. Progress: zero or greater. Decimal values use the device locale's decimal separator, with at most 9 digits before and 3 digits after the separator. | A value outside the contract is not saved, and the invalid field and rule are identified. | PD, 2026-09-23 |
| Progress | The non-negative decimal current total reported for an open applicable period. | A new valid entry replaces, rather than adds to, the prior open-period total; finalized totals are immutable. | IB; PD |
| Daily period | One local calendar day. | A newly created Daily habit begins on its creation day. | IB; PD |
| Weekly period | Monday through Sunday in the device's local calendar. | A Weekly habit created on a Monday begins with that week; otherwise it begins with the next full weekly period. | IB; PD; PD, 2026-09-23 |
| Fortnightly period | A consecutive 14-day period anchored to the habit's creation date. | The first Fortnightly period begins at creation. | IB; PD |
| Monthly period | One local calendar month. | A Monthly habit created on the first day of a month begins with that month; otherwise it begins with the next full monthly period. | IB; PD; PD, 2026-09-23 |
| Custom occurrence | Each selected weekday is a separate occurrence. | A Custom habit begins on the next selected weekday on or after creation. An unselected day has no occurrence, progress requirement, success, miss, points effect, or streak effect. | IB; PD |
| Device calendar | Future occurrences use the device's current local calendar and timezone. Each created occurrence retains its start/end local dates, boundary timezone, and start and end boundaries. | Daylight-saving changes do not change calendar-period meaning. A timezone change never changes an already-created occurrence's boundary or a finalized result; it affects only later occurrences. A later occurrence begins no earlier than the previous one ends, so a habit never has two open occurrences. When a timezone change leaves time between occurrences, the habit shows when its next occurrence begins. A local date left with no time has no occurrence; for a Daily habit it counts as a day without qualifying progress for the badge. | PD, 2026-09-21; PD, 2026-09-23 |
| Device clock | The device clock is the only time authority for period boundaries, finalization, and undo deadlines. | Manual clock changes are not detected. Moving the clock forward finalizes the periods it passes, and they stay final; moving it back never reopens, duplicates, or re-creates a period. | PD, 2026-09-23 |
| Finalization | A period finalizes after its local calendar boundary passes; elapsed periods are finalized on the next app use. | Each period's reward or deduction is applied only once. No post-finalization progress entry or edit is allowed; a progress total submitted after the boundary is not saved. | IB; PD; PD, 2026-09-23 |
| Target attainment | A finalized period succeeds when its recorded progress meets its effective target. | A final miss earns no points. | IB |
| Target change | A Daily or Custom target change applies immediately and recalculates the open occurrence; a Weekly, Fortnightly, or Monthly change starts next period. When no occurrence is open, the change applies from the habit's next occurrence. A change saved after an occurrence has closed applies by the same rule at the moment it is saved, and the app identifies its effective period. | Completed periods are never recalculated. When several changes are saved before a period begins, the latest applies; every saved change remains in history with its effective period. | IB; PD, 2026-09-23 |
| Points | One app-wide balance. Each successful finalized period adds 10 points. A miss deducts 1% of the current balance, rounded up, with a one-point minimum when positive and a floor of zero. | A reward or deduction applies once per habit-period. Retirement does not reverse finalized point changes. | IB; PD |
| Retirement | A habit transitions `ACTIVE → PENDING_RETIREMENT → RETIRED`, with undo returning `PENDING_RETIREMENT → ACTIVE`. Retirement stops tracking at confirmation. | At final retirement, all existing history and point effects remain available; no new occurrence, progress, target change, or notification is created for the retired habit. Occurrences that ended before confirmation finalize normally. The occurrence open at confirmation is frozen: final retirement cancels it with zero points and no streak or badge effect, even if its boundary passed during the undo window; undo resumes it, and it finalizes normally. | PD, 2026-09-21; PD, 2026-09-23 |
| Active run | The number of the habit's scheduled periods that have started since creation, including the current open period, while the habit remains active. | It is zero before the first period begins. For a Custom habit, only selected-weekday occurrences count. A miss or absent progress does not reset it. Undo preserves it; final retirement ends it. | IB; PD; PD, 2026-09-23 |
| Success streak | The number of consecutive finalized periods that met target. | A miss resets it to zero. | IB |
| 21-Day Consistency badge | The only V1 badge. Award it once per Daily habit the first time at least 18 days meet target in any 21 consecutive calendar days. | A day without a qualifying progress record counts as a miss. Non-Daily habits are ineligible. | IB |
| Missed-result acknowledgement | A missed result is unacknowledged until the user opens that habit's review. | Acknowledgement never changes a finalized result, points, streak, or badge. | PD, 2026-09-23 |

## States, transitions, errors, and recovery

| State or condition | Entry or transition | Observable outcome | Recovery or exit | Source decision |
| --- | --- | --- | --- | --- |
| No active habits | No habit has been created or every habit is retired. | The overview explains that no active habits exist and presents a create path. | Create a valid habit or open the retired-habits view. | PD |
| Open occurrence | A current applicable tracking period begins. | Current target/progress and unresolved outcome state are visible. | Record/replace valid progress or wait for finalization. | IB; PD |
| Awaiting next occurrence | An active habit has no open occurrence: before its first Weekly or Monthly period, on an unselected Custom weekday, or between occurrences after a timezone change. | The overview shows when the next occurrence begins; there is no progress requirement, outcome, or points effect. | Wait for the next occurrence; a target change applies from it. | IB; PD, 2026-09-23 |
| Finalized success | Open period reaches its boundary with progress meeting target. | Final result, 10-point reward, and continuity updates are visible. | Review the immutable record. | IB |
| Finalized miss | Open period reaches its boundary with insufficient or absent progress. | Final result, one points deduction, the habit's missed-target notification, and its in-app unacknowledged-miss indicator are visible. | Open the habit review to review the immutable record and acknowledge the miss; Success streak is zero. | IB; PD; PD, 2026-09-23 |
| Late progress | A progress total is submitted after its period's boundary passed. | The total is not saved; the app states that the period closed and shows its final result; the typed value stays in the field. | Deliberately record the value for the current open occurrence, if one exists. | PD, 2026-09-23 |
| Invalid form entry | Creation, edit, or progress action violates an approved input rule. | The action is not saved; the invalid field and rule are identified while other form values remain. | Correct the field and submit again. | PD; PD, 2026-09-23 |
| Notification unavailable | A missed-period notification cannot be displayed by the device. | The missed result stays indicated in-app until the user opens that habit's review. | Open the relevant habit review. | PD; PD, 2026-09-23 |
| Retirement confirmation | The user requests retirement. | The habit remains active until confirmation. | Cancel or confirm. | PD, 2026-09-21 |
| Pending retirement | Confirmed retirement stops tracking and begins the 10-second undo window. | The habit is unavailable for tracking while a visible undo is available; the occurrence open at confirmation is frozen. | Undo is allowed only before the deadline and restores the habit, its Active run, and the frozen occurrence; expiry makes retirement final. | PD, 2026-09-21; PD, 2026-09-23 |
| Late undo | Undo is attempted at or after the deadline. | Undo is not applied; the app states that the habit is retired and offers the retired-habits view. | Open the retired-habits view. | PD, 2026-09-23 |
| Retired | The undo deadline expires without undo. | The habit, occurrences, target changes, badges, and point effects remain available in retired history; it is absent from the active overview. The occurrence frozen at confirmation becomes cancelled with zero points and no streak or badge effect. | None in V1. | PD, 2026-09-21; PD, 2026-09-23 |

## Interaction outcomes

### Creation and editing

Creating a habit requires the fields in FR-001, validated by the input
contract. A Custom schedule requires one or more weekdays. Invalid values,
including a name already used by an active or pending-retirement habit, do not
save and must be correctable without losing other entered form values. A user
may change only a habit's name or target; target effective dates follow the
domain rules, and a saved target change states the period from which it
applies. To use a different unit or frequency, the user creates a new habit.
[IB; PD; PD, 2026-09-23]

### Recording progress or activity

For an open applicable occurrence, the user enters a non-negative decimal total
that replaces the existing current total. The app visibly reflects a successful
save immediately. Finalized occurrences cannot receive new or edited progress.
If the period closes before a total is saved, the total is not saved, the app
states that the period closed and shows its final result, and the typed value
stays in the field. [IB; PD; PD, 2026-09-23]

### Review and history

The active overview shows the app-wide points balance and every active habit's
name, unit, current-period target/progress or next occurrence start, schedule,
current outcome state, Active run, Success streak, and count of unacknowledged
missed results. An earned 21-Day Consistency badge is visible on the qualifying
active habit's overview and review. Each habit's review is chronological and
shows finalized period, effective target, progress, success/miss, points
effect, and target changes with effective period. Opening a habit's review
acknowledges its missed results. [IB; PD; PD, 2026-09-23]

### Notifications

An active habit has at most one missed-target notification. When the app
processes a missed finalization, it posts or updates that habit's notification
immediately, stating how many of the habit's missed results are
unacknowledged, and alerts the user again. Selecting it opens that habit's
review, which acknowledges the misses and removes the notification. If the
device cannot display it, each missed result remains indicated in-app until
the user opens that habit's review. Exact wording and visual presentation are
deferred. [IB; PD; PD, 2026-09-23]

### Retirement, undo, and final retirement

The app asks for confirmation before retirement. Confirmation moves the habit
to `PENDING_RETIREMENT`, stops tracking, and opens a visible 10-second undo
window. Occurrences that ended before confirmation are finalized normally; the
occurrence open at confirmation is frozen. While pending or retired, the habit
does not accept new progress or target changes, create occurrences, or produce
notifications. Undo is accepted only when the current time is strictly before
the persisted deadline; it restores `ACTIVE`, and the frozen occurrence resumes
and finalizes normally, even if its boundary passed during the window. An undo
attempted at or after the deadline is not applied; the app states that the
habit is retired and offers the retired-habits view. At or after that
deadline, final retirement changes the habit to `RETIRED`, keeps the habit,
occurrences, target changes, badges, and points effects, and ends active
tracking. The frozen occurrence becomes `CANCELLED` with zero points and no
streak or badge effect. The retired-habits view exposes the retained history.
Undo and final-retirement attempts use the persisted deadline as their
mutually exclusive condition, so only one can succeed. When retirement begins,
the habit's visible missed-target notification is removed and no notification
is posted for it; its missed results remain in history. After undo, the
habit's unacknowledged misses are indicated in-app again, and later misses
notify normally. [PD, 2026-09-21; PD, 2026-09-23]

## Local data lifecycle and historical integrity

All V1 data remains on the original device. Active and retired habits, their
history, and the app-wide point balance persist across application restart.
For a created occurrence, its period boundary does not change when the device
timezone changes; the new timezone is used only for occurrences created later.
Retirement preserves the complete history, including target changes, badges,
and points effects. Acknowledging a missed result never changes history.
Android cloud backup and device-to-device transfer are
disabled for Room, DataStore, preferences, and all other user-data files.
[PD, 2026-09-21]

## Non-functional product expectations

| Area | Expectation | Source decision |
| --- | --- | --- |
| Privacy | Keep all V1 data on the original device only; disable Android cloud backup and device-to-device transfer for all user data; do not introduce accounts, backend services, APIs, or synchronization. | IB; IN; PD, 2026-09-21 |
| Accessibility | Respect system text scaling; make controls and statuses screen-reader-identifiable; do not use color as the only success/miss signal. | PD |
| Reliability and restart survival | Preserve active and retired local data and finalized outcomes over restart; process elapsed periods once on next app use. A conditional undo/final-retirement race has one winner. Device-clock or timezone changes never reopen or re-score a finalized period. | IB; PD, 2026-09-21; PD, 2026-09-23 |
| Observable responsiveness | After a valid user action saves, visibly show its saved result immediately. | PD |

## Requirement acceptance criteria and traceability

| Requirement ID | Observable acceptance criteria | Source decision |
| --- | --- | --- |
| FR-001 | A user can create each supported cadence with valid values; an invalid, duplicate, or out-of-range name, unit, target, or progress value, or an empty Custom weekday selection, is not saved and remains correctable. | IB; PD; PD, 2026-09-23 |
| FR-002 | A reviewer can observe cadence-specific first periods, including Weekly creation on a Monday and Monthly creation on the first day, and Custom no-occurrence days that do not generate outcomes. | IB; PD; PD, 2026-09-23 |
| FR-003 | A reviewer can set an open-period total, correct it, and see the corrected total before finalization; a total submitted after the boundary is rejected with the typed value retained. | IB; PD; PD, 2026-09-23 |
| FR-004 | A reviewer can observe one immutable success or miss per finalized period, including an elapsed period processed after reopening the app. | IB; PD |
| FR-005 | A reviewer can change a target, including while no occurrence is open, and see the approved current/next-period effect and its stated effective period while completed history remains unchanged. | IB; PD; PD, 2026-09-23 |
| FR-006 | A reviewer can inspect the required active-habit values, the awaiting-next-occurrence status, unacknowledged misses, and the intentional no-habits state. | IB; PD; PD, 2026-09-23 |
| FR-007 | A reviewer can inspect chronological finalized outcomes and target changes with effective periods. | IB; PD |
| FR-008 | A reviewer can verify 10-point success rewards, a single 1%-rounded-up miss deduction, run/streak behavior, Daily-only 18-of-21 badge qualification, and the awarded badge on the qualifying active habit's overview and review. | IB; PD |
| FR-009 | A reviewer can observe one per-habit missed-target notification counting unacknowledged misses, or its in-app indicator, and see both cleared by opening that habit's review. | IB; PD; PD, 2026-09-23 |
| FR-010 | A reviewer can confirm retirement, undo it within 10 seconds, observe a rejected late undo, and observe final retirement with retained history and a cancelled frozen occurrence after expiry. | IB; PD, 2026-09-21; PD, 2026-09-23 |
| FR-011 | A reviewer can restart the app and find active and retired local data intact, while backup and device transfer do not copy it elsewhere. | IB; IN; PD, 2026-09-21 |
| FR-012 | A reviewer can observe accessible/non-color-only status, invalid-input correction, and immediate saved-action feedback. | PD |

## Deliberately deferred decisions

| Deferred item | Why deferred | Later owner | Concrete reopening event |
| --- | --- | --- | --- |
| Persistence technology, database/schema, generated-code tooling, navigation, architecture, implementation, and tests | These are technical realization choices, not unresolved product behavior. | Technical-design owner | After this product specification is approved. |
| Visual style, charts, exact copy, and layout | Required product outcomes are specified; their presentation is an interaction-design concern. | Interaction-design owner | UX design begins after PRD approval. |
| PIN protection and automatic cleanup | No creator-approved privacy threat model or retention requirement makes either meaningful for V1. | Creator/product owner | A concrete privacy or retention requirement emerges. |

## Out of scope

- Backend services, REST APIs, accounts or login, network synchronization,
  cloud sync, multi-user, social, and collaborative capabilities.
- Non-Android clients, data export or export formats, and badges other than the
  21-Day Consistency badge.
- Write-then-sync behavior, including network staleness, remote conflict, and
  sync recovery; the prior casting requirement is retired and is not a missing
  V1 requirement.
- Architecture, implementation plans, Android code, tests, and content
  packages.

## Decision log

| Decision | Rationale | Immediate consequence | Source reference |
| --- | --- | --- | --- |
| Keep V1 private, single-user, Android-only, and on-device. | Personal-use boundary and no backend intent. | Accounts, APIs, sync, and multi-user behavior remain excluded. | IB; IN |
| Support measurable units and five creator-approved cadences. | Different habits use different measurements and schedules. | Period-based tracking must respect the selected cadence. | IB |
| Preserve completed history and apply cadence-specific target changes. | History must stay trustworthy while edits remain useful. | Completed records never recalculate. | IB |
| Use deterministic points, distinct continuity measures, and one Daily-only badge. | Compact motivating rewards suit the V1 demonstration. | Scoring and badge eligibility are auditable. | IB; IN |
| Require habit name/unit/target validity and non-negative progress. | Creator approved the recommended input contract. | Invalid records cannot be saved. | PD, 2026-08-19 |
| Finalize against local calendar boundaries and protect finalized results. | Creator approved deterministic period closure. | Elapsed periods process once on next app use. | PD, 2026-08-19 |
| Replace permanent deletion with retirement and offer a 10-second undo. | Creator approved preserving a complete points audit trail. | Retirement retains habit history while ending active tracking. | PD, 2026-09-21 |
| Notify on a miss and offer in-app fallback. | Creator approved observable missed-target handling. | Notification permission/device limits do not hide a miss. | PD, 2026-08-19 |
| Show chronological period/target-change review. | Creator approved minimum history visibility. | Trustworthy history is inspectable without prescribing visualization. | PD, 2026-08-19 |
| Provide accessibility and immediate saved-action feedback. | Creator approved meaningful V1 quality outcomes. | Status is inclusive and observable. | PD, 2026-08-19 |
| Replace open-period progress totals rather than adding entries. | Creator approved correction-friendly cumulative tracking. | Duplicate-entry ambiguity is removed. | PD, 2026-08-19 |
| Restrict edits to name and target. | Unit/schedule changes would compromise historical comparison. | Meaning-changing edits require a new habit. | PD, 2026-08-19 |
| Preserve all point-calculation history on retirement. | The retained app-wide balance must remain explainable. | Retired-habit history remains available and no points are reversed. | PD, 2026-09-21 |
| Define first-period behavior and Custom schedule validity. | Creator approved predictable starts and no-occurrence days. | Partial Weekly/Monthly targets are avoided. | PD, 2026-08-19 |
| Define active overview, empty state, and badge locations. | Creator approved minimum observable product states. | Core loop, reward, and no-data states are inspectable. | PD, 2026-08-19 |
| Lock each created occurrence's period boundary and exclude user data from backup/transfer. | Creator approved deterministic period boundaries and original-device-only privacy. | Timezone changes affect only future occurrences; V1 user data cannot be copied by Android backup or device transfer. | PD, 2026-09-21 |
| Start a Weekly or Monthly habit in the current period when created on its first day. | Daily already counts the creation day as a full day; waiting a whole period contradicts the intent of avoiding partial periods. | Replaces the unqualified "next full period" wording for Weekly and Monthly creation. | PD, 2026-09-23 |
| Freeze tracking at retirement confirmation. | The user cannot record progress during the undo window, so scoring a miss for it would be unfair. | The occurrence open at confirmation is cancelled on final retirement or resumes after undo. | PD, 2026-09-23 |
| Count started periods, including the current one, as Active run. | Immediate feedback on day one while staying distinct from Success streak. | Active run is zero only before the first period begins. | PD, 2026-09-23 |
| Reject late progress but apply late target changes by rule, and reject a late undo. | Finalized history is protected; a target change concerns the habit rather than a closed period. | Late actions state their outcome; typed progress is retained. | PD, 2026-09-23 |
| Replace one notification per missed period with one per-habit notification counting unacknowledged misses, and define acknowledgement as opening the habit's review. | A catch-up could otherwise post more notifications than Android displays; acknowledgement defines when the in-app fallback appears and clears. | Replaces the per-period notification wording of the 2026-08-19 decision; every miss remains in history. | PD, 2026-09-23 |
| Apply a target change with no open occurrence from the next occurrence; the latest change before a period applies. | Nothing has been tracked yet, so nothing needs protection. | Every saved change remains in history with its effective period. | PD, 2026-09-23 |
| Trust the device clock without tamper detection. | A private single-user app has nothing at stake beyond the user's own points, and detection would require network time. | Clock changes never reopen or duplicate periods. | PD, 2026-09-23 |
| Allow at most one open occurrence per habit across timezone changes. | Overlapping occurrences would make the target of a progress entry ambiguous. | A timezone gap shows the next occurrence start; a date left without time has no occurrence. | PD, 2026-09-23 |
| Adopt the input contract with unique active names. | The overview and notifications identify habits by name; retired names stay reusable for replacement habits. | Name, unit, and decimal limits are enforced at entry. | PD, 2026-09-23 |

## Open questions

`Open questions: None`

`Unclassified product questions: None`
