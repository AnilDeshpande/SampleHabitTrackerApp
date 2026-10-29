# Upstream: android/skills

Vendored copy of Google's official Android skills, installed so Codex and Claude
Code share them from this repository.

- **Source:** https://github.com/android/skills (Apache-2.0; license kept in
  `LICENSE-android-skills.txt` beside this file).
- **Pinned commit:** `6daa53ef27fa2aa87827d183a85c06cedceca833` (plugin version 1.0.19, 2026-10-29).
- **Layout:** upstream groups skills in topic folders; here each skill is flattened to
  `.agents/skills/<name>/` (the canonical bundle Codex reads) with a thin Claude
  entry point at `.claude/skills/<name>/SKILL.md`. Bundle contents are unmodified.
- **Names:** upstream names are kept verbatim (no `ht-` prefix) because the skills
  reference each other by name; each is registered in `.agents/harness-exceptions.json`.
- **Personal copies:** `android-cli` also exists under `~/.claude/skills/` and
  `~/.agents/skills/` and differs from this upstream copy. Claude Code resolves a
  personal skill ahead of a project skill of the same name, so the personal copy
  wins there. Personal installations are not modified.

## Updating

Clone the upstream repository, then replace each bundle below with the matching
upstream folder (names are unique, so the flattening is lossless), update the pinned
commit above, add entry points and exceptions for any new skills, remove those of any
dropped skills, and run the checks in [the authoring standard](../authoring.md).

| Skill | Upstream path |
| --- | --- |
| `adaptive` | `jetpack-compose/adaptive` |
| `agp-9-upgrade` | `build-system/agp/agp-9-upgrade` |
| `android-cli` | `devtools/android-cli` |
| `android-for-cars-parked-apps` | `cars/android-for-cars-parked-apps` |
| `android-intent-security` | `security/android-intent-security` |
| `android-permissions-security` | `security/android-permissions-security` |
| `android-profiler` | `profilers/android-profiler` |
| `anr-audit-and-fix` | `performance/anr-audit-and-fix` |
| `appfunctions` | `device-ai/appfunctions` |
| `camerax` | `camera/camerax` |
| `display-glasses-with-jetpack-compose-glimmer` | `xr/display-glasses-with-jetpack-compose-glimmer` |
| `edge-to-edge` | `system/edge-to-edge` |
| `engage-sdk-integration` | `play/engage-sdk-integration` |
| `glance-appwidget-updates-and-memory` | `system/glance-appwidget-updates-and-memory` |
| `glance-canonical-layouts` | `system/glance-canonical-layouts` |
| `ink-app-builder` | `jetpack-compose/ink/ink-app-builder` |
| `ink-custom-brush-builder` | `jetpack-compose/ink/ink-custom-brush-builder` |
| `jetpack-compose-performance` | `jetpack-compose/jetpack-compose-performance` |
| `leanback-to-compose-tv-migration` | `tv/leanback-to-compose-tv-migration` |
| `litert-lm` | `device-ai/litert/litert-lm` |
| `litert-runtime` | `device-ai/litert/litert-runtime` |
| `media3-cast-integration` | `media/media3-cast-integration` |
| `migrate-xml-views-to-jetpack-compose` | `jetpack-compose/migration/migrate-xml-views-to-jetpack-compose` |
| `ml-kit-genai-prompt-api` | `device-ai/ml-kit-genai-prompt-api` |
| `mlkit-agent-api` | `device-ai/mlkit-agent-api` |
| `navigation-3` | `navigation/navigation-3` |
| `navigation-event` | `navigation/navigation-event` |
| `play-billing-library-version-upgrade` | `play/play-billing-library-version-upgrade` |
| `play-policy-insights` | `play/play-policy-insights` |
| `projected-hardware` | `xr/projected-hardware` |
| `r8-analyzer` | `performance/r8-analyzer` |
| `restore-credentials` | `identity/restore-credentials` |
| `testing-setup` | `testing/testing-setup` |
| `verified-email` | `identity/verified-email` |
| `wear-compose-m3` | `wear/wear-compose-m3` |
| `wear-widgets` | `wear/wear-widgets` |
