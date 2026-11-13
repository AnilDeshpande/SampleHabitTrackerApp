# adb and tooling gotchas

Observed while driving this project's app. Read the evidence in the third column before applying a fix. Add a row when a new
one is found.

| Symptom | Cause | Evidence | What to do |
| --- | --- | --- | --- |
| Taps land in the wrong place | Screenshots are shown scaled; coordinates multiplied by hand drift | The tap hits the neighbor | Use [the UI finder](../scripts/ui-find.sh), which reads centres from `uiautomator dump` |
| The UI finder finds nothing | The app is not in the foreground (launcher or another app is), or the element is scrolled out of view | `dumpsys window \| grep mCurrentFocus`; the dump's `package=` | Launch or scroll first. Compose text and content descriptions are in the dump |
| `adb shell input text reading` shows "Reading" | The keyboard's sentence capitalization | Screenshot | Type what the field would show, or assert case-insensitivity in the repository tests instead |
| `input keyevent 66` does not move to the next field on one API level | IME actions are mapped differently per system image | The focus ring stays put | Tap fields by position from the finder; do not depend on IME actions in scripts |
| The draft is gone after killing the app and relaunching | `monkey` and the launcher intent start a new task; they do not restore the retained one | App opens on the start destination | Background the app, `am kill <package>`, check the pid is gone, then reopen from Recents (`KEYCODE_APP_SWITCH`, tap the card) |
| The process is still alive after `am kill` | `am kill` is documented to kill only processes that are safe to kill, which means backgrounded ones | `pidof <package>` still returns a pid | Press Home first, then `am kill`, then confirm `pidof` is empty |
| Instrumented run removes the app you installed by hand | The test run installs and then uninstalls the app | `pm list packages` | Reinstall afterwards; do not run a suite on a device the creator is using |
| UI tests pass on one device and fail on another | A taller screen shows content that a shorter one scrolls | Failure says "is not displayed" | Scroll before asserting; run Compose tests on the smallest-screen tier |
| `assertExists` fails but the unmerged tree has the node | A button merges its children's semantics | Failure text suggests `useUnmergedTree` | Look it up with `useUnmergedTree = true` |
| Screenshot is a different size than the device reports | Downscaled by the script, or by the viewer | `sips -g pixelWidth` | Keep device-pixel coordinates from the finder, never from images |
| Settings you changed on the emulator persist | They are device state | `settings get global window_animation_scale` etc. | Restore: animation scales to 1, `font_scale` 1.0, `cmd uimode night no` |
| `draw.io` export gives the wrong page | This CLI numbers pages from 1 (0-based before v27.0.2) | The exported image | Match the page index to the page-name list |
| Empty output from a log search | `grep` may be `ugrep` with different `-c` and option behavior; zsh runs a bare `=====` | The odd error text | Use Python for log analysis, and quote separators |
| `sed -i` fails on macOS | BSD `sed` needs `-i ''` | Usage error | Edit with Python, or `sed -i ''` |
| First install on a fresh API 24 boot hangs | Intermittent `dex2oat` hang | `adb logcat` | [Retrying installer](../../ht-device-tiers/scripts/install-with-retry.sh) |
| Android Studio Run reports nothing in its log but the creator sees a banner | The log is INFO level; sync and deploy events are there | [Studio events script](../scripts/studio-events.py) | Read the events summary; ask the creator for the exact banner text |
| `Unable to write magic number to pipe` in the Studio log | Studio's on-device installer daemon did not start; it falls back to a normal install | The deploy continues to "Launching" | Benign; not the cause of a failed Run |
