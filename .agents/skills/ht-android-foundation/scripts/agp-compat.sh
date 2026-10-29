#!/usr/bin/env bash
# Print the AGP release-note compatibility section and the Android Studio rows that support it.
# Usage: agp-compat.sh <agp-version>   (for example 9.3.0)
set -euo pipefail

if [ $# -ne 1 ] || ! [[ "$1" =~ ^[0-9]+\.[0-9]+(\.[0-9]+)?$ ]]; then
  echo "usage: $(basename "$0") <agp-version, e.g. 9.3.0>" >&2
  exit 2
fi
version="$1"
major_minor="${version%.*}"
if [[ "$version" =~ ^[0-9]+\.[0-9]+$ ]]; then major_minor="$version"; fi
slug="agp-${major_minor//./-}-0-release-notes"

fetch_text() {
  curl -fsSL --max-time 30 "$1" | python3 -I -c '
import re, sys, html
text = sys.stdin.read()
text = re.sub(r"<script.*?</script>|<style.*?</style>", "", text, flags=re.S)
text = html.unescape(re.sub(r"<[^>]+>", " ", text))
print(re.sub(r"\s+", " ", text))
'
}

echo "== AGP ${major_minor} compatibility (https://developer.android.com/build/releases/${slug})"
fetch_text "https://developer.android.com/build/releases/${slug}" | python3 -I -c '
import re, sys
text = sys.stdin.read()
start = text.find("Compatibility")
print(text[start:start + 520] if start >= 0 else "no Compatibility section found")
'

echo
echo "== Android Studio versions whose supported AGP range includes ${major_minor}"
fetch_text "https://developer.android.com/build/releases/about-agp" | MM="$major_minor" python3 -I -c '
import os, re, sys
text = sys.stdin.read()
mm = tuple(int(x) for x in os.environ["MM"].split("."))
for name, build, low, high in re.findall(r"([A-Z][A-Za-z0-9 ]+?) \| (\d{4}\.\d+\.\d+) ([0-9.]+)-([0-9.]+)", text):
    hi = tuple(int(x) for x in high.split(".")[:2])
    if hi >= mm:
        name = name.replace("Android Studio version Required AGP version", "").strip()
        print(f"{name:28} {build:10} supports AGP {low}-{high}")
' | head -12
