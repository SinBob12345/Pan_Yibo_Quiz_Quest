# QuizQuest

An Android education app for secondary-school learners (13–17) that turns general
knowledge into a game of worlds and levels — designed around privacy and
"motivation without manipulation".

> CP3406 Assessment 3 · Yibo Pan · James Cook University

---

## Detailed Features Overview

QuizQuest delivers a complete, interactive, and privacy-focused learning experience built around gamified knowledge exploration, intelligent memory reinforcement, and ethical user design.

### 1. Gamified Knowledge Worlds & Level Progression
- **9 Distinct Knowledge Worlds:** Explore diverse subject domains including General Knowledge, Science & Nature, History, Geography, Entertainment, Sports, Literature, Art, and Mythology.
- **Structured 9-Level Map:** Every world features 9 progressive levels categorized by difficulty: Easy (Levels 1–3), Medium (Levels 4–6), and Hard (Levels 7–9).
- **Star Rating System:** Earn 0 to 3 stars per level based on quiz accuracy ($\ge 80\%$ for 3 stars, $\ge 50\%$ for 2 stars, $\ge 1$ star to pass and unlock the next level).
- **Non-Blocking Progression:** Levels unlock progressively when earning at least 1 star. Learners can replay previously unlocked levels anytime to improve their star ratings without losing access to higher levels.

### 2. Interactive Quiz Engine & Accessibility Feedback
- **Multiple Choice & True/False Questions:** Dynamically rendered quiz questions with deterministically shuffled options (`AnswerShuffler`).
- **50/50 Lifeline Hint:** Use 50/50 hints to remove two incorrect options on challenging multiple-choice questions (2 hints provided per level).
- **Per-Question Timer Modes:** Configure timer preferences in Settings—Timer Off (default), Relaxed (30 seconds per question), or Challenge (15 seconds per question)—featuring live animated progress bars and clear "Time's up" feedback.
- **Color-Independent Feedback:** Post-answer feedback cards display prominent status cards, explicit text ("Correct!", "Time's up", "Incorrect"), icons (`CheckCircle`, `Timer`, `Close`), and TalkBack screen reader state descriptions (`"Correct answer"`, `"Your answer, incorrect"`).
- **Audio Feedback (`SoundPool`):** Optional, custom-synthesized sound effects for correct and wrong answers (toggable in Settings).
- **Smooth Motion & Reduce Motion:** Fluid Compose transitions (`AnimatedContent`, `AnimatedVisibility`, `animateFloatAsState`) with a dedicated "Reduce motion" setting that overrides all animations with instant `snap()` transitions.

### 3. Spaced Repetition Flashcards (Leitner Review System)
- **Automatic Memory Cards:** Questions answered incorrectly during quizzes are automatically saved into a 5-box Leitner flashcard review queue.
- **Adaptive Review Scheduling:** Leitner box intervals adjust review timing: Box 1 (1 day), Box 2 (3 days), Box 3 (7 days), Box 4 (14 days), Box 5 (30 days). Answering correctly promotes a card to the next box; answering incorrectly returns the card to Box 1 for reinforcement.
- **Lifecycle Auto-Refresh:** The Review tab utilizes `LifecycleResumeEffect` to automatically check and refresh due flashcards whenever the learner opens or resumes the screen.
- **Double-Tap Protection & Safety:** Prevents accidental double-taps during card updates (`isUpdating` guard) and handles error recovery gracefully.
- **Gentle Stopping Cue:** Displays an "All caught up!" card when no flashcards are due, encouraging natural study breaks.

### 4. Offline-First Architecture & Network Resilience
- **Room SQLite Local Caching:** Downloaded trivia questions, user progress, flashcards, and settings are cached locally on device in Room (`QuizDatabase`).
- **Smart Deduplication & Replay:** Prevents duplicate questions using unique database indices (`OnConflictStrategy.IGNORE`) and randomizes offline question replay order (`ORDER BY RANDOM()`).
- **OpenTDB API Integration & Throttling:** Fetches questions via Retrofit from OpenTDB with an automated `RateLimiter` enforcing a 5-second interval between network requests to prevent HTTP 429 rate limit errors.
- **Offline Error Handling & Retry:** Renders friendly error cards with "Try again" buttons when offline or when category loading fails.

### 5. Learning Analytics & Visual Statistics
- **Overview Summary:** Tracks total questions answered, overall accuracy percentage %, total stars earned, and active learning days this week.
- **7-Day Daily Activity Chart:** Visual bar chart showing the number of questions answered each day over the past 7 days.
- **World Accuracy Breakdown:** Progress bars displaying accuracy percentages across each knowledge world played.
- **Recent Attempts Log:** Real-time log of recent level attempts showing world names, level numbers, correct answer counts, and earned star ratings.

### 6. Privacy Protection & Learner Control
- **Storage Access Framework (SAF) File Export:** One-tap data export saving a local JSON file (`quizquest-export-YYYY-MM-DD.json`) via system file picker (`CreateDocument`), requiring **zero runtime storage permissions** and avoiding Binder transaction limits.
- **Complete Local Data Deletion:** One-tap "Delete All Data" option with an unambiguous, neutral confirmation dialog offering equal visual weight choices ("Keep my data" vs "Delete everything").
- **Customizable Answer Retention:** Set answer history expiration (30 days, 90 days, or Forever) with immediate background database purging.
- **On-Device Question Flagging:** Tap the flag icon on any question to report bad content—reported questions are stored locally in Room (`FlaggedQuestionEntity`) and permanently filtered out of future quizzes and review queues (`NOT IN (SELECT questionText FROM FlaggedQuestion)`).
- **Direct Developer Email Feedback:** Embedded feedback launcher opening external email clients pre-addressed to `yibo.pan@my.jcu.edu.au` with fallback Snackbar notification ("No email app found on this phone.") if no mail client exists.

### 7. Ethical Design & Accessibility
- **Motivation Without Manipulation:** No streak counters (eliminates reset anxiety), no push notifications, no dark patterns, no ads, and no tracking/analytics SDKs.
- **Gentle Stopping Cues:** "You reached today's goal. Great work. It is a good time to take a break!" cards and "All caught up!" flashcard screens encourage healthy learning boundaries.
- **Accessibility Standards:** $48\text{ dp}$ minimum touch targets, high contrast dark/light themes, 200% font scaling support without text clipping, TalkBack semantics (`heading()`, `stateDescription`, `mergeDescendants`), and color-independent status indicators.
- **Adaptive Screen Layouts:** Renders a bottom `NavigationBar` on compact phone screens and a `NavigationRail` / two-pane side-by-side layout on tablets (`WindowWidthSizeClass.EXPANDED`).

---

## Features Summary Matrix

| Feature | Description | Implementation Details |
|---|---|---|
| **Worlds & Levels** | 9 knowledge domains with 9 progressive levels per world (Easy, Medium, Hard). | OpenTDB API, Room caching, `LevelPolicy` unlocking rules. |
| **Interactive Quiz** | Quiz gameplay with 50/50 hints, timers, feedback, and sound effects. | Pure Kotlin `QuizEngine`, `animateFloatAsState`, `SoundPool`. |
| **Spaced Repetition** | Leitner 5-box flashcard system for review of wrongly answered questions. | `LeitnerScheduler`, `ReviewCardDao`, `LifecycleResumeEffect`. |
| **Offline-First Play** | Play quizzes and review flashcards without an active internet connection. | Room SQLite database, `OnConflictStrategy.IGNORE`, `RateLimiter`. |
| **Learning Statistics** | Comprehensive accuracy %, active days, 7-day chart, and recent attempts. | `StatsSummary`, `QuizSessionDao`, `CategoryAccuracyRow`. |
| **Settings & Preferences** | Custom timer mode, questions per level, daily goal, sound, theme, and retention. | `SettingsDao`, `SettingsViewModel`, `MainViewModel.init`. |
| **Privacy & Export** | One-tap SAF file export (`.json`) and complete local data deletion. | `ExportWriter`, `CreateDocument` picker, `PrivacyRepository`. |
| **On-Device Flagging** | Report unsuitable questions to hide them permanently on-device. | `FlaggedQuestionEntity`, Room SQL `NOT IN` subqueries. |

---

## Screens

Home (landing) · World map · Quiz (activity) · Result · Review · Stats · Settings · Privacy

---

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

- **Navigation:** Jetpack Navigation with string routes and `navArgument`s (`Screens` sealed class). Renders a bottom `NavigationBar` on compact phone screens and a `NavigationRail` on large screens based on `WindowWidthSizeClass` (Lecture 3).
- **Room Schema & Storage:** `QuizDatabase` manages entities for categories, questions, flagged questions, level progress, quiz sessions, answer records, review cards, and user settings. Uses `@TypeConverters` for string list conversion and `OnConflictStrategy.IGNORE` with unique indices.
- **Offline Cache & Rate Limiting:** `TriviaRepositoryImpl` queries Room first. `RateLimiter` enforces a 5-second interval between OpenTDB network requests to prevent HTTP 429 rate limit errors.

---

## Course Alignment

| Lecture | Used for |
|---|---|
| 1 Kotlin & Android Studio | Idiomatic Kotlin (data classes, sealed classes, enums, extension functions), project configuration (`minSdk = 24`, `targetSdk = 36`). |
| 2 Compose layouts | Declarative UI layouts (`Column`, `Row`, `Box`, `LazyColumn`, `LazyVerticalGrid`), state hoisting, composable previews. |
| 3 Material 3, large screens, accessibility | Theme styling, `Scaffold`, `AppTopBar`, `Card`, `WindowSizeClass` adaptive layouts (`NavigationRail`, two-pane Quiz & Stats), touch targets ($\ge 48\text{ dp}$), semantics (`heading()`, `stateDescription`, `mergeDescendants`). |
| 4 Architecture | MVVM architecture, Repository pattern, Koin dependency injection (`appModules`, `koinViewModel`). |
| 5 Network calls | Retrofit, OkHttp, `kotlinx.serialization`, Coroutines (`Dispatchers.IO`), `StateFlow`, `NetworkResult`, Compose animations (`AnimatedContent`, `AnimatedVisibility`, `animateFloatAsState`). |
| 6 Navigation | Jetpack Navigation Compose, sealed `Screens` class, `NavHost`, route parameters, back stack management (`BackHandler`). |
| 7 Room | Room database (`QuizDatabase`), DAOs (`@Query`, `@Insert`, `@Update`), entities, TypeConverters, offline-first repository caching. |
| 8 Debugging | Logcat technical event logging (`BuildConfig.DEBUG` check via `AppLog`), LeakCanary for memory leak detection, Android Studio App Inspection (Database Inspector). |

---

## Data Source & Attribution

Questions come from the [Open Trivia Database](https://opentdb.com/), licensed under
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/). Questions are
community-written and may contain mistakes; learners can report a question to hide it permanently on their device.

Sound effects (`res/raw/sfx_*.wav`) were generated for this project and played via `SoundPool`.

---

## Ethical Design

Based on Assessment 2 commitments (location privacy and dark patterns avoidance).

| A2 commitment | Where it is in the app |
|---|---|
| No location permission | Manifest has only `INTERNET` |
| Local storage, one-tap export and delete | Local Room storage; SAF file picker export (`ExportWriter`); one-tap "Delete All Data" with confirmation. |
| Opt-in notifications | The app sends no push or local notifications at all. |
| Honest data-safety declaration, plain-language notice | Plain-language Privacy screen explaining data safety in teen-friendly terms. |
| No ads/SDKs; dependencies reviewed | No advertising SDKs, tracking telemetry, or third-party analytics. |
| Motivation without manipulation | Active days counter (no streak resets/penalties); equal-weight buttons; gentle "done for today" and "all caught up" stopping cues. |
| Accessible by default | High contrast colour palette, $\ge 48\text{ dp}$ touch targets, TalkBack semantics, 200% font scaling, colour-independent feedback, and "Reduce motion" support. |
| Ethics review per feature | Recorded in `docs/ethics-review.md`. |

### Dependency Review

| Library | Lecture | Purpose | Collects or sends user data? |
|---|---|---|---|
| AndroidX Core / Activity / Lifecycle | 1 | App framework & lifecycle management | No |
| Jetpack Compose, Material 3, Adaptive | 2, 3 | Declarative UI, Material 3 components, window size class | No |
| Koin | 4 | Dependency injection | No |
| Retrofit, OkHttp, kotlinx.serialization, Coroutines | 5 | HTTPS requests to opentdb.com only | No |
| Navigation Compose | 6 | Screen navigation & back stack | No |
| Room | 7 | Local SQLite database on device | No |
| LeakCanary | 8 | Memory-leak detection (**debug builds only**) | No (stays on device) |
| JUnit, AndroidX Test, Compose UI test, coroutines-test | — | Unit and instrumented UI tests only | No |

---

## Accessibility

- **Touch Targets:** All interactive buttons and cards enforce a minimum size of $48\text{ dp}$ (`defaultMinSize(minHeight = 48.dp)`).
- **Screen Reader Semantics:** Includes `contentDescription` for action icons, `onClickLabel` for clickable items, `heading()` semantics for titles, and `stateDescription` for level lock states, star ratings, and option feedback.
- **Group Semantics:** Applies `semantics(mergeDescendants = true)` to composite list items, stat cards, and flashcards so TalkBack reads them as single nodes.
- **Colour Independence:** Correct/incorrect feedback uses CheckCircle/Close icons and explicit text ("Correct!", "Time's up", "Incorrect"), never colour alone.
- **Font Scaling:** Supports 200% font scaling (`fontScale = 2f`) with scrollable containers preventing text clipping.
- **Motion Control:** Respects the "Reduce motion" setting, overriding all Compose transitions with instant `snap()` animations.
- **Adaptive Two-Pane Layouts:** Displays two-pane side-by-side layouts on `EXPANDED` tablet screens for Quiz and Stats screens.

---

## Testing

```bash
./gradlew testDebugUnitTest          # JVM unit tests (rules, mappers, repositories, ViewModels)
./gradlew connectedDebugAndroidTest  # Room DAO and Compose UI tests (needs an emulator)
```

### Test Suite Summary Table

#### **Unit Tests (JVM)** (`app/src/test`) — Total: **162 tests**

| Test Class | Location | Scope / What it Covers | Number of Tests |
| :--- | :--- | :--- | :---: |
| `SkeletonSanityTest` | `app/src/test/...` | Verifies basic JVM test execution environment and sanity checks | 4 |
| `EntityMappersTest` | `app/src/test/.../database` | Room database entity <-> domain model bi-directional mapping functions | 9 |
| `QuizTypeConvertersTest` | `app/src/test/.../database` | Type converters for `List<String>` list serialization in Room entities | 5 |
| `ExportSerializerTest` | `app/src/test/.../export` | Serialization and formatting of complete user data export JSON structures | 1 |
| `NetworkMappingTest` | `app/src/test/.../network` | OpenTDB API response DTO mapping into domain models & HTML unescaping | 5 |
| `RateLimiterTest` | `app/src/test/.../network` | Throttling logic enforcing 5-second interval between OpenTDB requests | 5 |
| `TextDecoderTest` | `app/src/test/.../network` | HTML entity decoding helper (`&quot;`, `&#039;`, `&amp;`, etc.) | 5 |
| `ProgressRepositoryTest` | `app/src/test/.../repository` | Quiz session result saving, star computation, and daily completion tracking | 3 |
| `ReviewRepositoryTest` | `app/src/test/.../repository` | Leitner box card creation, promotion, demotion, and `createdAt` preservation | 4 |
| `SettingsRepositoryTest` | `app/src/test/.../repository` | User settings DataStore/Room persistence and reactive Flow updates | 2 |
| `StatsRepositoryTest` | `app/src/test/.../repository` | Aggregation of overall accuracy, total stars, active days, and 7-day stats | 1 |
| `TriviaRepositoryTest` | `app/src/test/.../repository` | Question fetching, token expiration/depletion handling, and offline fallback | 7 |
| `AnswerShufflerTest` | `app/src/test/.../rules` | Deterministic shuffling of multiple-choice and boolean question options | 2 |
| `CategoryFilterTest` | `app/src/test/.../rules` | Filtering allowed categories and handling disallowed/empty category IDs | 5 |
| `DailyGoalTrackerTest` | `app/src/test/.../rules` | Start-of-day timestamp calculations, completion counting, and goal detection | 8 |
| `LeitnerSchedulerTest` | `app/src/test/.../rules` | Leitner 5-box intervals, box promotions/demotions, and due date scheduling | 9 |
| `LevelPolicyTest` | `app/src/test/.../rules` | Level difficulty mapping (1-3 Easy, 4-6 Medium, 7-9 Hard) & unlocking rules | 10 |
| `QuizEngineTest` | `app/src/test/.../rules` | State machine transitions (`start`, `selectAnswer`, `timeUp`, `next`, `50/50`, `skipCurrent`) | 16 |
| `RetentionPolicyTest` | `app/src/test/.../rules` | History retention period calculations (30 days, 90 days, Forever) | 3 |
| `StarRatingTest` | `app/src/test/.../rules` | Star calculations based on correct answer percentages (0, 1, 2, or 3 stars) | 8 |
| `FakeRepositoriesTest` | `app/src/test/.../fakes` | Unit test validation of in-memory fake repositories used across ViewModel tests | 6 |
| `HomeViewModelTest` | `app/src/test/.../viewmodel` | Home UI state mapping, daily goal detection, and offline world retry logic | 5 |
| `MainViewModelTest` | `app/src/test/.../viewmodel` | App startup theme selection (`shouldEnableDarkTheme`) and history retention cleanup | 2 |
| `PrivacyViewModelTest` | `app/src/test/.../viewmodel` | File export via `ExportWriter`, data deletion confirmation, and error state handling | 3 |
| `QuizViewModelTest` | `app/src/test/.../viewmodel` | Quiz gameplay, per-question timers, 50/50 hint, sound effects, question reporting, and save retries | 11 |
| `ResultViewModelTest` | `app/src/test/.../viewmodel` | Result score mapping, stopping cue detection, and cumulative best-stars next level unlocking | 5 |
| `ReviewViewModelTest` | `app/src/test/.../viewmodel` | Flashcard queueing, Leitner reviews, double-tap protection, and lifecycle refresh | 5 |
| `SettingsViewModelTest` | `app/src/test/.../viewmodel` | Setting updates (timer, sound, reduce motion, theme) and immediate retention cleanup | 6 |
| `StatsViewModelTest` | `app/src/test/.../viewmodel` | Overview summary mapping, empty state zeros, 7-day activity, and flow error handling | 4 |
| `WorldMapViewModelTest` | `app/src/test/.../viewmodel` | 9-level grid generation, star mapping, level unlocking, and unknown world fallback | 4 |

#### **Android Instrumented Tests** (`app/src/androidTest`) — Total: **33 tests**

| Test Class | Location | Scope / What it Covers | Number of Tests |
| :--- | :--- | :--- | :---: |
| `AppSmokeTest` | `app/src/androidTest/...` | Verifies application launch and Activity context instantiation | 1 |
| `AppNavigationTest` | `app/src/androidTest/...` | End-to-end bottom bar tab switching, play-flow tab hiding, and back navigation | 2 |
| `QuizDatabaseTest` | `app/src/androidTest/.../database` | In-memory Room database DAO integration, cascade deletes, indices, and queries | 12 |
| `RoomRepositoryIntegrationTest` | `app/src/androidTest/.../repository` | End-to-end Room repository integration testing with actual SQLite database | 5 |
| `HomeScreenTest` | `app/src/androidTest/.../views` | Stateless `HomeScreenContent` UI rendering, offline error card, and Retry button | 1 |
| `WorldMapScreenTest` | `app/src/androidTest/.../views` | Stateless `WorldMapScreenContent` UI grid, star rows, and locked state descriptions | 2 |
| `QuizScreenTest` | `app/src/androidTest/.../views` | Stateless `QuizScreenContent` UI, options, feedback, 50/50, leave dialog, and option state descriptions | 5 |
| `ResultScreenTest` | `app/src/androidTest/.../views` | Stateless `ResultScreenContent` UI, score, equal-weight action buttons, and locked next level | 3 |
| `ReviewScreenTest` | `app/src/androidTest/.../views` | Stateless `ReviewScreenContent` UI, flashcard question/answer sides, and all-caught-up state | 3 |
| `SettingsScreenTest` | `app/src/androidTest/.../views` | Stateless `SettingsScreenContent` UI rows, option dialogs, and sound toggle callbacks | 2 |
| `StatsScreenTest` | `app/src/androidTest/.../views` | Stateless `StatsScreenContent` UI overview totals, world accuracy, 7-day bars, and recent attempt rows | 2 |
| `PrivacyScreenTest` | `app/src/androidTest/.../views` | Stateless `PrivacyScreenContent` UI sections, file export card, and neutral delete confirmation dialog | 2 |

---

## Build & Run

1. Open the project in Android Studio and let Gradle sync.
2. Run the `app` configuration on an emulator or device (Android 7.0 / API 24+).

---

## AI Use

Generative AI was used during development and is acknowledged in the Declaration of AI-Generated Material submitted with this assignment.
