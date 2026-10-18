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

Read the shared [AI harness index](.agents/README.md) to select the applicable
workflow, then follow its linked canonical skill, role, and templates. The index
is the shared starting point for Codex and Claude; keep workflow routing there
and method instructions in their existing skills.

## Useful commands

Run commands from the repository root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

For device or emulator tests, use:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Before handing work back

- Run the narrowest relevant Gradle checks when the local Android toolchain is available.
- Summarize changed files, validation performed, and anything that still needs a device, emulator, or product decision.
