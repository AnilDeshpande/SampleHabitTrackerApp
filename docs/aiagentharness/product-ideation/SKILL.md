---
name: product-ideation
description: "Discover agreed product intent before product definition or implementation, and preserve it in a current Idea Brief plus dated session notes. Use when a creator asks to build an app or product but the target user, problem, promise, core loop, V1 boundary, or success conditions are not yet decided."
---

# Product Ideation

Turn fuzzy creator intent into a current Idea Brief and a chronological session record that a later product-definition step can trust. Manage the reasoning state; never silently become the product owner.

## Start from the current state

Before asking questions, show:

- **Decided:** Facts or choices explicitly provided or agreed.
- **Open now:** The next material decisions that are meaningful now.
- **Not yet specifiable / fog:** Decisions that are premature because product intent is unresolved.
- **Out of scope:** Decisions explicitly excluded from this ideation pass.

Do not infer a decision from a plausible default.

## Discover breadth before depth

Establish these dimensions in order when possible:

1. Target user
2. Problem or unmet need
3. Product promise
4. Core loop: the repeatable action and value returned to the user
5. V1 boundary and evidence of success

Ask only the next one to three questions that are meaningful now. Do not present a static exhaustive questionnaire. Explain relevant options and trade-offs briefly, then leave each material decision to the creator.

After every material decision, record:

- The decision
- The creator's short rationale, in their words or a faithful concise summary
- Its immediate consequence
- A source reference to the creator turn or existing session evidence

Recalculate the state. Expose newly meaningful questions in **Open now** and retire questions invalidated by the decision.

## Maintain durable session notes

Keep the two ideation artifacts distinct:

- Treat the **Idea Brief** as the current creator-approved product truth for the next consumer.
- Treat **ideation session notes** as the chronological evidence trail: session source, decisions, rationale, consequences, source references, replacements, and final state audit.

In a writable project workspace, create one notes file after the first material creator decision and update it after every later material decision. Use `docs/idea-briefs/<product-slug>-ideation-session-notes-YYYY-MM-DD.md`. Reuse that file across turns in the same session. After handoff or when the creator explicitly starts a new session, create a new dated file; if the name already exists for a different session on that date, append `-02`, `-03`, and so on.

When a decision changes, retain the previous decision and record its replacement and creator-provided reason. Never promote an agent suggestion, plausible default, or omission into either artifact as a creator decision.

If no writable workspace is available, maintain the same structured record in the conversation and present copy-ready session notes at handoff. State that they were not persisted; never imply that a file exists.

Include an acceptance, casting, or other gate scorecard only when the creator or project explicitly makes that gate part of the session. Keep it in the notes rather than turning it into product intent.

## Keep premature detail in fog

Treat persistence, sync, architecture, navigation, reminders, data schemas, dependency injection, detailed behavior, and implementation plans as fog unless they become a necessary product-boundary question.

Do not write a PRD, architecture, implementation plan, or project-specific acceptance criteria. Keep acceptance criteria as separate gates on the resolved Idea Brief.

## Complete the artifacts

When the decision state is sufficient, reconcile the notes with a durable Idea Brief containing:

- Product intent: target user, problem, promise, and core loop
- V1 boundary: included outcomes, explicit exclusions, and success evidence
- Decision log: decision, rationale, and consequence
- Open material questions
- Deferred fog
- Handoff: a later product-definition step may convert this brief into a PRD

Before handoff, classify every previously Open or fog item as Resolved, Deliberately deferred, or Out of scope. For each deferred item, record what is deferred, why it is premature, and the later event or owner that may reopen it. Identify any unclassified product question explicitly.

Stop when the product intent and V1 boundary are sufficient for that handoff. Present both artifacts for creator review. Explicitly defer anything still premature; never invent an answer or continue into a PRD.
