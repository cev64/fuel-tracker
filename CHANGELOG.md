# Changelog

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
