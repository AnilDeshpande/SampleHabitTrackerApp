# Idea Brief — Habit Tracker V1

> This brief records creator-led product decisions from the ideation sessions. It is not a PRD, architecture document, implementation plan, or project-specific acceptance gate.

## Metadata

| Field | Value |
| --- | --- |
| Status | Ideation complete; ready for product definition |
| Session / source reference | Creator-led Codex ideation sessions, 2026-08-12 and 2026-08-15 |
| Owner | Creator |
| Committed domain | Personal habit tracking and habit formation |
| Next consumer | Later product-definition step |

## Product concept

A private, on-device Android app that helps one person build measurable habits on selected schedules by recording progress, preserving history, and using motivating consistency rewards.

## Product intent

| Dimension | Resolved answer | Creator rationale | Immediate consequence |
| --- | --- | --- | --- |
| Target user | One individual managing personal habits. | The app is intended for personal use rather than shared or organizational use. | V1 does not need accounts, multi-user, social, or collaborative behaviour. |
| Job to be done / problem | Define measurable habit targets on suitable schedules, record actual progress, see long-term consistency and target changes, and build good habits. | Different habits naturally use different measurements and schedules, such as two litres of water daily or 10,000 steps daily. | The product must support user-defined units, multiple frequencies, reliable progress, and understandable history. |
| Product promise | Make personal habit progress trustworthy, easy to manage, and motivating. | The user wants useful progress visibility, gamification, and safe personal control of data. | Accuracy, clear habit identity, historical integrity, and recoverable deletion are product boundaries. |
| Core loop | Create a measurable habit; choose its frequency; define its target and unit; record progress; see target attainment, rewards, or missed-target consequences; review history. | This accommodates the creator's water, reading, and step examples and the approved frequency choices. | V1 evaluates progress against an explicit tracking period rather than assuming every habit is Daily. |
| V1 boundary | Include user-defined habit units; Daily, Weekly, Fortnightly, Monthly, and Custom-weekday frequencies; progress and target-change history; progress review; points; one consistency badge; missed-target handling and notification; and safe edit/delete with undo. Keep all data on the device. | The creator wants a bounded Android application that primarily demonstrates AI-agent-led development while remaining a coherent habit tracker. | V1 has meaningful domain behaviour and visible states without accounts, backend services, REST APIs, or cloud sync. |
| Success signal | A reviewer can complete and clearly observe the full on-device journey: create a measurable habit, record progress, see successful and missed-target consequences, change a target without altering completed history, review progress, undo deletion, restart the app, and find the data intact. | This is a demonstration project, so an observable end-to-end product journey is more appropriate than adoption or retention metrics. | Product definition should preserve a demonstrable, restart-safe core journey; it must not invent a commercial adoption target. |

## Domain language and behaviour edges

| Domain term or behaviour | Creator-defined meaning or rule | Open edge / exception | Source reference |
| --- | --- | --- | --- |
| Habit | A user-defined measurable activity with a target, a creator-defined unit, and a selected frequency. | Exact field validation is deliberately deferred to product definition because it is an input-contract detail; the product-definition owner reopens it when defining creation and editing behaviour. | Creator sessions, 2026-08-12 and 2026-08-15 |
| Unit | The measurement label the user chooses while defining a habit, such as litres, steps, pages, or minutes. | Formatting, normalization, and validation are deliberately deferred to the product-definition owner when defining input rules. | Creator session, 2026-08-15 |
| Tracking period | The interval over which one target is evaluated: Daily is one calendar day; Weekly is Monday through Sunday; Fortnightly is a consecutive 14-day period anchored to the habit's creation date; Monthly is one calendar month; Custom treats each selected weekday as a separate scheduled occurrence. | Exact device-time and calendar-boundary handling is deliberately deferred until the product-definition and technical owners define period finalization. | Creator session, 2026-08-15 |
| Progress | The actual amount the user reports for a habit within its applicable tracking period. | Exact entry interaction is deliberately deferred to interaction design because it does not change the approved measurable-progress concept. | Creator sessions, 2026-08-12 and 2026-08-15 |
| Target attainment | A tracking period succeeds when its recorded progress meets the habit's target when the period is finalized. | Presentation of partial progress is deliberately deferred to interaction design. | Creator session, 2026-08-15 |
| Target change | For Daily and Custom-weekday habits, a change made before that occurrence is finalized applies immediately, preserves its recorded progress, and recalculates its result. For Weekly, Fortnightly, and Monthly habits, a change begins with the next tracking period. | No completed period is recalculated. Detailed editing feedback is deliberately deferred to interaction design. | Creator session, 2026-08-15 |
| Historical progress | Completed targets, progress, and results remain unchanged when a target changes. | Exact historical visualization is deliberately deferred until the product-definition owner defines progress-review interactions. | Creator sessions, 2026-08-12 and 2026-08-15 |
| Missed target | A finalized tracking period in which progress does not meet the target. It earns no points, triggers one deduction for that habit and period, and produces a notification. | Notification delivery time and wording are deliberately deferred until the interaction and notification contract is defined. | Creator sessions, 2026-08-12 and 2026-08-15 |
| Points | One app-wide balance. A successful finalized habit period earns 10 points. A miss deducts 1% of the current balance, rounded up to a whole point, with a minimum deduction of one when the balance is positive and a balance floor of zero. A habit-period reward or deduction is applied only once. | Visual presentation is deliberately deferred to interaction design; the earning and deduction rules are resolved. | Creator session, 2026-08-15 |
| Active run | The number of scheduled periods since the habit was created while it remains active. A missed target or absent progress record does not reset it. Undoing deletion preserves it; final deletion ends it. | The exact undo window and finalization interaction are deliberately deferred until safe-delete behaviour is specified. | Creator session, 2026-08-15 |
| Success streak | The number of consecutive finalized tracking periods in which the target was met. A missed target resets it to zero. | Visual presentation is deliberately deferred to interaction design. | Creator session, 2026-08-15 |
| 21-Day Consistency badge | The only V1 badge. It is awarded once per Daily habit the first time the target is met on at least 18 days in any 21 consecutive calendar days. A day without a qualifying progress record counts as a miss. | Weekly, Fortnightly, Monthly, and Custom habits are not eligible for this specifically Daily milestone. | Creator session, 2026-08-15 |
| Delete recovery | Deletion offers undo. Undo preserves the habit's active run; final deletion ends it. | Confirmation, undo duration, and final-delete interaction are deliberately deferred until the product-definition owner specifies safe deletion. | Creator sessions, 2026-08-12 and 2026-08-15 |

## Decision state

### Decided

- Build a private, single-user, Android-only application whose data remains entirely on the device.
- Do not add accounts, Gmail login, backend services, REST APIs, network synchronization, or cloud sync.
- Let the user define each habit's unit and choose Daily, Weekly, Fortnightly, Monthly, or Custom selected weekdays.
- Use the approved tracking-period meanings and evaluate each target once per applicable period.
- Preserve completed targets and progress; use the approved cadence-specific target-change rules.
- Provide progress and target-change history that can be reviewed over time.
- Maintain one app-wide point balance, award 10 points for success, and apply the approved 1% missed-target deduction exactly once.
- Distinguish an Active run from a Success streak using the approved reset rules.
- Include only the 21-Day Consistency badge in V1, using the approved 18-of-21 Daily qualification rule.
- Notify after a missed target and offer undo after deletion.
- Use the approved observable reviewer journey as the V1 success signal.

### Open now

None. No material product question remains unclassified in this ideation session.

### Not yet specifiable / fog

- **Input contracts:** unit formatting, numeric validation, limits, and editing feedback are deferred because they are detailed product rules rather than product-intent choices. The product-definition owner reopens them when specifying creation and editing.
- **Time boundaries:** timezone, daylight-saving, and exact period-finalization behaviour are deferred because they require detailed calendar rules. The product-definition and technical owners reopen them when specifying tracking-period evaluation.
- **Interaction design:** dashboard layout, exact visualizations, partial-progress presentation, score presentation, notification timing and wording, and delete confirmation/undo duration are deferred because their required outcomes are decided but their interactions are not. The interaction/product-definition owner reopens them when defining observable behaviour.
- **Privacy and data lifecycle details:** PIN protection and automatic cleanup are deferred because the creator has not established a threat model or retention need. The product-definition owner may reopen either item only if a concrete privacy or retention requirement emerges.
- **Technical realization:** persistence technology, generated-code tooling, database/schema choices, navigation, architecture, and implementation are deferred because this artifact is an Idea Brief. Technical design reopens them only after product definition establishes the specification.

### Out of scope

- Backend services, REST APIs, cloud sync, network synchronization, accounts, Gmail login, multi-user/social/collaborative features, and non-Android clients.
- V1 data export, export formats, and additional badges.
- PRD creation, architecture, data model, navigation plan, implementation phases, Android code, and tests in this ideation session.

## Decision log

| # | Creator decision | Creator rationale | Immediate consequence | Source reference |
| --- | --- | --- | --- | --- |
| 1 | Focus the app on helping users build good habits. | Users need to sustain habits and avoid losing momentum. | Product intent centers on repeatable progress. | Creator session, 2026-08-12 |
| 2 | Initially define habits as Daily measurable targets. | The creator cited water, reading, and step examples with measurable limits. | The original brief used daily target-versus-actual tracking. This decision was replaced by decision 13. | Creator session, 2026-08-12 |
| 3 | Initially apply target changes from the current day forward. | Historical targets must remain trustworthy and comparable. | The original brief preserved earlier values but left same-day behaviour open. This rule was refined and replaced by decision 15. | Creator session, 2026-08-12 |
| 4 | Include points, badges, and a 21-day milestone. | Gamification should make continued use motivating. | V1 requires explicit reward and milestone rules. | Creator session, 2026-08-12 |
| 5 | Do not reset the active streak after a missed target. | A miss should have a consequence without discarding ongoing effort. | Two continuity concepts were required; the terminology and rule were refined by decision 16. | Creator session, 2026-08-12 |
| 6 | Notify and deduct points after a missed target. | Gamification should make misses meaningful. | V1 requires a notification and deterministic penalty policy. | Creator session, 2026-08-12 |
| 7 | Make deletion recoverable with undo. | Prevent accidental data loss. | Safe deletion is part of the product boundary. | Creator session, 2026-08-12 |
| 8 | Keep export outside V1. | The creator excluded it from the first release boundary. | Export and export formats are outside V1. | Creator session, 2026-08-12 |
| 9 | Keep the app single-user, on-device only, and without Gmail login. | It is a private personal mobile app with no backend integration. | Online, account, and multi-user behaviours are excluded. | Creator session, 2026-08-12 |
| 10 | Treat the application primarily as a demonstration of AI-agent-led Android development and close ideation with reasonable explicit conclusions. | The creator wants to proceed to the next development stage rather than optimize a commercial product. | The success signal uses an observable reviewer journey instead of adoption or retention metrics. | Creator session, 2026-08-15 |
| 11 | Reaffirm the on-device-only boundary after reviewing the casting conflict. | The creator stated that there are no REST APIs and no backend of any kind. | The boundary remains local-only even though the separate write-then-sync casting requirement cannot pass. | Creator session, 2026-08-15 |
| 12 | Qualify the 21-day milestone at 18 successful days out of 21, use the approved 1% penalty rule, and use the reviewer journey as the V1 success signal. | These are reasonable, deterministic conclusions for the demonstration project. | Milestone, deduction, and success evidence are no longer open. | Creator session, 2026-08-15 |
| 13 | Replace Daily-only habits with user-defined units and Daily, Weekly, Fortnightly, Monthly, or Custom selected-weekday frequencies. | Different habits naturally use different measurements and schedules. | V1 needs explicit tracking-period semantics; the prior Daily-only decision is retained here as replaced evidence. | Creator session, 2026-08-15 |
| 14 | Use the approved calendar and occurrence meanings for all five frequencies; assess success or a miss once per completed tracking period; limit the 21-Day badge to Daily habits. | This keeps cumulative schedules coherent and keeps a specifically Daily milestone truthful. | Weekly, Fortnightly, and Monthly habits are not penalized daily, and non-Daily habits do not qualify for the 21-Day badge. | Creator session, 2026-08-15 |
| 15 | Apply Daily and Custom target changes immediately before occurrence finalization, but begin Weekly, Fortnightly, and Monthly changes with the next period. | This preserves same-day usefulness without applying a cumulative target partway through a longer period. | The blanket current-day rule in decision 3 is replaced; completed history is never recalculated. | Creator session, 2026-08-15 |
| 16 | Replace user-facing “Active streak” with Active run and define a separate Success streak. | The two concepts should not be confused: continued activity survives misses, uninterrupted success does not. | Misses do not reset Active run; misses reset Success streak. | Creator session, 2026-08-15 |
| 17 | Award 10 points for each successful finalized habit period, keep one app-wide balance, apply rewards and penalties only once, and include only the 21-Day Consistency badge in V1. | A small deterministic reward system is sufficient for the demonstration. | The positive score formula and complete V1 badge catalog are resolved. | Creator session, 2026-08-15 |
| 18 | Begin Active run at habit creation, let misses and absent entries leave it intact, preserve it after undo, and end it on final deletion. | Active run measures continued habit activity rather than uninterrupted achievement. | The active-run lifecycle is resolved independently of Success streak. | Creator session, 2026-08-15 |

## Handoff to product definition

### Decided for the next step

Use the product concept, product-intent table, domain definitions, V1 boundary, and decision log as creator-approved direction. Preserve the strictly on-device single-user scope, frequency-specific tracking periods and target changes, historical integrity, deterministic rewards, distinct continuity measures, the single V1 badge, safe deletion, and the observable reviewer journey.

### Still open for the next step

No product-intent or V1-boundary decision remains open. Later owners must resolve only the deliberately deferred specification and technical details below; they must not reinterpret them as permission to change the approved product boundary.

### Deferred / fog

Product definition may specify input validation, time-boundary behaviour, detailed interactions, visualization, notifications, safe-delete timing, and may reopen PIN or automatic-cleanup questions only if a concrete privacy or retention requirement emerges. Technical design may select persistence and code-generation tooling, storage structures, navigation, architecture, and implementation only after the product specification exists.

### Out of scope for the next step

Do not add backend services, REST APIs, cloud or network sync, accounts, social or multi-user capabilities, Gmail login, non-Android clients, export, or extra badges without a new creator decision.

The next product-definition step may convert this brief into a PRD. Project-specific casting criteria remain in the separate dated session notes and are not product requirements implied by this brief.
