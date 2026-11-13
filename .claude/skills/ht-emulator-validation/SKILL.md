---
name: ht-emulator-validation
description: "Validate an Android increment by running it on emulators and looking at it: install on named devices, drive the real app with adb, capture and read screenshots, check each prototype state, process-death restore, dark mode and large fonts, curate evidence, and diagnose why Android Studio cannot sync or run the project from its own log. Use after UI work, when the creator wants to see or run the app, or when Run in Android Studio fails."
---

Read [the canonical skill](../../../.agents/skills/ht-emulator-validation/SKILL.md) and
follow it for the user's request and command arguments. For creator-led work, also read
[the implementation engineer agent role](../../../.agents/roles/implementation-engineer-agent.md) and operate in that role; for an independent check,
read [the implementation verifier agent role](../../../.agents/roles/implementation-verifier-agent.md). Resolve supporting files
from `.agents/skills/ht-emulator-validation/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
