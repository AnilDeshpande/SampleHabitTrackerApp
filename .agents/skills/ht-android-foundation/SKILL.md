---
name: ht-android-foundation
description: "Lay the build, dependency-injection, persistence, test and device foundation of an Android app from an empty or minimal template, driven by the approved PRD, architecture, LLD and ADRs, with gated phases, version compatibility checks including the creator's IDE, and recorded evidence. Use before feature implementation; not for building features."
---

# Android Foundation

Turn an empty or minimal Android project into a verified foundation that the approved
documents can be implemented on: toolchain, dependencies, application skeleton, test tiers
and device tiers. The approved documents decide what the foundation contains. This method
decides the order, the gates and the evidence. It adds no product behavior.

Use it when the project is a fresh template (or close to one) and implementation is about to
start. Do not use it to build a feature, to redesign architecture, or to upgrade a mature
project's dependencies. The [Foundation Engineer Agent](../../roles/foundation-engineer-agent.md)
orchestrates the conversation. This is a repository-local skill, not a global entry point.

## Inputs

- The approved product, architecture, LLD and ADR sources, and the creator's scope decisions.
- The current checkout, the host environment, and the creator's Android Studio version.

## Outputs

- A [foundation plan](../../templates/foundation-plan-template.md) approved by the creator
  before any build file changes.
- Gated, reviewable commits, one per phase, on the branch the creator names.
- A [foundation record](../../templates/foundation-record-template.md) with commands, platform
  evidence and what is not yet covered.
- A verification handoff, produced with [the verification skill](../ht-foundation-verification/SKILL.md).

## Operating boundary

- Plan first and wait for approval. Carry earlier approvals forward; do not re-ask settled choices.
- Commit per phase on request or under existing authority. Never push, and never change personal
  or global installations (`~/.android`, global skills, IDE settings) beyond what a documented
  recipe creates, such as an AVD definition the creator accepted.
- Use native sub-agents only when the creator asks for them. Otherwise one agent does the work;
  parallel per-device checks are an option the creator can request.
- Google's installed Android skills (AGP 9 upgrade, testing setup, Android CLI) are subordinate to
  project documents wherever the documents decide the same question.
- Keep spoken or abbreviated terms honest: confirm what a term means before planning on it.

## Procedure

### 0. Preflight (read-only)

1. Fill the [environment inventory](../../templates/environment-inventory-template.md): JDKs, Gradle
   cache, SDK platforms and images, AVDs, connected devices, repository state and the creator's IDE.
   Find Android Studio's version from its bundle when present; otherwise ask the creator.
2. Audit the repository against the documents. Use [the audit skill](../ht-foundation-audit/SKILL.md)
   when the repository is not a bare template, or when other branches, ADR links or earlier setup
   attempts exist. Treat claims from unmerged branches as evidence to verify, not as truth.
3. Run the harness checker. A document that links a missing file is a finding to resolve first.

### 1. Requirement matrix and plan

1. Extract every foundation-relevant statement from the documents into the plan's requirement matrix:
   libraries, plugins, test tiers, minimum-API evidence rules, manifest, privacy and backup rules,
   notification setup, time and concurrency seams, and the permitted mock scope.
2. Resolve scope with the creator: how much domain code belongs in the foundation (default: only the
   smallest real table that exercises each wire), toolchain baseline, mandatory minimum-API device,
   CI, and sub-agents (default: none).
3. Present the plan. Wait for approval.

### 2. Version resolution

Follow [version resolution](references/version-resolution.md). Fill the
[compatibility matrix](../../templates/version-compatibility-matrix-template.md). A version is
pinned only after the IDE, Gradle, JDK, SDK, Kotlin, processor and spec gates all pass. The
[AGP compatibility script](scripts/agp-compat.sh) prints the release-note minimums and the IDE row.

### 3. Build in gated phases

| Phase | Work | Gate before the commit |
| --- | --- | --- |
| Toolchain | Wrapper, AGP, SDK levels, version catalog, plugin aliases | `help`, `build --dry-run`, `assembleDebug` |
| Dependencies | Plugins, libraries, processors, desugaring, schema export | `assembleDebug`, `assembleDebugAndroidTest`, unit tests |
| Application skeleton | Application, DI modules, injectable seams, storage, notification channel, manifest and backup rules | assemble; merged manifest read |
| Test infrastructure | Host tier, instrumented runner and test graph, shared fixtures, one smoke test per wire | unit tests; androidTest assembles |
| Device tiers | Mandatory minimum-API device and the newest managed device, per [the device-tier skill](../ht-device-tiers/SKILL.md) | Both instrumented suites pass; platform evidence recorded |
| Close-out | Lint, record, independent verification | Checks green; record written |

Rules for every phase:

- Read the actual failing artifact (test result XML, `buildEnvironment`, merged manifest, logcat)
  before changing anything. The [troubleshooting reference](references/troubleshooting.md) maps known
  symptoms to causes; extend it when a new one appears.
- Add one smoke test per wire (DI graph, database open and seed, preferences persistence, worker
  factory configuration, merged manifest, mock library) so each dependency is proven, not assumed.
  Mutate or remove the rule once to prove a subtle test is not vacuous.
- Follow the documents' real-versus-fake rules. A smoke test for a mock library mocks a test-local
  stand-in, not a production type that the documents forbid mocking.
- Keep fixtures shared by both test tiers in one place so both tiers run identical assertions.
- Record platform facts (API level, platform SQLite version, journal mode) with every SQL-facing run.
  Evidence for the minimum API counts only from that API.

### 4. Close-out

1. Run every build, test, lint and checker command from a final state; record counts and accepted warnings.
2. Write the foundation record. Add any decision verification (for example an ADR's compatibility
   obligation) to the decision record, and state what is not yet covered.
3. Hand off to [independent verification](../ht-foundation-verification/SKILL.md). Fix what it finds,
   then re-verify the fix, not just the original check.
4. Report to the creator: what passed, what is held back and why, what is unverified, and the next
   implementation order.

## Stopping boundary

Stop when the foundation record is written and verification passes or its remaining findings are
reported. Feature work starts from the documents' next implementation order, under its own plan.
