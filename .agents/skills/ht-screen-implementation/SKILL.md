---
name: ht-screen-implementation
description: "Implement Compose screens and their ViewModels from prototype screen states: UI state and effect types, restorable input in SavedStateHandle, derived validation, one-shot results between destinations, a fully wired navigation graph with placeholders, accessibility, string resources, and Compose and ViewModel tests that cover every state on the smallest screen and at large font sizes. Use for the UI layer of an approved increment."
---

# Screen Implementation

Build the UI layer of an approved increment so that every prototype state is a state in code, a row in the
[screen-state map](../../templates/screen-state-map-template.md), and a test. Behavior comes from the approved documents and
the data and execution layers beneath; this method decides how the screen layer is shaped and proven.

Use it as layer 4 of [increment implementation](../ht-increment-implementation/SKILL.md). Do not use it to decide
behavior, to invent states the prototype lacks, or to bypass the lower layers. The
[Implementation Engineer Agent](../../roles/implementation-engineer-agent.md) guides the conversation. This is a
repository-local skill.

## Inputs

- The screen-state map, the prototype frames and copy, the design note (palette, dynamic colour, components).
- The repository, read models and commands from layers 2 and 3, the LLD's state-ownership table and saved-state key register.

## Outputs

- UI state and effect types, ViewModels, stateless screens with stateful routes, the navigation host, theme, string resources.
- ViewModel tests and Compose tests that cover every row of the map; the map's "seen on a device" column filled by
  [emulator validation](../ht-emulator-validation/SKILL.md).

## Rules

### State

1. **Classify before coding.** Each value is durable, restorable input, derived, transient effect or visual-only. Business input
   lives in `SavedStateHandle` under exactly the LLD's keys; scroll, cursor and expansion live in Compose. Never copy durable
   data into saved state, and never save a parsed value, a list or a complete UI state.
2. **Write input on every event** so a kill between events loses nothing. Detect restoration by reading the handle's keys
   before the first `getStateFlow`; an intentionally empty field must not be replaced by a default.
3. **Validate by deriving**, from the raw text, the locale and the clock, on every emission. Errors are never saved. Show them
   after the first Save attempt and on a restored draft. A rejection that only the transaction can decide (a duplicate name)
   comes back as a command result and is held in screen-local state, cleared when the field changes.
4. **Make every emission consistent.** When two writes change related values, normalize in the derivation (for example a
   non-Custom frequency reads as "no weekdays") instead of relying on write order. Add a test that reads state immediately
   after the second write.
5. **Loading and failure are states.** A wait indicator appears only after 300 ms. A failed read is never rendered as an
   empty state and blocks actions that need the data.

### Effects, results and navigation

1. One-off events (navigate, snackbar, permission request) are effects: delivered once, never restored or replayed. Hand a result
   to the destination that opened a screen through that entry's saved-state handle, and consume it when shown.
2. **Sequence effects against system UI.** A transient message whose timer runs behind a system dialog is never seen. Show it after
   the dialog closes.
3. Wire the **whole** route register now. Destinations whose feature is a later increment are placeholders that hold no state;
   they make the graph reachable and are replaced one at a time.
4. Back, Up and Cancel all go through the ViewModel, which decides between the discard dialog and leaving. A save already in
   flight is not cancelled by leaving.

### Composables

1. A route connects the ViewModel; the screen is stateless and renders state and sends events. No schedule, points, validation or
   finalization rule is calculated in a composable; formatting happens in a mapper.
2. A text field that edits ViewModel-owned text keeps its own cursor and adopts the ViewModel's text whenever it differs.
3. Strings live in resources; copy that the handoff marks as draft stays draft. State every deviation from prototype copy or
   layout in the increment record.
4. **Accessibility.** Every actionable control has a label or description that names its subject; selection and results are
   stated in words or an icon, never colour alone; touch targets are at least 48 dp; layouts reflow at 200% text size; errors use
   a live region.
5. Theme tokens come from the design note. Match the window background to the surface so no flash shows before the first frame.

### Tests

1. **ViewModel tests** use the real repository on in-memory Room, a manual clock, an unconfined main dispatcher and real-time waits
   for database work. Cover restoration (a pre-filled handle), each validation outcome, a duplicate, the saving state and a
   duplicate tap, failure then retry, discard flow, and the derived preview. Wrap the repository by delegation to gate or fail a
   call; do not mock it.
2. **Compose tests** render each state statelessly and assert text, enabled state, selection and callbacks. Run them on the
   **smallest-screen tier** and scroll to an element before asserting it is displayed. Look up controls that merge their children
   in the unmerged tree. Include one test at twice the font scale.
3. Put one passing and one deliberately broken variant through a mutation check for the rule you rely on most.

## Procedure

1. Fill the state map's remaining columns; write the state and effect types; confirm every value has an owner in a lower layer.
2. Theme and navigation host with placeholders. 3. ViewModels with their tests. 4. Screens with their Compose tests.
5. Build, run every tier, fix test-design faults separately from product defects.
6. Hand off to [emulator validation](../ht-emulator-validation/SKILL.md); fix what looking finds and add a test where a test could
   have caught it.

## Stopping boundary

Stop when every map row has a passing test on every tier and the app builds and installs. Looking at it on a device is
[validation](../ht-emulator-validation/SKILL.md).
