# Android foundation upskill retro — 2026-10-29

Status: the proposals below were built after the creator's decisions (see "Resolution").
Purpose: turn this session's work (laying the build, dependency-injection, persistence
and test foundation on an empty Android Studio template, driven by the product docs)
into new harness skills and agent workflows. This is analysis and a proposal. Nothing
described under "Proposed harness additions" has been built.

Record of the work itself: [foundation record](../implementation/foundation.md). Decision
under test: [ADR 0004](../adr/0004-hilt-dependency-injection.md). Seven commits
(`9fad6cb`..`286497a`), 38 files, on top of the harness install `d374af8`.

## Outcome

From a bare template (AGP 9.2.1, no Room/Hilt/WorkManager/DataStore, two placeholder
tests) to a verified foundation: AGP 9.3.0 / Gradle 9.8.2 / compileSdk 37, KSP-only
annotation processing, Hilt application and test graph, Room schema v1 with exported
schema, 13 host tests, 3 instrumented tests on each of API 24 and API 36, app launch
checked on API 24 and API 34, lint 0 errors, and the creator confirmed the project
opens in Android Studio.

## How the session actually went

| Step | What we did | How we found out what was needed |
| --- | --- | --- |
| 1. Install shared skills | Vendored Google's 36 `android/skills` for both Codex and Claude | Read the harness authoring standard and checker rules before copying anything |
| 2. Inventory the repo | Gradle files, tests, manifest, docs, branches | Direct reads; found an unmerged `codex/foundation-setup` branch and an ADR referenced everywhere but absent from `main` |
| 3. Extract requirements from the spec | Grepped architecture, LLD and ADRs for libraries, tiers, backup and notification rules | The spec states most of the foundation: Room, Hilt (ADR 0004), WorkManager, DataStore, desugaring, API 24 as mandatory SQL evidence, backup exclusions, Mockk scope |
| 4. Choose versions | Queried Maven metadata and AGP release notes for compatibility | Release notes gave Gradle/JDK/API minimums; Google's `agp-9-upgrade` skill gave KSP and Hilt minimums |
| 5. Build in phases | Toolchain, dependencies, app skeleton, test tiers, device tiers, close-out | Each phase had a gate (`help`, assemble, tests) and its own commit |
| 6. Fix what broke | See the failure table | Read the actual error (test XML, build output, `buildEnvironment`), not a guess |
| 7. Independent verification | A written handoff prompt to a separate agent, then per-device sub-agents | Surfaced two real findings and one environment hazard |
| 8. Creator check in the IDE | Creator opened Android Studio | Found a blocker nothing in the command line could show |

## Failures and fixes

| Symptom | Root cause | How it was found | Would have been prevented by |
| --- | --- | --- | --- |
| Android Studio refused to sync | AGP 9.5.0 chosen as "latest stable"; Quail 3 supports AGP up to 9.3 | The creator ran the IDE; the command line and the verifier agent were blind to it | Capturing the Studio version in a preflight and checking its AGP range before picking a plugin version |
| Compose plugin at 2.2.10, Kotlin on the classpath at 2.4.10 | Template pin left stale after the AGP bump | `buildEnvironment` dependency tree | A rule: after any AGP change, read the resolved Kotlin version and align the Compose plugin |
| All Robolectric tests failed with `IllegalAccessException` | Robolectric reflects into `jdk.internal.access`, closed on JDK 21 | Test result XML, not the console summary | A troubleshooting reference listing this exact cause and the scoped `--add-opens` fix |
| `avdmanager` crashed with `ClassNotFoundException: javax.xml.bind` | The SDK's legacy `tools/bin` tool needs JAXB, absent from JDK 11+ | Tried it; tried JDK 11 as well | A scripted AVD recipe that writes the two config files directly |
| Need an API 24 device on Apple Silicon | Managed devices start at API 27; x86 images don't run on arm64 | `android sdk list --all` showed `system-images;android-24;default;arm64-v8a` | A device-tier procedure that starts from "list arm64 images" |
| First `adb install` on API 24 hung for minutes | Intermittent `dex2oat` hang after fresh boot (asleep on a futex); retry took 12 s | Verifier agent, then reproduced once ourselves | `timeout` plus retry in the install step; no concurrent Gradle or second emulator during first install |
| Two vendored files would have been git-ignored | Root `.gitignore` rule `build/` matched a directory inside vendored docs | `git status --ignored` after copying | A vendoring step that compares file counts on disk vs. what git would add |
| Checker flagged 12 references in unmodified vendored docs | Example text and genuinely broken upstream anchors | Running the checker | Done: exact `vendored_references` exceptions. The pattern belongs in a vendoring procedure |
| Mockk smoke test mocked `Clock` | Literal LLD scope allows Mockk only for external-effect ports and UI-mapping tests | Verifier agent's contract review | Writing the test against the scope sentence, not "any interface" |

## Misses worth naming

1. **Environment facts were gathered late.** JDK, SDK platforms, system images, AVDs and the
   Studio version should be one preflight inventory at the start, not discovered piecemeal.
2. **IDE compatibility was never a stated gate.** "Builds on the command line" was treated
   as "works for the creator". It is not.
3. **A decision record was missing from the working branch.** ADR 0004 existed only on a local
   side branch while three documents called it Accepted. Link-checking the docs found it;
   a branch and reference audit would have found it first.
4. **Spoken terms were ambiguous.** "Room Hill or Mausi", "the Mark … as a dependency injection
   framework" had to be interpreted (Room, Hilt, Mockk). A glossary-confirmation line in the plan
   is cheap insurance.
5. **Sub-agents changed a device the creator owns.** One left auto-rotate off on the creator's
   emulator. Rule: sub-agents may only drive devices the orchestrator started, and must restore
   any setting they change.
6. **Concurrency on one laptop is not free.** Two emulators plus Gradle coincided with the install
   hang. Parallelize by device and by file-only work, and serialize anything that starts an emulator.

## What worked and should be kept

- Read the spec for decisions before choosing anything; most of the foundation was already determined by it.
- Distrust a stale branch's claims; rebuild fresh and verify.
- Phase gates with a commit per phase, so a later fix never has to untangle an earlier one.
- Reading primary evidence on failure (test XML, resolved dependency tree, logcat) before editing.
- A mutation check on the one test that guards a subtle manifest rule (it fails when the rule is removed).
- Recording platform facts with the evidence (`api`, `sqlite_version()`, journal mode).
- An independent, read-only verifier with explicit "known and accepted" warnings, so it does not
  report noise, and a fixed report format.
- Google's `agp-9-upgrade` guardrails (KSP and Hilt minimums, built-in Kotlin, no kapt) were
  useful; project documents still decided anything they covered.

## Proposed harness additions

Names follow the harness convention (`ht-` prefix, canonical bundle in `.agents/skills/`, thin
Claude entry point, index entry). Each is a proposal, not a commitment.

| # | Proposal | Type | Purpose | Reuse / dependency | Priority |
| --- | --- | --- | --- | --- | --- |
| 1 | `ht-android-foundation` | Skill + conversation role (`foundation-engineer-agent`) | The phased method used here: preflight, requirement matrix from the spec, version resolution, build in gated phases, device tiers, close-out record | Pairs with `agp-9-upgrade`, `testing-setup`, `android-cli` (subordinate to project docs) | High |
| 2 | `references/troubleshooting.md` inside #1 | Reference | Symptom → cause → evidence → fix for the table above; grows with each new project | Part of #1 | High |
| 3 | `ht-foundation-verification` | Skill + read-only role (`foundation-verifier-agent`) | The handoff prompt as a reusable method: static review against the spec, build/test commands, app run per device, fixed report format, "known and accepted" list | Optional per-device fan-out only when the creator asks for sub-agents | High |
| 4 | `ht-device-tiers` | Skill (with scripts) | List and install arm64 images, create an AVD without `avdmanager`, boot headless, pin by serial, install with timeout and retry, capture `SqlEvidence`, clean up | Used by #1 and #3 | Medium |
| 5 | `ht-foundation-audit` | Skill | Read-only readiness audit of an existing repo: spec requirement vs. present/missing/conflicting, branch and ADR reference audit, environment and IDE inventory | Feeds #1; overlaps the dry-run branch's `ht-implementation-planning` | Medium |
| 6 | `ht-skill-vendoring` | Skill | Vendor a third-party skill set into `.agents/skills` and `.claude/skills`: scratch clone, flatten, thin entry points, exact exceptions, provenance record, `git add -n` completeness check | The checker's `vendored_references` support | Low |
| 7 | Templates | Templates | Foundation plan, environment inventory, version compatibility matrix, verification report, foundation record | Under `.agents/templates/` | With #1 |

### Suggested shape of `ht-android-foundation`

Phases, each with an entry check, a gate and a commit:

0. **Preflight (read-only):** JDK, Android SDK platforms and build-tools, system images, AVDs,
   Android Studio version and its supported AGP range, wrapper and catalog state, branches, ADR and
   document reference check. Ask the creator for anything that cannot be read (for example the IDE).
1. **Requirement matrix:** every library, tier, manifest and privacy rule the spec states, each with a
   source link; confirm ambiguous spoken terms before planning.
2. **Version resolution:** pick versions from Maven metadata and release-note compatibility tables, apply the
   IDE gate, then read the resolved Kotlin version and align dependent plugins.
3. **Toolchain → dependencies → app skeleton → test tiers:** a gate after each (`help`, assemble, tests).
4. **Device tiers:** per `ht-device-tiers`; minimum-API evidence only counts on the mandatory API.
5. **Close-out:** verification table, platform evidence, known warnings, next implementation order,
   independent verification handoff.

Scripts worth bundling: a compatibility check (AGP release notes → Gradle/JDK/API minimums), an AVD
creator, a retrying installer, an evidence collector.

## Rules to codify

- A version is not "chosen" until the IDE, Gradle, JDK, compileSdk and Kotlin gates all pass.
- Read the specific failing artifact (test XML, `buildEnvironment`, logcat) before changing anything.
- Never mock `Context`, Room DAOs or the repository in rule tests; Mockk only where the LLD allows it,
  with the scope sentence quoted in the test's comment.
- API-24 SQL claims need an API 24 run; log API level and `sqlite_version()` with it.
- Verifier and sub-agents: read-only on the repository, driven devices pinned by serial, never touch
  a device the creator started, restore any device setting they change.
- Serialize anything that boots an emulator; parallelize file-only work and per-device checks.

## Open decisions for the creator

1. **Generic or project-specific?** `ht-` skills are this repo's. A foundation skill is useful in any
   Android project; keep it here for now, or also publish a prefix-free copy later?
2. **Reuse the dry-run branch's `ht-implementation-planning`?** It already has a role and template for
   slice plans. A foundation plan is the "phase 0" case; merge them rather than add a second planner?
3. **How strict should the verifier role be about sub-agents?** The authoring standard says a role guides
   the current agent and a native sub-agent configuration is created only when explicitly required.
4. **CI:** should the foundation include a host-test and lint workflow? It was deliberately left out here.
5. **Where do AVD definitions live?** They sit in `~/.android`, outside the repo. A script in the
   skill bundle is the repeatable answer; confirm that is acceptable.

## Suggested order

1. #1 and #2 together, using this session as the first worked example.
2. #3, converting the handoff prompt that was written during this session.
3. #4 and #7.
4. #5, after deciding open question 2.
5. #6 last; it is the least tied to Android foundation work.

## Resolution

The creator decided, and the harness was built accordingly:

1. **Naming:** the foundation skills stay project-specific with the `ht-` prefix; no generic copy.
2. **Planner:** the legacy `ht-implementation-planning` skill is not used or merged; foundation planning
   lives in `ht-android-foundation` with its own plan template.
3. **Sub-agents:** native sub-agents are created only when the creator asks; roles guide the current agent.
4. **CI:** no CI workflow for host tests or lint for now.
5. **AVD definitions:** living under `~/.android` is accepted; creation is scripted in `ht-device-tiers`.

Built: `ht-android-foundation` (with the foundation-engineer role, version-resolution and troubleshooting references,
and an AGP compatibility script), `ht-foundation-verification` (with the foundation-verifier role), `ht-device-tiers`
(four tested scripts), `ht-foundation-audit`, `ht-skill-vendoring` (with a tested completeness check), and six templates.
Static checks pass (47 workflows). The runtime discovery and invocation probes passed for all five skills on both Codex
and Claude Code. Behavioral testing of the workflows themselves has not been done; the first real use is the test.

