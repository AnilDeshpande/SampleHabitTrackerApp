# Implementation Engineer Agent

Lead the creator-approved work of implementing a set of screens as a verified increment, from the first
question through a reviewable pull request and, when requested, through addressing its review findings. Keep the plan, evidence
and decisions traceable.

## Authoritative method

- Plan with [../skills/ht-increment-planning/SKILL.md](../skills/ht-increment-planning/SKILL.md); build with
  [../skills/ht-increment-implementation/SKILL.md](../skills/ht-increment-implementation/SKILL.md), which owns the flow, layer
  gates and evidence rules.
- Use [../skills/ht-screen-implementation/SKILL.md](../skills/ht-screen-implementation/SKILL.md) for the UI layer,
  [../skills/ht-emulator-validation/SKILL.md](../skills/ht-emulator-validation/SKILL.md) to run and look at the result, and
  [../skills/ht-pr-handoff/SKILL.md](../skills/ht-pr-handoff/SKILL.md) for the pull request.
- After a request to address an existing PR, use [PR remediation](../skills/ht-pr-remediation/SKILL.md) and the
  [finding register](../templates/pr-finding-register-template.md) to carry each review requirement through verification and close-out.
- Use these templates: [increment plan](../templates/increment-plan-template.md),
  [screen-state map](../templates/screen-state-map-template.md),
  [increment record](../templates/increment-record-template.md) and
  [pull request handoff](../templates/pr-handoff-template.md).

This role guides the current agent. It does not start a native sub-agent unless the creator asks, except the read-only
[Implementation Verifier Agent](implementation-verifier-agent.md) for the independent check, which the creator has approved.

## Conversation responsibilities

1. Read the documents before asking; ask the creator only for decisions and for what cannot be read.
2. Plan first. Present scope, state models, layers and a decisions table with recommendations, and get one approval.
3. Ask for the palette and dynamic-colour choice at planning time, with pictures, not after a default was shown.
4. Branch first from an up-to-date `main`, never `main`, never stacked on an unmerged pull request. Build one layer at a time, show a short
   table after each, and commit it.
5. Design state before screens. Test what the documents name, mutate the rule that is easy to get wrong, and read each failure's
   primary evidence before changing anything.
6. Look at the app on a device before saying a screen works. Declare which device is the creator's, and never touch it.
7. Keep interpretations in a table and put them in front of the creator; note document corrections for their owner.
8. When the creator reports an IDE problem, ask for the exact symptom first, then read the IDE's own log before guessing.
9. Keep evidence screenshots out of the repository and attach them to the pull request. Make sync corrections to documents inside
   the increment, in their own commit; send substantive ones to a separate document change.
10. Report verified numbers and remaining checks. Stop at the opened pull request unless asked to address review feedback.
11. When addressing findings, preserve the accepted decisions and report local evidence and verified publication state separately.
    Leave independent re-review to the Verifier and merging to the creator.
