# Agent instructions — Fuel

The standing rules for this project live in
**[Android Personal App — Master Agent Instructions.md](Android%20Personal%20App%20—%20Master%20Agent%20Instructions.md)**.
Read that first. This file records what is specific to Fuel.

Before changing anything, read in order:

1. `README.md` — what exists and how it is built
2. the master instructions
3. `CHANGELOG.md`
4. the source files you intend to touch

---

## The application

Fuel is a personal calorie, protein, fiber and deficit tracker for a Galaxy
Z Fold 8 Ultra. It is a native port of a single-page PWA that still lives in
this repository (`index.html`, `manifest.webmanifest`, `sw.js`). Do not delete
the PWA files: they are the fallback and the design reference.

**Primary workflow:** log food throughout the day (usually from the home-screen
widget), check the day's totals and deficit, and review the month.

**Screens:** Log, Today, Calendar, Settings.

**Data:** entirely local. Room for the log and burn values, DataStore for
appearance preferences. No network, no permissions.

**Widgets:** Rings (goals), Macros (compact), Quick Log and Today — all Glance,
all resizable.

## Conventions

- Package namespace `com.personal.fuel`. Do not change the application ID.
- Dependencies are declared in `gradle/libs.versions.toml`, never inline.
- The design system lives in `ui/components` and `ui/theme`. Build new screens
  out of `FuelCard`, `SectionLabel`, `FuelTextField`, `MacroFieldRow`,
  `FuelButton`, `TotalTile`. Do not introduce a second visual language.
- `FuelTheme.colors` carries the colours Material 3 has no slot for (macro
  accents, extra surfaces, hairline borders). Macro colours encode meaning and
  must stay branded even under dynamic colour.
- Screen state that should survive folding belongs in `FuelViewModel`, not in
  `remember` inside a composable. Draft text in a form is the exception — use
  `rememberSaveable`.
- Every write goes through `FuelRepository`, which notifies the widgets. Do not
  write to the DAO directly from UI or widget code.
- Work started from a widget or a sheet that finishes immediately must run on
  `AppContainer.applicationScope`, never on a `viewModelScope` that is about to
  be cancelled.

## Haptics

- Every button gives feedback. Use `rememberFuelHaptics()` and the `FuelHaptic`
  vocabulary — `Light` to move, `Press` for a control, `Confirm` when something
  is written, `Reject` when something is removed or refused.
- Fire the haptic inside the shared component (`FuelButton`, `FuelTextButton`,
  `CircleIconButton`), not at the call site, so nothing double-buzzes. The only
  call-site haptics are outcomes the component cannot know about, such as a
  submit rejected for a blank name.
- Text fields get none; the keyboard already provides its own.
- Where there is no `View` — widget callbacks, an Activity launched by a widget
  button — use `FuelVibration` instead, which mirrors the same four levels.

## The three-layout rule

Every new screen must answer all three, as the master instructions require:

- **Compact** — how it looks on the cover screen, in both orientations.
- **Expanded** — what extra information fills the inner display. Add panes, do
  not scale everything up.
- **Transition** — what happens when the device folds or rotates mid-use. State
  lives in the ViewModel, so the answer should be "the layout changes and
  nothing else".

Layout thresholds live in `ui/navigation/FuelLayout.kt` and are unit tested
against the window sizes a Fold produces. Change them there, not inline.

## Widgets

- Glance only. Widgets render a snapshot in `provideGlance`; they do not observe
  flows. `GlanceWidgetNotifier` redraws them after every write — add any new
  widget to it, or it will silently go stale.
- App widgets cannot host text input. Anything requiring typing opens
  `QuickLogActivity` over the home screen instead of the full app.
- Widget colours come from `FuelGlanceColors`, not the app theme — widgets sit
  on the wallpaper. Respect the `WidgetBackground` setting: use
  `background.panel()`, `rowSurface()`, `captionColor()` and `ringTrack()`
  rather than hardcoding, and check any new widget against a light wallpaper in
  transparent mode.
- Glance has no canvas. Anything that is not a box, row or text must be drawn to
  a bitmap (`RingRenderer`) and passed to `Image`. Keep bitmaps small — they
  travel inside the size-limited RemoteViews payload.
- Text is in sp and widget geometry is in dp, so a figure placed inside a fixed
  shape must be divided by the system font scale or it will clip.
- `SizeMode.Responsive` with the breakpoints in `FuelWidgetSizes` suits widgets
  with a few distinct states. Where the type should scale continuously with the
  cell — as in `MacrosWidget` — use `SizeMode.Exact` and derive sizes from
  `LocalSize`, capping against both width and height so nothing overflows.
- Check a new widget at 2x1, 4x1, 2x2 and 4x2 before calling it done.

## The database

Room schemas are exported to `app/schemas` and the database has no destructive
fallback, deliberately: a schema change without a migration must fail loudly in
testing rather than silently wipe the user's log on their phone. Any change to
an entity needs a version bump and a real `Migration`.

## Signing

Every APK, debug included, must be signed with the persistent keystore. An
identity change forces an uninstall, and an uninstall destroys the user's log.
Do not add an `applicationIdSuffix` or let a build type fall back to the
generated debug key in CI.

## Before finishing a change

```bash
./gradlew testDebugUnitTest assembleDebug lintDebug
```

Then update `CHANGELOG.md`, and `README.md` if behaviour changed.

Do not claim a feature works if it is mocked, hardcoded or unverified — say
plainly what was tested and what was not.
