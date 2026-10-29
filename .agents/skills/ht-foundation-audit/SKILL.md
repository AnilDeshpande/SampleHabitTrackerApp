---
name: ht-foundation-audit
description: "Read-only audit of an Android repository's readiness for its approved documents: inventory the host, IDE and repository, compare build configuration and tests with documented requirements, and find missing decision records, stale branches and broken references before any foundation work. Use before planning a foundation, or to explain why a setup is stuck."
---

# Foundation Audit

Establish what actually exists before anything is changed: what the repository, the machine and the
documents say, where they disagree, and what must be decided. The audit changes nothing.

Use it before [laying a foundation](../ht-android-foundation/SKILL.md) when the project is not a bare
template, when other branches or earlier setup attempts exist, or when documents reference files that
do not resolve. This is a repository-local skill. It has no separate role: the current agent follows it.

## Inputs

- The approved product, architecture, LLD and ADR documents.
- The checkout and all local and remote branches; the host environment; the creator's Android Studio version.

## Outputs

- A filled [environment inventory](../../templates/environment-inventory-template.md).
- A requirement matrix in the style of the [foundation plan](../../templates/foundation-plan-template.md): each documented
  requirement marked present, missing or conflicting, with its source and the evidence observed.
- A list of findings and the decisions the creator must make. No files in the project are modified.

## Procedure

1. **Inventory** the host, SDK, devices, IDE and repository with the inventory template. Read versions from files and
   tools; ask the creator only for what cannot be read (the IDE version when no bundle is found).
2. **Check references.** Run the harness checker. For each missing file a document links to, search every branch
   (`git log --all -- <path>`, `git branch -a --contains`) and say where it lives. A decision record that three documents
   call Accepted but that exists only on a side branch is a blocker finding.
3. **Read build configuration** against the documents: wrapper, AGP, Kotlin, SDK levels, plugins, processors, libraries,
   manifest, backup rules, source sets and runner. Mark each documented requirement present, missing or conflicting.
4. **Read tests against the documented tiers**: which tiers exist, what each proves, whether the mandatory minimum-API
   evidence tier is possible on this machine (devices, images, architecture).
5. **Treat other branches' claims as unverified.** An earlier setup branch can inform options, but nothing on it is
   accepted until it is rebuilt or reproduced and verified. Do not merge or cherry-pick during an audit.
6. **Check gates that block a plan:** IDE-supported AGP range, JDK and Gradle minimums, installed platform for the
   chosen compileSdk, and whether a spoken term in the request maps to exactly one documented tool.
7. **Report.** Findings by severity (blocker, should-fix, note), each with evidence and source; then the decisions
   needed. Offer to continue with the foundation plan; do not start it under this skill.

## Rules

- Read-only. Scratch output goes outside the repository.
- Evidence beats assumption: quote the command or file that shows each fact.
- Separate "the document says" from "the repository shows" from "the machine offers".

## Stopping boundary

Stop when the inventory, matrix and decision list are delivered.
