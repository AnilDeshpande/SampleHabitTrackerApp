# Version compatibility matrix — <project> — <date>

Every version below must pass every gate before it is pinned. Stable releases only
unless a source document requires otherwise. Record the source of each fact.

## Gates

| Gate | Question | Source |
| --- | --- | --- |
| IDE | Does the creator's Android Studio support this AGP? | Studio-to-AGP table |
| Gradle | Is the wrapper at or above the AGP minimum? | AGP release-notes compatibility table |
| JDK | Do the running JDK and bytecode target meet the AGP minimum? | Same table |
| SDK | Does AGP support this compileSdk, and does every library accept it? | Same table, library release notes, a real build |
| Kotlin | Which Kotlin version is actually on the build classpath, and do dependent plugins match it? | `./gradlew buildEnvironment` |
| Processors | Are KSP/Hilt/Room at or above the minimums for this AGP? | The `agp-9-upgrade` skill, library release notes |
| Spec | Does the architecture, LLD or an ADR pin or forbid this choice? | Linked document |

## Pinned versions

| Component | Version | Latest stable seen | Gates passed | Notes |
| --- | --- | --- | --- | --- |
| Gradle | | | | |
| AGP | | | | |
| Kotlin (resolved) | | | | |
| compileSdk / minSdk / targetSdk | | | | |
| KSP | | | | |
| <library> | | | | |

## Deliberately not upgraded

Name each component held back and the gate that blocks it, so it can be raised when
the gate changes (for example after an IDE update).
