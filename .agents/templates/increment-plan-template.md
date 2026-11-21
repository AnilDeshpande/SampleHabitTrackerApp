# Increment plan — <increment name> — <date>

Approved by <creator> on <date>. Builds bottom-up: domain rules, database and repository, execution
and coordination, then ViewModels and UI. Each layer ends at a checkpoint with evidence and one commit.

## Sources and authority

PRD, architecture, LLD, ADRs, prototype handoff, design direction and foundation record used, and the
statement in each that decides a choice here. Note any conflict and who owns it.

## Branch and commit policy

Branch name and base. The base is an up-to-date `main`. Keep one commit per layer. If the increment depends on an open PR,
wait for that PR's review, approval, and merge. The creator approves each merge.

## Decisions

| Decision | Kind (product or engineering) | Recommendation | Owner | Blocks merge? | Accepted answer and source |
| --- | --- | --- | --- | --- | --- |
| | | | | | |

Save decisions here or link the owning document. The PR links back to this durable record.

## Scope

| Included (S-numbers and states) | Built to final contract now | Deferred, and what it becomes now |
| --- | --- | --- |
| | | (for example a placeholder destination) |

State why this is the closed loop of the journey.

## Layers and checkpoints

| Layer | Build | Checkpoint evidence |
| --- | --- | --- |
| 1. Domain rules | | |
| 2. Database and repository | | |
| 3. Execution and coordination | | |
| 4. ViewModels and UI | | |

## Screen-state map

Link the filled [screen-state map](screen-state-map-template.md), or summarize its state types here.

## Design lock

Palette and tokens, dynamic colour decision, reference renders, and what is still open.

## Device tiers

Which devices run which suites, which device is the mandatory minimum API, which device the creator owns,
and the smallest-screen tier used for Compose tests.

## Process

Per layer: short design note, build, named tests, a mutation check on the subtle rule, device evidence where
needed, graph update, commit. Evidence and the record are written at close-out.
