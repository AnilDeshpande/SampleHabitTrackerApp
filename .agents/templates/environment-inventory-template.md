# Environment inventory — <project> — <date>

Read-only snapshot taken before choosing any version or device. Record what was
observed and how; mark anything that could not be read as **ask the creator**.

## Host

| Item | Observed | How observed |
| --- | --- | --- |
| OS and CPU architecture | | `uname -a` |
| JDKs available / daemon JDK required by the build | | `/usr/libexec/java_home -V`, wrapper and daemon JVM properties |
| Gradle distributions already cached | | `~/.gradle/wrapper/dists` |
| Package or toolchain managers needed (for example Homebrew, `android` CLI) | | |

## Android SDK and devices

| Item | Observed | How observed |
| --- | --- | --- |
| SDK location and `local.properties` | | |
| Installed platforms and build-tools | | |
| Installed system images (API, tag, ABI) | | |
| Installable system images for the mandatory minimum API (ABI matches the host) | | `android sdk list --all` |
| Existing AVDs and which are owned by the creator | | `emulator -list-avds` |
| Devices currently connected | | `adb devices` |

## IDE

| Item | Observed | How observed |
| --- | --- | --- |
| Android Studio name, version and build | | **ask the creator** if no bundle is found |
| Highest AGP that this Studio supports | | Google's Studio-to-AGP table |

## Repository

| Item | Observed |
| --- | --- |
| Branch, clean/dirty, remote state | |
| Other branches that hold relevant work (list, do not trust their claims) | |
| Current Gradle wrapper, AGP, Kotlin, compile/min/target SDK | |
| Plugins, libraries and test source sets present | |
| Tests present, by tier | |
| Documents that reference files which do not exist | |

## Constraints that gate version choice

List each (for example "IDE supports AGP up to X", "JDK 17 minimum") and its source.
