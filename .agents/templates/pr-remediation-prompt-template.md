# PR remediation handoff prompt

Fill the text below for a session that cannot see the earlier conversation. Copy the relevant rows and evidence from the
[finding register](pr-finding-register-template.md). Include unresolved requirements and approvals as well as fixes.

---

You are the Implementation Engineer for `<absolute repository path>`. Apply the review feedback on `<PR URL>` using
`.agents/skills/ht-pr-remediation/SKILL.md` and `.agents/roles/implementation-engineer-agent.md`.

- Branch, base and current head: `<branch and SHAs>`. Originally reviewed SHA(s): `<SHAs>`.
- All review rounds and durable record: `<review URLs and repository path>`. Preserve unrelated local changes: `<details>`.
- Approved source sections and confirmed decisions: `<requirements, recorded answers, dates, owners and paths>`.
- Findings: `<stable ID, thread link, reviewed SHA and code location, merge impact, approved requirement, reproduction or
  acceptance test, evidence origin, targeted mutation where applicable, and remaining work for each finding>`.
- Verification: `<commands, required device tiers, expected assertions, evidence sources and outstanding blockers>`.
- Authorized Git and PR actions: `<edit scope, commit and push scope, reply and resolve scope, or local-only>`. Follow the
  repository's existing boundaries for branch structure, merging and closure.

Keep decisions and per-finding evidence durable. Report local changes, committed and pushed SHAs, GitHub replies and resolved
threads, and independently unverified requirements at close-out.
