---
name: ht-device-tiers
description: "Set up and run the instrumented device tiers of an Android project: find an arm64-compatible system image for the mandatory minimum API, create and boot an AVD without avdmanager, install with timeout and retry, run suites pinned to a serial, and capture platform SQLite evidence. Use when a document requires minimum-API device evidence."
---

# Device Tiers

Provide the real-device evidence that approved documents require, such as "SQL guards and migrations
count only after they pass on the minimum API". The method turns "we need an API 24 device" into a
repeatable procedure on this machine, including Apple-silicon hosts where x86 images do not run.

Use it from [the foundation method](../ht-android-foundation/SKILL.md), from
[verification](../ht-foundation-verification/SKILL.md), or whenever an instrumented tier must run.
This is a repository-local skill. Native sub-agents are used only when the creator asks.

## Tier model

| Tier | Device | How it runs |
| --- | --- | --- |
| Newest API | A Gradle Managed Device declared in the module (managed devices start at API 27) | `./gradlew :app:<device>DebugAndroidTest` |
| Mandatory minimum API | A regular AVD, because the minimum API is below managed-device support | Boot it, then `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest` |

Select the minimum-API device explicitly by serial so a connected higher-API device can never satisfy
the minimum-API job. The same instrumented suites run on both tiers.

## Procedure

1. **Find an image that matches the host.** List with `android sdk list --all 'system-images*'` and pick
   `arm64-v8a` on Apple silicon (x86 images need an x86 host). Prefer the AOSP `default` tag for platform
   SQLite evidence: it carries the platform's own SQLite and no Google services. Use `google_apis` only if the
   tests need it. Install: `android sdk install "system-images;android-<api>;default;arm64-v8a"`. Ask the creator
   before a large download if they have not approved one.
2. **Create the AVD** with [the AVD script](scripts/create-avd.sh). It writes the two definition files directly
   because the SDK's legacy `avdmanager` cannot run on JDK 11+. It refuses to overwrite an existing AVD. The
   definition lives under `~/.android/avd` (outside the repository), which the creator has accepted.
3. **Boot it** with [the boot script](scripts/boot-avd.sh). It starts the emulator headless, waits for boot, and
   reports the serial it started. Boot one emulator at a time, and do not run a Gradle build during the first install.
4. **Install** with [the retrying installer](scripts/install-with-retry.sh). A first install after a fresh boot can
   hang in `dex2oat`; the script times out and retries.
5. **Run the suite pinned to the serial**: `ANDROID_SERIAL=<serial> ./gradlew :app:connectedDebugAndroidTest`. The run
   installs and then uninstalls the app, so run it last on that device.
6. **Capture evidence** with [the evidence script](scripts/sql-evidence.sh), pointed at the Gradle results directory.
   Record API level, platform SQLite version and journal mode. Evidence for the minimum API counts only from that API.
7. **Clean up.** Stop only the emulators you started (`adb -s <serial> emu kill`) and confirm `adb devices` shows only
   devices you did not start.

## Rules

- Never touch, reconfigure or stop a device the creator started. Drive devices only with `adb -s <serial>`.
- Restore every device setting you change (for example auto-rotate).
- Serialize emulator boots; running several emulators plus Gradle at once starves the host and has coincided with
  install hangs. Parallelism is for file-only work, or for per-device checks when the creator asks for sub-agents.
- Do not report a device tier as passed from a run on a different API level.

## Stopping boundary

Stop when each required tier has passed on its own device with recorded platform evidence, or when a concrete blocker
(no image, boot failure) is reported with the log path.
