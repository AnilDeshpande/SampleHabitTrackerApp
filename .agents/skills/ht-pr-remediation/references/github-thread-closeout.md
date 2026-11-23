# GitHub thread close-out

Use this from [PR remediation](../SKILL.md) when remote actions are authorized. Carry existing authorization forward. Push,
reply and resolve are individually scoped actions. For an authorized status-only reply without a pushed fix, report the actual
status and leave the thread open.

1. After an authorized normal push, read back the current PR head and confirm it contains the fix SHA. If the head moved beyond
   the validated state, inspect the delta and re-verify affected evidence before making a fixed claim. Wait for GitHub to
   reflect the push when necessary. An unpushed fix stays local in the register.
2. List the PR's review threads and their top-level comments, following pagination. Match the register by thread ID. For each
   authorized reply, cite the pushed fix SHA, behavior or named test, relevant mutation result and any caveat. Use the
   top-level review comment ID to reply. Use the thread's GraphQL ID to resolve it, only when resolution is authorized and the
   thread's acceptance criteria are met. An outdated anchor alone does not justify resolution.
3. On retry, read current replies and resolution first. If the reply for the same fix and evidence already exists, reuse its
   URL and advance only the missing authorized step. When a request fails ambiguously, read back before retrying. Leave
   disputed or partially verified threads open and report their blocker.
4. Re-read the remote replies, resolved flags and head before updating the durable register. Record read-back date and head,
   reply links and pending checks. A resolved thread does not confer independent verification or merge approval. Keep any
   follow-up for committing or pushing the refreshed record within the authorized scope.

## Worked example

For a fixed tracking thread, reply on the top-level comment with “Fixed in `<commit link>`. The real Create Habit path now
rearms the idle ticker. `<test>` passes on `<tiers>` and fails at the finalized-period assertion with the rearm signal removed.
`<remaining caveat, if relevant>`.” Then use `resolveReviewThread` with that thread's ID if authorized. If the reply succeeded
and resolution failed, list the thread again and resolve the existing thread without posting a second reply.
