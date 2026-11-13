---
name: ht-pr-handoff
description: "Open a pull request that works as a self-contained review handoff: before and after tables, a commit-by-commit review guide, verification with reproduction commands, decisions that need confirmation, what is not covered and the risks, with every number taken from commands. Use when an increment or change is verified and the creator asks for a pull request; never use a generic template."
---

# Pull Request Handoff

Give a reviewer everything needed to understand a change without the conversation that produced it: what existed before, what
this adds, in what order to read it, what proves it, what the author decided, and what is not covered. The creator asked for
a tabular structure that makes "before" and "after" visible at a glance; that structure is this method's core.

Use it when the work is verified and the creator asks for a pull request. Do not use it to decide what to build or to verify
(that is [implementation](../ht-increment-implementation/SKILL.md) and
[validation](../ht-emulator-validation/SKILL.md)). The
[Implementation Engineer Agent](../../roles/implementation-engineer-agent.md) guides the conversation. This is a
repository-local skill.

## Inputs

- The branch, its base, and the increment record or equivalent evidence.
- The creator's request for a pull request (this authorizes pushing that branch, nothing else).

## Outputs

- A pull request whose description follows the [handoff template](../../templates/pr-handoff-template.md).
- The link, the CI state, and an offer of the next step.

## Rules

- **Never commit to `main`; never force-push.** Push the named branch normally. The base is `main`. Do not stack a pull request on
  another open one; if the work depends on an unmerged pull request, say so and wait.
- **Never merge, enable auto-merge or push to `main`.** The sequence is: open, wait for review, approval, and merge only when the
  creator has approved and says to merge. After the merge, update local `main`; the next increment branches from it.
- **Numbers come from commands, not memory.** Compute file counts, line counts, test counts and per-commit stats from git and the
  test result files at the time of writing; recompute if anything changed.
- **Before versus after is a table, per area.** "Before" is what the base branch has, read from it (`git ls-tree`, `git show`), not
  recalled. If you cannot read it, write "not checked".
- **Say what is not covered**, and what was checked only indirectly. Do not imply a check ran because it is listed.
- **Interpretations are a table** with the alternative and the location, so a reviewer can disagree with one choice.
- **Links to files are absolute blob URLs on the branch.** Relative paths in a pull request description resolve against the default
  branch and break until merge.
- **Screenshots are attached to the pull request, not committed.** The GitHub CLI has no command for attaching images, so prepare
  the curated folder outside the repository (see [emulator validation](../ht-emulator-validation/SKILL.md)), then either ask the creator
  to drag the files into a comment on the pull request (give the folder path and a comment skeleton that lists each image with its
  caption), or, only with the creator's explicit yes and a connected browser, upload them into that comment through the browser
  tool. Uploading publishes the images; do not do it unasked. The description's Screens table lists each image by caption and says
  where it is attached.
- **Documents changed in this pull request are listed.** Sync corrections made in the increment are named in their own table;
  substantive corrections point to the separate document change.
- No generic or boilerplate sections. Every section must say something about this change. Add the attribution line the session
  provides at the end.

## Procedure

1. **Preconditions.** Current branch is not `main`; the tree is clean; the record exists; checks that the description will cite
   have been run from the final state.
2. **Gather facts** with commands: `git log <base>..HEAD`, `git diff --stat <base>...HEAD`, per-area numstat, per-commit
   `--shortstat`, the base's file and test counts, dependency and catalog diffs, test counts from the result XML.
3. **Write the body to a file** outside the repository from the template: summary; before-and-after tables (by area, what the user
   sees, build and dependencies); a commit-by-commit review guide (read-first files and what to check in each); verification table
   with reproduction commands and the CI situation; screens; decisions to confirm; not covered; deferred; risks.
4. **Check the draft** against the record, and confirm each cited command was actually run.
5. **Push and open.** `git push -u origin <branch>`, then `gh pr create --base <base> --head <branch> --title … --body-file …`.
   Do not make it a draft unless the creator asks. Do not request reviewers or enable auto-merge unless asked.
6. **Verify the result.** Read the pull request back (`gh pr view`): state, base, head, counts, that the tables rendered. If the app
   provides pull-request tools, bind or check the pull request and read its CI state instead of polling `gh pr checks`.
7. **Report** the link, size and CI state, saying exactly which workflows ran, which did not, and that the Android workflow's first
   run is its own evidence. Name the screenshots still to attach. Offer the next step (Auto-fix, waiting for review, a retro).
   Do not merge; the creator approves first.

## Stopping boundary

Stop when the pull request is open, read back, and reported. Review comments and fixes are separate requests.
