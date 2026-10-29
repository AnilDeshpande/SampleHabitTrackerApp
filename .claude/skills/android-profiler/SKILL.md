---
name: android-profiler
description: "Records and analyzes Android performance data and diagnoses performance bottlenecks across user apps, system apps, and system services, even if the user does not explicitly mention profiling or traces. Use when: recording system traces, CPU call stacks, Vulkan GPU/DRM memory, or Java/native heap profiles; analyzing Perfetto traces for UI jank, frame drops, ANRs, or app startup latency issues; investigating CPU bottlenecks, IPC/Binder storms, or thread scheduling stalls; diagnosing battery drain, modem power rails, and kernel wake locks; debugging memory pressure, Low Memory Kills (LMK), OOMs, leaks, and allocation churn; performing A/B baseline trace comparisons to find regressions; executing ad hoc PerfettoSQL queries on Android traces."
---

Read [the canonical skill](../../../.agents/skills/android-profiler/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/android-profiler/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
