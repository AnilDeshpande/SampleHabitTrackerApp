---
name: ht-pr-remediation
description: Address review findings on an existing pull request. Map comments to approved requirements, fix and verify each issue, preserve decisions and report local and published state. Use when asked to implement PR review feedback or close out completed fixes.
---

# Pull request remediation

Use the [Implementation Engineer role](../../roles/implementation-engineer-agent.md). For independent review or re-review use
[PR review](../ht-pr-review/SKILL.md). Opening a new PR remains [PR handoff](../ht-pr-handoff/SKILL.md).

1. **Read the review and the current diff.** Confirm the branch, current PR head and SHAs originally reviewed. Read all review
   rounds, current threads and relevant approved requirements. Preserve unrelated work. Populate the
   [finding register](../../templates/pr-finding-register-template.md) in the existing durable review or implementation
   record, with links to the original threads, acceptance criteria and required evidence. Keep the same ID for comments about
   the same obligation. Retain each thread's state and distinguish stale anchors from addressed behavior.
2. **Resolve contract questions.** Use accepted answers without reopening them. Ask about unsettled product behavior or a
   consequential engineering choice only when needed. Record the kind, answer, approver, date, owning document and affected
   sections. Apply authorized amendments through the owning document workflow. Follow
   [spec drift guidance](../ht-increment-implementation/SKILL.md#cross-layer-rules) for sync and substantive corrections.
3. **Fix and prove the behavior.** Define the expected observation before changing code. Where feasible, run the regression
   against the reviewed or current unfixed code first. Exercise real production entry points, scheduling and continuing
   subscribers when relevant. Use [targeted mutation testing](../ht-increment-implementation/SKILL.md#targeted-mutation-testing)
   for consequential rules, [time and Room test guidance](../ht-increment-implementation/SKILL.md#time-and-room-tests), and the
   [minimum-API tier](../ht-device-tiers/SKILL.md) early for SQL and migration checks. Record concrete results and any still-open
   verification. The [Android evidence reference](../ht-pr-review/references/android-evidence.md) covers relevant platform checks.
4. **Close out within scope.** Carry forward authorization for each Git or GitHub action. Record fix and evidence before
   claiming a finding is addressed. If committing a mixed change, keep the fix, harness and durable record distinct where that
   helps review, and commit the fix before recording its SHA. When publication is authorized, follow
   [GitHub thread close-out](references/github-thread-closeout.md) and reconcile the durable record from remote read-back. Treat
   local changes, committed changes, pushed changes, replies, thread resolution and independent verification as distinct states.
   Keep outstanding conditions visible. A request to address findings on an existing PR does not by itself authorize pushing,
   posting replies, resolving threads, closing or merging the PR. Leave PR structure to the creator.

For a fresh session, use the [remediation prompt](../../templates/pr-remediation-prompt-template.md) as the portable handoff.
For a close-out-only request, start with the existing fix, register and remote state. Finish at the authorized boundary with
the evidence for each finding and any remaining blockers.

For the next stage and its gates, use [the navigator](../ht-next/SKILL.md).
