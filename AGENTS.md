# QuizQuest — instructions for AI coding agents

Android Studio's Gemini agent adds this file to every prompt. Follow it for every task.

## 1. Project

- **What:** QuizQuest, an Android education app. Coursework for JCU **CP3406** Assessment 3,
  written by one student (Yibo Pan). The student must understand and explain all the code,
  so prefer **simple, readable, well-commented** solutions over clever ones.
- **Learners:** secondary-school students aged 13–17. Copy must be plain, friendly English.
- **Learning goal:** general knowledge, analytical thinking (50/50 elimination) and memory
  retention (wrong answers become spaced-repetition flashcards).
- **Core loop:** each allowed Open Trivia DB category is a *world* with 9 *levels*
  (1–3 easy, 4–6 medium, 7–9 hard). A level is 10 questions (5–15 in Settings).
  Stars 0–3 come from accuracy; 1 star unlocks the next level.
- **Screens:** Home (landing), World map, Quiz (activity), Result, Review (flashcards),
  Stats (user statistics), Settings, Privacy.

## 2. Course alignment — use what the lectures teach

The code must be built with the techniques from CP3406 Lectures 1–9 (textbook: Wangereka,
*Mastering Kotlin for Android 14*). **Use these patterns; do not swap in other libraries.**

| Lecture | Topic | How this project uses it |
|---|---|---|
| 1 | Kotlin basics, Android Studio project | data classes, enums, functions; minSdk 24 |
| 2 | Compose layouts | Column/Row/Box, LazyColumn, LazyVerticalGrid (GridCells.Adaptive), modifiers, @Preview |
| 3 | Material 3, large screens, accessibility | Scaffold + TopAppBar, cards, NavigationBar; Theme.kt/Color.kt (Theme Builder); `currentWindowAdaptiveInfo().windowSizeClass`; 48 dp, contentDescription, `clickable(onClickLabel = …)`, `semantics { heading() }`, `stateDescription`, `mergeDescendants` |
| 4 | MVVM, repository pattern, Koin | packages `data` / `viewmodel` / `views` / `di`; `interface XRepository` + `class XRepositoryImpl`; `di/Modules.kt` with `appModules`; `QuizQuestApplication` with `startKoin`; `koinViewModel()` |
| 5 | Retrofit + coroutines | `TriviaAPI` with `suspend` functions returning `Response<T>`; `@Serializable`/`@SerialName`; `NetworkResult` (Success/Error); repository `withContext(dispatcher) { try { … } catch … }`; `single { Dispatchers.IO }`; `XUIState` data class; `MutableStateFlow` + `update { it.copy(…) }` + `viewModelScope.launch` + `init { }`; `collectAsStateWithLifecycle()`; `AnimatedVisibility` for loading / content / error; INTERNET permission |
| 6 | Navigation | `sealed class Screens(val route: String)`; `AppNavigation()` with `rememberNavController()` + `NavHost`; `composable(route, arguments = listOf(navArgument(…) { type = NavType.IntType }))`; `backStackEntry.arguments?.getInt(…)`; `popBackStack()`; screens receive callbacks like `onBackPressed` |
| 7 | Room | `@Entity(tableName = …)` `XEntity` classes; `@TypeConverter` class using `Json`; `@Dao` with `@Insert(onConflict = OnConflictStrategy.REPLACE)`, `@Query` returning `Flow`, `@Update`; `@Database` abstract class; Koin `single { Room.databaseBuilder(androidContext(), …).build() }` and `single { get<QuizDatabase>().xDao() }`; offline-first repository (`map` entities to models, `onEach { if (it.isEmpty()) fetchRemote…() }`); `asResult()`; schema export via `ksp { arg("room.schemaLocation", …) }`; **AutoMigration** with `@ColumnInfo(defaultValue = …)` |
| 8 | Debugging | `Log.d(TAG, …)` (debug builds only), LeakCanary (`debugImplementation`), App Inspection (Database Inspector, Network Inspector) |
| 9 | Publishing | no logs in release, strings in `strings.xml` (localisation), friendly error messages, test on several devices, feedback channel, small app (vector graphics), Data safety / privacy policy / target audience under 18 |

**Deliberate differences from the slides (keep them):**
- Library versions are newer where the slide versions do not work with Kotlin 2.x
  (Koin 4.1, Room 2.7, KSP 2.x, kotlinx-serialization 1.9). The APIs are the same as in the lectures.
- Koin 4 import path: `org.koin.compose.viewmodel.koinViewModel` (slides: `org.koin.androidx.compose.koinViewModel`).
- ViewModels are registered with Koin's `viewModel { … }`, not `single { … }` as in Lecture 4,
  so every screen gets its own ViewModel that is cleared when the screen closes.
- Navigation arguments reach ViewModels with `koinViewModel(parameters = { parametersOf(…) })`.
- Settings are stored in Room (Lecture 7) — no DataStore.
- **Testing is not covered in Lectures 1–9.** Use only what the Android Studio template already
  has (JUnit4, AndroidX Test, Compose `ui-test-junit4`) plus `kotlinx-coroutines-test`.
  No Mockito/MockK/Turbine/MockWebServer: test with hand-written fakes (Lecture 4: a repository
  interface makes it easy to swap in a fake).
- Not used: Coil (no images), ConstraintLayout (not needed), Chucker (App Inspection's
  Network Inspector does the same job with no setup — Lecture 8).

**Do not add, remove or upgrade any dependency or Gradle plugin** unless the task explicitly
says so. If you think one is needed, stop and ask.

## 3. Architecture rules (MVVM, Lecture 4)

```
com.yibopan.quizquest
├── QuizQuestApplication.kt   startKoin { androidContext(...); modules(appModules) }
├── MainActivity.kt           setContent { QuizQuestTheme { AppNavigation() } }
├── data/                     MODEL layer
│   ├── model/                plain data classes / enums used by the whole app
│   ├── rules/                pure Kotlin game rules (StarRating, LevelPolicy, QuizEngine, ...)
│   ├── network/              TriviaAPI, @Serializable network models, NetworkResult, RateLimiter, TextDecoder
│   ├── database/             Room: entities, DAOs, QuizDatabase, QuizTypeConverters
│   ├── repository/           XRepository interfaces + XRepositoryImpl classes
│   ├── export/               JSON export of the learner's data
│   └── sound/                optional sound effects
├── viewmodel/                VIEWMODEL layer: XViewModel + XUIState
├── views/                    VIEW layer: XScreen (stateful) + XScreenContent (stateless), components/
├── navigation/               Screens (sealed class), BottomNavItem, AppNavigation
├── di/                       Modules.kt (appModules)
└── ui/theme/                 Color.kt, Theme.kt, Type.kt
```

- **View:** `XScreen(...)` gets its ViewModel with `koinViewModel()` and collects
  `uiState` with `collectAsStateWithLifecycle()`, then calls `XScreenContent(uiState, callbacks)`.
  `XScreenContent` is stateless (used by previews and UI tests).
- **ViewModel** (Lectures 5 and 7):
  ```kotlin
  private val _uiState = MutableStateFlow(XUIState())
  val uiState: StateFlow<XUIState> get() = _uiState
  init { loadX() }
  private fun loadX() {
      _uiState.update { it.copy(isLoading = true) }
      viewModelScope.launch {
          repository.getX().asResult().collect { result ->
              when (result) {
                  is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, x = result.data) }
                  is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.error) }
              }
          }
      }
  }
  ```
  ViewModels depend only on repository **interfaces**, `data/rules` and `TimeProvider` —
  never on DAOs, Retrofit, Context or Android resources.
- **Repository** (Lectures 5 and 7): constructor takes the API/DAOs and
  `dispatcher: CoroutineDispatcher`; every function uses `withContext(dispatcher)`;
  network calls are wrapped in `try { … } catch (e: Exception) { … }`.
- `data/rules` is plain Kotlin (no Android imports) and fully unit tested.
- Time: inject `TimeProvider`; never call `System.currentTimeMillis()` in ViewModels or
  repositories. Dates use `java.util.Calendar`/`TimeZone` (`java.time` needs API 26; minSdk is 24).
- Koin: everything is registered in `di/Modules.kt` → `appModules`.

## 4. Data model (Room, database name `quiz-database`)

| Entity / table | Columns | Notes |
|---|---|---|
| `CategoryEntity` / `Category` | `id` Int PK (Open Trivia id), `name`, `updatedAt` Long | only allowed ids |
| `QuestionEntity` / `Question` | `id` Long PK autoGenerate, `categoryId`, `difficulty`, `type`, `text`, `correctAnswer`, `incorrectAnswers` List<String> (TypeConverter), `fetchedAt`, `used` Boolean | unique index (`categoryId`, `text`); offline cache |
| `FlaggedQuestionEntity` / `FlaggedQuestion` | `questionText` PK, `flaggedAt` | excluded from every question query |
| `LevelProgressEntity` / `LevelProgress` | `categoryId`, `level`, `stars`, `bestScore`, `updatedAt` | primaryKeys = [categoryId, level]; unlocking is NOT stored |
| `QuizSessionEntity` / `QuizSession` | `id` Long PK autoGenerate, `categoryId`, `level`, `startedAt`, `durationMs`, `correctCount`, `totalCount`, `stars` | |
| `AnswerRecordEntity` / `AnswerRecord` | `id` PK autoGenerate, `sessionId` (ForeignKey → QuizSession, CASCADE), `questionText`, `chosenAnswer` nullable, `correctAnswer`, `isCorrect`, `answeredAt`, *(version 2)* `timeTakenMs` with `@ColumnInfo(defaultValue = "0")` | deleted by the retention rule |
| `ReviewCardEntity` / `ReviewCard` | `id` PK autoGenerate, `questionText` (unique index), `correctAnswer`, `box`, `nextReviewAt`, `timesWrong`, `createdAt` | |
| `SettingsEntity` / `Settings` | `id` Int PK (always 0) + one column per `UserSettings` field (enums stored by name) | one row |

All timestamps are epoch milliseconds. Database **version 1** has no `timeTakenMs`;
**version 2** adds it with `autoMigrations = [AutoMigration(from = 1, to = 2)]` (Lecture 7).
Schemas are exported to `app/schemas/` and committed to Git.

## 5. Ethics rules — NON-NEGOTIABLE (from the student's Assessment 2)

1. **Permissions:** the release manifest has only `INTERNET`. Never add location, camera,
   microphone, contacts, storage, notification or any other permission.
2. **No tracking:** no analytics, crash-reporting, advertising or attribution SDKs (Lecture 9
   mentions them, but this app deliberately does not use them); never read the advertising ID.
3. **Data stays on the device** (Room). The only network traffic is HTTPS to `opentdb.com`.
   Never put personal data in URLs or logs. `Log.d` only inside `if (BuildConfig.DEBUG)`.
4. **Learner control:** the learner can export all their data and delete all of it; answer
   history follows the retention setting.
5. **No dark patterns:** two choices get equal visual weight (e.g. two `OutlinedButton`s with
   `Modifier.weight(1f)`); no confirmshaming copy; never re-ask after "Not now"; no fake
   urgency; no auto-advance to the next level; **no streaks**, loss-aversion or random rewards —
   stars are mastery-based only; the timer is OFF by default; no notifications.
6. **Stopping cues:** levels are finite; when the daily goal is reached show the friendly
   "done for today" card (never block the learner).
7. **Honest copy:** credit Open Trivia DB (CC BY-SA 4.0); say that opentdb.com can see the
   device's IP address; say that questions may contain mistakes (report button).
8. **Age-appropriate content:** only `CategoryFilter.ALLOWED_CATEGORY_IDS` are used.

## 6. Accessibility rules (Lecture 3)

- Touch targets ≥ 48 dp. Text in `sp`; no fixed heights around text (font scaling).
- Meaningful icons have a `contentDescription` from `strings.xml`; decorative icons use `null`.
- Clickable cards/rows use `Modifier.clickable(onClickLabel = …)`.
- Screen titles and section headers use `Modifier.semantics { heading() }`.
- State is described with `semantics { stateDescription = … }` (e.g. "Locked", "2 of 3 stars").
- Never rely on colour alone: correct/incorrect feedback shows an icon **and** text, and is
  announced with `liveRegion = LiveRegionMode.Polite`.
- Respect `UserSettings.reduceMotion`: no animation when it is true.
- Colours only from `MaterialTheme.colorScheme` (contrast-checked palette; dynamic colour off).
- Large screens: use the window size class from `currentWindowAdaptiveInfo()` (Lecture 3).

## 7. Coding conventions

- Kotlin official style; 4-space indent; trailing commas in multi-line lists.
- KDoc on every public class and non-trivial function; comments explain **why**, and mention
  the lecture a pattern comes from when it helps (e.g. `// Lecture 7: offline first`).
- Every user-visible string in `res/values/strings.xml`.
- No magic numbers: use named constants.
- An `@Preview` for every `XScreenContent`, wrapped in `QuizQuestTheme`.
- Use `TestTags` constants for UI-test tags.
- When a screen's stage is finished, delete its `StageNotice(...)` call and its `dev_*_stage`
  string. Never delete `TODO(Stage N)` markers that belong to other stages.

## 8. Testing rules

- JVM tests in `app/src/test/...` mirror the main package. Test names use backticks,
  e.g. ``fun `returns two stars at 80 percent`()``.
- `data/rules`, mappers, `TextDecoder`, `RateLimiter`: thorough tests incl. edge cases.
- Repositories: fake DAOs / a `FakeTriviaAPI` implementing `TriviaAPI` that returns
  `Response.success(...)`. ViewModels: fake repositories from `fakes/` + `MainDispatcherRule`
  + `runTest`; read state with `viewModel.uiState.value`.
- Room DAO tests (`Room.inMemoryDatabaseBuilder`) and Compose UI tests go in
  `app/src/androidTest/...`.
- After every change run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`;
  both must pass before you finish.

## 9. How to finish every task

1. Read the relevant files before editing. Change only what the task needs.
2. Build and run the unit tests (see §8). Fix failures you caused.
3. Do **not** commit, push, or edit `docs/ethics-review.md` / `docs/dev-log.md`
   (those are the student's own reflection).
4. End your reply with:
   - **Summary** — what you changed and why (short).
   - **Lectures used** — which lecture techniques the code applies.
   - **Files changed** — list.
   - **How to check manually** — 2–5 steps in the emulator (mention App Inspection where useful).
   - **Ethics & accessibility notes** — which rules in §5/§6 this work touches.
   - **Suggested commit message** — Conventional Commits, e.g. `feat(quiz): add 50/50 hint`.
