---
name: r8-analyzer
description: "Analyzes Android build files and R8 keep rules to identify redundancies, broad package-wide rules, and rules that subsume library consumer keep rules. Use this skill when developers want to optimize their Android app's size, remove redundant or overly broad keep rules, or troubleshoot ProGuard configurations. Don't use for general Gradle build errors, Compose UI performance, or runtime CPU profiling."
---

Read [the canonical skill](../../../.agents/skills/r8-analyzer/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/r8-analyzer/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
