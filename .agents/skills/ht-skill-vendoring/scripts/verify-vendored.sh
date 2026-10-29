#!/usr/bin/env bash
# Check a vendored skill set against its upstream clone.
# Run from anywhere inside the repository.
# Usage: verify-vendored.sh <upstream-clone> <map-file>
#   map-file lines: "<skill-name> <upstream-relative-path>"   (blank lines and # comments allowed)
# Checks, per skill: bundle byte-identical to upstream, Claude entry point present with matching name, and
# no vendored file excluded by a git ignore rule. Exits 1 if any check fails.
set -euo pipefail

[ $# -eq 2 ] && [ -d "$1" ] && [ -f "$2" ] || { echo "usage: $(basename "$0") <upstream-clone> <map-file>" >&2; exit 2; }
src="$(cd "$1" && pwd)"; map="$2"
root="$(git rev-parse --show-toplevel)"
cd "$root"
bad=0; count=0

while read -r name path _; do
  [ -n "${name:-}" ] && [[ "$name" != \#* ]] || continue
  count=$((count + 1))
  bundle=".agents/skills/$name"
  if [ ! -d "$bundle" ]; then echo "MISSING bundle: $bundle"; bad=1; continue; fi
  if [ ! -d "$src/$path" ]; then echo "MISSING upstream folder: $path"; bad=1; continue; fi
  if ! diff -rq -x .DS_Store "$src/$path" "$bundle" >/dev/null; then echo "DIFFERS from upstream: $name"; bad=1; fi
  entry=".claude/skills/$name/SKILL.md"
  if [ ! -f "$entry" ]; then echo "MISSING entry point: $entry"; bad=1
  elif ! grep -q "^name: $name\$" "$entry"; then echo "entry point name mismatch: $entry"; bad=1; fi
  ignored="$(git ls-files --others --ignored --exclude-standard "$bundle" | head -3)"
  if [ -n "$ignored" ]; then echo "IGNORED by git in $name: $ignored"; bad=1; fi
done < "$map"

if [ "$bad" -eq 0 ]; then echo "OK: $count skill(s) verified (identical to upstream, entry points present, nothing git-ignored)"; else exit 1; fi
