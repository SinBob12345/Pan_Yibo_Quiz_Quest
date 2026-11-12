# QuizQuest

An Android education app for secondary-school learners (13–17) that turns general
knowledge into a game of worlds and levels — designed around privacy and
"motivation without manipulation".

> CP3406 Assessment 3 · Yibo Pan · James Cook University
> _TODO (Stage 10): add 3–5 screenshots here (phone + tablet, light + dark)._

## Features

| Feature | How it is implemented |
|---|---|
| Worlds & levels | _TODO_ |
| Quiz with 50/50 hint and optional timer | _TODO_ |
| Offline play | _TODO_ |
| Review flashcards (spaced repetition) | _TODO_ |
| Statistics | _TODO_ |
| Settings | _TODO_ |
| Privacy: export & delete my data | _TODO_ |

## Screens

Home (landing) · World map · Quiz (activity) · Result · Review · Stats · Settings · Privacy

## Architecture

MVVM with the Repository pattern and Koin dependency injection (Lecture 4), organised by layer:

```
views/      Compose screens (XScreen + stateless XScreenContent)
   │  collectAsStateWithLifecycle()        ▲ user events
   ▼                                       │
viewmodel/  XViewModel exposes StateFlow<XUIState>   ── uses ──►  data/rules (pure Kotlin)
   │
   ▼
data/repository/  XRepository interfaces ◄── XRepositoryImpl
                                              ├── data/network  Retrofit → Open Trivia DB (Lecture 5)
                                              └── data/database Room, offline first (Lecture 7)
navigation/  Screens sealed class + NavHost (Lecture 6)
di/          Koin appModules (Lecture 4)
```

- Navigation: Jetpack Navigation with string routes and `navArgument`s; a bottom
  `NavigationBar` on phones and a `NavigationRail` on large screens, chosen with the
  window size class (Lecture 3).
- _TODO (Stage 10): short notes on the Room schema and AutoMigration, the offline cache and the rate limiter._

## Course alignment

| Lecture | Used for |
|---|---|
| 1 Kotlin & Android Studio | data classes, enums, project setup (minSdk 24) |
| 2 Compose layouts | Column/Row/Box, LazyColumn, LazyVerticalGrid, previews |
| 3 Material 3, large screens, accessibility | theme, Scaffold/TopAppBar/cards, WindowSizeClass, semantics |
| 4 Architecture | MVVM, repository pattern, Koin |
| 5 Network calls | Retrofit, kotlinx.serialization, coroutines, StateFlow, NetworkResult |
| 6 Navigation | sealed `Screens` class, NavHost, arguments, back stack |
| 7 Room | entities, DAOs, TypeConverters, offline first, AutoMigration |
| 8 Debugging | Logcat (debug only), LeakCanary, App Inspection |
| 9 Publishing | release checklist, localisable strings, data safety, target audience |

## Data source & attribution

Questions come from the [Open Trivia Database](https://opentdb.com/), licensed under
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). Questions are
community-written and may contain mistakes; learners can report a question to hide it.

Sound effects (`res/raw/sfx_*.wav`) were generated for this project.

## Ethical design

Based on my Assessment 2 presentation (location privacy and dark patterns).

| A2 commitment | Where it is in the app |
|---|---|
| No location permission | Manifest has only `INTERNET` |
| Local storage, one-tap export and delete | _TODO_ |
| Opt-in notifications | The app sends no notifications at all |
| Honest data-safety declaration, plain-language notice | Privacy screen + table below |
| No ads/SDKs; dependencies reviewed | Dependency review below |
| Motivation without manipulation | No streaks; equal-weight buttons; "done for today" cue |
| Accessible by default | See Accessibility |
| Ethics review per feature | `docs/ethics-review.md` |

### Data safety (as it would be declared on Google Play — Lecture 9)

| Data | Collected? | Shared? | Notes |
|---|---|---|---|
| Location | No | No | No location permission |
| Personal info (name, email, age) | No | No | No accounts |
| App activity (scores, answers) | Stored on device only | No | Deleted by the retention setting or "Delete all my data" |
| Device identifiers / advertising ID | No | No | |
| Network | — | — | HTTPS requests to opentdb.com contain only category, difficulty, amount and a random session token. Like any web server, opentdb.com can see the device's IP address. |

_TODO (Stage 10): re-check this table against the final code._

### Dependency review

| Library | Lecture | Purpose | Collects or sends user data? |
|---|---|---|---|
| AndroidX Core / Activity / Lifecycle | 1 | App framework | No |
| Jetpack Compose, Material 3, Adaptive | 2, 3 | UI, window size class | No |
| Koin | 4 | Dependency injection | No |
| Retrofit, OkHttp, kotlinx.serialization, Coroutines | 5 | Calls to opentdb.com only | No |
| Navigation Compose | 6 | Navigation | No |
| Room | 7 | Local database on the device | No |
| LeakCanary | 8 | Memory-leak detection, **debug builds only** | No (stays on the device) |
| JUnit, AndroidX Test, Compose UI test, coroutines-test | — | Tests only (not in the app) | No |

## Accessibility

- 48 dp touch targets, content descriptions, click labels, heading semantics, state
  descriptions, font scaling (Lecture 3).
- Contrast-checked colour palette (dynamic colour off), light and dark themes.
- Correct/incorrect feedback uses icon + text, not colour alone.
- "Reduce motion" setting.
- _TODO (Stage 8): Accessibility Scanner results / screenshots._

## Testing

```bash
./gradlew testDebugUnitTest          # JVM unit tests (rules, mappers, repositories, ViewModels)
./gradlew connectedDebugAndroidTest  # Room DAO and Compose UI tests (needs an emulator)
```

_TODO (Stage 10): list the main test classes and what they cover._

## Build & run

1. Open the project in Android Studio and let Gradle sync.
2. Run the `app` configuration on an emulator or device (Android 7.0 / API 24+).

## AI use

Generative AI was used during development and is acknowledged in the Declaration of
AI-Generated Material submitted with this assignment.
