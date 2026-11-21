# PR finding register — <PR name and number>

Record the repository, PR link, review links, base and reviewed head SHA, and current head SHA. Fill the same register in the
verification report, fix handoff and durable review record, or link the populated register where the recipient has access.

## Findings

| ID | Reviewed SHA and code location (path, line, side) | Approved source and requirement | Severity and merge impact | Observable failure and evidence origin | Acceptance test and required evidence |
| --- | --- | --- | --- | --- | --- |
| F-01 | | | | | |

Keep IDs stable across review rounds. When comments overlap, link them to the same finding. Mark evidence as independently
observed, CI, or supplied by the author and identify the SHA it applies to. Distinguish a reproduction on the original code,
a regression on the fix, and a named mutation that fails at the intended assertion. Record a required check's concrete blocker.

## Fix and publication status

| Finding ID | Outcome (open, addressed, blocked, deferred) | Local or committed fix and SHA | Verification result and caveats | Pushed head and read-back date | Review thread and reply links, resolved state | Independent re-review at SHA |
| --- | --- | --- | --- | --- | --- | --- |
| F-01 | | | | | | |

Record each thread separately where several refer to one finding. A thread may be open, resolved, or outdated and still open.
Record reviewer verdict, GitHub review event, and CI state in the report. An outdated anchor does not change the finding's status.

## Decisions and questions

| Decision or question | Product or engineering | Owner | Answer, approver and date | Blocks merge? | Durable record and affected document sections |
| --- | --- | --- | --- | --- | --- |
| | | | | | |

Record confirmed answers and their provenance in the repository, and link any authorized updates to the approved sources.
Routine implementation choices within the approved contract need no new creator decision.
