# Inputs

Confirmed by user on 2026-10-11: docs/prototypes/, Android only, supplied source hierarchy.
All confirmed documents below were read in full.

## Inventory (confirmed with the user)

| Category | File | Version / date | Authoritative? | Notes |
|---|---|---|---|---|
| Spec | [PRD](../../prds/habit-tracker-v1-prd.md) | 2026-08-19; approved amendments 2026-09-21/23 | Yes, product behavior | PRD abbreviation below |
| Architecture | [Architecture](../../architecture/habit-tracker-v1-architecture.md) | 2026-09-21 baseline; 2026-09-23 delta | Yes, feasibility | Architecture abbreviation below |
| Detailed design | [LLD](../../design/habit-tracker-v1-low-level-design.md) | Accepted 2026-10-05 | Yes, detailed state/navigation | LLD abbreviation below |
| ADR | [0001](../../adr/0001-deterministic-on-device-tracking.md) | 2026-09-21 | Historical only | Superseded; do not reinstate old rules |
| ADR | [0002](../../adr/0002-global-catch-up-and-occurrence-continuity.md) | Accepted 2026-09-23 | Yes | Current tracking contract |
| ADR | [0003](../../adr/0003-manual-composition-root-and-android-dependency-wiring.md) | Accepted 2026-10-05 | Yes | Wiring; later LLD resolutions retained |
| Other | [ADR index](../../adr/README.md) | Current | Status index | Confirms supersession |
| Visual design | None supplied | — | — | LLD defines interactions, not an approved visual design |
| API | None | — | — | Intentional: local-only product |

Missing categories: no visual design or API needed for the rough sketch. Draft realistic local habit examples; brand direction belongs to optional later design. No additional idea briefs or refinement logs treated as authoritative inputs.

## Fact register

PRD, Architecture and LLD refer to the linked inventory above. IDs identify atomic facts; section names provide source locations.

| ID | Fact | Source | Cat |
|---|---|---|---|
| F01 | Private single-user Android V1; original-device storage; no login, backend, sync, export, or non-Android clients. | PRD § Product goal and boundary / Out of scope | Spec |
| F02 | Create → record/correct total → see finalized outcome → review history is the primary journey. | PRD § Users and observable journeys | Spec |
| F03 | Reviewer success includes create, successful/missed results, target edit, review, undo retirement, restart and retained data. | PRD § Observable success journey | Spec |
| F04 | Name, unit, positive target and five frequencies: Daily, Weekly, Fortnightly, Monthly, Custom weekdays. | PRD § FR-001 / Domain vocabulary and rules | Spec |
| F05 | Name trimmed 1–50 characters; unit trimmed 1–20; name unique ignoring case among active/pending habits; retired names reusable. | PRD § Input contract / Habit | Spec |
| F06 | Locale decimal separator; at most 9 integer and 3 fraction digits; target >0, progress ≥0; invalid fields retain all entered values. | PRD § Input contract / Invalid form entry | Spec |
| F07 | Daily starts creation day; Weekly starts this week only on Monday, otherwise next Monday; Monthly starts this month only on day 1, otherwise next month. | PRD § Domain vocabulary and rules | Spec |
| F08 | Fortnightly starts at creation in 14-day blocks; Custom uses selected weekdays, one occurrence each, first on/after creation; at least one selected weekday. | PRD § Domain vocabulary and rules / FR-001 | Spec |
| F09 | Open-period progress replaces the total; closed periods cannot be changed. Late save shows final result and retains typed text. | PRD § Recording progress or activity | Spec |
| F10 | Only name/target editable; Daily/Custom target applies to open occurrence, longer cadence from next period; without open occurrence from next; saved message identifies effective period. | PRD § Target change / Creation and editing | Spec |
| F11 | Overview shows global points, name/unit/schedule, current target/progress or next start, outcome, Active run, Success streak, badge and unacknowledged miss count. | PRD § Review and history | Spec |
| F12 | No active habits offers create or retired view; no-current-occurrence states show next start and no progress requirement. | PRD § States, transitions, errors, and recovery | Spec |
| F13 | Success +10; miss deducts 1% rounded up with floor zero; Active run counts started periods, streak counts consecutive finalized successes. | PRD § Domain vocabulary and rules | Spec |
| F14 | Daily-only badge awarded once for ≥18 successes within 21 consecutive calendar dates; visible in active overview/review. | PRD § 21-Day Consistency badge / Review and history | Spec |
| F15 | Chronological finalized results show period, target, progress, result and points; target changes show effective period. | PRD § Review and history | Spec |
| F16 | One notification per active habit counts unacknowledged misses; opening review acknowledges and clears notification/indicator, without changing history. | PRD § Notifications | Spec |
| F17 | Unavailable notifications retain in-app miss indicator until review opens. | PRD § Notification unavailable / Notifications | Spec |
| F18 | Confirm retirement; durable pending row offers 10-second undo; tracking stops and open occurrence freezes; expiry cancels frozen occurrence without scoring; late undo links to retired view. | PRD § Retirement, undo, and final retirement; Architecture § Presentation and navigation | Spec |
| F19 | Retirement retains all history, badges and points; retired history is read-only and cannot reactivate. | PRD § States, transitions, errors, and recovery / Local data lifecycle | Spec |
| F20 | Created occurrence boundaries persist across timezone changes; future periods use current zone; at most one open occurrence; clock changes never reopen or re-score finalized periods. | PRD § Device calendar / Device clock | Spec |
| F21 | Material 3 Compose, minimum API 24, target API 36; only runtime permission is notifications at API 33+. | Architecture § Scope, constraints, and exclusions / Error handling and operational limits | Arch |
| F22 | API 33+ request notification permission once after first successful creation; never automatically re-request; channel blocking uses same fallback. | Architecture § Platform integrations, privacy, and external effects | Arch |
| F23 | Overview is start; create/edit/progress save returns overview; review from overview/retired list; notification builds Overview → Review, Back to overview. | LLD § Navigation route register | Design |
| F24 | Progress is own destination, may render as sheet; late rejection keeps destination available. Retirement confirmation is dialog; pending row is overview content. | LLD § Navigation route register | Design |
| F25 | Raw draft fields restore in retained task after system process death; progress stays bound to original occurrence; never silently assign old typed total to new period; task removal discards drafts. | LLD § Initialization and restoration | Design |
| F26 | Accepted commands survive screen departure; disable duplicate Save while transaction runs; commit remains saved even without result delivery; no automatic replay. | LLD § Concurrency and work lifetime | Design |
| F27 | Restored create draft resubmitted after prior commit produces duplicate-name error; saved data persists and transient feedback/permission requests never replay. | LLD § Decision lock / Initialization and restoration | Design |
| F28 | Recoverable repository failures must not claim successful save before commit; notification effect failure does not undo committed save. | Architecture § Error handling and operational limits; LLD § After-commit effects and foreground recovery | Arch |
| F29 | Foreground catch-up/ticker updates boundaries; closed-app work best effort, periodic six hours; no midnight delivery guarantee and no quiet-hours policy. | Architecture § Foreground boundary ticker; LLD § Worker timing and retry budget | Arch |
| F30 | Respect system text scaling, screen-reader names and non-color-only results; immediate committed-save feedback. | PRD § Non-functional product expectations | Spec |
| F31 | ADR 0001 superseded by accepted ADR 0002; ADR 0003 selects manual wiring; LLD resolves undo ordering/notification cleanup. | ADR index; ADR 0002 § Metadata; LLD § Architecture and ADR consistency review | Arch |
| F32 | Superseded target changes remain marked in review; current statuses distinguish in-progress, waiting first, no occurrence today and between periods. | Architecture § Presentation and navigation | Arch |
| F33 | Validator rejects signs, exponents and grouping separators; length measured in Unicode code points. | Architecture § Rule services | Arch |
