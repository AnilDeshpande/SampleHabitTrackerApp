# Android implementation upskill retro — 2026-11-13

Status: the proposals below were built in the same change as this retro (see "Resolution"). They are
awaiting the creator's review; their behavior is untested until the next increment uses them.
Purpose: turn the session that built the first feature increment (screens S1 to S3) into harness
improvements, and give the implementation phase a method from "a set of approved screens" to "a
reviewable pull request". The previous retro covered the foundation:
[android-foundation-upskill-retro-2026-10-29.md](android-foundation-upskill-retro-2026-10-29.md).

Record of the work itself: [increment record](../implementation/increment-1-record.md) and
[increment plan](../implementation/increment-1-plan.md). Pull request:
[#3](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3) (six commits, 115 files).

## Outcome

From a foundation whose only screen said "Hello Android!" to a working feature: S1, S2 and S3 with every
state of S1 and S2, built bottom-up in four verified layers. 146 host tests; 42 device tests passing on each of
API 24, 34 and 36; the real app driven on emulators (create loop, validation, discard dialog, process-death
restore, dark mode, 200% font scale); a pull request written as a review handoff. Two creator corrections
steered the work and are the most valuable inputs to this retro (see "Misses worth naming").

## How the session actually went

| Step | What we did | How we found out what was needed |
| --- | --- | --- |
| 1. Orient | Read the harness index, a 108 KB prototype handoff, the architecture, the LLD and the foundation record | Asked the creator only for the decision (which screens); the documents decided the rest |
| 2. Get a look | The handoff had no visual design. Exported the two sketch pages with the draw.io CLI, built a Material 3 mockup as HTML, rendered it with headless Chrome, showed light and dark | The creator asked for images first; the first palette (template purple) was rejected, green chosen |
| 3. Fix the order | First plan was screen-first. The creator required domain rules, then database and repository, then execution, then ViewModels and UI, with UI state models designed up front | The creator's feedback; the architecture and LLD already contained everything needed (state ownership table, commands, schema, seams) |
| 4. Plan, approve, branch | Saved a plan with decisions; cut a feature branch because the creator forbade commits on `main` | Plan approval, then one commit per layer |
| 5. Build layer by layer | Each layer: short design note, code, named tests, a mutation check on the subtle rule, device evidence where SQL or UI is involved, commit | The architecture's traceability and verification tables named the cases |
| 6. Device tiers | API 24 AVD (mine), API 34 AVD (the creator's, already running), API 36 managed device | Serial pinning kept the creator's device out of the minimum-API evidence |
| 7. Look at it | Installed the debug app and drove it with `adb`: every state, process death from Recents, dark mode, 200% font | Screenshots read one by one; two bugs found only this way |
| 8. IDE problem | The creator could not Run from Android Studio. I could not see the IDE | Asked for the symptom, read Studio's own log, reproduced Studio's exact Gradle command |
| 9. Pull request | Push, PR with before/after tables, review order, verification, decisions, gaps | The creator asked for a handoff, not a generic template |

## Failures and fixes

| Symptom | Root cause | How it was found | Would have been prevented by |
| --- | --- | --- | --- |
| "Habit saved" snackbar never seen on the first creation | Its timer ran while the system notification-permission dialog covered it | A device screenshot; no test or ViewModel could show it | A validation rule: check transient UI against system dialogs on a device; sequence effects explicitly |
| After Custom then Daily, state kept the old weekday mask | Two separate saved-state writes produced an inconsistent intermediate emission | A ViewModel test that read state immediately | Deriving state so every emission is consistent, plus a per-emission consistency test |
| 63 and then 102 tests passed on the first run | Suspicion, not evidence, that they guard anything | A mutation check: removing the planner's overlap rule failed exactly the two timezone tests; removing a trigger's NULL handling failed the guard test | Making "mutate the riskiest rule once per layer" a layer gate |
| SQL trigger would have let a NULL `result` through | Three-valued logic: `NOT (… IN …)` is NULL, so the trigger never fires | Designed in advance, then proven by the mutation above | A rule: every guard condition is NULL-safe and has a NULL-input test |
| Room `MigrationTestHelper` failed with `AbstractMethodError` | AGP consistent resolution pinned `kotlinx-serialization-core` to 1.7.3 from a lifecycle dependency | `dependencyInsight` showed the constraint's source | A troubleshooting row; constraint with a `because` note |
| Compose would not compile `Icons` | `material-icons-core` is not a transitive dependency of Material 3 | Compiler output | A library-trap row in troubleshooting |
| Three form tests failed on API 24 and API 36 but passed on API 34 | Tests asserted items were displayed without scrolling; the smaller devices scroll | Same tests on three tiers; failure text | A rule: run Compose tests on the smallest-screen tier and scroll before asserting |
| `assertExists` on the floating button failed | Its text lives only in the unmerged semantics tree | Failure text named it | `useUnmergedTree = true` guidance |
| JUnit: "Method … should be void" | `= runBlocking { … }` ended with an expression that returned a value | Test class initialization error | Ending such tests with an assertion |
| Ticker test timed out after "30 s" instantly | A virtual-time `withTimeout` wrapped real database I/O | Failure message said "virtual" time | A rule: never mix virtual-time timeouts with real I/O in the same test |
| A zone-id assertion failed | `ZoneOffset.UTC.id` is `"Z"`, not `"UTC"` | One failing assertion | Use `ZoneId.of("UTC")` when the id is asserted |
| Draft not restored after a "kill" | `monkey` relaunch starts a new task; it does not restore the retained one | The app opened on Overview | Reopen from Recents after `am kill` of a backgrounded app |
| Scripted typing produced "Reading" for "reading", and Enter behaved differently on API 24 | The IME auto-capitalizes and maps Enter differently per image | Screenshots | Read coordinates from `uiautomator dump`, not from a scaled screenshot; do not rely on IME actions in scripts |
| Taps landed wrongly | Screenshots were displayed scaled (×1.21) | Arithmetic on every tap | `ui-find.sh` prints device-pixel centres |
| First draw.io export was the wrong page | This CLI numbers pages from 1 (0-based before v27.0.2) | The picture | Check the page index against the page name list |
| A shell step silently did nothing | macOS `sed -i` needs an argument; `grep` is `ugrep` here and misreports `-c`; zsh runs a bare `=====` | Empty output | Use Python for file edits and log analysis |
| "Run is disabled / sync banner" in Android Studio | Studio's model was last synced before build files changed, and auto-reload is `NONE` | `studio-events.py`: last sync 15:26, build files edited later, `autoReloadType NONE` | A preflight and close-out step: after any Gradle change outside the IDE, ask the creator to sync |
| Studio log looked empty of errors | The log is INFO level; deploy and sync events are INFO. The installer-daemon pipe errors are benign | Reading deploy sequences in full | The events script, with the benign lines named |

## Misses worth naming

1. **The first plan was screen-first.** The harness had no implementation-phase method, so nothing said "bottom-up,
   state models first". The creator had to say it. This is the main gap the new skills close.
2. **The design arrived mid-session.** The prototype handoff said the visual phase was skipped. A look was needed before
   the UI layer, and the first palette was a default, not a question. Ask for palette and dynamic colour at planning time.
3. **Interpretations accumulate silently.** Eight small decisions (number parsing, a floating action, a label) were
   made because the documents were silent. They are listed in the PR, but the harness has no register for them and no
   path back into the documents.
4. **Tests assumed one screen size.** The tiers differ in height; the failures appeared only on the smaller ones.
5. **The IDE symptom was diagnosed from logs.** The first reads of Studio's log used a tool that misreports, and the
   creator's actual symptom was asked for only after a long stretch of inference. Ask for the symptom first, and read the log
   with a script.
6. **A creator-owned device was used for read-only checks.** Nothing was changed, but device ownership was not declared
   up front. Declare it at the start of validation.
7. **Android tests do not run in CI.** The PR says so; the harness had no stance on it. The creator decided they should (see "Decisions").
8. **Spec drift is unowned.** The LLD still says "manual `AppContainer`" in places; implementation found no place to
   record corrections to the documents.

## What worked and should be kept

- Reading the architecture and LLD for the decisions before designing anything: most of the increment was already specified.
- Plan first, with the decisions as a table the creator can approve in one answer.
- A short design note per layer, then one commit per layer, so the pull request reads as a sequence.
- Named test cases taken from the architecture's traceability table, with shared scenarios so the host and device tiers
  assert the same things.
- A mutation check on the one rule that is easy to get subtly wrong.
- Looking at every state on a real device and reading each screenshot; this found a bug no test could.
- Process death tested the documented way (background, `am kill`, reopen from Recents).
- Pinning every device command by serial and killing only emulators I started.
- Showing the creator pictures at each decision point instead of describing them.
- A pull request whose tables separate "before" from "after", name the review order, and say what is not covered.

## Proposed harness additions

All follow the harness convention (`ht-` prefix, canonical bundle under `.agents/skills/`, thin Claude entry point,
index entry). The previous retro noted that the legacy `ht-implementation-planning` was not to be reused; these names
avoid it.

| # | Proposal | Type | Purpose | Priority |
| --- | --- | --- | --- | --- |
| 1 | `ht-increment-planning` | Skill + two templates | Approved screens in, approved increment plan out: scope cut by journey, layer plan, screen-state map, design lock, interpretations, decisions | High |
| 2 | `ht-increment-implementation` | Skill + role (`implementation-engineer-agent`) + template | The whole flow from approved plan to record: layered build with gates, commit per layer, evidence rules, hand-offs | High |
| 3 | `ht-screen-implementation` | Skill | The UI layer: state models, restorable input, derived validation, effects, navigation results, accessibility, tests on small screens and large fonts | High |
| 4 | `ht-emulator-validation` | Skill + three scripts + reference | Run it and look at it: device ownership, scripted driving, state checklist, process death, evidence curation, Android Studio diagnosis | High |
| 5 | `ht-pr-handoff` | Skill + template | A pull request that is a review handoff with before/after tables | High |
| 6 | Refinements | Edits | `ht-device-tiers`, `ht-android-foundation`, troubleshooting reference, `ht-foundation-audit`, environment inventory, the index and `AGENTS.md` | Medium |

Considered and not built: a prefix-free generic copy of the skills. Built after the creator's answers: an Android CI workflow and a
read-only verifier role with its templates (see "Decisions").

### The flow from screens to pull request

| Phase | Skill | Gate to leave the phase |
| --- | --- | --- |
| 0. Intake and preflight | `ht-increment-planning` (with `ht-foundation-audit` when the repository is unfamiliar) | Screens named by S-number; documents read; environment and IDE state known; branch is not `main` |
| 1. Plan | `ht-increment-planning` | Scope cut, state map, design lock, interpretations and decisions approved by the creator; plan saved |
| 2. Layers 1 to 3 | `ht-increment-implementation` | Per layer: named tests green, a mutation check, device evidence where SQL is involved, one commit |
| 3. Layer 4 | `ht-screen-implementation` inside `ht-increment-implementation` | ViewModel and Compose tests green on every tier, including the smallest screen |
| 4. Look at it | `ht-emulator-validation` | Every included state seen on a device; process death, dark mode and large font checked; evidence curated |
| 5. Record | `ht-increment-implementation` | Increment record written with decisions and what is not covered |
| 6. Handoff | `ht-pr-handoff` | Pull request opened with verified numbers; CI state reported; follow-ups offered |

## Rules to codify

- Branch first; never commit to `main`; one commit per layer.
- Plan before code; the plan carries a decisions table the creator approves in one answer.
- Design the state models before the screens; classify each value as durable, restorable, derived, transient or visual-only.
- Look at the app on a device before claiming a screen works; read each screenshot.
- A test that passes first time needs a mutation check on the rule it guards.
- Tests that touch the UI run on the smallest-screen tier and scroll before they assert.
- Never mix virtual-time timeouts with real I/O; use real-time waits for database work.
- After any Gradle file change outside the IDE, tell the creator to sync, and verify Studio's state from its log.
- Do not touch a creator-started device; declare device ownership before validating.
- Interpretations go in a table, in the PR, and (when they change a document) back to the document owner.
- Pull request numbers come from commands, not memory.
- Evidence screenshots stay out of the repository and are attached to the pull request.
- Pull requests are not stacked; they are merged only after review and the creator's approval, never by the agent.
- Document corrections come in two kinds: sync corrections inside the increment, substantive ones in a separate document change.
- An independent read-only verifier sub-agent is allowed for the device check; no other sub-agent unless the creator asks.

## Decisions (answered 2026-11-13)

| # | Question | Creator's answer | What changed in the harness |
| --- | --- | --- | --- |
| 1 | Run Android tests in CI? | Yes | New [`android-tests.yml`](../../.github/workflows/android-tests.yml): assemble, host tests and lint, plus the instrumented suite on API 24 and API 34. This reverses the foundation retro's "no CI for now". `AGENTS.md`, the device-tier skill and the PR template state what CI runs |
| 2 | Evidence screenshots in the repository? | In the pull request only | [Emulator validation](../../.agents/skills/ht-emulator-validation/SKILL.md) keeps them outside the repository; [PR handoff](../../.agents/skills/ht-pr-handoff/SKILL.md) attaches them to the pull request (the GitHub CLI cannot, so the creator drags them in, or the browser tool does with an explicit yes); the record and PR template list them by caption |
| 3 | Fix stale LLD or ADR text inside an increment or separately? | Both | Two kinds, in the implementation and planning skills, the record and PR templates: **sync corrections** (made true to what was built) go in the increment in their own commit and are listed in the PR; **substantive corrections** (behavior, a contract, an ADR decision) go to a separate document change with the creator's approval |
| 4 | Stack pull requests? | No: wait for review, then approval, and merge only when approved | Planning, implementation, PR handoff, the role, `AGENTS.md` and both templates: branch from an up-to-date `main`, no stacking, the agent never merges, enables auto-merge or pushes to `main` |
| 5 | Allow a read-only verifier sub-agent? | Yes | New [Implementation Verifier Agent](../../.agents/roles/implementation-verifier-agent.md) with a [handoff prompt](../../.agents/templates/increment-verification-prompt-template.md) and a [report template](../../.agents/templates/increment-verification-report-template.md); the independent-check mode of emulator validation and the engineer role name it as the one permitted sub-agent. Other sub-agents still only when asked |

## Resolution

The creator asked for the learnings to become part of the harness and for implementation-phase skills, and then answered the five
open decisions. Everything above was built in this change (branch `harness/implementation-upskill-retro`).

Built: five skills ([planning](../../.agents/skills/ht-increment-planning/SKILL.md),
[implementation](../../.agents/skills/ht-increment-implementation/SKILL.md),
[screens](../../.agents/skills/ht-screen-implementation/SKILL.md),
[emulator validation](../../.agents/skills/ht-emulator-validation/SKILL.md),
[PR handoff](../../.agents/skills/ht-pr-handoff/SKILL.md)); two roles
([implementation engineer](../../.agents/roles/implementation-engineer-agent.md),
[implementation verifier](../../.agents/roles/implementation-verifier-agent.md)); six templates; three tested scripts; one reference;
the Android CI workflow; and refinements to the existing skills, the troubleshooting reference, the inventory template, the index and
`AGENTS.md`.

Validation, by the standard's four categories:

| Category | Result |
| --- | --- |
| Static checks | Passed: 46 harness unit tests; checker 52 workflows (47 before), 1,467 local references, 85 anchors; whitespace check clean. The checker also caught a missing role link while this was built; fixed |
| Fresh discovery and invocation | Passed: runtime probes on both Codex and Claude for all five new skills. After the decisions changed four of them, those four were probed again; one Codex probe failed because `ht-increment-implementation` linked two roles, and passed after the verifier role was referenced from emulator validation only |
| Scripts | Passed on a live emulator and the real Studio log: the screenshot script, the UI finder (exact, `--contains`, `--tap`, content-description, not-found exit code) and the Studio events script |
| CI workflow | Parsed as YAML; the push and pull-request path lists match; all nine actions are pinned to commit SHAs resolved from the release tags. **It has not run on GitHub.** The steps it runs were run locally (assemble, unit tests, lint, and the device suite on API 24, 34 and 36 arm64), but the hosted runner's x86_64 emulator images, KVM setup and SDK license step are unverified until its first run |
| Behavioral testing of the workflows | **Not run.** The first real use, the next increment, is the test. The scripts were exercised; the planning, implementation, validation, verifier and pull-request methods were not run against a fresh scenario |

Caveats the creator should know:

- **This branch is stacked on [#3](https://github.com/AnilDeshpande/SampleHabitTrackerApp/pull/3)**, which decision 4 now forbids for future
  work. It was cut before the decision, and the retro links to the increment record that exists only on #3's branch. It should wait for #3
  to be reviewed, approved and merged, then be rebased onto `main`.
- **#3 already commits its 16 screenshots** (about 1.3 MB) under `docs/implementation/evidence/`. Decision 2 applies from now on; moving them
  into a PR comment is a separate change the creator may want.
- The new verifier role is not a native sub-agent configuration; the engineer starts the sub-agent with the handoff prompt when wanted.
