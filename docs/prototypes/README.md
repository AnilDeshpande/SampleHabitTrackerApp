# Habit Tracker V1 prototype — start here

This folder holds a draw.io sketch of the app's screens and flow, and the notes behind it. You only need the files in
**Read now**; the rest are there for reference or for the agent to resume from.

## Where we are

**Complete** — the project creator locked the flow on **2026-10-11** and requested the handoff. The creator chose to keep the separate journey pages and skip optional visual design.

The locked Android V1 prototype contains **7 destinations, 26 journeys, 121 numbered screen states and 95 transitions**, in both wireflow and click-through views. Native implementation is a separate task.

## Read now

| File | Why | Time |
|---|---|---|
| [HANDOFF.md](HANDOFF.md) | Approved experience, navigation, screen states, component mapping, draft copy, decisions and validation limits; start at Summary | 15–25 min |

The handoff links to all reference documents. The diagram uses fixed example states connected by page links; its fields do not accept live input. Each journey is an independent example.

## Opening the sketch

The sketch is [`flow.drawio`](flow.drawio). Open it in any of these:

- **VS Code** with the *Draw.io Integration* extension (`hediet.vscode-drawio`): open the file and
  it shows as a diagram you can edit.
- **draw.io Desktop** (free, macOS, Windows and Linux, from https://www.drawio.com): File › Open.
- **A browser**, nothing to install: https://app.diagrams.net, then File › Open from › Device.
  The file is read in the browser.

In Android Studio, don't count on a draw.io plugin; open the file in one of the above.

The pages are the tabs along the bottom of the window:

- **Wireflow**: one page per journey, named after it, with every screen (S1, S2, …) and arrow.
- **Both (this file)**: each journey wireflow is followed by its screen pages. Blue controls link onward. Standalone click-through exports use a separate Map page; this combined export uses the wireflow as its map.
- **Screen pages** (click-through, or both): one per screen state, named by its number, screen and
  state ("S1 Overview — Empty"). The blue controls link to the screen they lead to, and
  "↩ Back to the overview" returns to that journey’s diagram. This is a prototype navigation link outside the phone frame, not the app Back action. In an editor a click selects the shape: follow the link from
  the hint draw.io shows under it, or use the page tabs.

## All files

| File | For | What it is |
|---|---|---|
| `README.md` | reference | This guide and the journey index. |
| `HANDOFF.md` | you + native team | The complete handoff; read this first. |
| `notes/CLARIFICATIONS.md` | reference | Every question about the product, the answer proposed from your documents, and whether you approved it. |
| `notes/DECISIONS.md` | reference | What is agreed so far, and who decided it. |
| `notes/INPUTS.md` | reference | The documents used and the facts taken from them, with citations. Open it to check where a proposal came from. |
| `notes/STATE.md` | agent | Phase, gates passed and next step, so a new session can resume. You don't need to read it. |
| `flow.drawio` | reference | Both views: journey wireflows followed by numbered click-through pages. |
| `.flow.drawio.sha256` | agent | Generator stamp that protects manual diagram edits. |
| `flow.json` | agent | The flow as data. The sketch is generated from it; tell the agent what to change rather than editing it. |
<!-- Rows added by later stops, in this order:
| `flow.drawio` | reference | The sketch: every screen (S1, S2, …) and the arrows between them. See *Opening the sketch*. |
| `look/style-tile.html` | you | The design directions to choose from (design phase only). |
| `look/screens.html` | you | Every screen in the final Android / iOS look (design phase only). |
| `notes/DESIGN.md` | you | The chosen look, written down (design phase only). |
| `notes/THEME-REPORT.md` | reference | Colours and contrast results for the theme. |
| `HANDOFF.md` | you + native team | The final package for building the app. |
-->

## Assumptions for review

- A3/A4: portrait phone focus; no separate tablet/foldable journeys.
- C1/C2: draft English copy, realistic independent examples and October 2026 dates; numbers are illustrative.
- V1–V4: rough hand-drawn sketch, without a chosen brand or final visual design.
- Scenarios use fixed choices and values. A transition labelled as typing or time passing is read on the wireflow or followed using the page tabs.

## Journey index

Journey pages are named exactly as below. Each is followed by its numbered screen pages. Read the main path left to right; branches sit under the screen they interrupt. S-numbers identify screen states, not clarification question IDs.

| Journey page | Screens | Transitions / branches |
|---|---|---|
| Create, record and review | S1–S8 | S1 → S2 (Create habit); S2 → S3 (Save habit); S3 → S4 (Record progress); S4 → S5 (Save total); S5 → S6 (Next day / reopen); S6 → S7 (Review habit); S7 → S8 (Back) |
| Choose a schedule and first period | S9–S19 | S9 → S10 (Choose Daily); S10 → S11 (Save habit); S9 → S12 (Choose Weekly); S12 → S13 (Save habit); S9 → S14 (Choose Fortnightly); S14 → S15 (Save habit); S9 → S16 (Choose Monthly); S16 → S17 (Save habit); S9 → S18 (Choose Custom weekdays); S18 → S19 (Save habit) |
| Correct invalid habit inputs | S20–S23 | S20 → S21 (Save invalid draft); S21 → S22 (Edit fields); S22 → S23 (Save habit) |
| Correct the current total | S24–S29 | S24 → S25 (Record progress); S25 → S26 (Save 8 minutes); S25 → S27 (Enter 1e3 / Save); S27 → S28 (Edit total to zero); S28 → S29 (Save total) |
| Understand when tracking is due | S30–S33 | S30 → S31 (Next Monday); S31 → S32 (View off-day habit); S32 → S33 (Zone-change gap) |
| Edit name and a current target | S34–S37 | S34 → S35 (Edit habit); S35 → S36 (Save changes); S36 → S37 (Review habit) |
| Schedule targets for a later period | S38–S42 | S38 → S39 (Save changes); S39 → S40 (Edit target again); S40 → S41 (Save changes); S41 → S42 (Review habit) |
| Leave create habit with changes | S43–S46 | S43 → S44 (Back with changes); S44 → S45 (Keep editing); S44 → S46 (Discard) |
| Leave edit habit with changes | S47–S50 | S47 → S48 (Back with changes); S48 → S49 (Keep editing); S48 → S50 (Discard) |
| Leave record progress with changes | S51–S54 | S51 → S52 (Back with changes); S52 → S53 (Keep editing); S52 → S54 (Discard) |
| Handle a period ending before Save | S55–S59 | S55 → S56 (Save after midnight); S56 → S57 (Choose current period); S57 → S58 (Save total); S56 → S59 (Close) |
| Close a late draft when nothing is due | S60–S62 | S60 → S61 (Late Save); S61 → S62 (Close) |
| Allow or decline notifications | S63–S66 | S63 → S64 (Request once); S64 → S65 (Allow); S64 → S66 (Don’t allow) |
| Review and acknowledge missed periods | S67–S72 | S67 → S68 (Review habit); S68 → S69 (Back); S67 → S70 (Notification delivered); S70 → S71 (Tap notification); S71 → S72 (Back) |
| See a success and consistency badge | S73–S75 | S73 → S74 (Next day / reopen); S74 → S75 (Review habit) |
| Retire, undo or keep history | S76–S84 | S76 → S77 (Retire habit); S77 → S78 (Confirm Retire); S78 → S79 (Undo in time); S78 → S80 (10 seconds expire); S80 → S81 (Retired habits); S81 → S82 (Review Meditation); S82 → S83 (Back); S77 → S84 (Cancel) |
| Recover an expired Undo action | S85–S87 | S85 → S86 (Undo at deadline); S86 → S87 (View retired habits) |
| Recover a local read failure | S88–S90 | S88 → S89 (Local read fails); S89 → S90 (Retry) |
| Retry saving habit | S91–S94 | S91 → S92 (Save); S92 → S93 (Local save fails); S93 → S94 (Retry) |
| Retry saving changes | S95–S98 | S95 → S96 (Save); S96 → S97 (Local save fails); S97 → S98 (Retry) |
| Retry saving total | S99–S102 | S99 → S100 (Save); S100 → S101 (Local save fails); S101 → S102 (Retry) |
| Resume drafts without replaying actions | S103–S107 | S103 → S104 (System restores task); S104 → S105 (Save unchanged draft); S105 → S106 (Back); S104 → S107 (Remove task / reopen) |
| Resume multiple pending retirements | S108–S111 | S108 → S109 (Restart before deadlines); S109 → S110 (Undo Meditation); S110 → S111 (Reading deadline expires) |
| Record from review and return to Overview | S112–S115 | S112 → S113 (Record progress); S113 → S114 (Save total); S113 → S115 (Close unchanged) |
| Find retired history when the list is empty | S116–S118 | S116 → S117 (Retired habits); S117 → S118 (Back) |
| Change a target before the next occurrence | S119–S121 | S119 → S120 (Edit habit); S120 → S121 (Save changes) |

## Validation

- Generator check passed for both formats: no crossing/shared arrows, screen overlaps, obstructed labels, invalid identities or unreachable frames reported.
- Generated XML parsed: 147 pages; all 285 internal page links resolve to existing pages.
- Reviewed coverage against the approved decisions and the skill’s sketch-review checklist.
- Opened in draw.io Desktop, inspected the daily form, and followed Save from S2 to S3.
- Android Gradle checks do not apply: this phase creates prototype documents only.

## Next step

Handoff delivered. For later flow revisions, identify changes by S-number. Native implementation begins in a separate task using HANDOFF.md and the supplied product/design documents.
