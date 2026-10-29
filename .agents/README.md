# AI harness index

Shared starting point for Claude and Codex. Select the workflow below and read its
canonical skill, applicable conversation role, and templates. This index provides
navigation; the linked instructions own behavior, decision gates, and authorization.
Use the project-local files linked here. The four `ht-` names distinguish this
repository's harness from personal/global skills. Paths in file-based invocation
examples are relative to the repository root.

## Creating or changing the harness

Before creating, editing, moving, or renaming skills, roles, templates, supporting
resources, or entry points, read the [shared authoring standard](authoring.md) and
complete its validation and evidence checklist before handoff. It applies to both
tools. Run the [offline checker](scripts/check_harness.py) for every harness change;
use its explicit runtime mode for affected discovery/invocation paths.

## Direct invocation

| Workflow | Codex | Claude Code |
| --- | --- | --- |
| Product ideation | `$ht-product-ideation` | `/ht-product-ideation` |
| PRD creation | `$ht-prd-creation` | `/ht-prd-creation` |
| Architecture | `$ht-architecture-creation` | `/ht-architecture-creation` |
| Android LLD | `$ht-lld-creation` | `/ht-lld-creation` |
| Repository knowledge graph | `$ht-graphify` | `/ht-graphify` |
| Mobile prototype | `$mobile-prototype` | `/mobile-prototype` |

Append your request or source documents, for example:

- Codex: `$ht-prd-creation Create a PRD from docs/idea-briefs/habit-tracker-v1-idea-brief.md`.
- Claude Code: `/ht-prd-creation Create a PRD from docs/idea-briefs/habit-tracker-v1-idea-brief.md`.

Codex reads the canonical bundles in `.agents/skills/`. Claude Code reads the
[project entry points](../.claude/skills/ht-product-ideation/SKILL.md) under
`.claude/skills/`, which load the same canonical bundles and conversation roles.
The role guides the current agent; it does not start a separate subagent.

If a command is missing, open a fresh session in this repository and check the
skill selector (`$` in Codex, `/` in Claude Code). Confirm the corresponding
`SKILL.md` exists in the tool's project skill directory and that skills are enabled.
The four renamed harness skills have no unprefixed project aliases. The file-based
examples below remain available as fallbacks.

`mobile-prototype` intentionally keeps its unprefixed name. Claude Code may select
`~/.claude/skills/mobile-prototype/` ahead of this repository's entry point. That
personal bundle matched the project bundle when this setup was introduced. The
read-only invocation check on 2026-10-18 loaded that personal copy; future changes
can diverge. Use the canonical file-based invocation below when
repository-local resolution is required. Personal/global installations are not
modified by this setup.

## Android platform skills (upstream)

Google's official [android/skills](https://github.com/android/skills), vendored
unmodified so Codex and Claude Code load the same bundles. They are
domain references, not conversation workflows: both tools activate them
automatically when a task matches the description, and each can also be invoked by
name. They keep their upstream names (no `ht-` prefix) because they reference each
other by name. Provenance, pinned commit, license, and update steps are in
[the upstream record](upstream/android-skills.md).

- **Inputs:** An Android task in the skill's domain, plus the current checkout.
- **Outputs:** Whatever the task produces; each skill owns its own method and gates.
- **Invoke:** Codex: `$camerax Add a camera preview to the habit detail screen.`
  Claude Code: `/edge-to-edge Make the app edge-to-edge.`
- **File fallback:** “Read `.agents/skills/<name>/SKILL.md` and follow it for this
  Android task.”
- **Note:** Claude Code may select a personal `~/.claude/skills/android-cli/` ahead of
  this repository's entry point (they differ). Use the file fallback when
  repository-local resolution is required.

| Skill | Codex | Claude Code | Use for |
| --- | --- | --- | --- |
| [adaptive](skills/adaptive/SKILL.md) | `$adaptive` | `/adaptive` | Instructions to make or update an app's UI so that it adapts to different Android devices including phones, tablets, foldables, laptops, desktop, TV, Auto, and XR. |
| [agp-9-upgrade](skills/agp-9-upgrade/SKILL.md) | `$agp-9-upgrade` | `/agp-9-upgrade` | Upgrades, or migrates, an Android project to use Android Gradle Plugin (AGP) version 9. |
| [android-cli](skills/android-cli/SKILL.md) | `$android-cli` | `/android-cli` | Provides instructions for installing and using the `android` CLI. |
| [android-for-cars-parked-apps](skills/android-for-cars-parked-apps/SKILL.md) | `$android-for-cars-parked-apps` | `/android-for-cars-parked-apps` | Adapt existing Android video streaming apps, games, and web browsers to run as parked apps on Android Auto and Android Automotive OS. |
| [android-intent-security](skills/android-intent-security/SKILL.md) | `$android-intent-security` | `/android-intent-security` | Best practices for Android Intent security. |
| [android-permissions-security](skills/android-permissions-security/SKILL.md) | `$android-permissions-security` | `/android-permissions-security` | Audits, detects gaps, and remediates Android permissions and IPC component security vulnerabilities. |
| [android-profiler](skills/android-profiler/SKILL.md) | `$android-profiler` | `/android-profiler` | Records and analyzes Android performance data and diagnoses performance bottlenecks across user apps, system apps, and system services, even if the user does not explicitly mention profiling or traces. |
| [anr-audit-and-fix](skills/anr-audit-and-fix/SKILL.md) | `$anr-audit-and-fix` | `/anr-audit-and-fix` | Audits Android codebases for ANR risks and diagnoses ANR stack dumps. |
| [appfunctions](skills/appfunctions/SKILL.md) | `$appfunctions` | `/appfunctions` | Analyzes Android apps to find user workflows to expose as AppFunctions, such as creating a note, playing media, or sending a message. |
| [camerax](skills/camerax/SKILL.md) | `$camerax` | `/camerax` | Provide technical guidance for Android camera development with CameraX. |
| [display-glasses-with-jetpack-compose-glimmer](skills/display-glasses-with-jetpack-compose-glimmer/SKILL.md) | `$display-glasses-with-jetpack-compose-glimmer` | `/display-glasses-with-jetpack-compose-glimmer` | Provides guidelines for developing projected Android XR apps for display glasses using the Jetpack Compose Glimmer UI toolkit. |
| [edge-to-edge](skills/edge-to-edge/SKILL.md) | `$edge-to-edge` | `/edge-to-edge` | Use this skill to migrate your Jetpack Compose app to add adaptive edge-to-edge support and troubleshoot common issues. |
| [engage-sdk-integration](skills/engage-sdk-integration/SKILL.md) | `$engage-sdk-integration` | `/engage-sdk-integration` | Helps developers integrate, debug, and resolve Play Engage SDK implementation issues. |
| [glance-appwidget-updates-and-memory](skills/glance-appwidget-updates-and-memory/SKILL.md) | `$glance-appwidget-updates-and-memory` | `/glance-appwidget-updates-and-memory` | Manages Android AppWidget state updates, background syncs, and RemoteViews bitmap memory limits with Jetpack Glance (androidx.glance:glance-appwidget) and AppWidgetProvider receivers on Android 17 (API 37). |
| [glance-canonical-layouts](skills/glance-canonical-layouts/SKILL.md) | `$glance-canonical-layouts` | `/glance-canonical-layouts` | Use this skill to create home screen widgets using Jetpack Glance Canonical Layouts from the Android Platform Samples project, or when asked to implement standard widget layout patterns (including Check List, Image Grid, Snap Scrolling, and Standard Toolbar). |
| [ink-app-builder](skills/ink-app-builder/SKILL.md) | `$ink-app-builder` | `/ink-app-builder` | Adds inking and freehand drawing capabilities to an Android app using Jetpack Ink (androidx.ink) and Compose. |
| [ink-custom-brush-builder](skills/ink-custom-brush-builder/SKILL.md) | `$ink-custom-brush-builder` | `/ink-custom-brush-builder` | Creates custom BrushFamily definitions for an Android app using Jetpack Ink (androidx.ink 1.1.0-alpha03 and higher). |
| [jetpack-compose-performance](skills/jetpack-compose-performance/SKILL.md) | `$jetpack-compose-performance` | `/jetpack-compose-performance` | Diagnose and optimize Android Jetpack Compose UI code. |
| [leanback-to-compose-tv-migration](skills/leanback-to-compose-tv-migration/SKILL.md) | `$leanback-to-compose-tv-migration` | `/leanback-to-compose-tv-migration` | Provides instructions and architectural patterns for migrating Android TV applications from legacy Leanback UI Toolkit, Android Views, or Support Fragments to Jetpack Compose for TV (androidx.tv). |
| [litert-lm](skills/litert-lm/SKILL.md) | `$litert-lm` | `/litert-lm` | Builds an Android app that runs an open text LLM on the device, on the CPU or the GPU, with the LiteRT-LM Kotlin API (com.google.ai.edge.litertlm:litertlm-android). |
| [litert-runtime](skills/litert-runtime/SKILL.md) | `$litert-runtime` | `/litert-runtime` | Configures and implements LiteRT (.tflite) on-device inference on Android in Kotlin using the CompiledModel API on CPU and GPU. |
| [media3-cast-integration](skills/media3-cast-integration/SKILL.md) | `$media3-cast-integration` | `/media3-cast-integration` | Implements Google Cast support in Android apps using Jetpack Media3. |
| [migrate-xml-views-to-jetpack-compose](skills/migrate-xml-views-to-jetpack-compose/SKILL.md) | `$migrate-xml-views-to-jetpack-compose` | `/migrate-xml-views-to-jetpack-compose` | Provides a structured workflow for migrating an Android XML View to Jetpack Compose. |
| [ml-kit-genai-prompt-api](skills/ml-kit-genai-prompt-api/SKILL.md) | `$ml-kit-genai-prompt-api` | `/ml-kit-genai-prompt-api` | Analyzes Android codebases to implement ML Kit GenAI Prompt API. |
| [mlkit-agent-api](skills/mlkit-agent-api/SKILL.md) | `$mlkit-agent-api` | `/mlkit-agent-api` | Builds autonomous on-device agentic workflows in Android apps using the ML Kit GenAI Agent API powered by Gemini Nano and AICore. |
| [navigation-3](skills/navigation-3/SKILL.md) | `$navigation-3` | `/navigation-3` | Learn how to install and migrate to Jetpack Navigation 3, and how to implement features and patterns such as deep links, multiple backstacks, scenes (dialogs, bottom sheets, list-detail, two-pane, supporting pane), conditional navigation (such as logged-in navigation versus anonymous), returning results from flows, integration with Hilt, ViewModel, Kotlin, and view interoperability. |
| [navigation-event](skills/navigation-event/SKILL.md) | `$navigation-event` | `/navigation-event` | Intercept back gestures and run Predictive Back animations using the NavigationEvent (androidx.navigationevent) library in Compose Android. |
| [play-billing-library-version-upgrade](skills/play-billing-library-version-upgrade/SKILL.md) | `$play-billing-library-version-upgrade` | `/play-billing-library-version-upgrade` | Use this skill when upgrading or migrating an Android project from any legacy Google Play Billing Library (PBL) version to the latest stable version of PBL. |
| [play-policy-insights](skills/play-policy-insights/SKILL.md) | `$play-policy-insights` | `/play-policy-insights` | Automated auditor designed to verify Android applications against Google Play Policy domains. |
| [projected-hardware](skills/projected-hardware/SKILL.md) | `$projected-hardware` | `/projected-hardware` | Provides instructions and workflows for accessing and managing hardware on Android XR projected devices, such as audio glasses and display glasses. |
| [r8-analyzer](skills/r8-analyzer/SKILL.md) | `$r8-analyzer` | `/r8-analyzer` | Analyzes Android build files and R8 keep rules to identify redundancies, broad package-wide rules, and rules that subsume library consumer keep rules. |
| [restore-credentials](skills/restore-credentials/SKILL.md) | `$restore-credentials` | `/restore-credentials` | Provides knowledge and workflows to implement Android's Restore Credentials feature using the androidx.credentials library. |
| [testing-setup](skills/testing-setup/SKILL.md) | `$testing-setup` | `/testing-setup` | Analyze and create a testing strategy for Android apps - install testing libraries, set up test infrastructure, create harnesses for unit tests, UI tests, screenshot tests, and end-to-end tests. |
| [verified-email](skills/verified-email/SKILL.md) | `$verified-email` | `/verified-email` | Provides a complete workflow for implementing verified email retrieval on Android Credential Manager API. |
| [wear-compose-m3](skills/wear-compose-m3/SKILL.md) | `$wear-compose-m3` | `/wear-compose-m3` | Guidance for Wear OS Compose Material3 (Wear Compose Material 3 / Wear Compose Material3) watch and wearable apps. |
| [wear-widgets](skills/wear-widgets/SKILL.md) | `$wear-widgets` | `/wear-widgets` | Create, migrate, update, and verify Wear OS widgets using Glance Wear and Remote Compose in an Android project. |

## Repository knowledge graph

Build, refresh, or query the repository knowledge graph for cross-file
orientation, planning, implementation impact, debugging, reviews, test
discovery, documentation, architecture, or handoffs. Use it to locate evidence,
then verify material claims against current source. Approved project documents
remain authoritative.

- **Inputs:** A concrete question, affected file/symbol/document/workflow, the
  current checkout, and an existing graph when querying.
- **Outputs:** A source-verified evidence map or impact trace; an explicit build
  produces local ignored graph artifacts.
- **Instructions:** [Repository knowledge graph](skills/ht-graphify/SKILL.md) ·
  [Operations runbook](skills/ht-graphify/references/operations.md).
- **Invoke:** Codex: `$ht-graphify Trace the Habit feature from PRD through
  implementation and tests.` Claude Code: `/ht-graphify What is affected by a
  change to MainActivity?`
- **File fallback:** “Read `.agents/skills/ht-graphify/SKILL.md` and use it to
  answer this repository question.”

When the graph is unavailable or stale, explain the limitation and use focused
direct source inspection. The workflow does not install hooks, watchers,
services, global configuration, or automatic model calls.

## Product ideation

Discover agreed product intent when an app or product request is underspecified,
before product definition or implementation.

- **Inputs:** Creator's idea and any existing creator decisions or session records.
- **Deliverables:** Current Idea Brief and dated session notes in `docs/idea-briefs/`.
- **Instructions:** [Product Ideation](skills/ht-product-ideation/SKILL.md) · [Ideator Agent](roles/ideator-agent.md).
- **Templates:** [Idea Brief](templates/idea-brief-template.md) · [Session notes](templates/ideation-session-notes-template.md).
- **Invoke:** “Read `.agents/roles/ideator-agent.md` and run ideation for my app idea.”

## PRD creation

Define observable product behavior from an approved Idea Brief or other current
approved product-intent source; use for creator-led product definition.

- **Inputs:** Approved product-intent source, decision provenance, and any existing PRD.
- **Deliverable:** PRD after the method's decision lock and drafting authorization.
- **Instructions:** [PRD Creation](skills/ht-prd-creation/SKILL.md) · [PRD-Creator Agent](roles/prd-creator-agent.md).
- **Template:** [PRD](templates/prd-template.md).
- **Invoke:** “Read `.agents/roles/prd-creator-agent.md` and create a PRD from my approved Idea Brief.”

## Architecture creation, refinement, or review

Design technical ownership and contracts when product behavior is sufficiently
defined, or review/refine an existing architecture.

- **Inputs:** Approved PRD/product source, applicable architecture and ADRs, and material implementation constraints.
- **Deliverables:** Architecture under `docs/architecture/` and applicable durable decision records under `docs/adr/`.
- **Instructions:** [Architecture Creation](skills/ht-architecture-creation/SKILL.md) · [Architect Agent](roles/architect-agent.md).
- **Templates:** [Architecture](templates/architecture-template.md) · [ADR](templates/adr-template.md), when applicable.
- **Invoke:** “Read `.agents/roles/architect-agent.md` and review the architecture against my approved PRD.”

## Android low-level design

Create, refine, or review detailed Android design from approved architecture,
resolving concrete state, work lifetime, wiring, and test contracts.

- **Inputs:** Approved product source, architecture, applicable ADRs, existing LLD/history, and relevant Android/tooling constraints.
- **Deliverables:** LLD under `docs/design/` and applicable ADRs; review findings for review-only work.
- **Instructions:** [LLD Creation](skills/ht-lld-creation/SKILL.md) · [LLD-Creator Agent](roles/lld-creator-agent.md) · [Android review guide](skills/ht-lld-creation/references/android-design-review.md).
- **Templates:** [LLD](templates/lld-template.md) · [ADR](templates/adr-template.md), when applicable.
- **Invoke:** “Read `.agents/roles/lld-creator-agent.md` and refine my LLD from the approved architecture.”

## Mobile prototyping

Map native Android/iOS screens, states, and navigation before design or building;
also resume an existing prototype. Final-look design is optional within this skill.

- **Inputs:** Available spec, architecture/design documents, platform scope, and any existing prototype with `notes/STATE.md`.
- **Deliverables:** Flow specification, draw.io sketch, decision records, and handoff in the chosen project prototype folder; optional design artifacts.
- **Instructions:** [Mobile Prototype](skills/mobile-prototype/SKILL.md). Separate conversation role: not applicable.
- **Templates:** Existing [workspace templates](skills/mobile-prototype/assets/templates/README.md) and [flow template](skills/mobile-prototype/assets/templates/flow.json), kept inside the skill bundle.
- **Invoke:** “Read `.agents/skills/mobile-prototype/SKILL.md` and prototype the screens from my product documents.”
