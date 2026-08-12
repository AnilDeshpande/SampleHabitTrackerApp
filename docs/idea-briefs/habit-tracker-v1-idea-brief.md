# Idea Brief — Habit Tracker V1

> This brief records creator-led decisions from the ideation session. It is not a PRD, architecture document, implementation plan, or project-specific acceptance gate.

## Metadata

| Field | Value |
| --- | --- |
| Status | Ready for product definition, with open rules explicitly listed below |
| Session / source reference | Creator-led Codex ideation session, 2026-08-12 |
| Owner | Creator |
| Committed domain | Personal habit tracking and habit formation |
| Next consumer | Later product-definition step |

## Product concept

A private Android app that helps one person build daily measurable habits by recording progress, preserving history, and using motivating consistency rewards.

## Product intent

| Dimension | Resolved answer | Creator rationale | Immediate consequence |
| --- | --- | --- | --- |
| Target user | One individual managing personal habits. | The app is intended for personal use rather than shared or organizational use. | V1 does not need multi-user, social, or account-based behaviour. |
| Job to be done / problem | Define daily habit targets, record actual progress, see long-term consistency and goal changes, and build good habits. | The user needs to sustain habits and understand their progress over time. | The product needs reliable daily records and understandable historical progress. |
| Product promise | Make personal habit progress trustworthy, easy to manage, and motivating. | The user wants useful progress visibility, gamification, and safe personal control of data. | Accuracy, clear item identity, and recoverable deletion are product boundaries. |
| Core loop | Create a daily measurable habit; set a target and unit; record actual daily progress; see target attainment and earned progress; review history. | This follows the creator's examples of water, reading, and step targets. | V1 uses daily target-versus-actual tracking. |
| V1 boundary | Include the agreed habit model, progress history, target-change history, dashboard/progress review, gamification, missed-target handling, and safe edit/delete. Exclude export. | The creator designated all discussed capabilities except export as V1. | The later PRD must define these as V1 outcomes and leave export outside V1. |
| Success signal | *To resolve with the creator during product definition.* | No observable success measure has been agreed. | Do not invent a completion, adoption, or retention metric. |

## Domain language and behaviour edges

| Domain term or behaviour | Creator-defined meaning or rule | Open edge / exception | Source reference |
| --- | --- | --- | --- |
| Habit | A user-defined daily activity with a measurable target and unit. | Exact supported unit types and validation remain open. | Creator-led ideation session |
| Daily progress | The actual amount the user reports for a habit on a day. | Exact input interaction remains deferred. | Creator-led ideation session |
| Target change | A changed target applies from the current day forward only. | Whether a same-day change applies before or after an entry remains open. | Creator-led ideation session |
| Historical progress | Past targets and records remain unchanged when a target changes. | Exact presentation is deferred. | Creator-led ideation session |
| Active streak | A missed target does not reset it. | The distinction between an active streak and an unbroken-success streak needs product-definition wording. | Creator-led ideation session |
| Missed target | Notify the user and deduct points. | Exact notification timing and penalty calculation remain open. | Creator-led ideation session |
| Points | Successful continuing participation earns points; a miss deducts a percentage of accrued points. | A 1% deduction is proposed, not committed; rounding and minimum balance remain open. | Creator-led ideation session |
| 21-day consistency milestone | A V1 milestone for motivation. | The qualification rule must be defined because a miss does not reset the active streak. | Creator-led ideation session |
| Delete recovery | Deletion must offer undo. | Confirmation, undo duration, and final-delete behaviour remain deferred. | Creator-led ideation session |

## Decision state

### Decided

- Build a single-user, Android-only, local-device habit tracker with no Gmail login, backend, API, or cloud sync.
- Use daily measurable habits with user-defined targets and units, and record actual daily progress.
- Preserve historical targets and progress; apply target changes from the current day forward only.
- Provide long-term progress review, including comparison of target changes over time.
- Use points and badges; include a 21-day consistency milestone.
- On a missed target, notify the user and deduct points without resetting the active streak.
- Keep data accurate, make items distinguishable, and offer undo after deletion.
- Treat all agreed capabilities as V1 except data export.

### Open now

- Define the qualification rule and wording for the 21-day consistency milestone.
- Commit the missed-target point-deduction rate, rounding, and minimum balance; 1% is only a proposal.
- Define the success signal for V1.

### Not yet specifiable / fog

- Dashboard layout, exact weekly/monthly visualizations, score formula, badge catalog, notification timing and wording, PIN, undo duration, export formats, data cleanup, navigation, database, architecture, and implementation.

### Out of scope

- Backend services, APIs, cloud sync, multi-user and social features, Gmail login, non-mobile clients, and V1 data export.

## Decision log

| # | Creator decision | Creator rationale | Immediate consequence | Source reference |
| --- | --- | --- | --- | --- |
| 1 | Focus the app on helping users build good habits. | Users need to sustain habits and avoid losing momentum. | Product intent centers on repeatable daily progress. | Creator-led ideation session |
| 2 | Use daily measurable targets. | The creator cited water, reading, and step examples with limits. | V1 needs targets, units, and actual daily values. | Creator-led ideation session |
| 3 | Permit targets to change only from the current day forward. | Historical targets must remain trustworthy and comparable. | The product retains prior target values and outcomes. | Creator-led ideation session |
| 4 | Include points, badges, and a 21-day milestone. | Gamification should make continued use motivating. | Later definition must specify reward rules. | Creator-led ideation session |
| 5 | Do not reset the active streak after a missed target. | A missed target should have a consequence without discarding ongoing effort. | Product definition must distinguish the streak from uninterrupted achievement. | Creator-led ideation session |
| 6 | Notify and deduct points after a missed target. | Gamification should make misses meaningful. | Later definition must finalize the penalty policy. | Creator-led ideation session |
| 7 | Make deletion recoverable with undo. | Prevent accidental data loss. | Later definition must specify recoverability behaviour. | Creator-led ideation session |
| 8 | Keep export outside V1. | The creator excluded it from the first release boundary. | Do not include export or cleanup flows in V1. | Creator-led ideation session |
| 9 | Keep the app single-user, local-only, and without Gmail login. | It is a personal mobile app with no backend integration. | Do not add online, account, or multi-user requirements. | Creator-led ideation session |

## Handoff to product definition

### Decided for the next step

Use the product concept, product-intent table, committed V1 boundary, and decision log as the creator-approved direction. Preserve local-only single-user scope, forward-only target changes, the measurable daily core loop, gamification, and recoverable deletion.

### Still open for the next step

Resolve the 21-day milestone qualification, missed-target penalty rule, and V1 success signal with the creator. Do not treat the proposed 1% deduction as approved.

### Deferred / fog

Define detailed product behaviour, interaction design, dashboard and visualization choices, notifications, security, export, storage, navigation, architecture, and implementation only when product definition makes them meaningful.

### Out of scope for the next step

Do not add backend, APIs, cloud sync, social or multi-user capabilities, Gmail login, non-mobile clients, or V1 data export without a new creator decision.

The next product-definition step may convert this brief into a PRD. Project-specific acceptance criteria remain separate gates and are not implied by this document.
