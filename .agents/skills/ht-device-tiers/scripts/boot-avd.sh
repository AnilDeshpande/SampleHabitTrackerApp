#!/usr/bin/env bash
# Boot an AVD headless in the background and wait until Android has finished booting.
# Prints: SERIAL=<emulator-serial> API=<level> PID=<emulator pid> LOG=<log file>
# Only the device this script starts is reported as its own; other running emulators are left alone.
# Usage: boot-avd.sh <avd-name> [--timeout <seconds, default 300>] [--window]
set -euo pipefail

usage() { echo "usage: $(basename "$0") <avd-name> [--timeout <seconds>] [--window]" >&2; exit 2; }
[ $# -ge 1 ] || usage
name="$1"; shift
timeout_s=300; headless="-no-window"
while [ $# -gt 0 ]; do
  case "$1" in
    --timeout) timeout_s="${2:-}"; shift 2 ;;
    --window) headless=""; shift ;;
    *) usage ;;
  esac
done
[[ "$name" =~ ^[A-Za-z0-9_.-]+$ && "$timeout_s" =~ ^[0-9]+$ ]] || usage

sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
adb="$sdk/platform-tools/adb"
emulator="$sdk/emulator/emulator"
[ -x "$emulator" ] && [ -x "$adb" ] || { echo "emulator or adb not found under $sdk" >&2; exit 1; }
"$emulator" -list-avds | grep -qx "$name" || { echo "no such AVD: $name" >&2; exit 1; }

log="${TMPDIR:-/tmp}/boot-$name.log"
before="$("$adb" devices | awk '/^emulator-/ {print $1}' | sort)"
# shellcheck disable=SC2086
nohup "$emulator" -avd "$name" $headless -no-audio -no-snapshot -no-boot-anim -gpu swiftshader_indirect >"$log" 2>&1 &
pid=$!

serial=""
deadline=$(( $(date +%s) + timeout_s ))
while [ "$(date +%s)" -lt "$deadline" ]; do
  kill -0 "$pid" 2>/dev/null || { echo "emulator exited early; see $log" >&2; exit 1; }
  after="$("$adb" devices | awk '/^emulator-/ {print $1}' | sort)"
  serial="$(comm -13 <(echo "$before") <(echo "$after") | head -1)"
  if [ -n "$serial" ] && [ "$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
    api="$("$adb" -s "$serial" shell getprop ro.build.version.sdk | tr -d '\r')"
    echo "SERIAL=$serial API=$api PID=$pid LOG=$log"
    exit 0
  fi
  sleep 3
done
echo "timed out after ${timeout_s}s waiting for '$name' to boot; see $log" >&2
exit 1
