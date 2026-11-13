#!/usr/bin/env bash
# Capture a screenshot from ONE named device and optionally downscale it for review or for a repository.
#
# Usage: shot.sh <serial> <out.png> [max-long-side-px]
#
# Always pass the serial: a second device must never answer for the first. Prints the output path and
# the captured size. Downscaling uses `sips` (macOS); without it the full-size file is kept.
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "usage: $0 <serial> <out.png> [max-long-side-px]" >&2
  exit 2
fi

serial="$1"
out="$2"
max="${3:-}"
adb_bin="${ADB:-adb}"

mkdir -p "$(dirname "$out")"
"$adb_bin" -s "$serial" exec-out screencap -p > "$out"

if [ ! -s "$out" ]; then
  echo "empty screenshot from $serial (is the device awake and booted?)" >&2
  exit 1
fi

if [ -n "$max" ] && command -v sips >/dev/null 2>&1; then
  sips -Z "$max" "$out" >/dev/null
fi

if command -v sips >/dev/null 2>&1; then
  size="$(sips -g pixelWidth -g pixelHeight "$out" | awk '/pixel/ {printf "%s ", $2}')"
  echo "$out  ${size}px"
else
  echo "$out"
fi
