# Habit Tracker V1 — Ideation Session Notes

## Session record

| Field | Value |
| --- | --- |
| Date | 2026-08-15 |
| Session type | Creator-led decision closure using the repository product-ideation method and Ideator Agent contract |
| Session source | Creator conversation continuing the 2026-08-12 Habit Tracker ideation session |
| Creator | Project creator |
| Artifact revised | `docs/idea-briefs/habit-tracker-v1-idea-brief.md` |
| Outcome | Product ideation closed; no material product question remains unclassified |

## Purpose clarified by the creator

The application is primarily intended to demonstrate AI-agent-led Android application development rather than become a commercial product. The creator asked to close material ideation decisions with reasonable, explicit rules so the project can proceed to product definition. This changed the choice of success evidence, but it did not authorize the agent to change the creator's product boundary.

## Boundary conflict and resolution

The prior brief excluded backend services, APIs, and cloud sync, while casting requirement 5 asked for a bounded write-then-sync path covering local write, network, staleness, conflict, and recovery.

The creator explicitly reaffirmed that the app stores data only on the device and has no REST APIs or backend of any kind. The local-only decision therefore remains in force. The immediate consequence is that casting requirement 5 fails; the session did not add a fictitious synchronization feature to force a pass.

## Creator-approved decisions

| # | Decision | Creator rationale | Immediate consequence | Source reference |
| --- | --- | --- | --- | --- |
| 1 | Keep the app strictly on-device, with no REST API, backend, cloud sync, or other network synchronization. | This was the intended product boundary in the original ideation and remains unchanged. | The product remains private and local-only; casting requirement 5 fails. | Creator reply, 2026-08-15, boundary round |
| 2 | Treat the project primarily as an AI-agent Android-development demonstration and close decisions reasonably so the next stage can begin. | The goal is to demonstrate development rather than optimize a real commercial product. | V1 success uses an observable reviewer journey rather than adoption or retention metrics. | Creator reply, 2026-08-15, boundary clarification |
| 3 | Award the 21-Day Consistency badge once per Daily habit after at least 18 successful days in any 21 consecutive calendar days; an unqualified day counts as a miss. | The creator approved the recommended balance between consistency and limited imperfection. | The milestone has a deterministic qualification rule and is distinct from Active run. | Creator approval, 2026-08-15, milestone round |
| 4 | On each finalized missed habit period, deduct 1% of the current app-wide point balance, rounded up, with a one-point minimum when positive, a zero floor, and no duplicate deduction. | The creator approved a small but unambiguous consequence. | Missed-target scoring now has deterministic rate, rounding, minimum, and idempotency rules. | Creator approval, 2026-08-15, milestone round |
| 5 | Use the observable on-device core journey, including persistence across restart, as the V1 success signal. | Demonstrable behaviour fits the project's teaching purpose better than commercial metrics. | Product definition has concrete success evidence without an invented retention goal. | Creator approval, 2026-08-15, milestone round |
| 6 | Let the user define each habit's unit and select Daily, Weekly, Fortnightly, Monthly, or Custom selected weekdays. | Water, steps, and other habits naturally use different units and schedules. | The prior Daily-only decision is replaced and retained as superseded evidence in the Idea Brief log. | Creator reply, 2026-08-15, frequency round |
| 7 | Interpret Daily as a calendar day, Weekly as Monday–Sunday, Fortnightly as creation-anchored 14-day periods, Monthly as a calendar month, and each Custom weekday as a separate occurrence. Assess the result once per tracking period and limit the 21-Day badge to Daily habits. | The creator approved a coherent rule package across different schedules. | Targets, successes, misses, and deductions all have an applicable period. | Creator approval, 2026-08-15, frequency-closure round |
| 8 | Apply Daily and Custom target changes immediately before occurrence finalization; start Weekly, Fortnightly, and Monthly target changes with the next period; never recalculate completed history. | The creator approved the recommendation that avoids partial-period target ambiguity. | The earlier blanket current-day rule is replaced by cadence-specific behaviour. | Creator approval, 2026-08-15, frequency-closure round |
| 9 | Use Active run for continued habit activity and Success streak for consecutive successful periods. Misses do not reset Active run; misses reset Success streak. | The terms should not imply the same kind of continuity. | The two visible continuity measures cannot be treated as synonyms. | Creator approval, 2026-08-15, frequency-closure round |
| 10 | Award 10 points for a successful finalized habit period, use one app-wide balance, apply each reward or penalty only once, and include only the 21-Day Consistency badge in V1. | The creator approved a compact deterministic reward model for the demonstration. | The positive points formula and V1 badge catalog are closed. | Creator approval, 2026-08-15, final-closure round |
| 11 | Start Active run at habit creation, keep it through misses and absent records, preserve it after undo, and end it on final deletion. | Active run measures continued habit activity, not successful performance. | The Active-run lifecycle is closed. | Creator approval, 2026-08-15, final-closure round |
| 12 | For the separate casting gate, require the eventual on-device persistence approach to include a build-time code-generation step; defer the exact library and architecture. | This adds authentic Android build-time/generated-code material without introducing a backend or choosing architecture during ideation. | Casting requirement 3 passes, while the persistence library remains a later technical selection. | Creator approval, 2026-08-15, casting-clarification round |

## Changed prior decisions

| Prior decision | Replacement | Reason for replacement | Consequence |
| --- | --- | --- | --- |
| Every habit uses a Daily target. | A habit uses a user-selected Daily, Weekly, Fortnightly, Monthly, or Custom-weekday frequency and a user-defined unit. | The creator added examples and explicitly expanded supported schedules. | Domain rules now operate on an applicable tracking period. |
| Every target change applies from the current day forward. | Daily and Custom changes may apply immediately before finalization; longer cumulative frequencies change at the next period. | The expanded frequencies made a mid-period cumulative target ambiguous. | Completed history stays fixed and longer periods avoid partial-period threshold changes. |
| “Active streak” survives a miss, with its distinction unresolved. | User-facing Active run measures continued activity; Success streak measures uninterrupted target attainment. | The creator approved clearer terminology and reset rules. | The two values have separate, testable meanings. |
| Points and badges were included without a complete earning formula or catalog. | Success earns 10 points; misses apply the approved deduction; V1 contains only the 21-Day Consistency badge. | The creator wanted all ideation-level open ends closed. | Reward behaviour and V1 badge scope are deterministic. |

## Final decision-state audit

### Resolved

- Product purpose, target user, problem, promise, core loop, V1 boundary, and success signal.
- User-defined units and all supported frequencies.
- Tracking-period meanings and missed-period evaluation.
- Same-day and next-period target-change behaviour.
- Historical integrity.
- Positive point earnings, deduction rate, rounding, minimum, floor, and duplicate prevention.
- Active run, Success streak, and their reset/lifecycle rules.
- The 21-Day Consistency badge qualification and eligibility.
- Recoverable deletion as a product outcome.
- Strictly on-device scope.

### Deliberately deferred

| Deferred item | Why it is not meaningful to settle in ideation | Reopening event and owner |
| --- | --- | --- |
| Unit formatting, numeric validation, limits, and editing feedback | These are detailed input contracts; the user-defined-unit product decision is already resolved. | Product-definition owner reopens when specifying habit creation and editing. |
| Timezone, daylight-saving, and exact tracking-period finalization | These require detailed calendar and platform rules, not a change to product intent. | Product-definition and technical owners reopen when specifying time evaluation. |
| Dashboard layout, exact charts, partial-progress display, score display, and historical presentation | The required observable outcomes are decided; their presentation needs interaction design. | Interaction/product-definition owner reopens when defining the review experience. |
| Notification timing and wording | The missed-target notification outcome is decided; delivery details depend on the interaction contract. | Product-definition owner reopens when specifying notifications. |
| Delete confirmation, undo duration, and final-delete interaction | Recoverability and lifecycle effects are decided; timing and presentation are interaction details. | Product-definition owner reopens when specifying safe deletion. |
| PIN protection and automatic data cleanup | No creator-approved threat model or retention need makes either choice meaningful now. | Product-definition owner may reopen only if a concrete privacy or retention requirement emerges. |
| Persistence library, database/schema, generated-code tooling, navigation, architecture, and implementation | These belong after product definition. The casting constraint requires code generation but does not select a library. | Technical design owner reopens after the product specification is approved. |

### Out of scope

- Backend services, REST APIs, network or cloud sync, accounts, Gmail login, multi-user/social/collaborative features, and non-Android clients.
- V1 export and export formats, and additional badges.
- PRD, architecture, data model, navigation plan, implementation phases, Android code, and tests during ideation.

### Remaining unclassified product questions

None.

## Final casting scorecard

The casting gate is assessed only from creator-approved decisions and recorded evidence. It is separate from the product Idea Brief.

| Requirement | Result | Exact evidence | Exact follow-up |
| --- | --- | --- | --- |
| 1. At least five or six contested domain terms with meaningful definitions | **Pass** | The revised Idea Brief defines Habit, Unit, Tracking period, Progress, Target attainment, Target change, Historical progress, Missed target, Points, Active run, Success streak, the 21-Day Consistency badge, and Delete recovery, including material edges and exceptions. | None required. Preserve these definitions in product definition. |
| 2. Non-trivial pure-Kotlin logic with real edge cases and no Android dependency | **Pass** | Creator-approved rules include five cadence calculations, creation-anchored fortnightly periods, cadence-specific target-change effective dates, an 18-of-21 rolling qualification, app-wide percentage deductions with rounding/minimum/floor/idempotency, and distinct Active-run and Success-streak transitions. These rules are domain calculations that can be expressed without Android dependencies. | None required. The later specification should preserve these rules as platform-independent behaviour. |
| 3. Local persistence with a code-generation step | **Pass** | The product requires data to remain intact after app restart, and the creator separately approved the casting constraint that the eventual on-device persistence approach include a build-time code-generation step. The exact library and architecture remain deliberately deferred. | At technical design, select and verify an on-device persistence toolchain that actually performs build-time code generation; do not treat a non-generating persistence approach as satisfying this pass. |
| 4. An observable visual state that can visibly render wrong | **Pass** | The approved success journey and V1 boundary require visible target attainment, missed-target consequences, points, the consistency badge, Active run, Success streak, progress history, and preserved target-change history. Incorrect values, periods, or history would be visibly wrong to the reviewer. | None required. Product definition should make these states observable without changing their rules. |
| 5. A write-then-sync path: local write, network, staleness, conflict, recovery | **Fail** | The creator explicitly reaffirmed that all data is on-device only and that V1 has no REST APIs, backend, cloud sync, or other network synchronization. Therefore network staleness, remote conflicts, and synchronization recovery are not truthful product behaviours. | Keep this failure for Habit Tracker V1. Reopen only if the creator later changes the product boundary; otherwise use another series project to demonstrate write-then-sync. |

**Casting result: 4 Pass, 1 Fail. If the series gate requires all five requirements, the overall casting gate result is Fail because requirement 5 is intentionally unsupported.**

## Handoff

The revised Idea Brief is ready for creator review. After approval, a later product-definition step may convert it into a PRD. This session intentionally stops before PRD, architecture, data-model, navigation, implementation, code, or test work.
