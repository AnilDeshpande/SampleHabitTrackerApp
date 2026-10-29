---
name: litert-lm
description: "Builds an Android app that runs an open text LLM on the device, on the CPU or the GPU, with the LiteRT-LM Kotlin API (com.google.ai.edge.litertlm:litertlm-android). Use this skill when creating a chat, summarization, or extraction feature with a .litertlm model from Hugging Face litert-community (Gemma, Qwen, Llama, Phi), including the Gradle dependency, the GPU manifest entries, getting the model file onto the device, Engine initialization off the main thread, a streamed multi-turn Conversation with sendMessageAsync, and the AndroidViewModel and Compose screen. Don't use for running .tflite vision, audio, or embedding models with the CompiledModel API, for image or audio input, tool calling, or NPU backends, or for converting models."
---

Read [the canonical skill](../../../.agents/skills/litert-lm/SKILL.md) and
follow it for the user's request and command arguments. Resolve supporting files
from `.agents/skills/litert-lm/`, relative to the repository root, rather than
from this entry-point directory. The canonical method owns behavior and gates.
