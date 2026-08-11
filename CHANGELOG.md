# Changelog

## 1.4.0

### Added

- **Fuel · Rings** home-screen widget — calories, protein and fiber as progress
  rings against daily goals, with the quick-add button. Overshooting a goal
  starts a second, lighter lap rather than sitting at full. From roughly 4x3 up
  it switches layout: bigger rings, a burned/deficit line, and a full-width
  **+ Add food** button instead of the round one.
- **Daily goals** for calories, protein and fiber, set in Settings and stored in
  DataStore. These are what the rings fill against; setting one to 0 leaves that
  ring untracked rather than dividing by nothing. Defaults are 2,000 kcal,
  150g protein, 30g fiber.
- **Widget style** setting: Solid or Transparent, applied to all four widgets.
  Transparent keeps a light scrim and darkens the ring track instead of
  lightening it, so figures stay readable over a bright wallpaper.
- Unit tests for ring progress, including the over-goal lap, the two-lap ceiling
  and an untracked (zero) goal.

### Notes

- Rings are drawn to a bitmap and handed to Glance's `Image`: widgets render
  through `RemoteViews`, which has no canvas. Bitmaps are capped at 200px and
  scaled, keeping the RemoteViews payload well inside its size limit.

## 1.3.0

### Changed

- The Macros widget is now sized from the cell it actually occupies
  (`SizeMode.Exact`) instead of matching one of three fixed breakpoints, which
  meant a 4x2 cell drew the same small figures as a 4x1 and left most of the
  widget empty. Two rows tall or more it switches to a stacked layout —
  calories large on their own line with the button beside them, protein and
  fiber underneath — roughly doubling the type size in a 4x2 slot.
- Raised the Macros widget's resize ceiling from 120dp to 320dp; the old value
  was below two rows on a large phone.
- App layout now follows the window's real size rather than the width size
  class alone. The Fold's inner display in portrait is about 670dp wide — only
  MEDIUM, but wide enough for two panes, which it previously did not get. A
  wide, short landscape window now uses a navigation rail instead of a bottom
  bar. Split screen and free-form windows follow the same rule.

### Fixed

- The quick-log sheet scrolls, so the keyboard cannot clip it in landscape.
- The day navigator gives its date the space between the arrows instead of
  pushing them off a narrow window.
- The calendar grid is capped in width so its square cells do not grow enormous
  on the inner display or in landscape.

### Added

- Unit tests covering the layout rule at the window sizes a Fold produces:
  cover portrait and landscape, inner portrait and landscape, and split screen.

## 1.2.0

### Added

- Haptic feedback on every button, in the app and in the widgets, built on a
  four-level vocabulary (`FuelHaptic`): **Light** for navigation, day and month
  arrows and calendar days; **Press** for controls such as the edit button,
  theme chips and the dynamic-colour switch; **Confirm** when something is
  written — logging food, saving an edit, one-tap quick add; **Reject** for
  deleting an item or a submit refused for a missing name.
- Widget buttons are felt too: the Quick Log widget's one-tap re-log vibrates a
  Confirm directly, and the + buttons on the Macros and Quick Log widgets hand
  a Press to the quick-log sheet, which fires it as it opens.

### Changed

- Haptics moved out of individual screens and into the shared button components,
  so every button behaves the same way and nothing double-buzzes.

### Notes

- Adds the `VIBRATE` permission — normal, install-time, no runtime prompt. It is
  needed only for the widget paths, where there is no `View` to route feedback
  through. Text fields deliberately have no haptics.

## 1.1.0

### Added

- **Fuel · Macros** home-screen widget — a compact single-row widget showing
  today's calories, protein and fiber with a round plus button that opens the
  add-food sheet. Tapping the numbers opens the Today screen. Keeps all three
  figures down to roughly 2x1 by dropping the captions first.

## 1.0.0

The first native Android version of Fuel, ported from the PWA.

### Added

- Native Android app: Kotlin, Jetpack Compose, Material 3, Room, DataStore,
  Navigation Compose, WorkManager, package `com.personal.fuel`.
- Log screen — add food with calories, protein and fiber, plus one-tap re-logging
  of recently eaten foods.
- Today screen — day navigation, macro totals, editable burned-calories field,
  deficit, inline edit and delete of logged items, and an add form for the day
  being viewed.
- Calendar screen — month grid with per-day calories and macro dots, and
  per-week deficit totals.
- Settings screen — system/light/dark theme, optional dynamic colour, and
  version information.
- **Fuel · Quick Log** home-screen widget (Jetpack Glance): today's totals, an
  Add food button that opens a quick-log sheet over the home screen, and
  one-tap re-logging of recent foods. Resizes across three breakpoints.
- **Fuel · Today** home-screen widget: calories, protein, fiber, burned and
  deficit, plus recent items at larger sizes.
- Quick-log sheet (`QuickLogActivity`) — a floating add-food card launched from
  the widget, which saves and closes without opening the app.
- Deep links (`fuel://log`, `fuel://today`, `fuel://calendar`) so widget taps
  land on the relevant screen.
- Adaptive layouts: bottom navigation on the cover screen, navigation rail plus
  two-pane content on the unfolded inner display, stacked panes in tabletop
  posture.
- Fold continuity — selected day, visible month and in-progress edits survive
  folding, unfolding, rotation and multi-window changes.
- Dark and light themes carrying the original Fuel palette, with Syne and
  DM Sans bundled as app fonts.
- Adaptive launcher icon with foreground, background and monochrome layers,
  generated from the PWA icon, plus the native splash screen.
- Daily WorkManager job that refreshes widgets after midnight so "today" stays
  correct.
- Unit tests for the weekly-deficit grouping, day summary maths and formatting.
- GitHub Actions: build workflow (tests, lint, debug APK artifact) and release
  workflow (signed release APK attached to a tagged release).

### Changed

- Navigation moved from the web version's top pill tabs to Android-native
  navigation — a bottom bar on the cover screen and a rail when unfolded — for
  one-handed reachability. The visual language is unchanged.

### Notes

- The original PWA files remain in the repository and continue to work.
- PWA data is not imported yet; the Android app starts with an empty log.
