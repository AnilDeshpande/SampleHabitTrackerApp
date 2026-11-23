# Increment verification report — <increment name> — <date>

Verdict: **PASS / FAIL / PASS WITH FINDINGS**, one sentence on why.

Merge-blocking: **Yes / No**, with each blocking finding, unresolved product decision, or missing required check named.

Verified by: <agent or person>. Read-only: yes. Commit verified: `<sha>` on `<branch>`.
For PR review: `<PR link>`, base `<sha>`, diff base `<sha>`, reviewed head `<sha>`. Record the GitHub review event and
link only if published. State CI status and whether the branch was pushed separately from the verdict.

## Checks

| Check | Expected | Observed | Result | Evidence |
| --- | --- | --- | --- | --- |
| Git state: branch is not `main`, tree clean, commit list as planned | | | | |
| Build, unit tests, lint (counts per class) | | | | |
| Each device tier: API, platform SQLite version, counts | | | | |
| Shared scenarios on the file-backed database | | | | |
| Lint accepted warnings match the record | | | | |
| Merged debug/release manifest and Android backup/transfer configuration | | | | |
| Per-package backup eligibility (API, transport, result) | | | | |
| Completed backup/restore or transfer (API, transport, restored data) | | | | |

For each check, label the evidence as independently observed, CI, or supplied by the author. Add the build variant, device or
SHA when they change what the result establishes.

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

## PR findings and decisions

Include the populated [shared finding register](pr-finding-register-template.md) or a link to it. For a standalone
device check, include any applicable findings and decisions from the same template.

## Not verified

What could not be checked and the concrete blocker. Never list an unobserved check as passed.

## Cleanliness

Settings changed and restored; emulators started and stopped; `adb devices` before and after; confirmation that the repository
is unchanged.
