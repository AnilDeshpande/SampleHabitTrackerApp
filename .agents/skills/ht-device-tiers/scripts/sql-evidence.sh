#!/usr/bin/env bash
# Print the platform-SQLite evidence lines recorded by instrumented SQL tests.
# Each line is: api=<level> sqlite=<version> journal_mode=<mode>   (log tag SqlEvidence)
# Usage: sql-evidence.sh <androidTest-results-directory>
#   for example app/build/outputs/androidTest-results
set -euo pipefail

[ $# -eq 1 ] && [ -d "$1" ] || { echo "usage: $(basename "$0") <androidTest-results-directory>" >&2; exit 2; }
found="$(grep -rhE ' I SqlEvidence: ' "$1" --include='logcat-*.txt' 2>/dev/null | sed -E 's/.* I SqlEvidence: //' | sort -u || true)"
[ -n "$found" ] || { echo "no SqlEvidence lines under $1" >&2; exit 1; }
echo "$found"
