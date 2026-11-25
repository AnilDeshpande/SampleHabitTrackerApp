---
name: ht-next
description: "Inspect the repository read-only to identify the current lifecycle or increment stage, its evidence and human gates, and the next Codex and Claude skill commands. Use when the creator asks where the work stands or what to run next."
---

# Navigate the workflow

Orient the creator in the existing harness. Report evidence and uncertainty; do not run the next skill. Read the [shared index](../../README.md), repository instructions, and current sources for the suggested workflow. Use [Graphify](../ht-graphify/SKILL.md) when a current graph is available and verify material claims in source.

## Operating boundary

This skill is read-only: do not create or edit files, change branches, commit, fetch, push, open or merge a PR, post or resolve threads, or drive devices. Read-only Git and `gh` commands are permitted; do not fetch. Preserve human gates and authority. A suggested command does not authorize its execution.

## Inspect and choose the work

1. Read `git status --short --branch`, `git branch --show-current`, `git rev-parse HEAD`, local branch history, and the local `main` tip. Report dirty files and distinguish local from remote knowledge. Inspect the index and relevant approved documents, plans, state maps, records, reviews and finding registers. Filenames, branch names and commit messages alone do not prove completion.
2. Identify the intended increment and branch from the request and evidence. If several are plausible, or the checkout and requested work disagree, ask the creator to select one. Do not pick an increment, PR or review round by recency alone.
3. Use read-only `gh pr view` and, when needed, `gh api` to inspect PR state, base, head, review rounds, findings and thread resolution. Record command failures. If access fails, remote state is unknown: do not infer that no PR or review exists, findings are resolved, or a merge happened. Historical records are evidence at their recorded date and SHA, not proof of current remote state.
4. Check whether the plan explicitly records creator approval and who approved it. A draft is not approved. Missing, conflicting or ambiguous approval requires a question to the creator. A saved file or commit alone does not establish approval. Check design, device, test and record evidence before advancing a gate. Ask when material evidence conflicts or is absent.
5. Compare the PR head with the head actually reviewed and examine findings and resolution. A different SHA means re-review is due; it does not prove fixes exist or are correct. A review at the current head does not alone establish findings are resolved. Treat uncommitted changes separately from the pinned head.

## Route by evidence

Apply the most specific verified state, with a confirmed merge taking precedence over historical review and layer evidence. Each row is a candidate; check the target skill's prerequisites and report unmet gates. Substitute the actual increment, screens, PR and SHA when known. For each Codex command, also give the same arguments with `/` in place of `$` for Claude. Ask for unknown required arguments rather than inventing them.

| Evidence | Stage and next skill |
| --- | --- |
| Earlier product or design source is missing, unapproved or under discussion | Point to the owning workflow: `$ht-product-ideation`, `$ht-prd-creation`, `$ht-architecture-creation`, `$ht-lld-creation`, `$mobile-prototype`, or `$ht-android-foundation`, according to evidence. Ask when the intended work is unclear. |
| Foundation ready, no increment plan | `$ht-increment-planning Plan the next increment for <journey or screens>.` |
| Plan draft, approval missing or disputed | Ask the creator; `$ht-increment-planning` continues planning. |
| Plan approved, implementation not started | `$ht-increment-implementation Build the approved increment plan <path>.` |
| Layers in progress | `$ht-increment-implementation Continue the approved increment <path>.` Identify progress from the plan and evidence. |
| Earlier layers complete, UI remains | `$ht-screen-implementation Implement the UI layer for <plan and state map>.` |
| UI done, required device or visual evidence missing | `$ht-emulator-validation Validate <increment> on <authorized devices>.` Ask for device ownership and serials when missing. |
| Validation complete, record incomplete or missing | `$ht-increment-implementation Complete the record for <increment>.` |
| Record and verification complete; confirmed no open PR | `$ht-pr-handoff Prepare a PR for <branch>.` State applicable push and PR authorization gates. |
| Open PR without review of its current head | `$ht-pr-review Review PR <number> at <head>.` If a previous head was reviewed, say re-review is due. |
| Current review has unresolved findings | `$ht-pr-remediation Address the authorized findings on PR <number>.` |
| Fixes or other changes after the reviewed head | `$ht-pr-review Re-review PR <number> at <head>.` Include previous findings and reviewed SHA; establish independently what was fixed. |
| Current head reviewed and findings resolved; PR open | Report verdict and remaining checks; wait for the creator's merge decision. Never merge. |
| PR confirmed merged | `$ht-increment-planning Plan the next increment for <journey or screens>.` Do not assume new scope. |

The rows are not exhaustive. A review may have blocking findings despite GitHub recording it as a comment; check the finding register and verdict. If evidence is stale, incomplete or contradictory, describe competing possibilities and ask for the missing decision. Increment planning assumes approved prototype screens and a layered plan; report the limitation if the request does not fit rather than inventing a smaller workflow.

## Answer and stopping boundary

Give: (1) stage and confidence; (2) branch, head, increment and PR where known; (3) concrete evidence and date or SHA; (4) missing or conflicting facts and the question needed; (5) the exact next Codex and Claude commands with known arguments, or why an exact command is blocked; and (6) human decisions, device permissions and external-action gates. Link relevant repository paths. Stop after answering. For a portable prompt, suggest [handoff](../ht-handoff/SKILL.md).
