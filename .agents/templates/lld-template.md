# Low-Level Design — *Product or feature*

> Use after a passing LLD Decision Lock and scoped drafting authorization under
> [LLD Creation](../skills/ht-lld-creation/SKILL.md). Italic text is guidance
> to replace, not an accepted decision. Keep the four core sections; for a
> genuinely inapplicable area, state why and omit its empty sub-registers.
> New formal LLDs must not hand implementation-critical choices to the coding
> agent. Refine existing accepted documents proportionately rather than forcing
> a full rewrite to this structure.

## Metadata, authority, and scope

| Field | Value |
| --- | --- |
| Product / feature | *Resolve from current approved sources* |
| Status and acceptance source | *Ready for review, or Accepted with creator acceptance reference* |
| Date and decision owner | *Resolve for this document* |
| Current approved product source | *Link current source and relevant amendments* |
| Architecture baseline | *Link approved architecture and affected sections/version* |
| Applicable ADRs | *Link accepted records, or None* |
| Work classification | *New LLD, refinement, or review* |
| Source provenance | *Identify inherited decisions and creator decision references* |
| Platform constraints | *Verified SDK/API, libraries, modules, and material tooling constraints* |
| Protected documents and exclusions | *Identify artifacts/behavior this pass may not change* |

*State the affected feature boundary and how this document complements rather
than replaces architecture. Record relevant implementation constraints without
making existing code the authority for product behavior.*

### LLD Decision Lock

*Summarize the passing lock and drafting authorization already established in
conversation. Record inherited and newly accepted choices, alternatives where
material, source findings/dispositions, four-question coverage, focused
verification, ADR impact, and any justified external/deferred items.*

| Decision ID | Accepted contract | Rationale / material alternative | Source / owner |
| --- | --- | --- | --- |
| *Resolve* | *Resolve* | *Resolve* | *Resolve* |

`Implementation-critical questions: *Resolve truthfully before drafting*`

`Unclassified LLD questions: *Resolve truthfully before drafting*`

### Requirement coverage

| Requirement / invariant | LLD responsibility and component | Verification reference |
| --- | --- | --- |
| *Resolve from approved source* | *Resolve* | *Resolve to a focused obligation below* |

## Screen-state ownership and restoration

### State ownership register

*Classify each value exactly once: durable storage, restorable input,
derived state, transient effect, or visual-only. For Android Compose business
inputs, use the ViewModel's SavedStateHandle as the single draft owner and
rememberSaveable for visual choices. Identify no-input screens explicitly.*

| Screen / ViewModel | State value or coherent group | Class and owner | Reconstruction source / lifecycle boundary |
| --- | --- | --- | --- |
| *Resolve for each affected screen* | *Resolve* | *Resolve* | *Resolve* |

### Saved-state key register

*List complete application-owned keys, including minimal initialization/target
metadata. Keep route arguments distinct. Define absent-key defaults without
replacing intentionally empty restored values; save raw text, not parsed values,
errors, entities, history, or complete UiState.*

| ViewModel | Key | Type / encoding | Default, write point, and restoration contract |
| --- | --- | --- | --- |
| *Resolve, or None with reason* | *Resolve* | *Resolve* | *Resolve* |

### Initialization, restoration, and target identity

*Specify first-load seeding versus later durable refresh, initialization guards,
validation recomputation, and what happens when the restored target closed,
changed, or became read-only. Trace the same target ID into command input.
Define system process death versus rotation and task removal; identify one-off
effects/operations that are never restored and the payload-size boundary.*

### Navigation route register

| Destination / external entry | Route / arguments | ViewModel and saved-state owner | Entry, return, and recreation behavior |
| --- | --- | --- | --- |
| *Resolve every affected destination, including sheets/dialogs and notification entry when present* | *Resolve* | *Resolve* | *Resolve* |

### Verification obligations

*Name fixture, public stimulus, and assertion for fresh ViewModels with restored
keys, invalid raw text, edit initialization, changed target identity, and no
replayed effects. State the retained-task process-death device procedure and
separate task-removal behavior. Verify actual destination/factory ownership and
cold/warm external-entry back stacks where applicable.*

## Concurrency and work lifetime

### Scope and cancellation register

| Work / operation | Lifetime owner and execution context | Caller cancellation / screen departure | Process-death recovery |
| --- | --- | --- | --- |
| *Resolve observation, screen work, accepted commands, effects, maintenance, timers, and durable jobs when present* | *Resolve* | *Resolve* | *Resolve* |

### Operation and command contracts

*Specify public inputs/results, immutable capture point, target identity, raw
input validation/conversion, durable evidence, and rejection behavior. Include
feature-specific interfaces or exact durable representations only when needed
to close a gap in the architecture; link inherited schema rather than copying it.*

| Operation | Inputs and identity | Output / rejection / durable outcome | Enforcement owner |
| --- | --- | --- | --- |
| *Resolve* | *Resolve concrete types and meaning* | *Resolve* | *Resolve* |

### Admission and transaction ordering

| Command | Admission key and collision rationale | Reservation / release point | Transaction order and conditional predicates |
| --- | --- | --- | --- |
| *Resolve, or explain why admission is unnecessary* | *Resolve* | *Resolve transaction versus effect lifetime* | *Resolve gate, snapshot, reconciliation, write, commit, and exceptions where applicable* |

*Define who owns the transaction versus the caller's wait and after-commit jobs.
Specify in-flight UI projection on reopening, duplicate taps, deadline races,
process death before/after commit, resubmission, and outcomes with no remaining
screen. Resolve any command-specific ordering exception against the invariant.
Document injected dispatchers, transaction API, and cancellation propagation.*

### Effects, foreground sequence, and recovery

*For external effects, define stable identity, idempotency, serialization,
eligibility predicates/rechecks, and database/platform crash-window recovery.
Distinguish accepted behavior from proposals and existing durable outbox state
from any proposed schema change. Describe the process-level foreground sequence,
stop behavior, and coordination with other triggers.*

| Effect | Identity / eligibility | Commit relationship and owner | Failure / restart recovery |
| --- | --- | --- | --- |
| *Resolve, or Not applicable with reason* | *Resolve* | *Resolve* | *Resolve* |

### Scheduled-work register — when applicable

| Unique name / worker | Enqueue owner and trigger | Kind / policy / cadence / constraints | Retry budget owner, backoff, and terminal behavior |
| --- | --- | --- | --- |
| *Resolve; do not inherit example values* | *Resolve* | *Resolve concrete choices* | *Resolve persisted event budget versus worker counters and planned versus guaranteed timing* |

### Verification obligations

*Specify real-command persistence after screen/ViewModel departure, admission
release independent of slow effects, competing actions/deadlines, stale request
identity, and both sides of commit. Cover effect recovery/identity/isolation,
foreground serialization, worker request configuration, independent retry
counters, exhaustion, and orphaned-result behavior only where applicable.*

## Composition root and dependency wiring

### Construction decision and instance register

*Select the composition mechanism, rationale, material alternatives, and
reconsideration trigger. Identify ADR impact. Do not assume the worked example's
manual container is required for this project.*

| Object / service | Lifetime and owner | Construction / acquisition path | Initialization / execution constraint |
| --- | --- | --- | --- |
| *Resolve graph instances and adapters* | *Resolve* | *Resolve* | *Resolve lazy/off-main/heavy work where relevant* |

### Framework-created object register

| Android / AndroidX-created or invoked object | Provider / factory and dependency path | Restoration, cold/headless entry, and configuration requirements |
| --- | --- | --- |
| *Resolve Application, Activity, destination ViewModels, workers, callbacks, and external entries when present* | *Resolve* | *Resolve actual factory/manifest/provider mechanics* |

### Startup, time, and callback wiring

*Describe cheap Application setup, shared lazy database opening, foreground-only
activation, and headless entry. Specify clock/zone authority, transaction time
capture, injected dispatchers, timer wait calculation, dynamic receiver API/flags,
and registration cleanup. Connect scheduling owners above to the graph rather
than duplicating their timing decisions here.*

### Class and port placement

| New class / interface | Package and source set | Responsibility and dependency boundary |
| --- | --- | --- |
| *Resolve every new named component* | *Resolve within architecture layout* | *Resolve plain-value port versus Android adapter* |

### Test-container replacement

*Specify test graph installation before first framework initialization, custom
runner/Application when needed, shared fake clock/scheduler, main-thread handling,
worker factory configuration, and scope/database/executor teardown ownership.*

### Verification obligations

*Verify real saved-state-aware ViewModel factories, actual worker factories and
merged configuration, cold/headless startup, singleton/lazy acquisition,
process receiver pairing, and a timer boundary through the test graph where
applicable. Confirm all entry paths share the intended graph and serializer.*

## Seam register (test doubles)

### Component seam register

| Component | Real / fake and permitted claims | Injection path / fixture | Why this choice proves the relevant behavior |
| --- | --- | --- | --- |
| *Resolve rules, storage/guards/converters, repository/commands, coordination, time, scopes, ports, platform state, and harness Context when present* | *Resolve* | *Resolve* | *Resolve* |

*If persistence enforces a rule, a fake repository/DAO is not evidence for that
rule. Permit mapping-only repository fakes explicitly; do not give them a second
rule implementation. Assert public events, UiState, persisted outcomes, and
recorded calls rather than emission counts or private implementation details.*

### Execution, contention, and migration fixtures

*Specify actual database/driver, production callbacks/converters/journal mode,
query and transaction execution paths, real-thread contention topology, barriers,
clock/scheduler coordination, completion signals, and teardown. Explain what
in-memory serialization cannot prove. For SQL guards and migrations, define
catalog/name/definition checks, invalid/valid write behavior, supported migration
paths, and minimum-platform execution evidence.*

### Harness and evidence matrix

| Suite / claim | Source set / runner | Required API / engine / device mechanism | Passing evidence and known limitations |
| --- | --- | --- | --- |
| *Resolve concrete tiers appropriate to project* | *Resolve* | *Resolve verified compatible tooling* | *Resolve minimum-API SQL threshold; host validation cannot substitute for platform compatibility* |

### Verification obligations

| Requirement / architecture verification label | Real/fake seam and fixture | Public stimulus / observable assertion | Required harness / evidence status |
| --- | --- | --- | --- |
| *Map every applicable verification label* | *Resolve* | *Resolve* | *Resolve planned, executed, or unverified truthfully* |

*Include required migration/guard and race cases, entry-point graph tests, and
platform checks that port fakes cannot prove. Every obligation above needs a
fixture and meaningful assertion; document tests planned separately from tests
actually run during this design pass.*

## Architecture, ADR, and cross-section consistency review

| Finding ID | Source or cross-section issue | Disposition / accepted resolution | Owner / source and affected contracts |
| --- | --- | --- | --- |
| *Resolve, or None after review* | *Resolve* | *Resolve* | *Resolve* |

*Check route → saved target → command input → transactional eligibility → durable
outcome; deadlines versus admission/effect lifetime; foreground/headless graph
identity; schema and invariant preservation; and tests versus actual enforcement.
Record source discrepancies without silently editing protected documents.*

### Related ADRs and source-change impact

*Link relevant records and new ADRs with status and scope. Explain why no new
ADR is needed if applicable. Distinguish an owner's accepted interpretation from
an amendment that remains outside this pass.*

### Resolved questions

| Previous question ID | Accepted resolution / source | Registers and verification updated |
| --- | --- | --- |
| *Resolve, or None* | *Resolve* | *Resolve* |

## External questions, deliberate deferrals, and handoff

| Item / classification | Why it does not block this implementation scope | Owner | Reopening event |
| --- | --- | --- | --- |
| *Resolve product/source questions outside scope and genuinely later choices, or None* | *Resolve; otherwise the decision lock fails* | *Resolve* | *Resolve* |

`Implementation-critical questions: *Must be None for a complete formal handoff*`

`Unclassified LLD questions: *Must be None for a complete formal handoff*`

*Report actual documentation/runtime validation, outstanding future evidence,
creator acceptance status, and any authorized temporary-note cleanup. Do not
claim planned tests passed or leave stale proposal/open-question references.*
