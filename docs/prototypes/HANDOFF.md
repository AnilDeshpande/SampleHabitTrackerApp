# Handoff — Habit Tracker V1

Flow locked by: project creator (user) on 2026-10-11 · Platform: Android (minimum API 24 / Android 7.0) · Sketch: [`flow.drawio`](flow.drawio) · Final look: not done; direct handoff chosen.

## 1. Summary

Habit Tracker V1 helps one person create measurable personal habits, record or correct current-period totals, and review finalized results. V1 is Android only, on-device, with no sign-in or network dependency. Minimum API 24; notification channels apply on API 26+, and runtime notification permission on API 33+.

The creator approved the scope and interaction choices, explicitly locked the flow on **2026-10-11**, and requested a direct handoff. The creator prefers **separate journey pages** because the full flow is large. The approved diagram contains **7 app destinations, 26 journeys, 121 numbered screen states and 95 transitions**, presented as **26 wireflow pages plus 121 click-through screen pages**.

Journeys cover the primary create → record → finalize → review loop; all five schedules and first-period rules; validation and total correction; waiting states; immediate/deferred target edits and superseded changes; unsaved-draft confirmations; late progress; notification permission and acknowledgement; points and the Daily consistency badge; retirement, timely/late Undo and read-only history; local read/save recovery; retained-task restoration; multiple pending retirements; progress entry from review; and empty retired history.

The diagram is a fixed-state flow prototype. Form editing, time passing and real storage are represented by labelled transitions and example values, not an executable app. Product behavior is approved; English copy and examples remain draft. Optional visual design was skipped for this handoff.

## 2. Inputs

| Category | File | Version / date | Authoritative? | Notes |
|---|---|---|---|---|
| Spec | [PRD](../prds/habit-tracker-v1-prd.md) | 2026-08-19; approved amendments 2026-09-21/23 | Yes, product behavior | PRD abbreviation below |
| Architecture | [Architecture](../architecture/habit-tracker-v1-architecture.md) | 2026-09-21 baseline; 2026-09-23 delta | Yes, feasibility | Architecture abbreviation below |
| Detailed design | [LLD](../design/habit-tracker-v1-low-level-design.md) | Accepted 2026-10-05 | Yes, detailed state/navigation | LLD abbreviation below |
| ADR | [0001](../adr/0001-deterministic-on-device-tracking.md) | 2026-09-21 | Historical only | Superseded; do not reinstate old rules |
| ADR | [0002](../adr/0002-global-catch-up-and-occurrence-continuity.md) | Accepted 2026-09-23 | Yes | Current tracking contract |
| ADR | [0003](../adr/0003-manual-composition-root-and-android-dependency-wiring.md) | Superseded by ADR 0004 on 2026-10-28 (wiring mechanism only) | Authoritative: contracts only | Lifetimes, restoration, worker-factory and test-graph contracts retained. |
| ADR | [0004](../adr/0004-hilt-dependency-injection.md) | Accepted 2026-10-28 | Yes | Wiring: Hilt. |
| Other | [ADR index](../adr/README.md) | Current | Status index | Confirms supersession |
| Visual design | None supplied | — | — | LLD defines interactions, not an approved visual design |
| API | None | — | — | Intentional: local-only product |

Authority: PRD determines behavior; architecture and accepted ADRs determine constraints; LLD supplies detailed state/navigation. ADR 0001 is superseded by ADR 0002 and is historical only. ADR 0003 does not replace the tracking contract. ADR 0004 (Hilt) supersedes ADR 0003's wiring mechanism; ADR 0003's contracts still apply. Creator-resolved LLD findings are retained.

Evidence: [fact register](notes/INPUTS.md), [clarifications](notes/CLARIFICATIONS.md) and [decisions](notes/DECISIONS.md). All confirmed source documents were read in full. No visual design or external API contract was supplied.

## 3. Screen inventory
Each frame (S-number) is one state of one app screen. Screen names are the same here and in
`flow.drawio`; state and entry condition are listed apart from them. The tables between the
`generated` markers come from `flow.json`: run `node <skill>/scripts/handoff.mjs` to refresh them,
and don't edit inside them.

<!-- generated:screens (scripts/handoff.mjs from flow.json; don't edit inside) -->
| Screen | Screen id | Purpose | App screen / navigation location | Frames (S-number state) |
|---|---|---|---|---|
| Overview | `overview` | Track active habits, global points and pending retirements. | Start destination | S1 No active habits · S3 Daily habit created · S5 Total saved · S6 Next day, success finalized · S8 Returned from review · S11 Open today · S13 Waiting for first period · S15 Open 14-day period · S17 Waiting until 1 Nov · S19 Waiting for selected weekday · S23 Valid Custom habit saved · S24 Existing total · S26 Corrected total · S29 Zero total saved · S30 Waiting for first period · S31 First period starts · S32 No occurrence today · S33 Between periods · S34 Daily habit selected · S36 New target effective today · S39 Future target saved · S41 Latest pending target saved · S46 Draft discarded · S50 Draft discarded · S54 Draft discarded · S58 Current period saved · S59 Closed without new entry · S62 Next selected day shown · S63 First habit committed · S64 Android permission prompt · S65 Notifications allowed · S66 Notifications unavailable · S67 Misses awaiting review · S69 Miss indicator cleared · S70 Android miss notification · S72 Returned from notification review · S73 Daily consistency nearly met · S74 Success and badge awarded · S76 Active habit · S78 Pending retirement · S79 Timely Undo restored · S80 Retirement complete · S84 Retirement cancelled · S85 Pending row at deadline · S86 Undo too late · S88 Loading local data · S89 Read failed · S90 Read recovered · S94 Retry committed · S98 Retry committed · S102 Retry committed · S106 Committed habit preserved · S107 Reopened after task removal · S108 Two pending rows · S109 Pending rows after restart · S110 One restored; one pending · S111 Other retirement settled · S114 Review entry saved · S115 Closed without changes · S116 No active habits · S118 Returned to empty Overview · S119 No occurrence today · S121 Next occurrence target saved |
| Create habit | `create` | Choose a measurable habit and schedule. | Overview → Create habit | S2 Daily draft · S9 Choose frequency · S10 Daily selected · S12 Weekly selected · S14 Fortnightly selected · S16 Monthly selected · S18 Custom weekdays selected · S20 Invalid draft · S21 Validation errors · S22 Valid Custom draft · S43 Unsaved changes · S44 Discard changes? · S45 Editing continued · S91 Ready to save · S92 Saving · S93 Save failed; draft retained · S103 Draft before process death · S104 Retained-task draft restored · S105 Prior commit found on resubmit |
| Record progress | `progress` | Replace the current occurrence total. | Sheet destination from Overview or Habit review; save returns Overview | S4 Open period total · S25 Replace with lower total · S27 Invalid progress total · S28 Zero is valid · S51 Unsaved changes · S52 Discard changes? · S53 Editing continued · S55 Previous period draft · S56 Closed period; text retained · S57 Current period chosen · S60 Selected-day draft · S61 Closed; no current period · S99 Ready to save · S100 Saving · S101 Save failed; draft retained · S113 Entered from review |
| Habit review | `review` | Review results, points, target changes and badges; acknowledge misses. | Overview / Retired habits → Habit review; notification parent is Overview | S7 Saved result history · S37 Target change history · S42 Superseded target retained · S68 Misses acknowledged · S71 Opened from notification · S75 Badge in history · S82 Read-only retired history · S112 No finalized results yet |
| Edit habit | `edit` | Change name or target with its effective period. | Overview → Edit habit | S35 Daily target change · S38 Weekly target change · S40 Revise pending target · S47 Unsaved changes · S48 Discard changes? · S49 Editing continued · S95 Ready to save · S96 Saving · S97 Save failed; draft retained · S120 Target for next occurrence |
| Retire habit | `retirement` | Confirm retirement before the 10-second Undo window. | Dialog destination over Overview | S77 Confirm retirement |
| Retired habits | `retired` | Find retained read-only habit history. | Overview → Retired habits | S81 Retained habits · S83 Back from retired review · S87 Expired habit retained · S117 No retired habits |

| S-number | Screen | State | Entry condition | Reached from |
|---|---|---|---|---|
| S1 | Overview | No active habits | First launch | start of the journey |
| S2 | Create habit | Daily draft | — | S1 (Create habit) |
| S3 | Overview | Daily habit created | — | S2 (Save habit) |
| S4 | Record progress | Open period total | — | S3 (Record progress) |
| S5 | Overview | Total saved | — | S4 (Save total) |
| S6 | Overview | Next day, success finalized | — | S5 (Next day / reopen) |
| S7 | Habit review | Saved result history | — | S6 (Review habit) |
| S8 | Overview | Returned from review | — | S7 (Back) |
| S9 | Create habit | Choose frequency | Creation on Sun 11 Oct 2026 | start of the journey |
| S10 | Create habit | Daily selected | — | S9 (Choose Daily) |
| S11 | Overview | Open today | — | S10 (Save habit) |
| S12 | Create habit | Weekly selected | — | S9 (Choose Weekly) |
| S13 | Overview | Waiting for first period | — | S12 (Save habit) |
| S14 | Create habit | Fortnightly selected | — | S9 (Choose Fortnightly) |
| S15 | Overview | Open 14-day period | — | S14 (Save habit) |
| S16 | Create habit | Monthly selected | — | S9 (Choose Monthly) |
| S17 | Overview | Waiting until 1 Nov | — | S16 (Save habit) |
| S18 | Create habit | Custom weekdays selected | — | S9 (Choose Custom weekdays) |
| S19 | Overview | Waiting for selected weekday | — | S18 (Save habit) |
| S20 | Create habit | Invalid draft | — | start of the journey |
| S21 | Create habit | Validation errors | — | S20 (Save invalid draft) |
| S22 | Create habit | Valid Custom draft | — | S21 (Edit fields) |
| S23 | Overview | Valid Custom habit saved | — | S22 (Save habit) |
| S24 | Overview | Existing total | — | start of the journey |
| S25 | Record progress | Replace with lower total | — | S24 (Record progress) |
| S26 | Overview | Corrected total | — | S25 (Save 8 minutes) |
| S27 | Record progress | Invalid progress total | — | S25 (Enter 1e3 / Save) |
| S28 | Record progress | Zero is valid | — | S27 (Edit total to zero) |
| S29 | Overview | Zero total saved | — | S28 (Save total) |
| S30 | Overview | Waiting for first period | Created Sunday | start of the journey |
| S31 | Overview | First period starts | — | S30 (Next Monday) |
| S32 | Overview | No occurrence today | Selected-day habit on Sunday | S31 (View off-day habit) |
| S33 | Overview | Between periods | Timezone change creates a gap | S32 (Zone-change gap) |
| S34 | Overview | Daily habit selected | — | start of the journey |
| S35 | Edit habit | Daily target change | — | S34 (Edit habit) |
| S36 | Overview | New target effective today | — | S35 (Save changes) |
| S37 | Habit review | Target change history | — | S36 (Review habit) |
| S38 | Edit habit | Weekly target change | Open week of 12–18 Oct | start of the journey |
| S39 | Overview | Future target saved | — | S38 (Save changes) |
| S40 | Edit habit | Revise pending target | — | S39 (Edit target again) |
| S41 | Overview | Latest pending target saved | — | S40 (Save changes) |
| S42 | Habit review | Superseded target retained | — | S41 (Review habit) |
| S43 | Create habit | Unsaved changes | — | start of the journey |
| S44 | Create habit | Discard changes? | — | S43 (Back with changes) |
| S45 | Create habit | Editing continued | — | S44 (Keep editing) |
| S46 | Overview | Draft discarded | — | S44 (Discard) |
| S47 | Edit habit | Unsaved changes | — | start of the journey |
| S48 | Edit habit | Discard changes? | — | S47 (Back with changes) |
| S49 | Edit habit | Editing continued | — | S48 (Keep editing) |
| S50 | Overview | Draft discarded | — | S48 (Discard) |
| S51 | Record progress | Unsaved changes | — | start of the journey |
| S52 | Record progress | Discard changes? | — | S51 (Back with changes) |
| S53 | Record progress | Editing continued | — | S52 (Keep editing) |
| S54 | Overview | Draft discarded | — | S52 (Discard) |
| S55 | Record progress | Previous period draft | Draft open across midnight | start of the journey |
| S56 | Record progress | Closed period; text retained | — | S55 (Save after midnight) |
| S57 | Record progress | Current period chosen | — | S56 (Choose current period) |
| S58 | Overview | Current period saved | — | S57 (Save total) |
| S59 | Overview | Closed without new entry | — | S56 (Close) |
| S60 | Record progress | Selected-day draft | Restored on Saturday | start of the journey |
| S61 | Record progress | Closed; no current period | — | S60 (Late Save) |
| S62 | Overview | Next selected day shown | — | S61 (Close) |
| S63 | Overview | First habit committed | First successful creation on API 33+ | start of the journey |
| S64 | Overview | Android permission prompt | — | S63 (Request once) |
| S65 | Overview | Notifications allowed | — | S64 (Allow) |
| S66 | Overview | Notifications unavailable | — | S64 (Don’t allow) |
| S67 | Overview | Misses awaiting review | Two catch-up misses from balance 100 | start of the journey |
| S68 | Habit review | Misses acknowledged | — | S67 (Review habit) |
| S69 | Overview | Miss indicator cleared | — | S68 (Back) |
| S70 | Overview | Android miss notification | Android notification shade | S67 (Notification delivered) |
| S71 | Habit review | Opened from notification | — | S70 (Tap notification) |
| S72 | Overview | Returned from notification review | — | S71 (Back) |
| S73 | Overview | Daily consistency nearly met | — | start of the journey |
| S74 | Overview | Success and badge awarded | — | S73 (Next day / reopen) |
| S75 | Habit review | Badge in history | — | S74 (Review habit) |
| S76 | Overview | Active habit | After 10 Oct success; 11 Oct target change | start of the journey |
| S77 | Retire habit | Confirm retirement | — | S76 (Retire habit) |
| S78 | Overview | Pending retirement | — | S77 (Confirm Retire) |
| S79 | Overview | Timely Undo restored | — | S78 (Undo in time) |
| S80 | Overview | Retirement complete | — | S78 (10 seconds expire) |
| S81 | Retired habits | Retained habits | — | S80 (Retired habits) |
| S82 | Habit review | Read-only retired history | — | S81 (Review Meditation) |
| S83 | Retired habits | Back from retired review | — | S82 (Back) |
| S84 | Overview | Retirement cancelled | — | S77 (Cancel) |
| S85 | Overview | Pending row at deadline | Restored countdown or delayed tap | start of the journey |
| S86 | Overview | Undo too late | — | S85 (Undo at deadline) |
| S87 | Retired habits | Expired habit retained | — | S86 (View retired habits) |
| S88 | Overview | Loading local data | App launch; noticeable wait | start of the journey |
| S89 | Overview | Read failed | — | S88 (Local read fails) |
| S90 | Overview | Read recovered | — | S89 (Retry) |
| S91 | Create habit | Ready to save | — | start of the journey |
| S92 | Create habit | Saving | — | S91 (Save) |
| S93 | Create habit | Save failed; draft retained | — | S92 (Local save fails) |
| S94 | Overview | Retry committed | — | S93 (Retry) |
| S95 | Edit habit | Ready to save | — | start of the journey |
| S96 | Edit habit | Saving | — | S95 (Save) |
| S97 | Edit habit | Save failed; draft retained | — | S96 (Local save fails) |
| S98 | Overview | Retry committed | — | S97 (Retry) |
| S99 | Record progress | Ready to save | — | start of the journey |
| S100 | Record progress | Saving | — | S99 (Save) |
| S101 | Record progress | Save failed; draft retained | — | S100 (Local save fails) |
| S102 | Overview | Retry committed | — | S101 (Retry) |
| S103 | Create habit | Draft before process death | — | start of the journey |
| S104 | Create habit | Retained-task draft restored | — | S103 (System restores task) |
| S105 | Create habit | Prior commit found on resubmit | — | S104 (Save unchanged draft) |
| S106 | Overview | Committed habit preserved | — | S105 (Back) |
| S107 | Overview | Reopened after task removal | — | S104 (Remove task / reopen) |
| S108 | Overview | Two pending rows | Two retirements confirmed independently | start of the journey |
| S109 | Overview | Pending rows after restart | — | S108 (Restart before deadlines) |
| S110 | Overview | One restored; one pending | — | S109 (Undo Meditation) |
| S111 | Overview | Other retirement settled | — | S110 (Reading deadline expires) |
| S112 | Habit review | No finalized results yet | — | start of the journey |
| S113 | Record progress | Entered from review | — | S112 (Record progress) |
| S114 | Overview | Review entry saved | — | S113 (Save total) |
| S115 | Overview | Closed without changes | — | S113 (Close unchanged) |
| S116 | Overview | No active habits | — | start of the journey |
| S117 | Retired habits | No retired habits | — | S116 (Retired habits) |
| S118 | Overview | Returned to empty Overview | — | S117 (Back) |
| S119 | Overview | No occurrence today | — | start of the journey |
| S120 | Edit habit | Target for next occurrence | — | S119 (Edit habit) |
| S121 | Overview | Next occurrence target saved | — | S120 (Save changes) |
<!-- /generated:screens -->

## 4. Navigation graph

```text
Overview (start; no tabs)
├── Create habit ── Save / clean Cancel ──→ Overview
├── Edit habit ── Save / clean Cancel ────→ Overview
├── Record progress (sheet destination) ─→ Overview
├── Habit review ── Record progress ──────→ Overview on Save / Close
├── Retire habit (dialog) ── confirm ─────→ Overview pending row
│                                          ├─ Undo before deadline → active
│                                          └─ expiry → retired
└── Retired habits ── Habit review (read-only) ── Back → Retired habits
Android notification ──→ Overview → Habit review ── Back → Overview
```

| From | Back goes to | Up goes to | Confirmation needed? | Platform rules |
|---|---|---|---|---|
| Overview | Leaves app/task | None | No | Android start destination |
| Create habit | Overview | Overview | Dirty draft: Keep editing / Discard | Clean Cancel leaves directly |
| Edit habit | Overview | Overview | Dirty draft: Keep editing / Discard | Unit and cadence remain fixed |
| Record progress | Overview; closes sheet destination | Overview | Dirty draft: Keep editing / Discard | Same return after entry from Habit review; late rejection stays open |
| Habit review: active / notification | Overview | Overview | No | Notification builds Overview → Review parent stack |
| Habit review: retired | Retired habits | Retired habits | No | Read-only; no progress/edit/reactivate |
| Retire habit dialog | Dismiss dialog; invoking Overview | Dismiss dialog | Confirm Retire required for tracking to stop | Cancel/Back sends no retirement command |
| Retired habits | Overview | Overview | No | History is retained |
| Discard dialog | Keep editing on underlying form | Not applicable | Discard explicitly loses unsaved input | Dialog closes before destination navigation |
| Android permission dialog | OS-controlled dismissal/result | Not applicable | Platform permission choice | One contextual request on API 33+, after first successful creation |

Dirty means entered values differ from the destination’s initial values. Confirmation applies to Back, Up and explicit Cancel / Close before submission. Accepted commands continue if the user leaves; no discard action cancels an accepted save. Save success updates Overview only after commit. Failed reads block actions that need the unread data.

The “↩ Back to the overview” control outside a phone frame returns to that journey’s wireflow page. It is diagram navigation, not the app’s Android Back behavior. The Android notification-shade example is an external system surface represented as an Overview entry state, not an eighth app destination.

### Transitions
Where each action leads: the next screen is the transition target, not the screen's own place in
the navigation (that is the *App screen / navigation location* in section 3).

<!-- generated:transitions (scripts/handoff.mjs from flow.json; don't edit inside) -->
| Journey | From | User action | Next screen (transition target) |
|---|---|---|---|
| Create, record and review | S1 · Overview — No active habits | Create habit | S2 · Create habit — Daily draft |
| Create, record and review | S2 · Create habit — Daily draft | Save habit | S3 · Overview — Daily habit created |
| Create, record and review | S3 · Overview — Daily habit created | Record progress | S4 · Record progress — Open period total |
| Create, record and review | S4 · Record progress — Open period total | Save total | S5 · Overview — Total saved |
| Create, record and review | S5 · Overview — Total saved | Next day / reopen | S6 · Overview — Next day, success finalized |
| Create, record and review | S6 · Overview — Next day, success finalized | Review habit | S7 · Habit review — Saved result history |
| Create, record and review | S7 · Habit review — Saved result history | Back | S8 · Overview — Returned from review |
| Choose a schedule and first period | S9 · Create habit — Choose frequency | Choose Daily | S10 · Create habit — Daily selected |
| Choose a schedule and first period | S10 · Create habit — Daily selected | Save habit | S11 · Overview — Open today |
| Choose a schedule and first period | S9 · Create habit — Choose frequency | Choose Weekly | S12 · Create habit — Weekly selected |
| Choose a schedule and first period | S12 · Create habit — Weekly selected | Save habit | S13 · Overview — Waiting for first period |
| Choose a schedule and first period | S9 · Create habit — Choose frequency | Choose Fortnightly | S14 · Create habit — Fortnightly selected |
| Choose a schedule and first period | S14 · Create habit — Fortnightly selected | Save habit | S15 · Overview — Open 14-day period |
| Choose a schedule and first period | S9 · Create habit — Choose frequency | Choose Monthly | S16 · Create habit — Monthly selected |
| Choose a schedule and first period | S16 · Create habit — Monthly selected | Save habit | S17 · Overview — Waiting until 1 Nov |
| Choose a schedule and first period | S9 · Create habit — Choose frequency | Choose Custom weekdays | S18 · Create habit — Custom weekdays selected |
| Choose a schedule and first period | S18 · Create habit — Custom weekdays selected | Save habit | S19 · Overview — Waiting for selected weekday |
| Correct invalid habit inputs | S20 · Create habit — Invalid draft | Save invalid draft | S21 · Create habit — Validation errors |
| Correct invalid habit inputs | S21 · Create habit — Validation errors | Edit fields | S22 · Create habit — Valid Custom draft |
| Correct invalid habit inputs | S22 · Create habit — Valid Custom draft | Save habit | S23 · Overview — Valid Custom habit saved |
| Correct the current total | S24 · Overview — Existing total | Record progress | S25 · Record progress — Replace with lower total |
| Correct the current total | S25 · Record progress — Replace with lower total | Save 8 minutes | S26 · Overview — Corrected total |
| Correct the current total | S25 · Record progress — Replace with lower total | Enter 1e3 / Save | S27 · Record progress — Invalid progress total |
| Correct the current total | S27 · Record progress — Invalid progress total | Edit total to zero | S28 · Record progress — Zero is valid |
| Correct the current total | S28 · Record progress — Zero is valid | Save total | S29 · Overview — Zero total saved |
| Understand when tracking is due | S30 · Overview — Waiting for first period | Next Monday | S31 · Overview — First period starts |
| Understand when tracking is due | S31 · Overview — First period starts | View off-day habit | S32 · Overview — No occurrence today |
| Understand when tracking is due | S32 · Overview — No occurrence today | Zone-change gap | S33 · Overview — Between periods |
| Edit name and a current target | S34 · Overview — Daily habit selected | Edit habit | S35 · Edit habit — Daily target change |
| Edit name and a current target | S35 · Edit habit — Daily target change | Save changes | S36 · Overview — New target effective today |
| Edit name and a current target | S36 · Overview — New target effective today | Review habit | S37 · Habit review — Target change history |
| Schedule targets for a later period | S38 · Edit habit — Weekly target change | Save changes | S39 · Overview — Future target saved |
| Schedule targets for a later period | S39 · Overview — Future target saved | Edit target again | S40 · Edit habit — Revise pending target |
| Schedule targets for a later period | S40 · Edit habit — Revise pending target | Save changes | S41 · Overview — Latest pending target saved |
| Schedule targets for a later period | S41 · Overview — Latest pending target saved | Review habit | S42 · Habit review — Superseded target retained |
| Leave create habit with changes | S43 · Create habit — Unsaved changes | Back with changes | S44 · Create habit — Discard changes? |
| Leave create habit with changes | S44 · Create habit — Discard changes? | Keep editing | S45 · Create habit — Editing continued |
| Leave create habit with changes | S44 · Create habit — Discard changes? | Discard | S46 · Overview — Draft discarded |
| Leave edit habit with changes | S47 · Edit habit — Unsaved changes | Back with changes | S48 · Edit habit — Discard changes? |
| Leave edit habit with changes | S48 · Edit habit — Discard changes? | Keep editing | S49 · Edit habit — Editing continued |
| Leave edit habit with changes | S48 · Edit habit — Discard changes? | Discard | S50 · Overview — Draft discarded |
| Leave record progress with changes | S51 · Record progress — Unsaved changes | Back with changes | S52 · Record progress — Discard changes? |
| Leave record progress with changes | S52 · Record progress — Discard changes? | Keep editing | S53 · Record progress — Editing continued |
| Leave record progress with changes | S52 · Record progress — Discard changes? | Discard | S54 · Overview — Draft discarded |
| Handle a period ending before Save | S55 · Record progress — Previous period draft | Save after midnight | S56 · Record progress — Closed period; text retained |
| Handle a period ending before Save | S56 · Record progress — Closed period; text retained | Choose current period | S57 · Record progress — Current period chosen |
| Handle a period ending before Save | S57 · Record progress — Current period chosen | Save total | S58 · Overview — Current period saved |
| Handle a period ending before Save | S56 · Record progress — Closed period; text retained | Close | S59 · Overview — Closed without new entry |
| Close a late draft when nothing is due | S60 · Record progress — Selected-day draft | Late Save | S61 · Record progress — Closed; no current period |
| Close a late draft when nothing is due | S61 · Record progress — Closed; no current period | Close | S62 · Overview — Next selected day shown |
| Allow or decline notifications | S63 · Overview — First habit committed | Request once | S64 · Overview — Android permission prompt |
| Allow or decline notifications | S64 · Overview — Android permission prompt | Allow | S65 · Overview — Notifications allowed |
| Allow or decline notifications | S64 · Overview — Android permission prompt | Don’t allow | S66 · Overview — Notifications unavailable |
| Review and acknowledge missed periods | S67 · Overview — Misses awaiting review | Review habit | S68 · Habit review — Misses acknowledged |
| Review and acknowledge missed periods | S68 · Habit review — Misses acknowledged | Back | S69 · Overview — Miss indicator cleared |
| Review and acknowledge missed periods | S67 · Overview — Misses awaiting review | Notification delivered | S70 · Overview — Android miss notification |
| Review and acknowledge missed periods | S70 · Overview — Android miss notification | Tap notification | S71 · Habit review — Opened from notification |
| Review and acknowledge missed periods | S71 · Habit review — Opened from notification | Back | S72 · Overview — Returned from notification review |
| See a success and consistency badge | S73 · Overview — Daily consistency nearly met | Next day / reopen | S74 · Overview — Success and badge awarded |
| See a success and consistency badge | S74 · Overview — Success and badge awarded | Review habit | S75 · Habit review — Badge in history |
| Retire, undo or keep history | S76 · Overview — Active habit | Retire habit | S77 · Retire habit — Confirm retirement |
| Retire, undo or keep history | S77 · Retire habit — Confirm retirement | Confirm Retire | S78 · Overview — Pending retirement |
| Retire, undo or keep history | S78 · Overview — Pending retirement | Undo in time | S79 · Overview — Timely Undo restored |
| Retire, undo or keep history | S78 · Overview — Pending retirement | 10 seconds expire | S80 · Overview — Retirement complete |
| Retire, undo or keep history | S80 · Overview — Retirement complete | Retired habits | S81 · Retired habits — Retained habits |
| Retire, undo or keep history | S81 · Retired habits — Retained habits | Review Meditation | S82 · Habit review — Read-only retired history |
| Retire, undo or keep history | S82 · Habit review — Read-only retired history | Back | S83 · Retired habits — Back from retired review |
| Retire, undo or keep history | S77 · Retire habit — Confirm retirement | Cancel | S84 · Overview — Retirement cancelled |
| Recover an expired Undo action | S85 · Overview — Pending row at deadline | Undo at deadline | S86 · Overview — Undo too late |
| Recover an expired Undo action | S86 · Overview — Undo too late | View retired habits | S87 · Retired habits — Expired habit retained |
| Recover a local read failure | S88 · Overview — Loading local data | Local read fails | S89 · Overview — Read failed |
| Recover a local read failure | S89 · Overview — Read failed | Retry | S90 · Overview — Read recovered |
| Retry saving habit | S91 · Create habit — Ready to save | Save | S92 · Create habit — Saving |
| Retry saving habit | S92 · Create habit — Saving | Local save fails | S93 · Create habit — Save failed; draft retained |
| Retry saving habit | S93 · Create habit — Save failed; draft retained | Retry | S94 · Overview — Retry committed |
| Retry saving changes | S95 · Edit habit — Ready to save | Save | S96 · Edit habit — Saving |
| Retry saving changes | S96 · Edit habit — Saving | Local save fails | S97 · Edit habit — Save failed; draft retained |
| Retry saving changes | S97 · Edit habit — Save failed; draft retained | Retry | S98 · Overview — Retry committed |
| Retry saving total | S99 · Record progress — Ready to save | Save | S100 · Record progress — Saving |
| Retry saving total | S100 · Record progress — Saving | Local save fails | S101 · Record progress — Save failed; draft retained |
| Retry saving total | S101 · Record progress — Save failed; draft retained | Retry | S102 · Overview — Retry committed |
| Resume drafts without replaying actions | S103 · Create habit — Draft before process death | System restores task | S104 · Create habit — Retained-task draft restored |
| Resume drafts without replaying actions | S104 · Create habit — Retained-task draft restored | Save unchanged draft | S105 · Create habit — Prior commit found on resubmit |
| Resume drafts without replaying actions | S105 · Create habit — Prior commit found on resubmit | Back | S106 · Overview — Committed habit preserved |
| Resume drafts without replaying actions | S104 · Create habit — Retained-task draft restored | Remove task / reopen | S107 · Overview — Reopened after task removal |
| Resume multiple pending retirements | S108 · Overview — Two pending rows | Restart before deadlines | S109 · Overview — Pending rows after restart |
| Resume multiple pending retirements | S109 · Overview — Pending rows after restart | Undo Meditation | S110 · Overview — One restored; one pending |
| Resume multiple pending retirements | S110 · Overview — One restored; one pending | Reading deadline expires | S111 · Overview — Other retirement settled |
| Record from review and return to Overview | S112 · Habit review — No finalized results yet | Record progress | S113 · Record progress — Entered from review |
| Record from review and return to Overview | S113 · Record progress — Entered from review | Save total | S114 · Overview — Review entry saved |
| Record from review and return to Overview | S113 · Record progress — Entered from review | Close unchanged | S115 · Overview — Closed without changes |
| Find retired history when the list is empty | S116 · Overview — No active habits | Retired habits | S117 · Retired habits — No retired habits |
| Find retired history when the list is empty | S117 · Retired habits — No retired habits | Back | S118 · Overview — Returned to empty Overview |
| Change a target before the next occurrence | S119 · Overview — No occurrence today | Edit habit | S120 · Edit habit — Target for next occurrence |
| Change a target before the next occurrence | S120 · Edit habit — Target for next occurrence | Save changes | S121 · Overview — Next occurrence target saved |
<!-- /generated:transitions -->

## 5. State model per screen

The following are conceptual UI state families, not implementation code or a new architecture. All detailed frame states map to the canonical screen inventory. The supplied PRD/LLD remain the authority for behavior.

### Overview

State families: Loading, ReadError, Empty, ActiveContent, WaitingFirstPeriod, NoOccurrenceToday, BetweenPeriods, PendingRetirements, SaveFeedback and NotificationPermission. Show global points and active habit name, unit, schedule, current target/progress/end or next start, status/last outcome, Active run, Success streak, badge and unacknowledged misses. Pending habits are excluded from active tracking cards and shown as durable rows with individual countdowns. Foreground catch-up refreshes periods; never show false empty data after a read failure. No offline/sync banner is needed.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| No active habits | S1 | First launch | Global points: 0; Create a habit with a measurable target.; Retired habits — Personal on-device data; no sign-in. Back leaves app. Text scale / screen readers supported. |
| Daily habit created | S3 | S2 Save habit | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0<br>Badge: not earned<br>Unacknowledged misses: 0; Record progress; Review habit; Habit saved — Permission after first creation: see notification journey. |
| Total saved | S5 | S4 Save total | Global points: 0; Meditation · Daily<br>Today: 12 / 10 minutes<br>Target met · period still open<br>Active run 1 · Streak 0; Total saved |
| Next day, success finalized | S6 | S5 Next day / reopen | Global points: 10; Meditation · Daily<br>Today: 0 / 10 minutes<br>Last result: Success · +10<br>Active run 2 · Streak 1; Review habit — Finalize at boundary / next app use; no exact background timing promise. |
| Returned from review | S8 | S7 Back | Global points: 10; Meditation · Daily<br>Today: 0 / 10 minutes<br>Last result: Success<br>Active run 2 · Streak 1 |
| Open today | S11 | S10 Save habit | Global points: 0; Read · Daily<br>20 pages target<br>11 Oct → 12 Oct 00:00<br>0 / 20 pages · Active run 1<br>Streak 0 — One current occurrence. |
| Waiting for first period | S13 | S12 Save habit | Global points: 0; Read · Weekly<br>20 pages target<br>Mon 12 Oct → Mon 19 Oct<br>Next start shown · Active run 0<br>Streak 0 — No Record progress action until an occurrence opens. |
| Open 14-day period | S15 | S14 Save habit | Global points: 0; Read · Fortnightly<br>20 pages target<br>11 Oct → 25 Oct 00:00<br>0 / 20 pages · Active run 1<br>Streak 0 — One current occurrence. |
| Waiting until 1 Nov | S17 | S16 Save habit | Global points: 0; Read · Monthly<br>20 pages target<br>1 Nov → 1 Dec 00:00<br>Next start shown · Active run 0<br>Streak 0 — No Record progress action until an occurrence opens. |
| Waiting for selected weekday | S19 | S18 Save habit | Global points: 0; Read · Custom weekdays<br>20 pages target<br>Mon / Wed / Fri; first Mon 12 Oct<br>Next start shown · Active run 0<br>Streak 0 — No Record progress action until an occurrence opens. |
| Valid Custom habit saved | S23 | S22 Save habit | Global points: 0; Reading · Custom: Mon / Wed / Fri<br>Target: 20 pages<br>Next start: Mon 12 Oct<br>Active run 0 · Streak 0 |
| Existing total | S24 | Scenario entry: Correct the current total | Global points: 0; Meditation · Daily<br>Today: 12 / 10 minutes<br>Active run 1 · Streak 0; Record progress |
| Corrected total | S26 | S25 Save 8 minutes | Global points: 0; Meditation · Daily<br>Today: 8 / 10 minutes<br>Target not yet met<br>Active run 1 · Streak 0 |
| Zero total saved | S29 | S28 Save total | Global points: 0; Meditation · Daily<br>Today: 0 / 10 minutes<br>Active run 1 · Streak 0 |
| Waiting for first period | S30 | Created Sunday | Global points: 0; Reading · Weekly<br>Target: 20 pages<br>Next start: Mon 12 Oct<br>Waiting for first period<br>Active run 0 · Streak 0 — No progress action; other habits may still be due. |
| First period starts | S31 | S30 Next Monday | Global points: 0; Reading · Weekly<br>12–18 Oct: 0 / 20 pages<br>Ends Mon 19 Oct 00:00<br>Active run 1 · Streak 0 |
| No occurrence today | S32 | Selected-day habit on Sunday | Global points: 0; Stretch · Custom Mon/Wed/Fri<br>Target: 5 minutes<br>Sunday: no occurrence today<br>Next start: Mon 12 Oct<br>Active run 3 · Streak 3 |
| Between periods | S33 | Timezone change creates a gap | Global points: 0; Meditation · Daily<br>No current occurrence<br>Next start: 12 Oct 00:00<br>Active run 10 · Streak 3 — Existing boundaries stay fixed. Future periods use current timezone; none overlap. |
| Daily habit selected | S34 | Scenario entry: Edit name and a current target | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0; Edit habit |
| New target effective today | S36 | S35 Save changes | Global points: 0; Morning meditation · Daily<br>Today: 0 / 15 minutes<br>Active run 1 · Streak 0; Review habit; Target 15 applies to 11 Oct |
| Future target saved | S39 | S38 Save changes | Global points: 0; Reading · Weekly<br>Current target: 20 pages<br>Progress: 12 / 20<br>Next target: 30 from 19 Oct; Edit target again; Target 30 starts 19 Oct — Same deferred rule for Weekly, Fortnightly and Monthly. |
| Latest pending target saved | S41 | S40 Save changes | Global points: 0; Reading · Weekly<br>Current: 12 / 20 pages<br>Next target: 40 from 19 Oct; Review habit |
| Draft discarded | S46 | S44 Discard | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 — Intentional departure drops unsaved input; committed data remains. |
| Draft discarded | S50 | S48 Discard | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 — Intentional departure drops unsaved input; committed data remains. |
| Draft discarded | S54 | S52 Discard | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 — Intentional departure drops unsaved input; committed data remains. |
| Current period saved | S58 | S57 Save total | Global points: 99; Meditation · Daily<br>12 Oct: 12 / 10 minutes<br>11 Oct: Missed (unchanged)<br>Active run 2 · Streak 0 |
| Closed without new entry | S59 | S56 Close | Global points: 99; Meditation · Daily<br>12 Oct: 0 / 10 minutes<br>11 Oct: Missed · −1<br>1 unacknowledged miss |
| Next selected day shown | S62 | S61 Close | Global points: 0; Stretch · Mon/Wed/Fri<br>Target: 5 minutes<br>Next start: Mon 12 Oct<br>1 unacknowledged miss |
| First habit committed | S63 | First successful creation on API 33+ | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0; Habit saved — API 24–32 skips runtime prompt. No permission before commit. |
| Android permission prompt | S64 | S63 Request once | Allow Habit Tracker to send notifications about missed habit periods? — Request once. Does not replay after restoration; no automatic re-request. |
| Notifications allowed | S65 | S64 Allow | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0; Tracking continues normally |
| Notifications unavailable | S66 | S64 Don’t allow | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0; Misses remain visible in the app — Denied permission / blocked channel never prevents tracking. |
| Misses awaiting review | S67 | Two catch-up misses from balance 100 | Global points: 98; Meditation · Daily<br>Today: 0 / 10 minutes<br>Last: Missed · −1 point<br>Active run 11 · Streak 0<br>2 unacknowledged misses; Review habit — Each miss: ceil(1% of current global points), floor 0. Run continues; streak resets. |
| Miss indicator cleared | S69 | S68 Back | Global points: 98; Meditation · Daily<br>Today: 0 / 10 minutes<br>Last: Missed<br>Active run 11 · Streak 0 |
| Android miss notification | S70 | Android notification shade | Android notification<br>Meditation: 2 missed periods; Tap to review results — One notification per active habit; new miss re-alerts. Closed-app delivery best effort. |
| Returned from notification review | S72 | S71 Back | Global points: 98; Meditation · Daily<br>Today: 0 / 10 minutes<br>Misses acknowledged |
| Daily consistency nearly met | S73 | Scenario entry: See a success and consistency badge | Global points: 170; Meditation · Daily<br>Today: 10 / 10 minutes<br>21-date window: 17 successes<br>Active run 21 · Streak 4 — Target met while open does not finalize points or badge. |
| Success and badge awarded | S74 | S73 Next day / reopen | Global points: 180; Meditation · Daily<br>Last: Success · +10<br>Active run 22 · Streak 5<br>21-Day Consistency earned; Review habit — Daily only: ≥18 successes in 21 consecutive dates; badge once, never revoked. |
| Active habit | S76 | After 10 Oct success; 11 Oct target change | Global points: 10; Meditation · Daily<br>Today: 0 / 15 minutes<br>Last: Success · +10<br>Active run 2 · Streak 1; Retire habit |
| Pending retirement | S78 | S77 Confirm Retire | Global points: 10; No active Meditation<br>Global points retained; Meditation · Retirement pending<br>Undo available: 8 seconds; Undo retirement; Retired habits — Durable row, not snackbar; open period frozen. Multiple pending rows have own countdowns. |
| Timely Undo restored | S79 | S78 Undo in time | Global points: 10; Meditation · Daily<br>Today: 0 / 15 minutes<br>Last: Success · +10<br>Active run 2 · Streak 1 — Undo before deadline resumes frozen period/run, then catches up if its boundary elapsed. |
| Retirement complete | S80 | S78 10 seconds expire | Global points: 10; Retired history is still available.; Retired habits — At/after deadline: frozen occurrence cancelled; no points, streak or badge effects. |
| Retirement cancelled | S84 | S77 Cancel | Global points: 10; Meditation · Daily<br>Today: 0 / 15 minutes<br>Last: Success · +10<br>Active run 2 · Streak 1 — Cancel / Android Back sends no retirement command. |
| Pending row at deadline | S85 | Restored countdown or delayed tap | Global points: 99; Meditation · Retirement pending<br>Undo: less than 1 second; Undo retirement |
| Undo too late | S86 | S85 Undo at deadline | Undo window ended; habit retired; View retired habits — Wall-clock deadline survives restart. Never restart the 10-second timer. |
| Loading local data | S88 | App launch; noticeable wait | Loading habits… — App works offline; no network / sync banner. |
| Read failed | S89 | S88 Local read fails | Try again to load your saved habits. — No Create / Record / Edit until dependent data loads; never show false empty state. |
| Read recovered | S90 | S89 Retry | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 |
| Retry committed | S94 | S93 Retry | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0; Saved — Accepted command survives leaving screen. After-commit notification failure is not a save error. |
| Retry committed | S98 | S97 Retry | Global points: 0; Morning meditation · Daily<br>Today: 0 / 15 minutes<br>Active run 1 · Streak 0; Target 15 applies to 11 Oct — Accepted command survives leaving screen. After-commit notification failure is not a save error. |
| Retry committed | S102 | S101 Retry | Global points: 0; Meditation · Daily<br>Today: 12 / 10 minutes<br>Active run 1 · Streak 0; Total saved — Accepted command survives leaving screen. After-commit notification failure is not a save error. |
| Committed habit preserved | S106 | S105 Back | Global points: 0; Reading · Weekly<br>Next start: Mon 12 Oct<br>20 pages · Active run 0 |
| Reopened after task removal | S107 | S104 Remove task / reopen | Global points: 0; Saved habits remain<br>Unsaved drafts were discarded — Swiping task away removes drafts; committed commands/data remain saved. |
| Two pending rows | S108 | Two retirements confirmed independently | Global points: 99; Meditation · Pending<br>Undo: 7 seconds; Reading · Pending<br>Undo: 9 seconds |
| Pending rows after restart | S109 | S108 Restart before deadlines | Global points: 99; Meditation · Pending<br>Undo: 2 seconds; Undo Meditation; Reading · Pending<br>Undo: 4 seconds — Countdowns use saved deadlines; tracking and notifications suppressed for each pending habit. |
| One restored; one pending | S110 | S109 Undo Meditation | Global points: 99; Meditation · Daily<br>Frozen occurrence resumed<br>Active run restored; Reading · Pending<br>Undo: 3 seconds |
| Other retirement settled | S111 | S110 Reading deadline expires | Global points: 99; Meditation active<br>Reading retired; history retained — Expired pending rows settle before normal tracking. Only the undone habit resumes. |
| Review entry saved | S114 | S113 Save total | Global points: 0; Meditation · Daily<br>Today: 12 / 10 minutes |
| Closed without changes | S115 | S113 Close unchanged | Global points: 0; Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 |
| No active habits | S116 | Scenario entry: Find retired history when the list is empty | Create a habit or browse retired history.; Retired habits |
| Returned to empty Overview | S118 | S117 Back | Choose your target and schedule. |
| No occurrence today | S119 | Scenario entry: Change a target before the next occurrence | Stretch · Custom Mon/Wed/Fri<br>Target: 5 minutes<br>Next start: Mon 12 Oct; Edit habit |
| Next occurrence target saved | S121 | S120 Save changes | Stretch · Custom Mon/Wed/Fri<br>Next target: 10 minutes<br>Next start: Mon 12 Oct; Target 10 applies from 12 Oct |

### Create habit

State families: Draft, Invalid, Saving, SaveError, DiscardConfirmation, RestoredDraft and DuplicateAfterPriorCommit. Retain raw fields on validation/failure. Name is trimmed 1–50 Unicode code points, unit 1–20; names are unique ignoring case among active/pending habits, and retired names are reusable. Target >0. Decimals follow the device locale, allow up to 9 integer and 3 fractional digits after trailing-zero removal, and reject signs, grouping and exponents. Custom needs at least one weekday. Daily starts today; Weekly starts this week only on Monday, otherwise next Monday; Monthly starts this month only on day 1, otherwise next month; Fortnightly is creation-anchored in 14-day blocks; Custom starts on the first selected date on/after creation.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Daily draft | S2 | S1 Create habit | Name; Unit; Target; Frequency: Daily<br>Starts today; Save habit; Cancel — Other frequencies are shown on the schedule pages. Clean Cancel returns Overview. |
| Choose frequency | S9 | Creation on Sun 11 Oct 2026 | Name; Unit; Target — The five rows are frequency choices, not app destinations. |
| Daily selected | S10 | S9 Choose Daily | Name; Unit; Target; Daily<br>11 Oct → 12 Oct 00:00; Save habit — Daily starts on creation date. |
| Weekly selected | S12 | S9 Choose Weekly | Name; Unit; Target; Weekly<br>Mon 12 Oct → Mon 19 Oct; Save habit — Weekly starts today if Monday; Monthly if day 1. |
| Fortnightly selected | S14 | S9 Choose Fortnightly | Name; Unit; Target; Fortnightly<br>11 Oct → 25 Oct 00:00; Save habit — Fortnightly is creation-anchored, not calendar halves. |
| Monthly selected | S16 | S9 Choose Monthly | Name; Unit; Target; Monthly<br>1 Nov → 1 Dec 00:00; Save habit — Weekly starts today if Monday; Monthly if day 1. |
| Custom weekdays selected | S18 | S9 Choose Custom weekdays | Name; Unit; Target; Custom weekdays<br>Mon / Wed / Fri; first Mon 12 Oct; Save habit — Custom needs ≥1 weekday; only selected dates have an occurrence. |
| Invalid draft | S20 | Scenario entry: Correct invalid habit inputs | Name; Unit; Target; Frequency: Custom; no weekdays<br>Starts today; Save habit; Cancel |
| Validation errors | S21 | S20 Save invalid draft | Name; An active Meditation already exists; Unit; Target; Enter a positive number without a sign; Custom weekdays<br>Select at least one day; Save habit — Retain raw fields. Also reject empty/overlong text, grouping/exponents and excess digits. |
| Valid Custom draft | S22 | S21 Edit fields | Name; Unit; Target; Frequency: Custom: Mon / Wed / Fri<br>Starts today; Save habit; Cancel — Example decimal uses device locale. Retired names can be reused. |
| Unsaved changes | S43 | Scenario entry: Leave create habit with changes | Name; Unit; Target; Frequency: Daily<br>Starts today; Save habit; Cancel — Untouched forms close directly. Back and explicit Cancel / Close use this check. |
| Discard changes? | S44 | S43 Back with changes | Your unsaved changes will be lost. |
| Editing continued | S45 | S44 Keep editing | Name; Unit; Target; Frequency: Daily<br>Starts today; Save habit; Cancel — All raw values remain. Resume editing and save normally. |
| Ready to save | S91 | Scenario entry: Retry saving habit | Name; Unit; Target; Frequency: Daily<br>Starts today; Save habit; Cancel |
| Saving | S92 | S91 Save | Name; Unit; Target; Frequency: Daily<br>Starts today; Saving… — Relevant Save disabled until commit/failure; no duplicate submissions. Show wait after 300 ms. |
| Save failed; draft retained | S93 | S92 Local save fails | Name; Unit; Target; Frequency: Daily<br>Starts today; Your input is retained. Nothing was saved. |
| Draft before process death | S103 | Scenario entry: Resume drafts without replaying actions | Name; Unit; Target; Frequency: Weekly<br>Starts today; Save habit; Cancel |
| Retained-task draft restored | S104 | S103 System restores task | Name; Unit; Target; Frequency: Weekly<br>Starts today; Save habit; Cancel — Rotation / retained-task process death restores raw fields. No save, snackbar or permission replay. |
| Prior commit found on resubmit | S105 | S104 Save unchanged draft | Name; An active Reading already exists; Unit; Target; Weekly · next start 12 Oct; Back — Only if earlier accepted save committed: deliberate unchanged resubmit gets duplicate error. |

### Record progress

State families: OpenDraft, Invalid, Saving, SaveError, DiscardConfirmation, ClosedWithCurrentPeriod and ClosedWithoutCurrentPeriod. Save replaces the original occurrence’s total; progress ≥0, with the same decimal syntax/precision as target. A lower value or zero is valid. Closed history is immutable. A late Save shows the final result while retaining typed text bound to the old occurrence. “Record for current period” explicitly selects and displays the new period, keeps the text for review, and requires a separate Save. If no current occurrence exists, show next start and Close. Restoration never silently rebinds or submits a total.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Open period total | S4 | S3 Record progress | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — Separate destination rendered as a sheet. Save / Close returns Overview. |
| Replace with lower total | S25 | S24 Record progress | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — Correction may lower total; a zero total is valid. |
| Invalid progress total | S27 | S25 Enter 1e3 / Save | Meditation · Current period<br>Target: 10 minutes; Period total; Use a nonnegative decimal; Save total — Invalid save keeps entered text; no saved total changes. |
| Zero is valid | S28 | S27 Edit total to zero | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close |
| Unsaved changes | S51 | Scenario entry: Leave record progress with changes | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — Untouched forms close directly. Back and explicit Cancel / Close use this check. |
| Discard changes? | S52 | S51 Back with changes | Your unsaved changes will be lost. |
| Editing continued | S53 | S52 Keep editing | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — All raw values remain. Resume editing and save normally. |
| Previous period draft | S55 | Draft open across midnight | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close |
| Closed period; text retained | S56 | S55 Save after midnight | That period has ended; 11 Oct · final total 8 / 10<br>Missed · −1 point<br>Final result cannot change; Unsaved total; Record for current period; Close — Late Save rejected. Never silently bind old total to a new occurrence. |
| Current period chosen | S57 | S56 Choose current period | Meditation · 12 Oct · ends 13 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — Explicit choice rebinds retained text; review it, then tap Save. No automatic submission. |
| Selected-day draft | S60 | Restored on Saturday | Stretch · Fri 9 Oct; Period total; Save total |
| Closed; no current period | S61 | S60 Late Save | Friday’s period has ended; 9 Oct · 0 / 5 minutes<br>Missed · final; Unsaved total; No occurrence today<br>Next start: Mon 12 Oct; Close |
| Ready to save | S99 | Scenario entry: Retry saving total | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close |
| Saving | S100 | S99 Save | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Saving… — Relevant Save disabled until commit/failure; no duplicate submissions. Show wait after 300 ms. |
| Save failed; draft retained | S101 | S100 Local save fails | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Your input is retained. Nothing was saved. |
| Entered from review | S113 | S112 Record progress | Meditation · 11 Oct · ends 12 Oct 00:00; Target: 10 minutes; Period total; Save total; Close — Save / Close returns Overview, even when opened from Review. |

### Edit habit

State families: Draft, Invalid, Saving, SaveError, DiscardConfirmation and EffectivePeriodFeedback. Only name and target change; unit and cadence are fixed. Daily/Custom edits affect an open occurrence immediately; Weekly/Fortnightly/Monthly edits start next period; any edit with no open occurrence starts at the next occurrence. State the effective period after commit. Multiple changes to the same upcoming period keep all audit entries, mark earlier values Superseded, and apply the latest. Completed period targets/results never recalculate.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Daily target change | S35 | S34 Edit habit | Name; Target; Daily · minutes<br>Unit and frequency are fixed; Applies to today’s open period; Save changes; Cancel |
| Weekly target change | S38 | Open week of 12–18 Oct | Name; Target; Weekly · pages (fixed)<br>Current period: 20 pages<br>Next period: 19–25 Oct; Save changes |
| Revise pending target | S40 | S39 Edit target again | Name; Target; Effective next period: 19 Oct; Save changes |
| Unsaved changes | S47 | Scenario entry: Leave edit habit with changes | Name; Target; Save changes — Untouched forms close directly. Back and explicit Cancel / Close use this check. |
| Discard changes? | S48 | S47 Back with changes | Your unsaved changes will be lost. |
| Editing continued | S49 | S48 Keep editing | Name; Target; Save changes — All raw values remain. Resume editing and save normally. |
| Ready to save | S95 | Scenario entry: Retry saving changes | Name; Target; Save changes |
| Saving | S96 | S95 Save | Name; Target; Saving… — Relevant Save disabled until commit/failure; no duplicate submissions. Show wait after 300 ms. |
| Save failed; draft retained | S97 | S96 Local save fails | Name; Target; Your input is retained. Nothing was saved. |
| Target for next occurrence | S120 | S119 Edit habit | Name; Target; Custom · minutes (fixed)<br>Effective: Mon 12 Oct; Save changes — With no open occurrence, even Daily / Custom changes start at the next occurrence. |

### Habit review

State families: NoFinalizedResults, ActiveHistory, AcknowledgedMissHistory, TargetChangeHistory, BadgeHistory and RetiredReadOnlyHistory. Display chronological finalized periods with effective target, progress, result and points effect, plus target changes with effective/superseded status and earned badge. Opening from any route acknowledges misses and clears active miss indicators/notification, without modifying history or points. Success adds 10; a miss deducts ceil(1% of the current global balance), minimum 1 if positive, floor zero. Active run counts started periods including the open period and continues across misses; Success streak counts consecutive finalized successes and resets on a miss. The Daily-only badge is awarded once for ≥18 successes across 21 consecutive calendar dates. Retired review includes the cancelled frozen period with zero points and offers no mutation controls.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Saved result history | S7 | S6 Review habit | Meditation · Daily · minutes; Active run 2 · Success streak 1; 11 Oct · target 10 · total 12<br>Success · +10 points; Back to Overview |
| Target change history | S37 | S36 Review habit | Morning meditation · Daily · minutes; Today’s occurrence is still open.; Target 10 → 15<br>Effective: 11 Oct<br>Saved today — Daily / Custom: open period changes now. No open period: next occurrence. |
| Superseded target retained | S42 | S41 Review habit | Reading · Weekly · pages; Target changes<br>30 from 19 Oct · Superseded<br>40 from 19 Oct · Applies; Historical results keep original targets |
| Misses acknowledged | S68 | S67 Review habit | Meditation · Daily<br>Active run 11 · Streak 0; 9 Oct · 0 / 10 · Missed · −1<br>10 Oct · 8 / 10 · Missed · −1; No unacknowledged misses; Back to Overview — Opening review acknowledges; history and points do not change. |
| Opened from notification | S71 | S70 Tap notification | Meditation · Daily; 9 Oct · Missed · −1<br>10 Oct · Missed · −1; Miss count and notification cleared; Back to Overview — Explicit Android stack: Overview → Review. Back returns Overview. |
| Badge in history | S75 | S74 Review habit | Meditation · Daily; 11 Oct · 10 / 10<br>Success · +10 points; 21-Day Consistency<br>Earned: 12 Oct; 18 successes across 21 dates |
| Read-only retired history | S82 | S81 Review Meditation | Meditation · Retired · Daily; 10 Oct · 10 / 10 minutes<br>Success · +10 points; Target 10 → 15<br>Effective 11 Oct; 11 Oct · Cancelled<br>Frozen total: 0 / 15 · 0 points; No progress, edit or reactivate; Back to retired list — Same history as before retirement: balance 10 retained; no reactivation. |
| No finalized results yet | S112 | Scenario entry: Record from review and return to Overview | Meditation · Daily; Results appear after a period ends.; Today: 0 / 10 minutes; Record progress — Review acknowledges misses on any entry. Period history cannot be edited. |

### Retire habit

State family: Confirmation. Habit remains active until confirmed. Confirm stops tracking and freezes the current occurrence; Cancel/Back sends no command. A separate pending Overview row supports Undo for 10 seconds. Timely Undo restores active status, run and frozen occurrence, then catches up if its boundary elapsed. At/after the persisted deadline Undo is rejected and offers retired history. Final retirement cancels the frozen occurrence with zero points and no streak/badge effect; previously finalized history and global points remain. Pending/retired habits cannot accept progress, edits or new tracking notifications.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Confirm retirement | S77 | S76 Retire habit | Tracking stops. You have 10 seconds to undo; history and points remain. |

### Retired habits

State families: EmptyList, RetainedList and ReturnedFromReview. Show retained habit history outside the active Overview. Retired review is read-only and returns here. No reactivation in V1. Retired habit names may be reused by newly created habits.

| State | S-number | Trigger | Visible content / constraint |
|---|---|---|---|
| Retained habits | S81 | S80 Retired habits |  |
| Back from retired review | S83 | S82 Back | Meditation · Retired |
| Expired habit retained | S87 | S86 View retired habits | Meditation · Retired<br>History preserved |
| No retired habits | S117 | S116 Retired habits | Retired habits will remain here for review.; Back to Overview |

### Shared lifecycle and time behavior

Show indeterminate waiting feedback for work over 300 ms; disable only the relevant duplicate Save until commit or failure. Do not claim success before commit. Accepted work survives destination departure; after-commit notification failure never becomes a save failure. Raw destination drafts restore on rotation or retained-task system process death; transient Save/navigation/snackbar/permission effects do not replay. Removing the task discards unsaved drafts. If a restored create draft refers to an earlier committed save, deliberately saving it unchanged can produce the documented duplicate-name error.

Retirement deadlines persist and do not restart on recreation. Multiple pending rows restore independently. Existing occurrence boundaries survive timezone changes; only future periods use the new zone, with at most one open occurrence. Clock rollback never reopens/re-scores history; clock forward catches up elapsed periods. Background miss delivery is best effort; the prototype promises no exact-midnight delivery. These are approved behavior constraints, not timing verified by this diagram.

## 6. Component mapping

### Android — Material 3 / Compose

| Sketch element | Native component meaning | Variants / behavior |
|---|---|---|
| bar | TopAppBar | Back/Up only below Overview; labelled accessible navigation |
| text | Text | Semantic headings/status; readable at system text scale |
| field | OutlinedTextField | Visible label, supporting/error text; retain raw input |
| button | Button / TextButton | Primary filled Save; secondary/text Cancel/Close; destructive confirmation uses error semantics |
| list | ListItem in scrolling list | Frequency choices or retired habits; full row touch target |
| card | Card | Habit summary, period result, target history or pending-retirement row |
| chips | FilterChip | Custom weekday selection; at least one selected |
| dialog | AlertDialog | Retirement and unsaved-draft decisions; dismissive action first |
| dialog with system=true | Android runtime notification permission UI | Owned by Android, not a custom Material dialog; prompt wording varies by OS |
| Record progress destination | ModalBottomSheet | Form body with period identity, retained total and Save / Close; accessible dismissal |
| snackbar | Snackbar | Committed-save feedback only; retirement Undo remains a durable row |
| loading | CircularProgressIndicator or LinearProgressIndicator | Indeterminate noticeable wait; disable relevant Save |
| empty / error / banner | Inline content using Text and recovery Button | Clear state meaning; Retry preserves input; unavailable data blocks dependent actions |

No bottom tabs were approved. The diagram’s rough boxes/fonts and blue link affordances are review notation rather than chosen app components or colors. Touch targets must meet the approved 48dp baseline; essential content must survive 200% text size and be identifiable to screen readers. Success/miss and permission fallback use text, not color alone.

### iOS

None: iOS is outside the approved platform scope.

## 7. Theme

Visual design not done: use the **Android Material 3 platform baseline** until a look is approved. No brand palette, custom font, shape/spacing scale, motion system, light/dark color scheme or dynamic-color preference was selected. No DESIGN.md, THEME-REPORT.md or final-look screen artifact exists. The hand-drawn appearance is for flow review and does not specify the native theme.

## 8. Copy

All strings below are **draft** unless indicated otherwise; approval of the flow did not approve final wording or localization. Example habit names, dates, units, totals, points and countdowns illustrate states and must be populated from real data. Permission strings are illustrative Android system copy, whose actual wording belongs to the OS. Frame headers, S-numbers, notes and outside-phone navigation are review markup, not product copy. No iOS variant or permission purpose string is applicable.

### Overview

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| overview.001 | bar.text | Overview | S1, S3, S5, S6, S8, S11, S13, S15, S17, S19, S23, S24, S26, S29, S30, S31, S32, S33, S34, S36, S39, S41, S46, S50, S54, S58, S59, S62, S63, S65, S66, S67, S69, S72, S73, S74, S76, S78, S79, S80, S84, S85, S86, S88, S89, S90, S94, S98, S102, S106, S107, S108, S109, S110, S111, S114, S115, S116, S118, S119, S121 | draft |
| overview.002 | text.text | Global points: 0 | S1, S3, S5, S11, S13, S15, S17, S19, S23, S24, S26, S29, S30, S31, S32, S33, S34, S36, S39, S41, S46, S50, S54, S62, S63, S65, S66, S90, S94, S98, S102, S106, S107, S114, S115 | draft |
| overview.003 | empty.text | Create a habit with a measurable target. | S1 | draft |
| overview.004 | empty.title | Start your first habit | S1 | draft |
| overview.005 | empty.action.text | Create habit | S1, S80, S116, S118 | draft |
| overview.006 | button.text | Retired habits | S1, S78, S80, S116 | draft |
| overview.007 | card.text | Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0<br>Badge: not earned<br>Unacknowledged misses: 0 | S3 | draft |
| overview.008 | button.text | Record progress | S3, S24 | draft |
| overview.009 | button.text | Review habit | S3, S6, S36, S41, S67, S74 | draft |
| overview.010 | snackbar.text | Habit saved | S3, S63 | draft |
| overview.011 | card.text | Meditation · Daily<br>Today: 12 / 10 minutes<br>Target met · period still open<br>Active run 1 · Streak 0 | S5 | draft |
| overview.012 | snackbar.text | Total saved | S5, S102 | draft |
| overview.013 | text.text | Global points: 10 | S6, S8, S76, S78, S79, S80, S84 | draft |
| overview.014 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Last result: Success · +10<br>Active run 2 · Streak 1 | S6 | draft |
| overview.015 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Last result: Success<br>Active run 2 · Streak 1 | S8 | draft |
| overview.016 | card.text | Read · Daily<br>20 pages target<br>11 Oct → 12 Oct 00:00<br>0 / 20 pages · Active run 1<br>Streak 0 | S11 | draft |
| overview.017 | card.text | Read · Weekly<br>20 pages target<br>Mon 12 Oct → Mon 19 Oct<br>Next start shown · Active run 0<br>Streak 0 | S13 | draft |
| overview.018 | card.text | Read · Fortnightly<br>20 pages target<br>11 Oct → 25 Oct 00:00<br>0 / 20 pages · Active run 1<br>Streak 0 | S15 | draft |
| overview.019 | card.text | Read · Monthly<br>20 pages target<br>1 Nov → 1 Dec 00:00<br>Next start shown · Active run 0<br>Streak 0 | S17 | draft |
| overview.020 | card.text | Read · Custom weekdays<br>20 pages target<br>Mon / Wed / Fri; first Mon 12 Oct<br>Next start shown · Active run 0<br>Streak 0 | S19 | draft |
| overview.021 | card.text | Reading · Custom: Mon / Wed / Fri<br>Target: 20 pages<br>Next start: Mon 12 Oct<br>Active run 0 · Streak 0 | S23 | draft |
| overview.022 | card.text | Meditation · Daily<br>Today: 12 / 10 minutes<br>Active run 1 · Streak 0 | S24, S102 | draft |
| overview.023 | card.text | Meditation · Daily<br>Today: 8 / 10 minutes<br>Target not yet met<br>Active run 1 · Streak 0 | S26 | draft |
| overview.024 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Active run 1 · Streak 0 | S29 | draft |
| overview.025 | card.text | Reading · Weekly<br>Target: 20 pages<br>Next start: Mon 12 Oct<br>Waiting for first period<br>Active run 0 · Streak 0 | S30 | draft |
| overview.026 | card.text | Reading · Weekly<br>12–18 Oct: 0 / 20 pages<br>Ends Mon 19 Oct 00:00<br>Active run 1 · Streak 0 | S31 | draft |
| overview.027 | card.text | Stretch · Custom Mon/Wed/Fri<br>Target: 5 minutes<br>Sunday: no occurrence today<br>Next start: Mon 12 Oct<br>Active run 3 · Streak 3 | S32 | draft |
| overview.028 | card.text | Meditation · Daily<br>No current occurrence<br>Next start: 12 Oct 00:00<br>Active run 10 · Streak 3 | S33 | draft |
| overview.029 | card.text | Meditation · Daily<br>10 minutes target<br>Today: 0 / 10 minutes<br>Ends tonight at 00:00<br>Active run 1 · Streak 0 | S34, S46, S50, S54, S63, S65, S66, S90, S94, S115 | draft |
| overview.030 | button.text | Edit habit | S34, S119 | draft |
| overview.031 | card.text | Morning meditation · Daily<br>Today: 0 / 15 minutes<br>Active run 1 · Streak 0 | S36, S98 | draft |
| overview.032 | snackbar.text | Target 15 applies to 11 Oct | S36, S98 | draft |
| overview.033 | card.text | Reading · Weekly<br>Current target: 20 pages<br>Progress: 12 / 20<br>Next target: 30 from 19 Oct | S39 | draft |
| overview.034 | button.text | Edit target again | S39 | draft |
| overview.035 | snackbar.text | Target 30 starts 19 Oct | S39 | draft |
| overview.036 | card.text | Reading · Weekly<br>Current: 12 / 20 pages<br>Next target: 40 from 19 Oct | S41 | draft |
| overview.037 | text.text | Global points: 99 | S58, S59, S85, S108, S109, S110, S111 | draft |
| overview.038 | card.text | Meditation · Daily<br>12 Oct: 12 / 10 minutes<br>11 Oct: Missed (unchanged)<br>Active run 2 · Streak 0 | S58 | draft |
| overview.039 | card.text | Meditation · Daily<br>12 Oct: 0 / 10 minutes<br>11 Oct: Missed · −1<br>1 unacknowledged miss | S59 | draft |
| overview.040 | card.text | Stretch · Mon/Wed/Fri<br>Target: 5 minutes<br>Next start: Mon 12 Oct<br>1 unacknowledged miss | S62 | draft |
| overview.041 | dialog.text | Allow Habit Tracker to send notifications about missed habit periods? | S64 | draft |
| overview.042 | dialog.title | Allow notifications? | S64 | draft |
| overview.043 | dialog.actions.text | Don’t allow | S64 | draft |
| overview.044 | dialog.actions.text | Allow | S64 | draft |
| overview.045 | text.text | Tracking continues normally | S65 | draft |
| overview.046 | text.text | Misses remain visible in the app | S66 | draft |
| overview.047 | text.text | Global points: 98 | S67, S69, S72 | draft |
| overview.048 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Last: Missed · −1 point<br>Active run 11 · Streak 0<br>2 unacknowledged misses | S67 | draft |
| overview.049 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Last: Missed<br>Active run 11 · Streak 0 | S69 | draft |
| overview.050 | card.text | Android notification<br>Meditation: 2 missed periods | S70 | draft |
| overview.051 | text.text | Tap to review results | S70 | draft |
| overview.052 | card.text | Meditation · Daily<br>Today: 0 / 10 minutes<br>Misses acknowledged | S72 | draft |
| overview.053 | text.text | Global points: 170 | S73 | draft |
| overview.054 | card.text | Meditation · Daily<br>Today: 10 / 10 minutes<br>21-date window: 17 successes<br>Active run 21 · Streak 4 | S73 | draft |
| overview.055 | text.text | Global points: 180 | S74 | draft |
| overview.056 | card.text | Meditation · Daily<br>Last: Success · +10<br>Active run 22 · Streak 5<br>21-Day Consistency earned | S74 | draft |
| overview.057 | card.text | Meditation · Daily<br>Today: 0 / 15 minutes<br>Last: Success · +10<br>Active run 2 · Streak 1 | S76, S79, S84 | draft |
| overview.058 | button.text | Retire habit | S76 | draft |
| overview.059 | card.text | No active Meditation<br>Global points retained | S78 | draft |
| overview.060 | card.text | Meditation · Retirement pending<br>Undo available: 8 seconds | S78 | draft |
| overview.061 | button.text | Undo retirement | S78, S85 | draft |
| overview.062 | empty.text | Retired history is still available. | S80 | draft |
| overview.063 | empty.title | No active habits | S80, S116 | draft |
| overview.064 | card.text | Meditation · Retirement pending<br>Undo: less than 1 second | S85 | draft |
| overview.065 | banner.text | Undo window ended; habit retired | S86 | draft |
| overview.066 | button.text | View retired habits | S86 | draft |
| overview.067 | loading.text | Loading habits… | S88 | draft |
| overview.068 | error.text | Try again to load your saved habits. | S89 | draft |
| overview.069 | error.title | Could not load habits | S89 | draft |
| overview.070 | error.action.text | Retry | S89 | draft |
| overview.071 | snackbar.text | Saved | S94 | draft |
| overview.072 | card.text | Reading · Weekly<br>Next start: Mon 12 Oct<br>20 pages · Active run 0 | S106 | draft |
| overview.073 | card.text | Saved habits remain<br>Unsaved drafts were discarded | S107 | draft |
| overview.074 | card.text | Meditation · Pending<br>Undo: 7 seconds | S108 | draft |
| overview.075 | card.text | Reading · Pending<br>Undo: 9 seconds | S108 | draft |
| overview.076 | card.text | Meditation · Pending<br>Undo: 2 seconds | S109 | draft |
| overview.077 | button.text | Undo Meditation | S109 | draft |
| overview.078 | card.text | Reading · Pending<br>Undo: 4 seconds | S109 | draft |
| overview.079 | card.text | Meditation · Daily<br>Frozen occurrence resumed<br>Active run restored | S110 | draft |
| overview.080 | card.text | Reading · Pending<br>Undo: 3 seconds | S110 | draft |
| overview.081 | card.text | Meditation active<br>Reading retired; history retained | S111 | draft |
| overview.082 | card.text | Meditation · Daily<br>Today: 12 / 10 minutes | S114 | draft |
| overview.083 | empty.text | Create a habit or browse retired history. | S116 | draft |
| overview.084 | empty.text | Choose your target and schedule. | S118 | draft |
| overview.085 | empty.title | Start a habit | S118 | draft |
| overview.086 | card.text | Stretch · Custom Mon/Wed/Fri<br>Target: 5 minutes<br>Next start: Mon 12 Oct | S119 | draft |
| overview.087 | card.text | Stretch · Custom Mon/Wed/Fri<br>Next target: 10 minutes<br>Next start: Mon 12 Oct | S121 | draft |
| overview.088 | snackbar.text | Target 10 applies from 12 Oct | S121 | draft |

### Create habit

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| create.001 | bar.text | Create habit | S2, S9, S10, S12, S14, S16, S18, S20, S21, S22, S43, S45, S91, S92, S93, S103, S104, S105 | draft |
| create.002 | field.label | Name | S2, S9, S10, S12, S14, S16, S18, S20, S21, S22, S43, S45, S91, S92, S93, S103, S104, S105 | draft |
| create.003 | field.value | Meditation | S2, S43, S45, S91, S92, S93 | draft |
| create.004 | field.helper | 1–50 characters; unique active name | S2, S20, S22, S43, S45, S91, S92, S93, S103, S104 | draft |
| create.005 | field.label | Unit | S2, S9, S10, S12, S14, S16, S18, S20, S21, S22, S43, S45, S91, S92, S93, S103, S104, S105 | draft |
| create.006 | field.value | minutes | S2, S20, S21, S43, S45, S91, S92, S93 | draft |
| create.007 | field.helper | 1–20 characters; fixed after save | S2, S20, S22, S43, S45, S91, S92, S93, S103, S104 | draft |
| create.008 | field.label | Target | S2, S9, S10, S12, S14, S16, S18, S20, S21, S22, S43, S45, S91, S92, S93, S103, S104, S105 | draft |
| create.009 | field.value | 10 | S2, S43, S45, S91, S92, S93 | draft |
| create.010 | field.helper | Positive; up to 9 + 3 digits | S2, S20, S22, S43, S45, S91, S92, S93, S103, S104 | draft |
| create.011 | card.text | Frequency: Daily<br>Starts today | S2, S43, S45, S91, S92, S93 | draft |
| create.012 | button.text | Save habit | S2, S10, S12, S14, S16, S18, S20, S21, S22, S43, S45, S91, S103, S104 | draft |
| create.013 | button.text | Cancel | S2, S20, S22, S43, S45, S91, S103, S104 | draft |
| create.014 | field.value | Read | S9, S10, S12, S14, S16, S18 | draft |
| create.015 | field.value | pages | S9, S10, S12, S14, S16, S18, S22, S103, S104, S105 | draft |
| create.016 | field.value | 20 | S9, S10, S12, S14, S16, S18, S22, S103, S104, S105 | draft |
| create.017 | list.items.text | Daily | S9 | draft |
| create.018 | list.items.text | Weekly | S9 | draft |
| create.019 | list.items.text | Fortnightly | S9 | draft |
| create.020 | list.items.text | Monthly | S9 | draft |
| create.021 | list.items.text | Custom weekdays | S9 | draft |
| create.022 | card.text | Daily<br>11 Oct → 12 Oct 00:00 | S10 | draft |
| create.023 | card.text | Weekly<br>Mon 12 Oct → Mon 19 Oct | S12 | draft |
| create.024 | card.text | Fortnightly<br>11 Oct → 25 Oct 00:00 | S14 | draft |
| create.025 | card.text | Monthly<br>1 Nov → 1 Dec 00:00 | S16 | draft |
| create.026 | card.text | Custom weekdays<br>Mon / Wed / Fri; first Mon 12 Oct | S18 | draft |
| create.027 | chips.items | Mon ✓ | S18 | draft |
| create.028 | chips.items | Wed ✓ | S18 | draft |
| create.029 | chips.items | Fri ✓ | S18 | draft |
| create.030 | field.value |  Meditation  | S20, S21 | draft |
| create.031 | field.value | -10 | S20, S21 | draft |
| create.032 | card.text | Frequency: Custom; no weekdays<br>Starts today | S20 | draft |
| create.033 | field.error | An active Meditation already exists | S21 | draft |
| create.034 | field.error | Enter a positive number without a sign | S21 | draft |
| create.035 | card.text | Custom weekdays<br>Select at least one day | S21 | draft |
| create.036 | field.value | Reading | S22, S103, S104, S105 | draft |
| create.037 | card.text | Frequency: Custom: Mon / Wed / Fri<br>Starts today | S22 | draft |
| create.038 | dialog.text | Your unsaved changes will be lost. | S44 | draft |
| create.039 | dialog.title | Discard changes? | S44 | draft |
| create.040 | dialog.actions.text | Keep editing | S44 | draft |
| create.041 | dialog.actions.text | Discard | S44 | draft |
| create.042 | loading.text | Saving… | S92 | draft |
| create.043 | error.text | Your input is retained. Nothing was saved. | S93 | draft |
| create.044 | error.title | Could not save habit | S93 | draft |
| create.045 | error.action.text | Retry | S93 | draft |
| create.046 | card.text | Frequency: Weekly<br>Starts today | S103, S104 | draft |
| create.047 | field.error | An active Reading already exists | S105 | draft |
| create.048 | text.text | Weekly · next start 12 Oct | S105 | draft |
| create.049 | button.text | Back | S105 | draft |

### Record progress

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| progress.001 | bar.text | Record progress | S4, S25, S27, S28, S51, S53, S55, S56, S57, S60, S61, S99, S100, S101, S113 | draft |
| progress.002 | text.text | Meditation · 11 Oct · ends 12 Oct 00:00 | S4, S25, S28, S51, S53, S55, S99, S100, S101, S113 | draft |
| progress.003 | text.text | Target: 10 minutes | S4, S25, S28, S51, S53, S55, S57, S99, S100, S101, S113 | draft |
| progress.004 | field.label | Period total | S4, S25, S27, S28, S51, S53, S55, S57, S60, S99, S100, S101, S113 | draft |
| progress.005 | field.value | 12 | S4, S55, S56, S57, S99, S100, S101, S113 | draft |
| progress.006 | field.helper | Replaces total; does not add to it | S4, S25, S28, S51, S53, S55, S57, S99, S100, S101, S113 | draft |
| progress.007 | button.text | Save total | S4, S25, S27, S28, S51, S53, S55, S57, S60, S99, S113 | draft |
| progress.008 | button.text | Close | S4, S25, S28, S51, S53, S55, S56, S57, S61, S99, S113 | draft |
| progress.009 | field.value | 8 | S25, S51, S53 | draft |
| progress.010 | text.text | Meditation · Current period<br>Target: 10 minutes | S27 | draft |
| progress.011 | field.value | 1e3 | S27 | draft |
| progress.012 | field.error | Use a nonnegative decimal | S27 | draft |
| progress.013 | field.value | 0 | S28 | draft |
| progress.014 | dialog.text | Your unsaved changes will be lost. | S52 | draft |
| progress.015 | dialog.title | Discard changes? | S52 | draft |
| progress.016 | dialog.actions.text | Keep editing | S52 | draft |
| progress.017 | dialog.actions.text | Discard | S52 | draft |
| progress.018 | banner.text | That period has ended | S56 | draft |
| progress.019 | card.text | 11 Oct · final total 8 / 10<br>Missed · −1 point<br>Final result cannot change | S56 | draft |
| progress.020 | field.label | Unsaved total | S56, S61 | draft |
| progress.021 | field.helper | Still bound to 11 Oct | S56 | draft |
| progress.022 | button.text | Record for current period | S56 | draft |
| progress.023 | text.text | Meditation · 12 Oct · ends 13 Oct 00:00 | S57 | draft |
| progress.024 | text.text | Stretch · Fri 9 Oct | S60 | draft |
| progress.025 | field.value | 5 | S60, S61 | draft |
| progress.026 | banner.text | Friday’s period has ended | S61 | draft |
| progress.027 | card.text | 9 Oct · 0 / 5 minutes<br>Missed · final | S61 | draft |
| progress.028 | field.helper | Retained; Friday cannot be changed | S61 | draft |
| progress.029 | text.text | No occurrence today<br>Next start: Mon 12 Oct | S61 | draft |
| progress.030 | loading.text | Saving… | S100 | draft |
| progress.031 | error.text | Your input is retained. Nothing was saved. | S101 | draft |
| progress.032 | error.title | Could not save total | S101 | draft |
| progress.033 | error.action.text | Retry | S101 | draft |

### Edit habit

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| edit.001 | bar.text | Edit habit | S35, S38, S40, S47, S49, S95, S96, S97, S120 | draft |
| edit.002 | field.label | Name | S35, S38, S40, S47, S49, S95, S96, S97, S120 | draft |
| edit.003 | field.value | Morning meditation | S35, S47, S49, S95, S96, S97 | draft |
| edit.004 | field.helper | Unique among active / pending | S35 | draft |
| edit.005 | field.label | Target | S35, S38, S40, S47, S49, S95, S96, S97, S120 | draft |
| edit.006 | field.value | 15 | S35, S47, S49, S95, S96, S97 | draft |
| edit.007 | card.text | Daily · minutes<br>Unit and frequency are fixed | S35 | draft |
| edit.008 | text.text | Applies to today’s open period | S35 | draft |
| edit.009 | button.text | Save changes | S35, S38, S40, S47, S49, S95, S120 | draft |
| edit.010 | button.text | Cancel | S35 | draft |
| edit.011 | field.value | Reading | S38, S40 | draft |
| edit.012 | field.value | 30 | S38 | draft |
| edit.013 | card.text | Weekly · pages (fixed)<br>Current period: 20 pages<br>Next period: 19–25 Oct | S38 | draft |
| edit.014 | field.value | 40 | S40 | draft |
| edit.015 | text.text | Effective next period: 19 Oct | S40 | draft |
| edit.016 | dialog.text | Your unsaved changes will be lost. | S48 | draft |
| edit.017 | dialog.title | Discard changes? | S48 | draft |
| edit.018 | dialog.actions.text | Keep editing | S48 | draft |
| edit.019 | dialog.actions.text | Discard | S48 | draft |
| edit.020 | field.helper | Applies to today’s open period | S96, S97 | draft |
| edit.021 | loading.text | Saving… | S96 | draft |
| edit.022 | error.text | Your input is retained. Nothing was saved. | S97 | draft |
| edit.023 | error.title | Could not save changes | S97 | draft |
| edit.024 | error.action.text | Retry | S97 | draft |
| edit.025 | field.value | Stretch | S120 | draft |
| edit.026 | field.value | 10 | S120 | draft |
| edit.027 | text.text | Custom · minutes (fixed)<br>Effective: Mon 12 Oct | S120 | draft |

### Habit review

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| review.001 | bar.text | Habit review | S7, S37, S42, S68, S71, S75, S82, S112 | draft |
| review.002 | text.text | Meditation · Daily · minutes | S7 | draft |
| review.003 | text.text | Active run 2 · Success streak 1 | S7 | draft |
| review.004 | card.text | 11 Oct · target 10 · total 12<br>Success · +10 points | S7 | draft |
| review.005 | button.text | Back to Overview | S7, S68, S71 | draft |
| review.006 | text.text | Morning meditation · Daily · minutes | S37 | draft |
| review.007 | empty.text | Today’s occurrence is still open. | S37 | draft |
| review.008 | empty.title | No finalized periods | S37, S112 | draft |
| review.009 | card.text | Target 10 → 15<br>Effective: 11 Oct<br>Saved today | S37 | draft |
| review.010 | text.text | Reading · Weekly · pages | S42 | draft |
| review.011 | card.text | Target changes<br>30 from 19 Oct · Superseded<br>40 from 19 Oct · Applies | S42 | draft |
| review.012 | text.text | Historical results keep original targets | S42 | draft |
| review.013 | text.text | Meditation · Daily<br>Active run 11 · Streak 0 | S68 | draft |
| review.014 | card.text | 9 Oct · 0 / 10 · Missed · −1<br>10 Oct · 8 / 10 · Missed · −1 | S68 | draft |
| review.015 | text.text | No unacknowledged misses | S68 | draft |
| review.016 | text.text | Meditation · Daily | S71, S75, S112 | draft |
| review.017 | card.text | 9 Oct · Missed · −1<br>10 Oct · Missed · −1 | S71 | draft |
| review.018 | text.text | Miss count and notification cleared | S71 | draft |
| review.019 | card.text | 11 Oct · 10 / 10<br>Success · +10 points | S75 | draft |
| review.020 | card.text | 21-Day Consistency<br>Earned: 12 Oct | S75 | draft |
| review.021 | text.text | 18 successes across 21 dates | S75 | draft |
| review.022 | text.text | Meditation · Retired · Daily | S82 | draft |
| review.023 | card.text | 10 Oct · 10 / 10 minutes<br>Success · +10 points | S82 | draft |
| review.024 | card.text | Target 10 → 15<br>Effective 11 Oct | S82 | draft |
| review.025 | card.text | 11 Oct · Cancelled<br>Frozen total: 0 / 15 · 0 points | S82 | draft |
| review.026 | text.text | No progress, edit or reactivate | S82 | draft |
| review.027 | button.text | Back to retired list | S82 | draft |
| review.028 | empty.text | Results appear after a period ends. | S112 | draft |
| review.029 | text.text | Today: 0 / 10 minutes | S112 | draft |
| review.030 | button.text | Record progress | S112 | draft |

### Retire habit

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| retirement.001 | dialog.text | Tracking stops. You have 10 seconds to undo; history and points remain. | S77 | draft |
| retirement.002 | dialog.title | Retire Meditation? | S77 | draft |
| retirement.003 | dialog.actions.text | Cancel | S77 | draft |
| retirement.004 | dialog.actions.text | Retire | S77 | draft |

### Retired habits

| Copy reference | Role | Text / example value | Frames | Status |
|---|---|---|---|---|
| retired.001 | bar.text | Retired habits | S81, S83, S87, S117 | draft |
| retired.002 | list.items.text | Meditation | S81 | draft |
| retired.003 | card.text | Meditation · Retired | S83 | draft |
| retired.004 | card.text | Meditation · Retired<br>History preserved | S87 | draft |
| retired.005 | empty.text | Retired habits will remain here for review. | S117 | draft |
| retired.006 | empty.title | No retired habits | S117 | draft |
| retired.007 | button.text | Back to Overview | S117 | draft |

## 9. Decisions

### Assumptions to confirm before build

| ID | Assumption | Review needed |
|---|---|---|
| A3/A4 | Portrait phone sketch; no separate tablet/foldable journeys | Installed Android conventions; prototype default |
| C1/C2 | Draft English copy and realistic habit examples | No approved copy/localization supplied |
| V1–V4 | Plain hand-drawn frames, clear hierarchy, scrollable content; optional later light/dark design | No supplied visual design; skill sketch contract |

### All decisions

Source labels: document = creator-approved documented behavior; assumption-approved = creator-approved inference; user = explicit creator choice; assumption = agent default still flagged for review. IDs are clarification question IDs, not screen-state numbers. The authoritative log is [DECISIONS.md](notes/DECISIONS.md).

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

## 10. Open questions

**None blocking the locked flow.** Gates A, B and C passed. No unresolved source conflict remains.

Before native implementation, review the three recorded assumptions (portrait focus, draft English copy/examples, final visual direction) with the project creator/designer. They are flagged assumptions, not new blocking questions or an unapproved optional design phase. Detailed theme/copy decisions can be made in the implementation/design task.

## 11. What was checked

| Check | Result |
|---|---|
| Flow locked (gate C) | Project creator explicitly approved on 2026-10-11; separate journeys retained by request |
| sketch.mjs --style both --check | Passed: 26 journeys / 121 frames / 95 transitions; no crossing/shared arrows, screen overlaps or obstructed labels |
| handoff.mjs --check | Passed: screen identities, states, transitions and page inventory agree across flow.json, flow.drawio and HANDOFF.md |
| XML and page links | 147 pages parsed; all 285 internal page links resolve |
| Draw.io Desktop inspection | Daily form inspected; Save link followed from S2 to S3 successfully |
| Coverage review | Reviewed against approved decisions and skill sketch-review checklist |
| Optional design / theme contrast | Not performed; creator requested direct handoff |
| Native Android tests | Not run; document/prototype phase only |

**Not checked:** executable behavior, real input validation, persistence, notifications and actual OS prompt timing, period boundaries, retirement races, process death, real device accessibility, theme contrast or performance. Nothing here was run as a native app. Diagram checks validate the representation and links; they do not establish native runtime correctness.

## 12. Visual references
The sketch is [`flow.drawio`](flow.drawio). Open it in VS Code with the *Draw.io Integration*
extension (`hediet.vscode-drawio`), in draw.io Desktop (https://www.drawio.com), or at
https://app.diagrams.net (File › Open from › Device). Don't count on a draw.io plugin in Android
Studio. The pages are the tabs along the bottom of the window:

<!-- generated:pages (scripts/handoff.mjs from flow.json; don't edit inside) -->
| Journey | Pages in `flow.drawio` | Screens |
|---|---|---|
| Create, record and review | "Create, record and review" (wireflow); 8 screen pages, "S1 Overview — No active habits" … "S8 Overview — Returned from review" | S1–S8 |
| Choose a schedule and first period | "Choose a schedule and first period" (wireflow); 11 screen pages, "S9 Create habit — Choose frequency" … "S19 Overview — Waiting for selected weekday" | S9–S19 |
| Correct invalid habit inputs | "Correct invalid habit inputs" (wireflow); 4 screen pages, "S20 Create habit — Invalid draft" … "S23 Overview — Valid Custom habit saved" | S20–S23 |
| Correct the current total | "Correct the current total" (wireflow); 6 screen pages, "S24 Overview — Existing total" … "S29 Overview — Zero total saved" | S24–S29 |
| Understand when tracking is due | "Understand when tracking is due" (wireflow); 4 screen pages, "S30 Overview — Waiting for first period" … "S33 Overview — Between periods" | S30–S33 |
| Edit name and a current target | "Edit name and a current target" (wireflow); 4 screen pages, "S34 Overview — Daily habit selected" … "S37 Habit review — Target change history" | S34–S37 |
| Schedule targets for a later period | "Schedule targets for a later period" (wireflow); 5 screen pages, "S38 Edit habit — Weekly target change" … "S42 Habit review — Superseded target retained" | S38–S42 |
| Leave create habit with changes | "Leave create habit with changes" (wireflow); 4 screen pages, "S43 Create habit — Unsaved changes" … "S46 Overview — Draft discarded" | S43–S46 |
| Leave edit habit with changes | "Leave edit habit with changes" (wireflow); 4 screen pages, "S47 Edit habit — Unsaved changes" … "S50 Overview — Draft discarded" | S47–S50 |
| Leave record progress with changes | "Leave record progress with changes" (wireflow); 4 screen pages, "S51 Record progress — Unsaved changes" … "S54 Overview — Draft discarded" | S51–S54 |
| Handle a period ending before Save | "Handle a period ending before Save" (wireflow); 5 screen pages, "S55 Record progress — Previous period draft" … "S59 Overview — Closed without new entry" | S55–S59 |
| Close a late draft when nothing is due | "Close a late draft when nothing is due" (wireflow); 3 screen pages, "S60 Record progress — Selected-day draft" … "S62 Overview — Next selected day shown" | S60–S62 |
| Allow or decline notifications | "Allow or decline notifications" (wireflow); 4 screen pages, "S63 Overview — First habit committed" … "S66 Overview — Notifications unavailable" | S63–S66 |
| Review and acknowledge missed periods | "Review and acknowledge missed periods" (wireflow); 6 screen pages, "S67 Overview — Misses awaiting review" … "S72 Overview — Returned from notification review" | S67–S72 |
| See a success and consistency badge | "See a success and consistency badge" (wireflow); 3 screen pages, "S73 Overview — Daily consistency nearly met" … "S75 Habit review — Badge in history" | S73–S75 |
| Retire, undo or keep history | "Retire, undo or keep history" (wireflow); 9 screen pages, "S76 Overview — Active habit" … "S84 Overview — Retirement cancelled" | S76–S84 |
| Recover an expired Undo action | "Recover an expired Undo action" (wireflow); 3 screen pages, "S85 Overview — Pending row at deadline" … "S87 Retired habits — Expired habit retained" | S85–S87 |
| Recover a local read failure | "Recover a local read failure" (wireflow); 3 screen pages, "S88 Overview — Loading local data" … "S90 Overview — Read recovered" | S88–S90 |
| Retry saving habit | "Retry saving habit" (wireflow); 4 screen pages, "S91 Create habit — Ready to save" … "S94 Overview — Retry committed" | S91–S94 |
| Retry saving changes | "Retry saving changes" (wireflow); 4 screen pages, "S95 Edit habit — Ready to save" … "S98 Overview — Retry committed" | S95–S98 |
| Retry saving total | "Retry saving total" (wireflow); 4 screen pages, "S99 Record progress — Ready to save" … "S102 Overview — Retry committed" | S99–S102 |
| Resume drafts without replaying actions | "Resume drafts without replaying actions" (wireflow); 5 screen pages, "S103 Create habit — Draft before process death" … "S107 Overview — Reopened after task removal" | S103–S107 |
| Resume multiple pending retirements | "Resume multiple pending retirements" (wireflow); 4 screen pages, "S108 Overview — Two pending rows" … "S111 Overview — Other retirement settled" | S108–S111 |
| Record from review and return to Overview | "Record from review and return to Overview" (wireflow); 4 screen pages, "S112 Habit review — No finalized results yet" … "S115 Overview — Closed without changes" | S112–S115 |
| Find retired history when the list is empty | "Find retired history when the list is empty" (wireflow); 3 screen pages, "S116 Overview — No active habits" … "S118 Overview — Returned to empty Overview" | S116–S118 |
| Change a target before the next occurrence | "Change a target before the next occurrence" (wireflow); 3 screen pages, "S119 Overview — No occurrence today" … "S121 Overview — Next occurrence target saved" | S119–S121 |
<!-- /generated:pages -->

Final look: not done. This handoff contains the locked rough flow only.

## 13. Out of scope for the prototype

- iOS, tablet/foldable-specific journeys, landscape-specific layouts and final visual styling.
- Accounts, backend/network services, sync, export, backup/device transfer, multi-user behavior, extra badges, quiet-hours controls, reactivation or permanent-delete flows.
- Live editable/clickable app behavior: draw.io links navigate example screens only; the fields are fixed data. Time-triggered transitions are reviewed using wireflows/page tabs.
- Exhaustive permutations of every validation error, Android OS version or repository failure. Representative states express the approved rules; the supplied PRD/LLD define the complete input and lifecycle contracts.
- A single giant consolidated flow page. The creator chose separate journey pages, with shared canonical names and S-numbers.

Native implementation is a separate task. This handoff specifies approved experience and points to the existing PRD, architecture and LLD; it introduces no architecture, libraries or app code.

## Sources
The S-numbers, screen names and states follow the skill's flow spec; decision IDs are question
IDs (`notes/CLARIFICATIONS.md` explains them). The conventions are the skill's; the content is
this project's.

| Convention | Defined in |
|---|---|
| Frames, S-numbers, screens, states and entry conditions | [Flow spec](../../.agents/skills/mobile-prototype/references/sketch-spec.md) (`references/sketch-spec.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/sketch-spec.md) |
| What each section of this handoff contains | [Handoff package](../../.agents/skills/mobile-prototype/references/handoff-package.md) (`references/handoff-package.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/handoff-package.md) |
| Question IDs used as decision IDs | [Question bank](../../.agents/skills/mobile-prototype/references/question-bank.md) (`references/question-bank.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/question-bank.md) |
| Opening and editing `flow.drawio` | [draw.io setup](../../.agents/skills/mobile-prototype/references/drawio-setup.md) (`references/drawio-setup.md`, installed skill, in this project) · [online copy](https://github.com/AnilDeshpande/codetutor-mobile-prototyper/blob/main/skills/mobile-prototype/references/drawio-setup.md) |
