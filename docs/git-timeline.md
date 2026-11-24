# Git Timeline

> Generated from git log by the AI agent – facts only

---

## 2026-11-24

| Date | Stage | What changed |
|---|---|---|
| 2026-11-24 | A1–A5 / Refinements | AI-drafted notes for five-step ACS ethics reviews in `docs/ethics-review-draft.md` |
| 2026-11-24 | A1–A5 / Refinements | Add `ContrastChecker` and `ThemeContrastTest` for WCAG 2.1 AA color contrast validation |
| 2026-11-24 | A1–A5 / Refinements | Add star-based neutral guidance text and liveRegion semantics on Result screen |
| 2026-11-24 | A1–A5 / Refinements | Simplify privacy disclosures and flashcard box labels in plain language |
| 2026-11-24 | A1–A5 / Refinements | Relocate retention controls to Privacy screen and set 30-day default retention |
| 2026-11-24 | A1–A5 / Refinements | Allow learners to restore reported questions with equal-weight confirmation dialog |
| 2026-11-24 | A1–A5 / Refinements | Export reported questions and dynamic app version in `UserDataExport` |
| 2026-11-24 | A1–A5 / Refinements | Fix operator assignment syntax in `FakeDaos.kt` |

---

## 2026-11-20

| Date | Stage | What changed |
|---|---|---|
| 2026-11-20 | A1–A5 / Refinements | Fix operator assignment syntax in `FakeDaos.kt` |
| 2026-11-20 | A1–A5 / Refinements | Ensure `AndroidSoundPlayer` loads `sfx_wrong.wav` via application context for wrong answer audio |
| 2026-11-20 | A1–A5 / Refinements | Observe `mainUIState.themeMode` in `MainActivity.setContent` for instant theme recomposition |
| 2026-11-20 | A1–A5 / Refinements | Constrain text `Column` with `weight(1f)` in `RecentResultRow` to prevent star icon clipping |
| 2026-11-20 | A1–A5 / Refinements | Sync due review cards in `HomeViewModel` dynamically via `LifecycleResumeEffect` |
| 2026-11-20 | Stage 10 / Documentation | Complete project `README.md` features, architecture, and testing summary tables |
| 2026-11-20 | Stage 10 / Release | Remove unused `StageNotice.kt`, delegate theme selection to `MainViewModel`, and update `versionName` to 1.0.0 |

---

## 2026-11-19

| Date | Stage | What changed |
|---|---|---|
| 2026-11-19 | A1–A5 / Refinements | Apply shorter answer retention immediately and preserve `used = true` flag on question re-downloads |
| 2026-11-19 | A1–A5 / Refinements | Next-level button on Result screen uses cumulative best-stars record |
| 2026-11-19 | A1–A5 / Refinements | Resolve real category names from Room in `WorldMapViewModel` and `StatsScreen` |
| 2026-11-19 | A1–A5 / Refinements | Export learner data as a file via Storage Access Framework `CreateDocument` picker |
| 2026-11-19 | A1–A5 / Refinements | Add plain-language privacy notice and neutral delete confirmation dialog |
| 2026-11-19 | A1–A5 / Refinements | Render offline error state with Retry button on Home screen when categories fail |
| 2026-11-19 | A1–A5 / Refinements | Add try/catch error handling and retry mechanism for quiz result saving |
| 2026-11-19 | A1–A5 / Refinements | Display "Time's up" feedback and preserve countdown timer across 50/50 hint usage |
| 2026-11-19 | A1–A5 / Refinements | Refactor `QuizScreen` option button layout for phone and two-pane tablet views |
| 2026-11-19 | Stage 3 / Game Rules | Integrate `QuizEngine.skipCurrent` in `QuizViewModel` for question reporting |
| 2026-11-19 | Stage 3 / Game Rules | Add `skipCurrent` state transition helper to `QuizEngine.kt` |
| 2026-11-19 | Stage 7 / Room Database | Prevent reported questions from re-appearing in review flashcards |

---

## 2026-11-18

| Date | Stage | What changed |
|---|---|---|
| 2026-11-18 | Stage 7 / Flashcards | Refresh due review cards dynamically in `ReviewViewModel` |
| 2026-11-18 | Stage 7 / Flashcards | Fix card queue selection in `ReviewViewModel.kt` |
| 2026-11-18 | Stage 8 / Debugging | Add debug-only logging helper `AppLog` with `BuildConfig.DEBUG` checks |
| 2026-11-18 | Stage 8 / Testing | Add Compose UI tests and end-to-end `AppNavigationTest` |
| 2026-11-18 | Stage 3 / Accessibility | Add content descriptions, heading semantics, state descriptions, and adaptive 600 dp content width |
| 2026-11-18 | Stage 5 / UI Effects | Animate quiz question transitions with "Reduce motion" support and add `SoundPool` sound effects |
| 2026-11-18 | Stage 9 / Privacy | Add plain-language Privacy screen and feedback email channel |

---

## 2026-11-17

| Date | Stage | What changed |
|---|---|---|
| 2026-11-17 | Stage 7 / Privacy | Add "Delete All Data" and background answer history retention purging |
| 2026-11-17 | Stage 7 / Privacy | Export all learner data as JSON string container |
| 2026-11-17 | Stage 7 / Flashcards | Add Leitner 5-box spaced repetition flashcard review system |
| 2026-11-17 | Stage 3 / Layouts | Add adaptive two-pane layout for Quiz and Stats on wide tablet screens |
| 2026-11-17 | Stage 7 / Statistics | Add statistics screen displaying accuracy %, daily activity chart, and recent attempts |
| 2026-11-17 | Stage 6 / Screens | Display today's summary card and daily goal stopping cue on Home screen |
| 2026-11-17 | Stage 3 / Theme | Apply light, dark, and system theme preferences across the application |
| 2026-11-17 | Stage 6 / Screens | Build Settings screen with questions per level, timer mode, and daily goal options |

---

## 2026-11-15

| Date | Stage | What changed |
|---|---|---|
| 2026-11-15 | Stage 6 / Screens | Build Result screen with equal-weight "Back to map" and "Next level" actions |
| 2026-11-15 | Stage 6 / Screens | Add leave confirmation dialog before exiting an in-progress quiz level |
| 2026-11-15 | Stage 6 / Screens | Build Quiz screen with option selection, immediate feedback, and 50/50 hint |
| 2026-11-15 | Stage 6 / Screens | Render World Map level grid with star ratings and locked level states |
| 2026-11-15 | Stage 5 / ViewModels | Implement `ResultViewModel` with daily goal tracking |
| 2026-11-15 | Stage 5 / ViewModels | Implement `QuizViewModel` with hint management, timers, and session saving |
| 2026-11-15 | Stage 5 / ViewModels | Build World Map level grid state from saved Room progress |
| 2026-11-15 | Stage 5 / Fakes | Add in-memory fake repositories for ViewModel unit tests |
| 2026-11-15 | Stage 4 / Testing | Add unit tests for Progress, Review, Settings, Stats, and Trivia repositories |
| 2026-11-15 | Stage 4 / Repositories | Add Progress, Review, Stats, and Settings repository implementations |
| 2026-11-15 | Stage 4 / Repositories | Make `TriviaRepositoryImpl` offline-first backed by Room database |

---

## 2026-11-13

| Date | Stage | What changed |
|---|---|---|
| 2026-11-13 | Stage 4 / Mappers | Implement bi-directional mapping functions between Room entities and domain models |
| 2026-11-13 | Stage 4 / Migrations | Add `timeTakenMs` column to `AnswerRecord` table with Room AutoMigration to version 2 |
| 2026-11-13 | Stage 6 / Navigation | Fix Jetpack Navigation route arguments and back stack handling |
| 2026-11-13 | Stage 4 / Room Database | Add DAOs and `QuizDatabase` Room database configuration |
| 2026-11-13 | Stage 4 / Room Database | Add Room database entities and `@TypeConverters` for string lists |
| 2026-11-13 | Stage 3 / Game Rules | Add `DailyGoalTracker` and answer retention policy rules |
| 2026-11-13 | Stage 3 / Game Rules | Add `LeitnerScheduler` spaced repetition flashcard scheduling rules |
| 2026-11-13 | Stage 3 / Game Rules | Add `AnswerShuffler` and `QuizEngine` state machine |
| 2026-11-13 | Stage 3 / Game Rules | Add `StarRating` and `LevelPolicy` level unlocking rules |
| 2026-11-13 | Stage 2 / Network | Add Retrofit API setup and `TriviaRepository` interface |
| 2026-11-13 | Stage 2 / Network | Map OpenTDB API response DTOs into domain models |
| 2026-11-13 | Stage 2 / Rules | Restrict OpenTDB trivia requests to age-appropriate categories via `CategoryFilter` |

---

## 2026-11-12

| Date | Stage | What changed |
|---|---|---|
| 2026-11-12 | Stage 2 / Network | Add HTML text decoder (`TextDecoder`) and API `RateLimiter` |
| 2026-11-12 | Stage 1 / Setup | Initial commit: project structure, dependencies, and baseline Gradle configuration |
