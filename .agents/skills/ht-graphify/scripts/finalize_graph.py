#!/usr/bin/env python3
"""Name Graphify communities and repair the generated community filter."""

from __future__ import annotations

import json
from collections import defaultdict
from pathlib import Path

from networkx.readwrite import json_graph

from graphify.analyze import god_nodes, suggest_questions, surprising_connections
from graphify.cluster import score_all
from graphify.export import to_html
from graphify.report import generate


ROOT = Path.cwd()
OUT = ROOT / "graphify-out"
GRAPH_PATH = OUT / "graph.json"
HTML_PATH = OUT / "graph.html"

AREA_RULES = (
    ("Prototype Sketch Generation", ("sketch.mjs", "makecomposedsketch")),
    ("Harness Checker Fixtures", ("check_harness", "harness_fixtures")),
    ("Prototype Handoff Generation", ("handoff.mjs", "migrate-flow")),
    ("Prototype Runtime UI", ("core.js", "makecontext")),
    ("Prototype Input Discovery", ("discover-inputs.mjs", "design-provider")),
    ("Tool Environment Detection", ("check-drawio-mcp", "configure")),
    ("Design Theme Generation", ("theme.mjs",)),
    ("Color and Update Utilities", ("color.mjs", "check-update")),
    ("Harness Validation", ("check_harness.py", "validator")),
    ("Prototype Screen Rendering", ("screens.mjs",)),
    ("Prototype Scaffolding", ("scaffold.mjs",)),
    ("Harness Runtime Probes", ("harness_runtime.py", "run_probes")),
    ("Android Compose Starter App", ("mainactivity.kt", "greeting")),
    ("Product and Design Documentation", ("habit-tracker-v1", "architecture")),
    ("Remote Asset Acquisition", ("fetch-archive.mjs",)),
    ("Prototype Preview Server", ("serve.mjs",)),
    ("Reference Link Resolution", ("references.mjs",)),
    ("Android Theme Validation", ("androidscheme",)),
    ("Interactive Flow Screens", ("flow-screens.js",)),
    ("Workspace Discovery", ("workspace.mjs",)),
    ("Graphify Workspace Harness", ("ht-graphify",)),
    ("iOS Theme Validation", ("iostheme",)),
    ("Android Instrumentation Tests", ("exampleinstrumentedtest",)),
    ("Android Unit Tests", ("exampleunittest",)),
    ("Style Tile Rendering", ("style-tile.js",)),
    ("App Gradle Build", ("app/build.gradle.kts",)),
    ("Compose Color Theme", ("ui/theme/color.kt",)),
    ("Compose Typography", ("ui/theme/type.kt",)),
    ("Gradle Settings", ("settings.gradle.kts",)),
    ("Root Gradle Build", ("build.gradle.kts",)),
    ("Harness Authoring Standards", (".agents/authoring.md",)),
)

# Community IDs are stable for this repository's checked-in corpus.  Curating
# the substantial clusters avoids ambiguous keyword matches (for example,
# harness fixture code and harness validation code share many identifiers).
CURATED_COMMUNITY_NAMES = {
    0: "Prototype Sketch Generation",
    1: "Harness Checker Fixtures",
    2: "Prototype Handoff Generation",
    3: "Prototype Runtime UI",
    4: "Prototype Input Discovery",
    5: "Tool Environment Detection",
    6: "Design Theme Generation",
    7: "Color and Update Utilities",
    8: "Harness Validation",
    9: "Prototype Screen Rendering",
    10: "Prototype Scaffolding",
    11: "Harness Runtime Probes",
    12: "Android Compose Starter App",
    13: "Product and Design Documentation",
    14: "Remote Asset Acquisition",
    15: "Prototype Preview Server",
    16: "Reference Link Resolution",
    17: "Android Theme Validation",
    18: "Interactive Flow Screens",
    19: "Workspace Discovery",
    20: "Graphify Workspace Harness",
    21: "iOS Theme Validation",
    22: "Android Instrumentation Tests",
    23: "Android Unit Tests",
    24: "Style Tile Rendering",
    25: "App Gradle Build",
    26: "Compose Color Theme",
    27: "Compose Typography",
    28: "Root Gradle Build",
    29: "Harness Authoring Standards",
}


def _display_label(node_id: str, graph: object) -> str:
    """Return a readable node label."""
    label = str(graph.nodes[node_id].get("label") or node_id).strip()
    return label.rsplit("/", maxsplit=1)[-1].replace("_", " ")


def _name_community(node_ids: list[str], graph: object) -> str:
    """Name a cluster from its representative source and labels."""
    text = " ".join(
        " ".join(str(graph.nodes[node_id].get(key, "")) for key in ("label", "source_file", "source_location")).lower()
        for node_id in node_ids
    )
    score, name = max(
        ((sum(term in text for term in terms), name) for name, terms in AREA_RULES),
        default=(0, ""),
    )
    if score:
        return name
    if len(node_ids) == 1:
        return _display_label(node_ids[0], graph)
    return f"Related {_display_label(node_ids[0], graph)}"


def main() -> None:
    """Label each community, rebuild the report, and wire Select All."""
    if not GRAPH_PATH.exists():
        raise SystemExit("graphify-out/graph.json is required before finalizing.")
    raw = json.loads(GRAPH_PATH.read_text(encoding="utf-8"))
    graph = json_graph.node_link_graph(raw, edges="links")
    communities: dict[int, list[str]] = defaultdict(list)
    for node_id, data in graph.nodes(data=True):
        if isinstance(data.get("community"), int):
            communities[data["community"]].append(node_id)
    if not communities:
        raise SystemExit("The graph has no community assignments to label.")
    communities = dict(sorted(communities.items()))
    labels = {
        community: CURATED_COMMUNITY_NAMES.get(community, _name_community(node_ids, graph))
        for community, node_ids in communities.items()
    }
    for node in raw["nodes"]:
        if isinstance(node.get("community"), int):
            node["community_name"] = labels[node["community"]]
    GRAPH_PATH.write_text(json.dumps(raw, indent=2, ensure_ascii=False), encoding="utf-8")
    (OUT / ".graphify_labels.json").write_text(
        json.dumps({str(key): value for key, value in labels.items()}, indent=2), encoding="utf-8"
    )

    cohesion = score_all(graph, communities)
    detect_path = OUT / ".graphify_detect.json"
    detection = json.loads(detect_path.read_text(encoding="utf-8")) if detect_path.exists() else {}
    report = generate(
        graph, communities, cohesion, labels, god_nodes(graph), surprising_connections(graph, communities),
        detection, {"input": 0, "output": 0}, str(ROOT), suggested_questions=suggest_questions(graph, communities, labels),
    )
    (OUT / "GRAPH_REPORT.md").write_text(report, encoding="utf-8")
    to_html(graph, communities, str(HTML_PATH), community_labels=labels)

    html = HTML_PATH.read_text(encoding="utf-8")
    old_control = 'id="select-all-cb" checked onchange="toggleAllCommunities(!this.checked)"'
    new_control = 'id="select-all-cb" checked'
    anchor = "const legendEl = document.getElementById('legend');"
    binding = "selectAllCb.addEventListener('change', () => {\n  toggleAllCommunities(!selectAllCb.checked);\n});\n\n"
    if old_control not in html or anchor not in html:
        raise SystemExit("Unexpected Graphify HTML template; Select All was not patched.")
    HTML_PATH.write_text(
        html.replace(old_control, new_control, 1).replace(anchor, binding + anchor, 1), encoding="utf-8"
    )
    print(f"Labeled {len(labels)} communities and refreshed {HTML_PATH}.")


if __name__ == "__main__":
    main()
