# Android Personal App — Master Agent Instructions

## Project Goal

Build a polished, native Android application intended primarily for my personal use on a **Samsung Galaxy Z Fold 8 Ultra**.

The exact purpose of the application may evolve over time. Build the project so features can be added, removed, and reorganized without requiring major architectural rewrites.

This is NOT intended to be a generic cross-platform app.

Prioritize:

1. Excellent Galaxy Z Fold 8 Ultra support
2. Native Android functionality
3. Modern UI
4. Performance
5. Reliability
6. Maintainable code
7. Easy GitHub-based development
8. Easy APK generation and sideloading

Do not artificially restrict functionality because something would be inappropriate for a mass-market Play Store application. This is a personal application installed on my own devices.

However, continue to follow Android security best practices and do not unnecessarily request sensitive permissions.

---

# Technology Stack

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Material 3 Adaptive where appropriate
- AndroidX
- Kotlin Coroutines
- Kotlin Flow / StateFlow
- ViewModel architecture
- Navigation Compose
- Room for structured persistent data when appropriate
- DataStore for preferences/settings
- WorkManager for reliable background work when appropriate
- Jetpack Glance for Android home-screen widgets
- Gradle Kotlin DSL

Prefer official Android/Jetpack libraries over third-party dependencies whenever practical.

Do not introduce a dependency simply to avoid implementing a small amount of straightforward functionality.

---

# Architecture

Use a clean, modular architecture.

Suggested structure:

app/
    data/
        local/
        remote/
        repository/

    domain/
        model/
        repository/
        usecase/

    ui/
        components/
        navigation/
        screens/
        theme/

    widgets/

    services/

    workers/

    utilities/

Do not over-engineer simple features.

The application should have clear separation between:

- UI
- application state
- business logic
- data persistence
- external APIs
- Android system integrations

UI composables should generally not directly perform networking, database operations, or complex business logic.

---

# Galaxy Z Fold 8 Ultra Design Philosophy

This application should be designed specifically with the Galaxy Z Fold 8 Ultra form factor in mind.

Do NOT simply create a normal phone interface and stretch it when the phone is unfolded.

Treat the Fold as having multiple useful application environments.

## Cover Screen

Use a compact, focused interface.

Prioritize:

- one-column layouts
- quick actions
- glanceable information
- large touch targets
- minimal unnecessary navigation
- easy one-handed operation

Do not overcrowd the cover display.

---

## Inner Display

Take advantage of the additional space.

Prefer layouts such as:

- list + detail
- dashboard + detail panel
- navigation rail + content
- two-pane interfaces
- three-column dashboards where useful
- master/detail layouts
- larger charts and data visualizations

Do not simply make buttons, cards, and text dramatically larger.

Use the additional space to display MORE useful information.

---

# Adaptive Layouts

Base layout decisions primarily on available window size rather than checking for a specific model name.

Use Android window size classes and adaptive APIs.

The application should gracefully react to:

- cover screen
- unfolded inner screen
- portrait
- landscape
- split screen
- floating/multi-window environments when applicable
- folding/unfolding while the application is already running

Changing display state should NOT require restarting the application.

Where useful, account for folding features and hinge/posture information.

---

# Fold Continuity

The application must maintain state when I fold or unfold the phone.

For example:

If I am viewing:

Team → Player → Statistics

and unfold the device, I should still be looking at that player.

The larger interface may reveal additional panels or information, but my navigation state should remain intact.

Likewise, folding the device should intelligently collapse the interface rather than resetting it.

---

# Navigation

Adapt navigation to screen size.

Possible behavior:

### Compact layout
Use:

- bottom navigation
- navigation drawer
- compact top app bar

### Expanded layout
Prefer:

- navigation rail
- persistent side navigation
- multi-pane layouts

Do not blindly use the same navigation component everywhere.

---

# Visual Design

Target a sleek, modern Samsung/Android aesthetic.

General design characteristics:

- clean
- minimal
- information-dense when appropriate
- subtle animations
- rounded cards where appropriate
- strong typography hierarchy
- consistent spacing
- modern Material 3 components
- excellent dark mode

Avoid:

- excessive gradients
- excessive shadows
- giant empty areas
- enormous headings
- web-page-looking UI
- unnecessary animations
- clutter

The app should feel like a premium native Android application.

---

# Dynamic Color

Support Android dynamic color where appropriate.

Also provide an application theme that still looks intentional when dynamic color is disabled.

Support:

- system theme
- light theme
- dark theme

Store the preference using DataStore.

---

# Android System Integration

Because this is a native personal Android application, take advantage of Android capabilities when they improve the experience.

Potential integrations include:

- notifications
- notification actions
- app widgets
- Quick Settings tiles
- Android share sheet
- receiving shared content
- Android intents
- deep links
- app shortcuts
- clipboard
- camera
- photo picker
- location
- Bluetooth
- NFC
- biometric authentication
- vibration/haptics
- background work
- foreground services where legitimately required
- local files
- media controls
- picture-in-picture where appropriate

Do NOT add all of these automatically.

Use them when they provide genuine functionality for the application.

---

# Permissions

Use the minimum permissions necessary.

Permissions should be requested contextually.

Bad:

App opens → immediately asks for five permissions.

Good:

User selects "Enable location-based feature" → request location permission.

The application should continue functioning as much as possible when optional permissions are denied.

---

# Widgets

Architect the application so Android home-screen widgets can be added.

Use Jetpack Glance unless there is a strong technical reason not to.

Widgets should be designed separately from the main application's Compose UI.

Potential widget sizes:

- small
- medium
- large

Widgets should resize gracefully.

Useful widgets may include:

- status cards
- upcoming events
- quick actions
- statistics
- progress indicators
- recent activity
- favorites
- dashboards

Widget interactions should deep-link into the relevant part of the application whenever appropriate.

Example:

Tapping a player's widget should open that player's page rather than merely opening the application's home screen.

---

# Quick Settings Tiles

If the application eventually contains functionality that would benefit from a quick toggle or action, consider implementing an Android Quick Settings tile.

Examples:

- toggle a mode
- start an activity
- trigger an automation
- refresh information
- enable/disable a feature

Do not add Quick Settings tiles merely for novelty.

---

# Notifications

Build notifications using proper Android notification channels.

Notifications should support useful actions when applicable.

For example:

EVENT STARTING

Chiefs vs Broncos
Starts in 15 minutes

[OPEN] [DISMISS]

Allow notification categories to be independently controlled where useful.

---

# Haptics

Use subtle haptic feedback for meaningful interactions.

Good examples:

- completing an action
- changing a major toggle
- selecting an important option
- drag/drop completion

Do not vibrate for every ordinary button press.

---

# Local-First Philosophy

Whenever practical, make the app local-first.

Core functionality should not fail just because internet access is unavailable.

Use:

Room

for structured application data.

Use:

DataStore

for preferences and lightweight settings.

The UI should read from local state whenever practical while remote data synchronizes separately.

---

# Offline Support

Where appropriate:

1. Load cached/local data immediately.
2. Fetch new data in the background.
3. Update local storage.
4. Automatically update the UI.

Do not make users stare at loading screens unnecessarily when usable cached data exists.

---

# Networking

When external APIs are needed:

- isolate networking from UI
- use typed models
- handle HTTP errors
- handle timeouts
- handle malformed responses
- handle rate limits
- handle offline conditions
- cache appropriate data

Never place secrets directly in the GitHub repository.

If an API requires a secret that cannot safely exist inside an APK, recommend an appropriate backend/proxy architecture instead.

---

# State Management

Use unidirectional data flow.

Prefer:

Repository
↓
ViewModel
↓
StateFlow
↓
Compose UI

UI actions should flow back through ViewModels or appropriate controllers.

Avoid large amounts of mutable global state.

---

# Configuration Changes

Preserve important state across:

- rotation
- resizing
- folding
- unfolding
- multi-window changes

Do not rely on Activity recreation to solve layout changes.

---

# Performance

Prioritize smooth performance.

Avoid:

- unnecessary recompositions
- blocking the main thread
- excessive network calls
- excessively large images
- repeatedly recalculating expensive values
- excessive polling

Use coroutines appropriately.

Heavy work should not run on the main UI thread.

---

# Animations

Use animations primarily when they communicate spatial or state changes.

Examples:

Cover screen:
single pane

↓

Unfold device

↓

detail panel appears beside existing content

Animations should generally be subtle and fast.

Do not make the app feel like a demo reel.

---

# Accessibility

Even though this is a personal application, follow good accessibility practices.

Include:

- meaningful content descriptions
- appropriate touch target sizes
- readable contrast
- support for font scaling where practical

---

# Orientation

Do not unnecessarily lock orientation.

Design the application to handle portrait and landscape intelligently.

If a specific feature genuinely benefits from locking orientation, document why.

---

# App Icon

Include proper adaptive Android launcher icons.

Provide:

- foreground layer
- background layer
- monochrome icon when supported

Make sure the icon works with themed Android icons.

---

# Splash Screen

Use Android's native splash screen system.

Keep it simple.

Do not create an artificial multi-second splash animation.

---

# Package Naming

Use a consistent package namespace such as:

com.personal.[appname]

or another namespace specified later.

Do not change the application ID casually after development begins because Android treats different application IDs as different applications.

---

# Versioning

Use semantic-ish internal versioning.

Example:

1.0.0
1.1.0
1.1.1

Increment versionCode for every installable release build.

Display version information somewhere in Settings/About.

---

# Logging

Use structured logging during development.

Do not leave excessive debug logging enabled in release builds.

Errors should provide enough context to diagnose failures.

---

# Error Handling

Do not silently fail.

Provide useful UI states for:

- loading
- empty data
- offline state
- authentication failure
- permission denied
- API failure
- unexpected error

Where reasonable, include:

Retry

instead of forcing the application to restart.

---

# Testing

Create tests for important business logic.

Especially test:

- calculations
- sorting
- filtering
- state transformations
- database logic
- parsing
- algorithms

UI tests should cover critical workflows where reasonable.

Do not spend disproportionate effort testing trivial visual components.

---

# Fold-Specific Testing

Explicitly test:

### Cover display
- portrait
- landscape if supported

### Inner display
- portrait
- landscape

### Runtime transitions
- folded → unfolded
- unfolded → folded

Verify:

- no crashes
- navigation is preserved
- selected content remains selected
- layout changes correctly
- dialogs do not break
- text fields retain state
- scrolling behaves reasonably

---

# GitHub Repository

GitHub should be the source of truth for this project.

Maintain:

README.md
AGENT_INSTRUCTIONS.md
CHANGELOG.md

Use meaningful commits.

Avoid committing:

- API secrets
- passwords
- signing passwords
- private keys
- unnecessary generated files
- IDE-specific junk

Configure .gitignore correctly for Android Studio/Gradle.

---

# GitHub Actions

Create a GitHub Actions workflow that validates and builds the Android project.

For normal commits/pull requests:

1. checkout repository
2. configure Java
3. configure Gradle
4. restore/cache dependencies where appropriate
5. run tests
6. compile application
7. build debug APK
8. upload APK as a GitHub Actions artifact

The workflow should fail when:

- compilation fails
- required tests fail
- critical lint/build checks fail

---

# Release APK Workflow

Also create a workflow for release builds.

Preferred trigger:

GitHub Release or version tag

Example:

v1.2.0

Workflow:

Git tag
↓
GitHub Actions
↓
Build release APK
↓
Sign APK
↓
Verify APK
↓
Attach APK to GitHub Release

Target output:

app-v1.2.0.apk

---

# APK Signing

Release builds should use a persistent signing key.

The signing key must NOT be committed publicly to the repository.

Use GitHub Actions secrets for sensitive signing configuration.

Document the signing setup carefully.

The same signing identity must be preserved because future APK updates need to be compatible with the installed application.

Back up the signing key securely.

---

# Debug Builds

Debug APKs may use standard Android debug signing.

Make debug builds easy to retrieve from GitHub Actions for rapid testing.

---

# Installation Workflow

My preferred workflow should eventually be:

Coding agent modifies app
↓
changes pushed to GitHub
↓
GitHub Actions validates project
↓
APK generated
↓
APK downloaded on Galaxy Z Fold 8 Ultra
↓
APK installed
↓
test changes

Keep this workflow simple.

---

# Development Documentation

README.md should explain:

## What the app does

## Current features

## Project architecture

## How to build locally

## How to create an APK

## How GitHub Actions works

## Required permissions

## External APIs

## Known limitations

## Planned features

---

# Agent Change Log

Maintain CHANGELOG.md.

Use categories such as:

Added
Changed
Fixed
Removed

Example:

## 1.3.0

### Added
- Home-screen standings widget
- Expanded Fold dashboard

### Changed
- Redesigned cover-screen navigation

### Fixed
- Selected team disappearing after unfolding device

---

# Feature Development Process

When implementing a significant new feature:

1. Understand the feature.
2. Determine whether Android-native functionality is useful.
3. Determine how it should behave on the cover screen.
4. Determine how it should behave on the inner screen.
5. Determine whether it should have a widget.
6. Determine whether background work is necessary.
7. Determine required permissions.
8. Design data/state architecture.
9. Implement feature.
10. Test compact layout.
11. Test expanded layout.
12. Test folding/unfolding.
13. Run tests/build.
14. Update documentation.

---

# UI Feature Rule

For every major new screen, explicitly consider THREE layouts:

## Compact

What should this look like on the Fold cover screen?

## Expanded

What additional information or controls should appear on the inner display?

## Transition

What happens if the user unfolds or folds the phone while this screen is open?

Do not consider a screen complete until all three have been addressed.

---

# Feature Scope Rule

Do not implement massive speculative systems just because they may eventually be useful.

Build features incrementally.

Prefer:

working simple version
↓
test
↓
improve

over:

large unfinished architecture

---

# Refactoring Rule

Agents may refactor code when there is a clear architectural or maintenance benefit.

However:

Do not rewrite functioning sections of the application merely because another approach is stylistically preferable.

Preserve working behavior unless the refactor intentionally changes it.

---

# Dependency Rule

Before adding a dependency:

1. Determine whether Android/Jetpack already provides the functionality.
2. Verify the dependency is actively maintained.
3. Ensure it provides meaningful benefit.
4. Avoid libraries with excessive transitive dependencies for trivial functionality.

---

# Security Rule

Never:

- commit API keys
- commit passwords
- commit private signing keys
- disable certificate validation
- expose sensitive local information unnecessarily
- create insecure WebView JavaScript bridges
- request powerful Android permissions without a reason

This remains true even though the application is only for personal use.

---

# WebView Rule

Prefer native Compose interfaces.

A WebView may be used when:

- integrating existing web content
- embedding something specifically designed for the web
- a web-based component provides substantial development benefit

Do NOT use a WebView merely because HTML is easier.

The application should remain fundamentally native Android unless specified otherwise.

---

# Future AI-Agent Instructions

Before making significant changes:

Read:

1. README.md
2. AGENT_INSTRUCTIONS.md
3. CHANGELOG.md
4. relevant source files

Do not assume the architecture from filenames alone.

Inspect existing implementation before modifying it.

When finished:

1. ensure project compiles
2. run relevant tests
3. fix introduced warnings/errors
4. update CHANGELOG.md when appropriate
5. update README.md when functionality changes
6. summarize files changed
7. explain important architectural decisions
8. mention any remaining issues

---

# Do Not Leave Fake Implementations

Do not claim functionality exists when it is:

- hardcoded
- mocked
- placeholder-only
- visually represented but nonfunctional

If something is unfinished, clearly label it unfinished.

---

# No Silent Feature Removal

Do not remove existing functionality to make a new feature easier to implement unless specifically instructed.

If two systems conflict, preserve the existing functionality and explain the conflict.

---

# Personal App Philosophy

This application is being built for one primary user.

Therefore optimize for:

- my workflows
- my preferences
- my Galaxy Z Fold 8 Ultra
- speed of iteration
- useful Android integration

Compatibility with obscure devices is not a priority.

However, still use good adaptive Android architecture rather than hardcoding every UI measurement specifically to one resolution.

The Fold 8 should receive the best experience while the architecture remains technically sound.

---

# Current Application Idea

**Fuel** — a personal calorie, protein, fiber and calorie-deficit tracker.

Ported from an existing single-page PWA that remains in this repository
(`index.html`). The PWA is both the fallback and the design reference.

## Purpose

Log what I eat during the day, record what I burned, and see the resulting
deficit — per day and per week.

## Primary user workflow

Log food (most often from the home-screen widget, without opening the app)
→ check today's totals and deficit → review the month.

## Major screens

- **Log** — add a food item; re-log a recent food with one tap.
- **Today** — day navigation, macro totals, editable burned kcal, deficit,
  the day's items with inline edit/delete, and an add form for that day.
- **Calendar** — month grid with per-day calories and macro dots; weekly
  deficit totals.
- **Settings** — theme, dynamic colour, version.

## Data sources

Entirely local and offline. Room stores food entries and per-day burn values;
DataStore stores appearance preferences. No network access and no permissions.

## Android integrations

App widgets (Glance), deep links (`fuel://log`, `fuel://today`,
`fuel://calendar`), a floating quick-log activity launched from a widget,
haptics on log/save/delete, WorkManager for the daily widget refresh, native
splash screen, adaptive/monochrome launcher icon.

Not used yet: notifications, Quick Settings tiles, share sheet, camera,
location, biometrics.

## Widgets

- **Fuel · Quick Log** — the Log screen on the home screen: today's totals, an
  Add food button that opens the quick-log sheet, and one-tap re-logging of
  recent foods.
- **Fuel · Today** — today's calories, protein, fiber, burned and deficit, plus
  recent items at larger sizes.

Both use `SizeMode.Responsive` across small/medium/large and deep-link into the
app. Android does not allow text input inside a widget, so typing always happens
in the quick-log sheet.

## Notifications

None yet. Reminders are a candidate feature, not implemented.

## Fold-specific behavior

Cover screen: one column, bottom navigation. Unfolded: navigation rail plus two
panes (form beside live log; totals beside item list; month grid beside the
selected day). Tabletop posture stacks the panes. The selected day, visible
month and in-progress edit live in one activity-scoped ViewModel, so folding
rearranges the layout without resetting anything.

---

# Initial Project Milestone

**Status: complete as of 1.0.0** — the foundation below exists and builds. What
remains unverified is on-device behaviour (see README "Known limitations"); the
project has never been installed on hardware from this environment.

Until an application concept is chosen, the initial project should establish a clean foundation capable of supporting future features.

The starter project should include:

- working Android application
- Kotlin
- Jetpack Compose
- Material 3
- adaptive application structure
- compact/expanded layout demonstration
- navigation foundation
- theme system
- dark/light/system themes
- DataStore preferences
- Room-ready architecture
- placeholder dashboard
- Settings screen
- About screen
- launcher icon structure
- native splash screen
- basic test infrastructure
- GitHub Actions build workflow
- APK artifact generation
- documentation

Avoid building unnecessary domain-specific functionality until the actual application idea has been chosen.

The starter app should prove that:

- it builds
- it installs
- it runs correctly
- it adapts between Fold layouts
- GitHub Actions can generate an APK

That becomes the foundation for everything built afterward.
