# Design direction — Habit Tracker V1

Locked by the project creator on 2026-11-13, after the flow was locked on 2026-10-11. This
records the visual direction that the handoff left open (HANDOFF.md § 7 Theme). The behavior and
screen flow in HANDOFF.md are unchanged.

## Decision

Green Material 3 palette replacing the Android Studio template's purple. The feel is growth,
improvement and personal development. Both light and dark schemes are in scope. Components stay
Material 3 baseline (HANDOFF.md § 6).

## Reference renders

Mockups of S1 and S2 only; they are an interpretation of the sketch, not pixel specs. Other
screens follow the same tokens and are designed as they are built.

- [m3-mockup-s1-s2.png](../exports/m3-mockup-s1-s2.png): S1 and S2, light and dark.
- [m3-mockup-s1-s2.html](../exports/m3-mockup-s1-s2.html): source of that render.
- [sketch-S1-overview-empty.png](../exports/sketch-S1-overview-empty.png) and
  [sketch-S2-create-habit-daily.png](../exports/sketch-S2-create-habit-daily.png): the original
  hand-drawn pages.

## Colour tokens (starting point)

| Role | Light | Dark |
| --- | --- | --- |
| primary | `#2C6E3F` | `#97D5A0` |
| onPrimary | `#FFFFFF` | `#003919` |
| primaryContainer | `#B2F1BB` | `#1B5330` |
| onPrimaryContainer | `#00210C` | `#B2F1BB` |
| surface | `#F7FBF3` | `#101410` |
| onSurface | `#191D18` | `#E0E4DB` |
| onSurfaceVariant | `#414941` | `#C1C9BF` |
| outline | `#717970` | `#8B9389` |
| outlineVariant | `#C1C9BF` | `#414941` |
| surfaceContainer | `#EBEFE8` | `#1C201B` |

These are a hand-picked tonal set for the mockup. Verify contrast, and fill in the remaining
Material 3 roles (secondary, tertiary, error and the other surface containers), when the theme is
implemented.

## Choices made in the S1 and S2 mockup

- S1 empty state uses a leaf icon in a primaryContainer circle; the sketch has no icon.
- S1 "Global points" is a surfaceContainer card.
- S2 uses a single top app bar with a back arrow, not the sketch's second in-body heading.
- The frequency card is a plain card in S2; it becomes a picker with the schedule screens (S9–S19).
- The sketch's yellow notes are review markup and are not product copy.

## Open

- **Dynamic colour:** the template theme has it on (Android 12+ would show the user's wallpaper
  colours instead of this green). Recommended off so the brand green is consistent; not yet decided.
- Final copy and localization remain draft (HANDOFF.md § 8).
