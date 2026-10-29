---
name: glance-appwidget-updates-and-memory
description: "Manages Android AppWidget state updates, background syncs, and RemoteViews bitmap memory limits with Jetpack Glance (androidx.glance:glance-appwidget) and AppWidgetProvider receivers on Android 17 (API 37). Use this skill when configuring Android 17 (API level 37) compileSdk/targetSdk and AndroidManifest.xml widget receivers, refreshing frozen or stale Glance widget UI after CoroutineWorker DataStore preference updates (updateAppWidgetState), downsampling images within the WindowMetrics memory limit for SizeMode.Responsive, SizeMode.Exact, or LazyColumn, sharing content:// URIs with FileProvider, or fixing ActionCallback NoSuchMethodException and AppWidgetProvider ForegroundServiceStartNotAllowedException crashes with WorkManager. Don't use for Glance layout templates (suggest the glance-canonical-layouts skill) or in-app Compose screens."
---

Read [the canonical skill](../../../.agents/skills/glance-appwidget-updates-and-memory/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/glance-appwidget-updates-and-memory/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
