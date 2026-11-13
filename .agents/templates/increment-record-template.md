# Increment record — <increment name> — <date>

What was built, what evidence proves it, and what is not covered. Written at close-out, on the increment's
branch, and linked from the pull request.

## What was built

| Layer | Result |
| --- | --- |
| 1. Domain rules | |
| 2. Database and repository | |
| 3. Execution and coordination | |
| 4. ViewModels and UI | |

## Scope delivered

Screens and states included (by S-number), what was deferred, and the placeholders that stand in for it.

## Verification

| Check | Result |
| --- | --- |
| Host tests (counts by area) | |
| Each device tier, with API, platform SQLite version and counts | |
| Shared scenarios on the file-backed database | |
| Lint (errors, warnings, each accepted warning with its reason) | |
| Mutation checks (rule removed, tests that failed) | |
| Independent check, if one was run | |

### Device inspection

Devices used and who owns them; each state seen; process death (how it was done); dark mode; large font; logs scanned
for crashes. List the curated screenshots by caption; they are attached to the pull request, not committed.

## Decisions taken while building

| Decision | Where | Why |
| --- | --- | --- |

## Interpretations to confirm

| Choice | Where | Alternative |
| --- | --- | --- |

## Not covered

What the increment cannot yet prove and the milestone that will. Include any claim that was checked indirectly.

## Document corrections found

Stale or conflicting text in the approved documents, with the owner who should decide.

| Document and text | Kind | Where handled |
| --- | --- | --- |
| | Sync correction (made true to what was built, no change to approved behavior) | Own commit in this increment |
| | Substantive (changes behavior, a contract or an ADR decision) | Separate document change, with the creator's approval |

## Next increment

Numbered, in the order the documents and dependencies require.
