---
name: mlkit-agent-api
description: "Builds autonomous on-device agentic workflows in Android apps using the ML Kit GenAI Agent API powered by Gemini Nano and AICore. Use this skill when implementing conversational agents, multi-turn chat with AgentSession in a ViewModel, autonomous tool calling using Android AppFunctions (@AppFunctionDeclaration and AppFunctionAgentTools.discover), ephemeral single-turn execution with runner.runSingleTurnAsync, or handling GenAiException error resilience. It solves the problem of orchestrating local reasoning loops, typed tool dispatch, and session persistence on-device with zero cloud latency and privacy preservation. Don't use for cloud-based Gemini models or Vertex AI, single-turn text generation without agent loops (suggest Prompt API), or non-Android platforms."
---

Read [the canonical skill](../../../.agents/skills/mlkit-agent-api/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/mlkit-agent-api/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
