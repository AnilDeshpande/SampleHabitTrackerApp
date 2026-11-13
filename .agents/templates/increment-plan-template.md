# Increment plan — <increment name> — <date>

Approved by <creator> on <date>. Builds bottom-up: domain rules, database and repository, execution
and coordination, then ViewModels and UI. Each layer ends at a checkpoint with evidence and one commit.

## Sources and authority

PRD, architecture, LLD, ADRs, prototype handoff, design direction and foundation record used, and the
statement in each that decides a choice here. Note any conflict and who owns it.

## Branch and commit policy

Branch name and base. The base is an up-to-date `main`; the branch is never `main`, and it is not stacked on an unmerged pull
request. If this increment depends on one, wait for its review, approval and merge. One commit per layer. The pull request is
merged only after review and the creator's approval.

## Decisions

| Decision | Choice | Alternative | Recommended by | Decided by |
| --- | --- | --- | --- | --- |
| | | | | |

Interpretations the documents do not settle belong here too; they move to the pull request's
"decisions to confirm" table.

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
