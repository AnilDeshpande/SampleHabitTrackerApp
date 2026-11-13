# Screen-state map — <increment name>

One row per state of every included screen. It is the contract between the prototype, the state models
and the tests: nothing is built or called done unless its row has a test.

## State types

| Class | Owner | Restoration |
| --- | --- | --- |
| Durable | Database | Reloaded; never copied into saved state |
| Restorable input | The destination ViewModel's `SavedStateHandle`, under the LLD's key register | Restored after process death; written on each input event |
| Derived | Recomputed from durable data, restorable input, clock, locale and operation status | Recomputed; never saved |
| Transient effect | One-off navigation, snackbar, permission request | Never restored or replayed |
| Visual-only | Compose (cursor, scroll) | May be lost |

## Map

| Prototype frame | Screen and state | Trigger | State model case | Classification and source | Command or read | Test that proves it | Seen on a device |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S1 | | | | | | | |

## State and effect types

Sketch the sealed state, data class and effect types the map implies, with the field each value comes from.

## Saved-state keys

| ViewModel | Key | Type and contract |
| --- | --- | --- |
| | | |

## Navigation

| Route | Screen or placeholder | Arguments | Result handed back |
| --- | --- | --- | --- |
| | | | |

## Gaps

Prototype states with no row yet, and the reason.
