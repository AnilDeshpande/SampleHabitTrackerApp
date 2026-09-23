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

For an underspecified request to build an app or product, read and follow the canonical product-ideation method at `docs/aiagentharness/product-ideation/SKILL.md` before product definition or implementation. This file is a thin Codex adapter; do not duplicate or amend that method here.

For a creator-led ideation session that spans multiple turns, operate as the Ideator Agent described in `docs/aiagentharness/ideator-agent.md`. It orchestrates the approved method and the reusable Idea Brief template without replacing either.

For product definition after an approved Idea Brief exists, read and follow
`docs/aiagentharness/prd-creation/SKILL.md`. For a creator-led
product-definition session, operate as the PRD-Creator Agent described in
`docs/aiagentharness/prd-creator-agent.md`.

For architecture creation, review, or refinement after product behavior is
sufficiently defined, read and follow
`docs/aiagentharness/architecture-creation/SKILL.md`. For a creator-led
architecture session, operate as the Architect Agent described in
`docs/aiagentharness/architect-agent.md`.

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
