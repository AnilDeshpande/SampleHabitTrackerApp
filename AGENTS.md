# Sample Habit Tracker — Codex Guide

## Project snapshot

- Android app written in Kotlin with Jetpack Compose and Material 3.
- The primary module is `:app`.
- Application package: `com.codetutor.samplehabittrackerapp`.
- Minimum supported Android version: API 24.

## Working conventions

- Keep changes focused on the requested feature or fix; preserve unrelated local work.
- Prefer idiomatic Kotlin and Compose. Keep UI state explicit and make composables small and testable.
- Put production Kotlin under `app/src/main/java/` and unit tests under `app/src/test/java/`.
- Add or update tests when behavior changes, especially for business logic and state transformations.
- Do not add dependencies, change SDK levels, or alter Gradle configuration unless the task requires it.

## AI harness

Before creating, editing, moving, or renaming skills, roles, templates, supporting
resources, or harness entry points, read the shared [authoring standard](.agents/authoring.md)
and complete its validation and evidence checklist before handoff.

Read the shared [AI harness index](.agents/README.md) to select the applicable
workflow, then follow its linked canonical skill, role, and templates. The index
is the shared starting point for Codex and Claude; keep workflow routing there
and method instructions in their existing skills.

For repository orientation, feature planning, implementation impact, debugging,
code review, test discovery, documentation, architecture, and handoffs, start
with the shared [Graphify guidance](.agents/skills/ht-graphify/SKILL.md) when a
current graph is available. Use graph results to locate evidence and verify
material conclusions against current source. If the graph is unavailable or
stale, state that limitation and continue with direct inspection. Existing
workflow decision gates and approved documents retain their authority.

## Useful commands

Run commands from the repository root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

For instrumented tests, the API 36 tier runs on a Gradle Managed Device:

```bash
./gradlew :app:pixel2Api36DebugAndroidTest
```

The API 24 tier (mandatory for SQL guard and migration evidence) needs the
`habit_api24` AVD booted and pinned to its serial; see
[the foundation record](docs/implementation/foundation.md#api-24-on-apple-silicon):

```bash
ANDROID_SERIAL=emulator-5554 ./gradlew :app:connectedDebugAndroidTest
```

## Before handing work back

- Run the narrowest relevant Gradle checks when the local Android toolchain is available.
- Summarize changed files, validation performed, and anything that still needs a device, emulator, or product decision.
