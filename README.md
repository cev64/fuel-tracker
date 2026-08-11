# Fuel

A personal calorie, protein, fiber and deficit tracker for a Samsung Galaxy
Z Fold 8 Ultra, built as a native Android app.

Fuel started as a single-page PWA. Those files still live in this repository and
still work (see [PWA-SETUP.md](PWA-SETUP.md)); the Android app is a native port
of the same design, with home-screen widgets that the web version could never
have.

---

## What the app does

Log what you eat, see the day's totals, record what you burned, and track the
resulting deficit across a month.

Everything is stored on the phone. There is no account, no server, and no
network access of any kind.

## Current features

**Log** — add a food item with calories, protein and fiber. Anything logged
before can be re-added with one tap from the Quick add row.

**Today** — day-by-day view with the three macro totals, an editable "burned
kcal" field, the resulting deficit, the list of items logged (edit or delete
inline), and an add form for that specific day.

**Calendar** — a month grid showing each day's calories and which macros were
tracked, plus a per-week deficit total. Tapping a day opens it.

**Settings** — theme (system / light / dark), optional Android dynamic colour,
and version information.

**Haptics** — every button responds, with a four-level vocabulary so the phone
tells you what happened before you have read the screen: a light tick for moving
around (navigation, day and month arrows, calendar days), a firmer press for
controls, a distinct confirm pattern when something is written (food logged,
edit saved), and a reject pattern for deletes or a refused submit. Text fields
have none — the keyboard already provides its own. Widget buttons buzz too. The
phone's own touch-feedback settings, including Samsung's intensity slider, still
apply.

**Widgets** — see below.

### Widgets

Three home-screen widgets, all built with Jetpack Glance, all resizable:

| Widget | Size | What it does |
| --- | --- | --- |
| **Fuel · Macros** | 3x1 | The compact one. Today's calories, protein and fiber, plus a round **+** button that opens the add-food sheet. Tapping the numbers opens Today. |
| **Fuel · Quick Log** | 4x2 | The Log screen on the home screen. Today's totals, an **Add food** button that opens a quick-log sheet over the home screen, and recently logged foods that are re-logged to today with a single tap. |
| **Fuel · Today** | 4x2 | Today's calories, protein, fiber, burned and deficit. At larger sizes it also lists the most recent items logged. Tapping it opens the Today screen. |

All three widgets resize across three breakpoints and show progressively more
information as they grow. Macros keeps all three numbers down to roughly 2x1,
dropping the captions before it drops any figure. Widget taps deep-link into the
relevant screen rather than just opening the app.

**A note on typing in widgets:** Android does not allow text input inside an app
widget — a widget renders through `RemoteViews`, which has no editable field. So
"Add food" opens a small quick-log sheet that floats over the home screen,
starts with the keyboard already up, saves, and closes. It never opens the full
app. One-tap re-logging of a recent food needs no sheet at all.

Widgets are refreshed whenever data changes (including edits made inside the
app), and once a day just after midnight so "today" stays correct.

## Project architecture

```
app/src/main/java/com/personal/fuel/
    data/
        local/          Room entities, DAO, database
        prefs/          DataStore appearance settings
        repository/     FuelRepositoryImpl
    domain/
        model/          FoodEntry, DaySummary, WeekSummary, ThemeMode
        repository/     FuelRepository, WidgetNotifier
        usecase/        BuildWeekSummaries
    ui/
        components/     Cards, tiles, fields, toast — the Fuel design system
        navigation/     Top-level destinations and deep links
        screens/        log, today, calendar, settings
        quicklog/       QuickLogActivity (the widget's add-food sheet)
        theme/          Colours, typography, Material 3 theme
        FuelRoot.kt     Adaptive shell: bottom bar, rail, two-pane layouts
        FuelViewModel   Shared UI state (selected day, month, edit target)
    widgets/            Glance widgets and their actions
    workers/            Daily widget refresh
    utilities/          Formatting
```

Data flows one way: Room → repository → `FuelViewModel` → `StateFlow` → Compose.
Writes go back through the ViewModel. Dependencies are wired by hand in
`AppContainer` (`FuelApp.kt`) — widgets and workers reach it from a plain
`Context`, which is why there is no DI framework here.

### Fold behaviour

Layout follows the window size class, not the device model:

- **Compact (cover screen)** — single column, bottom navigation, large targets.
- **Medium** — navigation rail, single column.
- **Expanded (unfolded)** — navigation rail plus two panes: the Log form beside
  the live day log; the day's totals beside its item list; the month grid beside
  the selected day and the weekly deficits.
- **Tabletop posture** — panes stack vertically instead of side by side.

Folding and unfolding never restarts the app or resets state: the activity
handles the configuration change itself, and the selected day, visible month and
in-progress edit all live in one activity-scoped ViewModel that both layouts read
from.

## How to build locally

Requirements: JDK 17 and the Android SDK (compileSdk 35, build-tools 35).

```bash
./gradlew testDebugUnitTest     # unit tests
./gradlew assembleDebug         # debug APK
./gradlew lintDebug             # lint
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. It
installs alongside a release build (the debug variant uses the
`com.personal.fuel.debug` application ID).

If Gradle cannot find the SDK, add a `local.properties` file with
`sdk.dir=/path/to/android-sdk`. That file is deliberately not committed.

## How to create an APK

Day-to-day, let GitHub build it:

1. Push to a branch.
2. The **Android build** workflow runs tests and lint, then builds a debug APK.
3. Download `fuel-debug-apk` from the workflow run's Artifacts section on the
   phone, and open it to install.

For a versioned build, tag a release:

```bash
git tag v1.2.0 && git push origin v1.2.0
```

The **Android release** workflow builds, signs and verifies a release APK, then
attaches `fuel-v1.2.0.apk` to the GitHub release. Bump `versionCode` and
`versionName` in `app/build.gradle.kts` before tagging.

## How GitHub Actions works

`.github/workflows/android-build.yml` — runs on every push and pull request:
unit tests, lint, debug APK, uploaded as an artifact along with the test and
lint reports. The build fails if compilation fails, a test fails, or lint
reports an error.

`.github/workflows/android-release.yml` — runs on a `v*` tag (or manually):
tests, signed release APK, `apksigner` verification, attached to the release.

### APK signing

Release builds must keep the same signing identity forever — Android refuses to
install an update signed with a different key. Create the keystore once, back it
up somewhere safe, and never commit it:

```bash
keytool -genkeypair -v -keystore fuel-release.jks -alias fuel \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 fuel-release.jks    # paste the output into the secret below
```

Then add four repository secrets (Settings → Secrets and variables → Actions):

| Secret | Value |
| --- | --- |
| `FUEL_KEYSTORE_BASE64` | base64 of `fuel-release.jks` |
| `FUEL_KEYSTORE_PASSWORD` | keystore password |
| `FUEL_KEY_ALIAS` | `fuel` |
| `FUEL_KEY_PASSWORD` | key password |

The release workflow fails loudly if the keystore secret is missing rather than
publishing an APK signed with a debug key.

To build a signed release locally, put the same values in a `keystore.properties`
file in the repository root (`storeFile`, `storePassword`, `keyAlias`,
`keyPassword`). It is gitignored.

## Required permissions

`VIBRATE` only — a normal, install-time permission with no runtime prompt.

It is needed solely where haptics cannot be routed through a `View`: widget tap
callbacks (which draw through the launcher) and the quick-log sheet launched by
a widget button. Everything inside the app uses `View.performHapticFeedback`,
which needs no permission.

No network, no storage, no location, no notifications.

## External APIs

None. Nothing leaves the phone.

## Known limitations

- **Existing PWA data is not imported yet.** The Android app starts with an
  empty database; the web version keeps its own data in browser storage. An
  import path is planned (see below).
- **Widgets cannot accept typed input.** This is an Android platform limit, not
  a shortcut — see the note under Widgets.
- **Release builds are not minified.** R8 is switched off for now so the first
  sideloaded builds are as close to the tested debug build as possible; the APK
  is around 24 MB as a result. Keep rules are already written in
  `app/proguard-rules.pro` for when it is turned on.
- **Widget haptics are approximations.** A widget cannot vibrate from the
  launcher's process, so the buzz comes from this app's vibrator instead: the
  one-tap re-log fires it directly, and a widget's + button hands it to the
  quick-log sheet, which fires it as it opens. Expect a few milliseconds more
  delay than an in-app button.
- **Widget text uses the system font.** App widgets cannot load bundled fonts,
  so Syne and DM Sans appear in the app but not on the home screen.
- **Dependencies are pinned to a known-good set.** Newer AndroidX and AGP
  releases exist; upgrading is its own task with its own testing.

## Planned features

- Import the existing PWA log (`fuel_v1` / `fuel_burns` JSON) into Room.
- Daily calorie/protein/fiber goals, with widget progress against them.
- A widget for the selected day rather than only today.
- Daily goals shown as progress on the Macros widget.
- Optional reminder notifications.
- Turn on R8 for release builds.
