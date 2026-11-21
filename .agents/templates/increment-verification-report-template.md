# Increment verification report — <increment name> — <date>

Verdict: **PASS / FAIL / PASS WITH FINDINGS**, one sentence on why.

Merge-blocking: **Yes / No**, with each blocking finding, unresolved product decision, or missing required check named.

Verified by: <agent or person>. Read-only: yes. Commit verified: `<sha>` on `<branch>`.

## Checks

| Check | Expected | Observed | Result | Evidence |
| --- | --- | --- | --- | --- |
| Git state: branch is not `main`, tree clean, commit list as planned | | | | |
| Build, unit tests, lint (counts per class) | | | | |
| Each device tier: API, platform SQLite version, counts | | | | |
| Shared scenarios on the file-backed database | | | | |
| Lint accepted warnings match the record | | | | |
| Merged debug/release manifest and Android backup/transfer configuration | | | | |
| Device backup/restore and transfer | | | | |

## Mutation checks

| Approved rule and entry point | Named test | Baseline | Mutation and assertion outcome | Restored run | Coverage gap |
| --- | --- | --- | --- | --- | --- |
| | | | | | |

## Per-device run

| Device (serial, owner, API, screen size) | Installed | Launched | States seen | Process death | Dark mode | Large font | Logs (FATAL/ANR) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| | | | | | | | |

## State coverage

One row per row of the screen-state map: seen on a device, not seen (reason), or defective.

| Map row | Seen | Notes |
| --- | --- | --- |
| | | |

## Findings

| # | Severity | Blocks merge? | File and line | Approved source | Reproduction and evidence |
| --- | --- | --- | --- | --- | --- |
| | | | | | |

## Decisions and questions

| Question or accepted answer | Kind (product or engineering) | Owner | Blocks merge? | Durable record and documents affected |
| --- | --- | --- | --- | --- |
| | | | | |

## Not verified

What could not be checked and the concrete blocker. Never list an unobserved check as passed.

## Cleanliness

Settings changed and restored; emulators started and stopped; `adb devices` before and after; confirmation that the repository
is unchanged.
