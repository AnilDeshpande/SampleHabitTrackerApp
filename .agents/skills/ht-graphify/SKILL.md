---
name: ht-graphify
description: Build, refresh, and query the Sample Habit Tracker repository knowledge graph for orientation, impact analysis, debugging, reviews, tests, documentation, architecture, and handoffs.
---

# Sample Habit Tracker Graphify

Use this skill before broad repository exploration when a request needs
cross-file evidence: repository orientation, feature planning, implementation
impact, debugging, code review, test discovery, documentation, architecture,
or a handoff. The graph helps locate evidence. Verify material conclusions
against the current source files before acting. Approved product and design
documents retain their existing authority.

## Inputs

- A concrete question, feature, file, symbol, document, or workflow.
- The current workspace checkout.
- An existing `graphify-out/graph.json` when querying or tracing.

## Outputs

- A source-verified answer, impact trace, or evidence map.
- For explicit refreshes, local `graphify-out/` artifacts: `graph.json`,
  `GRAPH_REPORT.md`, and `graph.html`.
- A clear statement when the graph is absent, stale, incomplete, or unable to
  represent a requested format.

## Operating boundary

Do not install hooks, watchers, background services, global configuration, or
run automatic model calls. Do not add Graphify output to Git. A user explicitly
requesting a full semantic build may run the model-backed command documented in
the [operations runbook](references/operations.md); ordinary CI stays offline.
When the graph is unavailable or stale, say so and continue with focused direct
source inspection where appropriate.

## Procedure

1. Check `graphify-out/graph.json`. For a question with a usable graph, query
   it first using the runbook, then verify every material result in source.
2. For a new graph, or when freshness evidence is insufficient, use the
   reproducible build procedure in the runbook. Inspect the discovery summary
   before accepting corpus coverage.
3. Use `graphify affected` for reverse impact and `graphify path` for a
   relationship trace. Follow each returned path to its current source file.
4. Refresh after relevant code, documentation, resource, workflow, or harness
   changes. Treat renames and deletions as refresh events and confirm that the
   old paths no longer occur in the generated graph.
5. State limitations rather than inferring unsupported relationships. Images,
   binary assets, diagrams, generated outputs, and non-extracted formats may
   need direct inspection.

## Provenance

This local method is adapted from the installed personal Graphify skill bundle:
`/Users/anildeshpande/.codex/skills/graphify/` (version `0.9.4`, inspected
2026-10-18). The local bundle retains only the project-specific operational
guidance needed for reproducible use. It does not modify or depend on a personal
installation at runtime.
