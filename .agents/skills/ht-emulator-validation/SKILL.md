---
name: ht-emulator-validation
description: "Validate an Android increment by running it on emulators and looking at it: install on named devices, drive the real app with adb, capture and read screenshots, check each prototype state, process-death restore, dark mode and large fonts, curate evidence, and diagnose why Android Studio cannot sync or run the project from its own log. Use after UI work, when the creator wants to see or run the app, or when Run in Android Studio fails."
---

# Emulator Validation

Answer "does it really work, and how does it look?" with evidence a person can see. Tests prove rules; looking at the
running app proves the experience, and it finds a class of defect tests do not (a message hidden behind a system dialog,
a layout that clips on a real screen). This method also covers the creator's own Android Studio, which cannot be seen from
here but writes a log.

Use it after [screen implementation](../ht-screen-implementation/SKILL.md), whenever the creator asks to see or install the app,
and whenever they report that Run, sync or install fails. It builds on [device tiers](../ht-device-tiers/SKILL.md). The
[Implementation Engineer Agent](../../roles/implementation-engineer-agent.md) guides the conversation. This is a
repository-local skill.

## Modes

- **Install for the creator:** build, install on the device they name, clear data if they want a first-launch view, launch,
  and say which state it is in. Do nothing else on that device.
- **Scripted validation:** drive devices you started through the state checklist and curate evidence.
- **Studio diagnosis:** find out why Android Studio cannot sync or run, from its log and the project's IDE files.
- **Independent check:** read-only; run the checks, report PASS or FAIL with evidence, never fix. The creator has approved
  running it as a **read-only native sub-agent** (the one permitted exception to "sub-agents only when asked"), guided by the
  [Implementation Verifier Agent](../../roles/implementation-verifier-agent.md). Fill the
  [handoff prompt](../../templates/increment-verification-prompt-template.md) so it carries every expected value, name the devices
  it may drive and who owns each, and take back its [report](../../templates/increment-verification-report-template.md). The
  verifier never fixes; the implementer decides, fixes, and asks for the specific check to be repeated. Rules as in
  [foundation verification](../ht-foundation-verification/SKILL.md).

## Device rules

1. **Declare ownership first** (`adb devices` plus the creator's statement): which device is theirs, which you start. Touch only
   your own. A read-only screenshot of theirs still needs their word; never tap, type, clear data or change a setting there.
2. Start your own emulator with [the boot script](../ht-device-tiers/scripts/boot-avd.sh); it reports the serial it started
   and leaves others alone. Pin every command with `adb -s <serial>`; never rely on a single default device.
3. Serialize emulator boots and first installs. Restore every setting you change (animation scales, font scale, night mode,
   rotation). Stop only emulators you started and confirm `adb devices` shows what it showed before.

## Scripted validation

### Drive and look

1. Install with [the retrying installer](../ht-device-tiers/scripts/install-with-retry.sh) or Gradle's install task with
   `ANDROID_SERIAL`; clear app data for a first-launch state. Turn animations off for scripted runs and restore them after.
2. For each row of the [screen-state map](../../templates/screen-state-map-template.md), reach the state and capture it with
   [the screenshot script](scripts/shot.sh). **Read every image.** Find controls with
   [the UI finder](scripts/ui-find.sh) (it prints device-pixel centres and can tap); never compute coordinates from a scaled
   screenshot. See [adb gotchas](references/adb-gotchas.md) for text entry, restoration and keyboard behavior.
3. Check on a device, beyond the happy path:
   - transient messages against system dialogs and permission prompts;
   - validation, duplicate, discard, save failure and empty or loading states from the map;
   - **process death:** put the app in the background, `am kill` it, reopen the retained task from Recents, confirm the same
     destination and every raw field (relaunching through `monkey` or the launcher intent does not prove restoration);
   - dark mode, 200% font scale (and that long forms scroll), and rotation;
   - the minimum-API device and a smaller screen, not only the creator's large one;
   - back behavior and that placeholder destinations open and return.
4. Scan logcat for `FATAL EXCEPTION` and ANR for the app with Python or a pipe you have tested; on this machine `grep` may be
   `ugrep` and its counts and options differ.
5. Fix what looking finds, and add a test where a test could have caught it. Record defects found only by looking.

### Evidence

Curate a small set: one image per state, plus dark mode, large font, the minimum API and process-death restore. Downscale with
the screenshot script's size argument and name by prototype S-number and state. **Do not commit them to the repository**: keep them in
a folder outside it (the session's scratch directory, `evidence-<increment>/`), attach them to the pull request as
[pull request handoff](../ht-pr-handoff/SKILL.md) describes, and refer to them from the increment record by caption only. Send the
key images to the creator.

## Studio diagnosis

1. **Ask for the symptom first**: Run disabled or a sync banner; sync fails with a message; build passes but install fails with
   a message; installs but misbehaves. The answer picks the branch below. You cannot see the IDE and may be denied screen capture
   and menu automation; do not rely on either.
2. Run [the Studio events script](scripts/studio-events.py) with the project directory. It reports the last syncs, the builds Run
   started, the deploy steps, failures that are not known-benign, `autoReloadType`, and whether any Gradle file is newer than the
   last successful sync. The Studio log is INFO level: sync, build and deploy events are in it even when no WARN or ERROR is.
3. Reproduce Studio's exact Gradle command from the log (its `-P` flags) with Studio's bundled JDK, then with the JDK the project's
   daemon criteria name. If it builds, the cause is not the toolchain.
4. Common findings and fixes, in order of likelihood:
   - **Stale model** (build files changed after the last sync, auto-reload disabled): the creator runs File > Sync Project with
     Gradle Files, and sets Settings > Build, Execution, Deployment > Build Tools > Gradle > "Reload project after changes in the
     build scripts" to Any changes.
   - **AGP newer than the IDE supports:** the log names the latest supported version; pin AGP to it.
   - **JDK mismatch:** compare the project JDK name in `.idea/misc.xml` with the IDE's JDK table, and the daemon criteria with the
     JDKs available.
   - **Benign installer-daemon pipe errors** ("Unable to write magic number to pipe"): Studio falls back to a normal install; not the cause.
5. **Do not edit `.idea` while Studio has the project open**; Studio rewrites it. Give exact click paths, or ask the creator to
   close Studio first. Report what was verified and what only the creator can do.

## Install for the creator

Build and install on the device they named; clear data if they want the first-launch state; launch; tell them the state and which
device. If they will test manually, leave the device alone afterwards and leave settings as found.

## Outputs

- Screenshots read and summarized per state, defects found by looking, and the curated evidence.
- For Studio: the events summary, the cause, and the exact action for the creator.

## Stopping boundary

Stop when every mapped state has been seen on a device (or listed as not seen, with the reason), devices are restored, and the
evidence is curated outside the repository and ready to attach to the pull request; or when the Studio cause and the creator's action
are stated.
