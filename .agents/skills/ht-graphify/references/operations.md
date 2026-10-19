# Graphify operations runbook

Run every command from the repository root. The current verified executable is
the `graphify` uv-tool command. Its interpreter is recorded locally after a
build in `graphify-out/.graphify_python`; it is generated output and ignored.
Never put credentials or model settings in tracked files.

## Corpus and exclusions

Index relevant tracked application and test code, Android manifests/resources,
Gradle build files and version catalog, project documentation and diagrams,
CI/scripts/tooling, root instructions, harness skills, roles, and templates.
Confirm hidden `.agents/`, `.claude/`, and `.github/` paths in discovery.

Exclude `.git/`, `graphify-out/`, Gradle/build outputs, caches, generated
artifacts, machine-local settings such as `local.properties`, credentials,
keystores, and unrelated parent-workspace files. Never recursively index
`graphify-out/`.

Graphify can structurally extract supported source languages and can represent
semantic facts from readable documents only when an explicit semantic build is
run. It does not fully understand binary WebP images, Draw.io diagrams, JARs,
arbitrary generated files, or Android resource meaning; inspect those files
directly when material. The graph's inferred and ambiguous edges are leads, not
evidence.

## Verified commands

```bash
# Identify the installed CLI and interpreter; no project mutation.
command -v graphify
head -1 "$(command -v graphify)"
graphify --help

# Fresh corpus discovery. It writes only ignored Graphify metadata.
mkdir -p graphify-out
GRAPHIFY_PYTHON="$(head -1 "$(command -v graphify)" | sed 's/^#!//')"
"$GRAPHIFY_PYTHON" -c 'import sys; print(sys.executable)' > graphify-out/.graphify_python
"$GRAPHIFY_PYTHON" -c 'import json; from graphify.detect import detect; from pathlib import Path; print(json.dumps(detect(Path(".")), indent=2))' > graphify-out/.graphify_detect.json

# Explicit local full build. Semantic document/image extraction can use a
# configured backend; it is intentionally not a CI command.
graphify extract . --out .

# Code-only changes: deterministic incremental refresh.
graphify update .

# After an intentional rename or deletion, permit the expected smaller graph.
graphify update . --force

# Re-cluster an existing graph without re-extraction.
graphify cluster-only . --no-viz

# Apply project-specific community names and rebuild the interactive HTML.
# This also binds the Select All filter from within the generated script.
"$(cat graphify-out/.graphify_python)" .agents/skills/ht-graphify/scripts/finalize_graph.py

# Bounded read-only exploration and impact tracing.
graphify query "habit tracker architecture" --budget 600
graphify path "Habit Tracker V1 Architecture" "MainActivity"
graphify affected "MainActivity" --depth 2
```

`graphify extract` must be an intentional local action. If an appropriate model
backend is not configured, use the structural extraction path below, document
the semantic coverage gap, and inspect documents directly instead of adding
credentials or weakening CI.

```bash
# Offline structural build for supported code files only.
GRAPHIFY_PYTHON="$(cat graphify-out/.graphify_python)"
"$GRAPHIFY_PYTHON" -c '
import json
from pathlib import Path
from graphify.detect import detect
from graphify.extract import collect_files, extract

result = detect(Path("."))
files = []
for item in result.get("files", {}).get("code", []):
    path = Path(item)
    files.extend(collect_files(path) if path.is_dir() else [path])
data = extract(files, cache_root=Path("."))
Path("graphify-out/.graphify_structural.json").write_text(json.dumps(data, indent=2), encoding="utf-8")
print("Structural extraction: %s nodes, %s edges" % (len(data["nodes"]), len(data["edges"])))
'
```

## Freshness, renames, and deletions

Freshness evidence is the `graphify-out/manifest.json` (or Graphify's current
manifest artifact), the build time in `cost.json`/report, and a clean
`graphify check-update .` result, corroborated by the current Git diff. After a
rename, deletion, or any document/resource change, run an explicit full build
or use the Graphify incremental procedure only if it confirms pruning of the
old source path. Verify with `rg -n 'old/path' graphify-out/graph.json` and
record any semantic-extraction limitation.

`graphify-out/` is local, reproducible generated output. Keep it ignored and
out of commits, reviews, and ordinary CI. Track only the harness instructions
and ignore rule. Do not use `--watch`, `hook install`, or any installer command
unless a later request explicitly asks for it.
