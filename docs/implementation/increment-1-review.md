# Increment 1 review and creator decisions

**Reviewed head:** `a5d6afd1b2c3432ff0c5c6deccef2bc02c462963` on [PR #3](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3).

**Verdict:** Request changes. **Merge-blocking: Yes.** The creator will close PR #3 when ready. GitHub recorded Comment reviews because the review was submitted from the PR author's account. The first review has seven inline findings. The follow-up adds the targeted test gaps, privacy evidence, and confirmed decisions. [Initial review](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#pullrequestreview-6005123411) · [Follow-up review](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#pullrequestreview-6005279190)

## Creator approvals on 2026-11-21

The creator answered “Go with recommendation” to each of the eight numbered choices in the Codex review. Product choices lock user-visible behavior. Engineering choices lock the implementation or build contract inside which that behavior is delivered. Approval of a choice does not resolve a code finding or supply verification evidence.

| # | Kind | Approved decision and rationale | Authority |
| --- | --- | --- | --- |
| 1 | Engineering | Plan the first applicable occurrence within `CreateHabit`'s gated transaction. Overview sees the saved habit and any started occurrence together. Rearm tracking on commit. | [Architecture commands and invariants](../architecture/habit-tracker-v1-architecture.md#domain-commands-and-invariants) |
| 2 | Product | Trim surrounding whitespace and allow digits on either side of the locale decimal separator, including `.5` and `5.` in dot-decimal locales. Count 9 integer digits after dropping leading zeros and 3 fractional digits after dropping trailing zeros. | [PRD domain rules](../prds/habit-tracker-v1-prd.md) |
| 3 | Product | Keep the floating Create habit action on populated Overview and give it an accessible name. | [PRD FR-012](../prds/habit-tracker-v1-prd.md) and [design notes](../prototypes/notes/DESIGN.md#increment-1-review-decisions) |
| 4 | Product | Non-Daily cards show the period date range and the explicit end date and time. Keep both so the user can identify the period tied to the progress total. | [PRD FR-006](../prds/habit-tracker-v1-prd.md) and [design notes](../prototypes/notes/DESIGN.md#increment-1-review-decisions) |
| 5 | Product | Keep Retry on a failed create. Preserve the draft and submit again on a deliberate tap. | [PRD FR-012](../prds/habit-tracker-v1-prd.md) and [S93 handoff](../prototypes/HANDOFF.md#create-habit) |
| 6 | Engineering | Retain the real Room migration 1→2 so existing v1 data survives. | [Increment plan](increment-1-plan.md#decisions) |
| 7 | Engineering | Retain the `kotlinx-serialization-core:1.9.0` constraint for Room's migration test. Dependency insight confirmed selection of 1.9.0 and all three device tiers passed. The reported failure without the constraint was not reproduced in review. | [Increment record](increment-1-record.md#decisions-taken-while-building) |
| 8 | Product design | Keep dynamic colour off so the approved palette stays consistent across devices. | [Design notes](../prototypes/notes/DESIGN.md#increment-1-review-decisions) |

The eight responses do not decide additional copy choices about badge eligibility or Custom status labels. The existing approved requirements remain the baseline for those cases.

## Findings and merge disposition

| Finding | Merge-blocking | Evidence or next proof |
| --- | --- | --- |
| First create leaves an idle ticker unarmed | Yes | The real Room probe advanced past the first boundary and found zero rows finalized, with one expected. The existing ticker test calls the catch-up pipeline directly and bypasses create. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412114) |
| Existing Overview subscriber stays stale when moving the clock backward changes only the gate | Yes | The real Room probe saw `BetweenPeriods` in a new subscription while the original subscriber emitted nothing. Monitor the same subscriber before and after catch-up. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412120) |
| API 33+ permission preference failure can suppress success after a committed create | Yes | The source awaits DataStore before sending the saved result. Inject a preference I/O failure after commit and assert that the saved habit and return to Overview are preserved. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412124) |
| Floating Create habit control lacks an accessible name in the platform hierarchy | Yes | Observed on API 24 and 34. Check the action's name in the merged accessibility tree after a habit exists. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412129) |
| Gate-before-snapshot SQL trace and post-migration trigger-definition comparison are missing | Yes | Version, rollback, contention, and trigger-name evidence passed. The [LLD](../design/habit-tracker-v1-low-level-design.md) also requires SQL ordering and canonical definitions. |
| Save snackbar disappears on the ordinary no-dialog path | No | Reproduced on API 24 and the second API 34 save. Cover both permission and no-permission paths. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412135) |
| Overview remembers an old timezone and ignores the 12/24-hour preference | No | Source finding. Check a live timezone change and the time format setting. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412137) |
| Cadence preview omits the first period end and non-Daily cards omit the approved range | Yes where the affected UI is in scope | Compare the form to S12, S14, and S16, and the card to S31 and approval 4. [Inline finding](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3#discussion_r4652412140) |

## Mutation evidence and missed coverage

At the reviewed head, 146 host tests and 42 device tests per tier on APIs 24, 34, and 36 passed. The follow-up reran 19 targeted host tests in scratch before mutation and after restoration, with all 19 passing both times. Removing the planner's previous-period-end bound failed exactly 2 of 18 planner tests. Removing the finalized-result NULL guard failed the shared `occurrenceGuards` host scenario. Neither mutation exercised the app's tracking refresh path.

| Required regression | Baseline evidence | Mutation to run after the correction |
| --- | --- | --- |
| Real create from an already-open empty Overview arms the next boundary | Reproduced against the unchanged PR code. The existing suite stayed green. | Disable the rearm signal from the committed create while leaving the transaction intact. The named regression must fail because the first elapsed occurrence remains unfinalized. Restore the signal and rerun. |
| An already-active subscriber refreshes when a clock change leaves the tracked tables unchanged | Reproduced against the unchanged PR code. The existing suite stayed green. | Disable the time-driven refresh while retaining Room invalidation for data writes. The same subscriber must fail to reach `BetweenPeriods` after the clock change. Restore refresh and rerun. |

The two failing probes demonstrate coverage gaps in the reviewed head. The corrections and the corresponding pass–fail–pass mutation runs belong to the future implementation PRs. The LLD retains the requirement for an instrumented real-Overview test across a boundary.

## Security and privacy evidence

The authority is the [PRD's original-device-only requirement](../prds/habit-tracker-v1-prd.md#assumptions) and the [architecture's platform integration contract](../architecture/habit-tracker-v1-architecture.md#platform-integrations-privacy-and-external-effects). I generated the merged debug and release manifests from an isolated copy of the reviewed head with `./gradlew --offline :app:processReleaseMainManifest :app:processDebugMainManifest`.

| Check | Result |
| --- | --- |
| Exported components | **Static pass.** The release manifest exports the launcher activity and three library components. Job scheduling is protected by `BIND_JOB_SERVICE`. WorkManager diagnostics and ProfileInstaller are protected by `DUMP`. Both merged providers are private. The Compose tooling and test activity exports appear in debug only. The app's time-change receiver is dynamically registered with `RECEIVER_NOT_EXPORTED`. |
| Room and application files | **Static pass.** Both merged manifests have `allowBackup=false` and point to `data_extraction_rules.xml`. Cloud backup and device transfer both exclude `database`, `sharedpref`, `file`, `external`, and `root` at `.`. The database exclusion covers `habit_tracker.db` and its WAL and SHM companions. The file exclusion covers the DataStore preference. On API 24–30 the architecture relies on `allowBackup=false`. On API 31+ the transfer rules are checked independently. |
| Backup eligibility on API 34 | **Local transport rejected the package.** The disposable emulator contained a synthetic saved habit, `habit_tracker.db`, its WAL and SHM files, and `files/datastore/settings.preferences_pb`. The per-package result from `bmgr backupnow` was `Backup is not allowed` with the on-device LocalTransport selected. The overall command reported success. This exercises eligibility on the emulator and did not upload user data. |
| Backup eligibility on API 24 | **NOT RUN.** The app created its Room files on the disposable API 24 emulator. Backup Manager reported `No transports available`, so there was no package backup attempt on this tier. |
| Device-to-device restore | **BLOCKED.** The disposable API 34 emulator selected the D2D test transport. Both backup attempts rejected the package at preflight. The monitored retry reported `BACKUP_MANAGER_POLICY / ERROR_PREFLIGHT`. A completed device-to-device restore remains unverified under FR-011. |

In-memory mutations of the generated release manifest and rule XML confirmed that the static check caught `allowBackup=true`, an exported provider, and removal of the database exclusion from device transfer. The original configuration passed before and after those mutations.

Android's manifest merger includes entries from dependencies. On Android 12 and later, some device manufacturers handle `allowBackup=false` differently for device-to-device migration. [Merged manifest guidance](https://developer.android.com/build/manage-manifests) · [Android Auto Backup guidance](https://developer.android.com/identity/data/autobackup) · [Backup and transfer testing](https://developer.android.com/identity/data/testingbackup)

## Resolution of the review findings (local, not pushed)

Fixes and regression tests were made on top of `a5d6afd`. Nothing is committed or pushed. Results below come from the
runs recorded in the hand-back message for this work.

| Finding | Fix | Regression evidence |
| --- | --- | --- |
| First create leaves an idle ticker unarmed | `HabitRepository.boundaryChanges` emits after a committed create; `BoundaryTicker` rearms on it and retries a failed `nextBoundary()` after a backoff instead of treating it as an empty schedule. | `TrackingScenarios.firstCreateFromAnEmptyOverviewRearmsTheTicker` (host and devices); `BoundaryTickerTest` create-signal and lookup-failure cases. |
| Active subscriber stays stale after a clock change | `observeOverview` also watches `reconciliation_lock`, so a catch-up that changes only the gate re-reads time-derived status. | `TrackingScenarios.aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges`. |
| Permission preference failure after a commit | `HabitFormViewModel` falls back to "do not ask" and still emits `Saved`. | `HabitFormViewModelTest` read-failure and write-failure cases. |
| Create habit action unnamed | The floating action carries its name on the clickable node. | `OverviewScreenTest.theRepeatCreateActionIsNamedOnTheClickableNodeInTheMergedTree`. |
| Save snackbar disappears | `HabitSavedFeedback` shows the message from the composition scope, after the dialog on the prompt path. | `HabitSavedFeedbackTest`. |
| Old timezone and 12/24-hour setting | `TimeDisplay` and `rememberTimeDisplay()` re-read zone and clock style on time, date and timezone broadcasts. | `TimeDisplayTest`, `OverviewScreenTest` live zone change, plus a manual API 34 zone and 12/24-hour change. |
| Preview and card omit the period end | Weekly, Fortnightly and Monthly previews show start and end. Non-Daily cards show the stored date range and the end date and time. | `HabitFormViewModelTest`, `HabitFormScreenTest`, `OverviewScreenTest`. |
| Gate-before-snapshot trace and trigger definitions | SQL trace with a recording clock, including a waiting contender; canonical trigger definitions compared after migration 1→2 and the guard scenarios run on migrated databases. | `GateOrderScenarios`, `MigrationDeviceTest`, `GuardScenarios.everyGuardHasItsCanonicalDefinition`. |

Remaining unverified item: a completed device-to-device restore under FR-011 is still blocked by the available
transports (see Security and privacy evidence).

## Learning for future pull requests

The creator chose to keep PR #3 intact for now and close it later. The 115-file diff made review difficult. The creator reconsidered the proposal to split PRs by implementation layer and will choose a different approach later. PR size remains a lesson from the review.

The PR's separate commits for domain rules, database and repository, coordination, and UI were useful reading checkpoints. A handoff can point reviewers to the changed contracts and evidence for each commit.

The reviewer reads the approved PRD, architecture, LLD, ADRs, plan, and design before using the PR body to locate claims. Review the production entry points, run focused mutations against the consequential rules, and compare merged Android manifests and actual data storage with the privacy contract. State whether merge is blocked and name the reason. Ask the creator to decide product behavior when the approved sources are silent or conflict. Label engineering questions and routine implementation choices separately. Preserve accepted answers and their document impact here or in the owning increment's decision record so a closed PR does not erase the rationale.
