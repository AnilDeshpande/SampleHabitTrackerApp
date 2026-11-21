---
name: ht-pr-review
description: Review an existing Android pull request against approved documents, re-review its fixes, or publish an existing review when asked. Pin the diff, verify consequential behavior, and report exact findings and merge impact.
---

# Pull request review

Use the [Implementation Verifier role](../../roles/implementation-verifier-agent.md) for independent review. The reviewer works
read-only on the checkout and PR. The main agent handles publication when the user has authorized it. For code changes after
review use [PR remediation](../ht-pr-remediation/SKILL.md). For a standalone device check use
[emulator validation](../ht-emulator-validation/SKILL.md).

## Review and re-review

1. **Pin the inputs.** Record the repository, PR, base tip, head SHA, and diff base. Read the approved PRD, architecture, LLD,
   ADRs, increment scope, mapped design states and accepted decisions that govern the change. Treat the PR body and implementation
   record as claims to verify. Identify source conflicts and their owners. Keep CI observations, author evidence, and your own
   runs distinct, with the SHA and variant or device where relevant. Use an isolated checkout for executions that write files.
2. **Follow the production paths.** Verify the changed contracts and wiring, including relevant transitions across layers or
   over time. Use the actual entry point and check the resulting observable behavior. Check LLD-defined evidence directly:
   a gate counter gives no ordering evidence, and a trigger-name comparison gives no definition or behavior evidence. Use the
   [Android evidence reference](references/android-evidence.md) for relevant Android checks and
   [device tiers](../ht-device-tiers/SKILL.md) for required platform coverage.
3. **Challenge consequential tests.** Use [targeted mutation testing](../ht-increment-implementation/SKILL.md#targeted-mutation-testing)
   at the reviewed SHA. Record a defect reproduced on unmodified code separately from a completed mutation cycle. A green suite
   alongside a reproduced failure establishes a coverage gap. Select tests at the seam where the defect occurs and state which
   checks were blocked, skipped as out of scope, or independently run.
4. **Give the verdict and handoff.** Use the [verification report](../../templates/increment-verification-report-template.md)
   and [shared finding register](../../templates/pr-finding-register-template.md). Record the approved requirement, tight code
   location with diff side, reproduction, evidence origin, acceptance test and merge impact for each material finding. Block
   merge for material contract failures or missing evidence required by the approved scope. Surface product decisions that
   leave approved behavior unsettled and mark their merge impact. A self-contained fix handoff uses the
   [remediation prompt](../../templates/pr-remediation-prompt-template.md); include the open findings, decisions, evidence,
   repository state and authorized actions.

For a re-review, read both the prior reviewed SHA and the current head, original findings, replies and fix delta. Recheck each
affected contract and remaining required evidence. Expand the review when the delta introduces a related risk. Verify the behavior
independently of thread resolution and report which findings are accepted, still open, or unverified at the new head.

## Authorized publication

Use [GitHub review publication](references/github-review-publication.md) when the user asks to post or follow up. If publishing
an existing review, recover its exact findings, reviewed SHA and evidence before posting. Keep the reviewer verdict, submitted
GitHub event, CI state, and branch publication separate in the close-out. Link the resulting review and verify the anchors.

## Stopping boundary

Deliver a pinned report with actionable findings, merge impact, evidence gaps, and any decision the creator owns. When publication
is authorized, finish with the verified GitHub result or the concrete blocker and the remaining unposted findings.
