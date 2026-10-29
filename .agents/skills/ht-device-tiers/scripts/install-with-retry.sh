#!/usr/bin/env bash
# Install an APK on one device, retrying when the install hangs.
# The first install after a fresh API 24 boot can stall for minutes in dex2oat; a retry usually takes seconds.
# Usage: install-with-retry.sh <serial> <apk> [attempts, default 3] [timeout-seconds, default 90]
set -euo pipefail

[ $# -ge 2 ] || { echo "usage: $(basename "$0") <serial> <apk> [attempts] [timeout-seconds]" >&2; exit 2; }
serial="$1"; apk="$2"; attempts="${3:-3}"; limit="${4:-90}"
[[ "$serial" =~ ^[A-Za-z0-9:._-]+$ && "$attempts" =~ ^[0-9]+$ && "$limit" =~ ^[0-9]+$ ]] || { echo "invalid argument" >&2; exit 2; }
[ -f "$apk" ] || { echo "no such APK: $apk" >&2; exit 2; }

sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
adb="$sdk/platform-tools/adb"
out="$(mktemp)"
trap 'rm -f "$out"' EXIT

for attempt in $(seq 1 "$attempts"); do
  started=$(date +%s)
  "$adb" -s "$serial" install -r "$apk" >"$out" 2>&1 &
  pid=$!
  while kill -0 "$pid" 2>/dev/null; do
    if [ $(( $(date +%s) - started )) -ge "$limit" ]; then
      kill "$pid" 2>/dev/null || true
      wait "$pid" 2>/dev/null || true
      echo "attempt $attempt timed out after ${limit}s" >&2
      continue 2
    fi
    sleep 1
  done
  if wait "$pid" && grep -q '^Success' "$out"; then
    echo "installed on $serial after $attempt attempt(s), last attempt $(( $(date +%s) - started ))s"
    exit 0
  fi
  echo "attempt $attempt failed: $(tail -1 "$out")" >&2
done
echo "install did not succeed after $attempts attempt(s)" >&2
exit 1
