# Troubleshooting

Known symptoms from laying an Android foundation, with cause, how it was found, and the fix. Read the
evidence named in the third column before applying a fix. Add a row when a new symptom is solved.

| Symptom | Cause | Evidence to read | Fix |
| --- | --- | --- | --- |
| Android Studio: "project is using an incompatible version of the Android Gradle plugin" | AGP is newer than the IDE supports. The command line still builds. | The IDE message; Google's Studio-to-AGP table; the [AGP compatibility script](../scripts/agp-compat.sh) | Pin AGP to the highest version the IDE supports, or update the IDE. Check the IDE before choosing AGP next time. |
| Build works but the Compose compiler plugin and the Kotlin on the classpath differ | The template pinned the Compose plugin to an older Kotlin than the one AGP's classpath resolves | `./gradlew buildEnvironment`, look for `kotlin-gradle-plugin:<old> -> <new>` | Set the catalog's Kotlin/Compose plugin version to the resolved Kotlin version |
| Every Robolectric test fails with `IllegalAccessException ... jdk.internal.access.SharedSecrets` | Robolectric's FileDescriptor interceptor reflects into a JDK 21 internal package | `app/build/test-results/testDebugUnitTest/*.xml` | In `testOptions`, `unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }` for the unit-test JVM only |
| Host tests that read the merged manifest or resources fail or see defaults | Robolectric is not given merged resources | Test failure text | `unitTests.isIncludeAndroidResources = true` |
| `kapt` plugin or `org.jetbrains.kotlin.android` plugin errors on AGP 9 | AGP 9 has built-in Kotlin and does not support the kapt plugin | The `agp-9-upgrade` skill's KSP/kapt reference | Use KSP for Room, Hilt and AndroidX Hilt; apply no Kotlin Android plugin; remove obsolete AGP flags |
| Hilt or KSP fails to configure on AGP 9 | Versions below the AGP 9 minimums | Processor release notes; the `agp-9-upgrade` skill (KSP 2.3.6+, Hilt 2.59.2+) | Raise the processors and re-run the build |
| Lifecycle or other AndroidX requires a newer compileSdk than the template | The library's AAR metadata demands it | Build output naming the AAR metadata check | Raise compileSdk to what the installed platform and AGP support; leave minSdk and targetSdk alone |
| Manifest merger note: "tagged to remove other declarations but no other declaration present" | Informational merger quirk for a `tools:node="remove"` entry | Read the merged manifest, not the warning | Confirm the unwanted entry is absent and the others (for example the process-lifecycle initializer) are present; guard it with a test |
| WorkManager initializes twice or ignores the Hilt worker factory | The default `WorkManagerInitializer` still runs | Merged manifest provider metadata | Make the Application a `Configuration.Provider` using `HiltWorkerFactory` and remove only `androidx.work.WorkManagerInitializer` from `androidx.startup.InitializationProvider` |
| `avdmanager` fails with `ClassNotFoundException: javax.xml.bind...` | The SDK's legacy `tools/bin/avdmanager` needs JAXB, which JDK 11+ removed | The stack trace | Create the AVD definition files directly with the [device-tier script](../../ht-device-tiers/scripts/create-avd.sh) |
| No API 24 managed device | Gradle Managed Devices start at API 27 | The managed-devices documentation | Use a regular AVD for the mandatory minimum API and pin the run by serial |
| API 24 image does not run on an Apple-silicon Mac | x86 images need an x86 host | `uname -m`; `android sdk list --all` | Use the `arm64-v8a` image (for example `system-images;android-24;default;arm64-v8a`) |
| First `adb install` after a fresh API 24 boot hangs for minutes | Intermittent `dex2oat` hang, asleep on a futex; a retry takes seconds | `adb logcat` (`dex2oat`), `ps` on the device | Install through the [retrying installer](../../ht-device-tiers/scripts/install-with-retry.sh); avoid a second emulator or a Gradle build during the first install |
| Vendored or generated directories silently missing from a commit | A root `.gitignore` pattern such as `build/` matches a directory inside them | `git status --ignored`; compare `git add -n` count with files on disk | Add an exact un-ignore for that directory |
| zsh: `$VAR:path` mangled, or a bare `=====` runs as a command | zsh history-style modifiers and `=` expansion | The odd error text | Brace variables (`${VAR}:path`) and quote separators |
| Instrumented run prints `additionalTestOutput is not supported ... API level 24` | Benign: the directory does not exist on API 24 | Test result XML shows passes | No action |
| `Unable to strip libandroidx.graphics.path.so` | Benign packaging note | Build succeeds | No action |
| Lint warns that `dataExtractionRules` needs `fullBackupContent` | Intentional when backup is disabled and the architecture says to omit `fullBackupContent` | The architecture's backup row | Keep it; list it as an accepted warning |
| Lint says a newer AGP exists | Expected when held back for the IDE | Compatibility matrix | List it under held-back versions |
| A smoke test mocks a type the documents forbid mocking | The test followed "any interface" instead of the documented scope | The LLD's real-versus-fake rules | Mock a test-local stand-in port; quote the scope sentence in the test comment |
| A test passes even without the rule it guards | The test is vacuous | Remove the rule and re-run | Keep only tests that fail when the guarded rule is removed |
| A sub-agent left a device setting changed | It changed a setting on a device the creator started | The device's `settings get` | Sub-agents drive only devices the orchestrator started and restore every setting they change |
