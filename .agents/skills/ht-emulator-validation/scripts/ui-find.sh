#!/usr/bin/env bash
# Find a UI element by its visible text or content description on ONE named device, print its centre in
# device pixels, and optionally tap it. Use this instead of reading coordinates off a scaled screenshot.
#
# Usage: ui-find.sh <serial> <text-or-description> [--tap] [--contains]
#
# Exact match by default; --contains matches a substring. Exit 0 when found, 1 when not found.
# Compose exposes visible text and content descriptions to uiautomator; elements that are scrolled out of
# view are absent from the dump, so scroll first.
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "usage: $0 <serial> <text-or-description> [--tap] [--contains]" >&2
  exit 2
fi

serial="$1"
needle="$2"
shift 2
tap=0
mode="exact"
for arg in "$@"; do
  case "$arg" in
    --tap) tap=1 ;;
    --contains) mode="contains" ;;
    *) echo "unknown option: $arg" >&2; exit 2 ;;
  esac
done

adb_bin="${ADB:-adb}"
remote="/sdcard/ht-window-dump.xml"
local_dump="$(mktemp -t ht-ui-dump)"
trap 'rm -f "$local_dump"' EXIT

"$adb_bin" -s "$serial" shell uiautomator dump "$remote" >/dev/null 2>&1
"$adb_bin" -s "$serial" pull "$remote" "$local_dump" >/dev/null 2>&1
"$adb_bin" -s "$serial" shell rm -f "$remote" >/dev/null 2>&1 || true

center="$(python3 -I - "$local_dump" "$needle" "$mode" <<'PY'
import re, sys, xml.etree.ElementTree as ET
path, needle, mode = sys.argv[1:4]
root = ET.parse(path).getroot()
for node in root.iter("node"):
    for attr in ("text", "content-desc"):
        value = node.get(attr, "")
        if not value:
            continue
        hit = needle in value if mode == "contains" else value == needle
        if hit:
            m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                print((x1 + x2) // 2, (y1 + y2) // 2)
                sys.exit(0)
sys.exit(1)
PY
)" || { echo "not found on $serial: $needle" >&2; exit 1; }

echo "$center"
if [ "$tap" -eq 1 ]; then
  # shellcheck disable=SC2086
  "$adb_bin" -s "$serial" shell input tap $center
fi
