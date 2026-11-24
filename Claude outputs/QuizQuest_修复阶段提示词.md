# QuizQuest 修复阶段提示词（给 Android Studio Agent）

这份文件对应《QuizQuest_代码审查与修复提示词.md》里找到的问题。

**和上一份文件里 A–G 的区别：**

- 每一步拆得更小：一次只改一两个文件，Agent 更不容易改乱；
- 提示词里写了真实的类名和字段名，都是从你的代码里核对过的。

**用法：**

1. 先 push 现有提交（`527034f4`）。
2. 在 Agent 里开新对话，先粘贴下面的「通用开头」，再粘贴一个修复步骤。
3. Agent 改完后：
   - 看一眼它列出的改动文件；
   - 跑测试；
   - 按「自检」在模拟器上点一遍；
   - 用给出的提交信息提交。
4. 一步完成后再做下一步。出错时把错误信息原样贴回给 Agent，并说 "Fix this error without changing the behaviour."

**顺序：** R1 → R16 → A1 → A9。

- R1–R9 修 bug；
- R10–R12 是隐私页；
- R13–R16 是小问题和测试；
- **A1–A9 是第六部分新增的，根据 Assessment 2 完善现有代码和文档。**

全部完成后，再做原来的阶段 9、阶段 10。

---

## 通用开头（每个新对话先粘贴一次）

```
You are working on QuizQuest, my CP3406 Android education app
(package com.yibopan.quizquest, Kotlin, Jetpack Compose, Koin, Retrofit, Room, minSdk 24).
Read AGENTS.md first and follow it, especially section 5 (ethics) and section 6 (accessibility).
Rules for every task:
- Keep the existing architecture: views/ -> viewmodel/ -> data/repository/ -> data/database and
  data/network; XUIState data classes with MutableStateFlow + update; Koin modules in di/Modules.kt.
- All user-visible text goes in res/values/strings.xml. No hard-coded UI strings.
- Do not add new libraries or permissions. The manifest must keep only INTERNET.
- minSdk is 24: do not use java.time (use Calendar / SimpleDateFormat like the rest of the code).
- Change only what the task needs. At the end, list every file you changed.
- Run ./gradlew testDebugUnitTest and tell me the result.
```

---

## 第一部分：复习卡片

### R1：复习页不再跳过卡片

```
Fix a bug in viewmodel/ReviewViewModel.kt.

Bug: processCardReview() calls reviewRepository.updateCard(), which moves the card's nextReviewAt
into the future, so the Room flow from getDueCards() re-emits WITHOUT that card. Then
processCardReview() ALSO does currentIndex + 1, so the next card is skipped. With 3 due cards the
learner only sees cards 1 and 3.

Fix:
1. Always show the FIRST card of the due list: currentCard = dueCards.firstOrNull().
   Remove the currentIndex + 1 step. Remove currentIndex from ReviewUIState if nothing else needs it.
2. Add reviewedThisSession: Int = 0 to ReviewUIState; increase it by 1 after each successful update.
3. Add isUpdating: Boolean = false to ReviewUIState. processCardReview() returns at once if
   isUpdating is true (ignores double taps). In ReviewScreen disable the "Not yet" and "I knew it"
   buttons while isUpdating is true.
4. Wrap updateCard() in try/catch. On failure set a new field errorMessageRes: Int? (a string
   resource "Could not save this card. Please try again.") and show it in the screen.
5. In ReviewScreen replace the "Card %1$d of %2$d" label (review_card_progress) with a plurals
   resource review_cards_left: one = "%1$d card left", other = "%1$d cards left",
   using pluralStringResource. Delete the unused review_card_progress string.

Tests in ReviewViewModelTest (FakeReviewRepository already removes a reviewed card from the due
list, like Room does):
- 3 due cards: review all 3 and assert every card is shown exactly once, in order, and the
  screen ends in the "all caught up" state.
- Calling onKnewIt() twice quickly updates the card only once.
Update ReviewScreenTest if it checks the old "Card x of y" label.
```

**自检：**

1. 故意答错 3 道题，再打开 Review 标签。
2. 应依次出现 3 张卡，显示 "3 cards left" → "2 cards left" → "1 card left"，最后是 "All caught up"。

**提交：**

```
fix(review): show every due card once

- Show the first due card; Room already removes the reviewed one
- Ignore double taps while a card is being saved
- "N cards left" label with plurals
- Regression test with three cards
```

### R2：复习页刷新"现在时间"

```
In viewmodel/ReviewViewModel.kt, loadDueCards() reads timeProvider.now() only once, when the
ViewModel is created. The Review tab keeps its ViewModel alive, so cards that become due later never
appear.

Fix:
1. Keep the collect Job in a private var. Add fun refresh(): cancel the old Job and start collecting
   reviewRepository.getDueCards(timeProvider.now()) again.
2. In views/ReviewScreen.kt call viewModel.refresh() every time the screen resumes, with
   LifecycleResumeEffect from androidx.lifecycle.compose (lifecycle 2.9.1 is already in the project).
   Use onPauseOrDispose { } with an empty body.

Test in ReviewViewModelTest: use a fake TimeProvider; a card due "tomorrow" is not shown, then move
the time forward one day, call refresh(), and the card is shown.
```

**提交：**

```
fix(review): refresh due cards when the Review tab resumes
```

### R3：复习卡保留创建时间，被举报的题不再出现

```
Two fixes in the review data layer.

1. data/repository/ReviewRepositoryImpl.kt: updateCard() builds a new ReviewCardEntity with
   createdAt = nowMillis, so the card's creation time is overwritten every review.
   Add to ReviewCardDao: @Query("SELECT * FROM ReviewCard WHERE id = :id") suspend fun getById(id: Long): ReviewCardEntity?
   In updateCard() load the entity with getById(card.id) and save
   entity.copy(box = nextBox, nextReviewAt = nextReview, timesWrong = timesWrong).
   If the entity is null, do nothing.

2. data/database/Daos.kt: a question reported with the flag button must not come back as a review card.
   Change ReviewCardDao.getDueReviewCards to:
   SELECT * FROM ReviewCard
   WHERE nextReviewAt <= :nowMillis
     AND questionText NOT IN (SELECT questionText FROM FlaggedQuestion)
   ORDER BY nextReviewAt ASC
   This is only a query change, so the database version and schema stay the same.

Tests:
- ReviewRepositoryTest (JVM, with the fake DAO in fakes/FakeDaos.kt – add getById to it):
  updateCard keeps createdAt.
- androidTest QuizDatabaseTest: insert a review card and a FlaggedQuestionEntity with the same text;
  getDueReviewCards does not return that card.
Run ./gradlew testDebugUnitTest. Tell me to run connectedDebugAndroidTest on an emulator for the
androidTest.
```

**提交：**

```
fix(review): keep createdAt and hide reported questions from review
```

---

## 第二部分：答题流程

### R4：QuizEngine 增加"跳过当前题"（纯 Kotlin）

```
Add a pure function to data/rules/QuizEngine.kt. No Android code, no ViewModel changes in this step.

fun skipCurrent(state: QuizState): QuizState
- If state.currentQuestion is null or state.isFinished, return state unchanged.
- Remove the current question from state.questions AND the matching entry from state.options
  (options is a parallel list with the same index).
- Remove any AnswerRecord in state.answers whose questionText equals the current question's text.
  If that record was correct, decrease correctCount by 1.
- Keep currentIndex (the next question moves into its place).
- Set selectedAnswer = null and removedOptions = emptySet().
- Keep hintsLeft as it is (a hint already used stays used).
Add KDoc explaining that this is used when the learner reports a question, so a reported question
never counts for or against the learner (AGENTS.md section 5).

Tests in QuizEngineTest:
- skipping question 1 of 3: 2 questions left, currentIndex 0, the old question 2 is now current;
- skipping after answering correctly removes the record and lowers correctCount;
- skipping the last question makes isFinished true when other questions remain;
- skipping the only question leaves questions empty.
```

**提交：**

```
feat(rules): add QuizEngine.skipCurrent for reported questions
```

### R5：举报题目不再算错

```
Use QuizEngine.skipCurrent (added in the last step) in viewmodel/QuizViewModel.kt.

Now flagCurrentQuestion() calls quizEngine.timeUp() for an unanswered question, so a reported
question is saved as a WRONG answer, lowers the stars, and becomes a review card through
reviewRepository.addWrongAnswers().

Change flagCurrentQuestion() to:
1. Call triviaRepository.flagQuestion(currentQ.text) inside try/catch (log errors with AppLog).
2. quizState = quizEngine.skipCurrent(quizState).
3. If quizState.questions is empty (every question was reported): stop the timer, do NOT save a
   session, and show the error state with the new error type ALL_REPORTED (see 5).
4. Else if quizState.isFinished: call saveSession() (the same path nextQuestion() uses).
5. Else: reset questionStartTime, restart the timer for the new current question, updateUIState().
6. Set a one-off field showReportedMessage = true in QuizUIState; QuizScreen shows a Snackbar
   "Thanks – this question is now hidden on this phone." and then calls a ViewModel function
   reportedMessageShown(). Reports stay on the device; do not add any network call.

Error type: replace `error: String?` in QuizUIState with `error: QuizError?` and add
enum class QuizError { LOAD_FAILED, ALL_REPORTED }. QuizScreen maps them to strings:
LOAD_FAILED -> the existing quiz_error_friendly with Retry; ALL_REPORTED -> new string
"All questions in this level were reported. Please try another level." with no Retry
(the top bar Back button is enough). This also removes the hard-coded
"No questions found for this level." string from the ViewModel.

Also in QuizScreen's report dialog, the dismiss button uses quiz_leave_dialog_dismiss ("Continue"),
which is ambiguous next to "Report". Add a new string common_cancel = "Cancel" and use it there.

Tests in QuizViewModelTest:
- Update "flagCurrentQuestion flags question in repository and advances": with 10 questions,
  after reporting one, totalQuestions is 9; finish the quiz and the saved QuizResult has
  totalCount = 9; FakeReviewRepository has no card for the reported question.
- Reporting every question saves no session and error == ALL_REPORTED.
- Update any test that checked error as a String.
```

**自检：**

1. 进入关卡后举报第 1 题：进度变成 "1 / 9"，并出现提示条。
2. 答完后，结果页显示 "x / 9"。
3. 打开 Review 标签，里面没有被举报的题。

**提交：**

```
fix(quiz): reporting a question no longer counts as a wrong answer

- Reported question is removed from the level with QuizEngine.skipCurrent
- Stars use the remaining questions; it never becomes a review card
- QuizError enum instead of a hard-coded error string
- Clear "Cancel" label in the report dialog and a short confirmation
```

### R6：答题后显示正确/错误颜色

```
Fix views/QuizScreen.kt. The answer-button code is duplicated in the phone layout and in the
two-pane EXPANDED layout.

Bug: each answer Button has enabled = uiState.selectedAnswer == null. A disabled Material 3 Button
draws disabledContainerColor / disabledContentColor, so after answering the computed
primaryContainer / errorContainer colours are ignored and every option turns grey.

Fix:
1. Extract ONE private composable:
   AnswerOptionButton(option: String, uiState: QuizUIState, onSelect: (String) -> Unit, selectLabel: String)
   and use it in both layouts.
2. Compute containerColor and contentColor once and pass them as containerColor, contentColor,
   disabledContainerColor AND disabledContentColor in ButtonDefaults.buttonColors(...).
3. Keep the CheckCircle / Close icons (feedback is not colour alone).
4. Add a stateDescription for TalkBack after answering: new strings
   quiz_state_correct_answer = "Correct answer" and
   quiz_state_your_wrong_answer = "Your answer, incorrect".
5. Keep the 48 dp minimum height and the existing onClick label.

Test in androidTest views/QuizScreenTest: render QuizScreenContent with selectedAnswer = "London"
and correctAnswer = "Paris"; assert the "Paris" option has the "Correct answer" state description and
"London" has "Your answer, incorrect".
```

**自检：**

- 手机布局：答对时，选项变绿并带勾；答错时，你选的选项变红带叉，正确答案变绿。
- 平板或横屏布局再检查一遍。
- 暗色主题下也检查一遍。

**提交：**

```
fix(quiz): keep correct/incorrect colours after answering

- One AnswerOptionButton for phone and two-pane layouts
- Disabled colours match the feedback colours
- TalkBack state descriptions for the correct and the wrong answer
```

### R7：超时提示 + 倒计时不跳

```
Two timer fixes in viewmodel/QuizViewModel.kt, viewmodel/QuizUIState.kt and views/QuizScreen.kt.

1. Time-up feedback. When the timer runs out the feedback card says
   "Incorrect. Correct answer: X". Add isTimeUp: Boolean = false to QuizUIState. Set it to true in
   updateUIState() when the current answer was recorded by quizEngine.timeUp (the current
   AnswerRecord has chosenAnswer == null). In the feedback card show a new string
   quiz_feedback_time_up = "Time's up. The answer was %1$s" with Icons.Rounded.Timer.
   Do this in both layouts.

2. The countdown jumps back. updateUIState() sets
   secondsLeft = secondsPerQuestion whenever it runs, so after using 50/50 the full time shows for
   one second. Keep the remaining seconds in a private var currentSecondsLeft in the ViewModel:
   set it when a new question's timer starts, decrease it in the timer loop, and use it in
   updateUIState(). Do not reset it in useFiftyFifty().

Tests in QuizViewModelTest (use the test dispatcher's advanceTimeBy):
- letting the timer run out sets isTimeUp = true;
- after 5 seconds, useFiftyFifty() does not reset secondsLeft to the full value.
```

**自检：** 先在设置里打开计时器。

- 等时间走完：显示 "Time's up"。
- 中途用 50/50：倒计时继续往下走，不会跳回满格。

**提交：**

```
fix(quiz): show "Time's up" and keep the countdown after 50/50
```

### R8：保存成绩失败不崩溃

```
In viewmodel/QuizViewModel.kt, saveSession() launches progressRepository.saveQuizResult() and
reviewRepository.addWrongAnswers() without error handling. If Room throws, the exception crashes
the app and isSaving stays true, so the learner can never finish.

Fix:
1. Wrap both calls in try/catch (log with AppLog.e).
2. On failure: isSaving = false and set saveFailed = true in QuizUIState.
3. Add fun retrySave() that clears saveFailed and calls saveSession() again.
4. In QuizScreen, when saveFailed is true, show a card with a new string
   "Your result could not be saved." and a Retry button (48 dp) that calls retrySave().
   Use the existing ErrorState from views/components/StateViews.kt if it fits.

Tests in QuizViewModelTest: give FakeProgressRepository a flag shouldFail; when it fails,
saveFailed = true and finishedSessionId is null; after shouldFail = false and retrySave(),
finishedSessionId is set.
```

**提交：**

```
fix(quiz): handle errors when saving a result, with Retry
```

---

## 第三部分：首页

### R9：首页离线状态 + 重试

```
Fix viewmodel/HomeViewModel.kt, viewmodel/HomeUIState.kt and views/HomeScreen.kt.

Bug: HomeViewModel uses SampleData.worlds when Room has no categories. On a first launch without
internet, or after "Delete all my data", the learner sees fake worlds that cannot load questions,
and the categories are never fetched again until the app restarts
(TriviaRepositoryImpl.getCategories() fetches only once, at the start).

Fix:
1. Production code must not use SampleData. Keep SampleData only for @Preview and tests.
2. HomeUIState already has isLoading. Add worldsUnavailable: Boolean = false and
   isRetrying: Boolean = false. After the first emission, set isLoading = false and
   worldsUnavailable = worlds.isEmpty().
3. Add fun retry() to HomeViewModel: return if isRetrying; set isRetrying = true; call
   triviaRepository.fetchRemoteCategories() in try/finally; set isRetrying = false. Room then emits
   the new categories by itself (offline first, Lecture 7).
4. In HomeScreenContent:
   - isLoading -> LoadingState() in place of the world list;
   - worldsUnavailable -> a card with a new string "We couldn't load the worlds. Check your
     internet connection and try again." and a Retry button (48 dp, disabled while isRetrying);
   - otherwise the world list as now.
   Add an onRetryClicked parameter and pass it from HomeScreen.
5. Add .catch { } to the combined flow: log the error and set worldsUnavailable = true instead of
   crashing.

Tests:
- HomeViewModelTest: empty categories -> worldsUnavailable = true and worlds is empty (not
  SampleData); retry() calls fetchRemoteCategories (add a counter to FakeTriviaRepository) and,
  when the fake then emits categories, worldsUnavailable = false.
- androidTest: add views/HomeScreenTest – with worldsUnavailable = true the Retry button is shown
  and clicking it calls onRetryClicked.
```

**自检：**

1. 在模拟器里打开飞行模式，然后在系统设置里清除 QuizQuest 的数据，再打开 app：应看到提示和 Retry，没有假世界。
2. 关掉飞行模式，点 Retry：真实的世界列表出现。

**提交：**

```
fix(home): show an offline state with Retry instead of sample worlds

- SampleData only in previews and tests
- Loading and "worlds unavailable" states; Retry refetches categories
- Errors from the combined flow are caught
- HomeScreen UI test
```

---

## 第四部分：隐私页

### R10：隐私页文字与删除对话框

```
Fix views/PrivacyScreen.kt and res/values/strings.xml (A2 commitments #2 and #4, Lecture 9).

1. Move EVERY hard-coded text in PrivacyScreen.kt to strings.xml: the delete dialog title and text,
   the Export card title/description/button, the Delete card title/description/button, and the
   three snackbar messages (exportReadyMsg, deletedMsg, failedMsg).

2. Delete dialog buttons (A2: no confusing labels):
   - dismiss button: new string privacy_delete_keep = "Keep my data"
     (do NOT reuse quiz_leave_dialog_dismiss "Continue" – next to "Delete" it is ambiguous);
   - confirm button: new string privacy_delete_confirm = "Delete everything".
   Keep both as OutlinedButton with weight(1f) and 48 dp – equal weight, as now.
   Dialog text: "This deletes your stars and scores, answer history, review cards, settings,
   reported questions and downloaded questions from this phone. It cannot be undone. Tip: use
   Export first to keep a copy."

3. Plain language for 13–17-year-olds. Rewrite these strings (no "Room database" or
   "database tables"):
   - privacy_what_is_stored_desc: "Your stars, scores, answer history, review cards and settings are
     saved only on this phone."
   - Delete card description: "Remove everything QuizQuest has saved on this phone."
   - privacy_backups_title "Backups", privacy_backups_desc: "Your data is not included in cloud
     backups. Use Export to keep your own copy."
   Add two new sections to the first card (with PrivacySection, heading semantics):
   - "How long we keep it": "Your answer history is kept for the time you choose in Settings
     (30 days, 90 days or until you delete it). Stars, scores and review cards stay until you
     delete them."
   - "Questions may contain mistakes": "Questions are written by the Open Trivia Database community
     and may contain mistakes. Tap the flag on a question to report it – it is then hidden on this
     phone. Reports are not sent anywhere."
   Keep the opentdb.com / IP address section and the CC BY-SA 4.0 attribution section.

4. Use the same "Keep my data" wording nowhere else; the quiz leave dialog stays as it is.

Test in androidTest views/PrivacyScreenTest: open the delete dialog; it shows "Keep my data" and
"Delete everything"; clicking "Keep my data" calls onDismissDeleteClicked and not
onConfirmDeleteClicked. Update existing tests that look for the old texts.
```

**自检：**

1. 打开设置 → Privacy，读一遍：没有技术术语，多了"保存多久"和"题目可能有错"两节。
2. 点 Delete：对话框里两个按钮一样大，分别写着 "Keep my data" 和 "Delete everything"。

**提交：**

```
fix(privacy): plain-language notice and clear delete dialog

- All Privacy texts moved to strings.xml
- "Keep my data" / "Delete everything" with equal weight
- New sections: how long data is kept; questions may contain mistakes
```

### R11：导出改为"保存为文件"

```
Replace the share-sheet export in the Privacy feature with "save as a file".

Why: PrivacyScreen puts the whole JSON into Intent.EXTRA_TEXT. When the answer history grows this
can exceed the 1 MB Binder limit and crash (TransactionTooLargeException), and many apps do not
accept application/json text.

1. data/export: add
   interface ExportWriter { suspend fun write(uriString: String, json: String) }
   class AndroidExportWriter(private val context: Context, private val dispatcher: CoroutineDispatcher) : ExportWriter
   It parses the uri with toUri() and writes the JSON with
   context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
   inside withContext(dispatcher). Throw an IOException if the stream is null.
   (The ViewModel works with a String, so the JVM unit tests need no android.net.Uri.)
2. di/Modules.kt: single<ExportWriter> { AndroidExportWriter(androidContext(), get()) } and
   PrivacyViewModel(privacyRepository = get(), exportWriter = get()).
3. PrivacyViewModel: remove prepareExport(), clearExportJson() and exportJson from PrivacyUIState.
   Add fun saveExport(uriString: String): isWorking = true; json =
   privacyRepository.exportAllDataAsJson(); exportWriter.write(uriString, json); message =
   EXPORT_SAVED (rename EXPORT_READY to EXPORT_SAVED); on any exception message = FAILED;
   finally isWorking = false.
4. PrivacyScreen: val launcher = rememberLauncherForActivityResult(
   ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null)
   onSaveExport(uri.toString()) }. The Export button calls
   launcher.launch("quizquest-export-$date.json") where date is today formatted with
   SimpleDateFormat("yyyy-MM-dd", Locale.US) (no java.time – minSdk 24).
   This uses the system file picker and needs NO permission.
   Snackbar after success: new string "Your data was saved."
   Rename the export button text to "Save a copy" and the card description to
   "Save a file with all your scores, answers and review cards."
5. Remove the old ACTION_SEND export code.

Tests:
- Add FakeExportWriter (records the json; can be set to throw).
- PrivacyViewModelTest: saveExport writes the JSON from FakePrivacyRepository and sets
  EXPORT_SAVED; when the writer throws, message = FAILED and isWorking = false.
- Update PrivacyScreenTest and any test that used exportJson / prepareExport.
After the tests, open the release Merged Manifest and confirm it still has only INTERNET.
```

**自检：**

1. 点 "Save a copy"：弹出系统的保存界面，文件名里带今天的日期。
2. 保存后，用系统"文件"app 打开这个 JSON，能看到成绩和复习卡。

**提交：**

```
feat(privacy): export data as a file with the system file picker

- ExportWriter writes the JSON through the Storage Access Framework
- No permission needed; no 1 MB Intent limit
- Share-sheet export removed; ViewModel and UI tests updated
```

### R12：反馈联系方式

```
In views/PrivacyScreen.kt the feedback card sends email to support@quizquest.edu.au, an address that
does not exist (A2: honest design).

1. Add a string resource feedback_email (put the placeholder value "your.name@my.jcu.edu.au";
   I will replace it with my real address) and a string feedback_subject = "QuizQuest feedback".
2. Use them in the ACTION_SENDTO intent (data = "mailto:" + email) and in the fallback intent.
3. If no email app is found, show a Snackbar with a new string "No email app found on this phone."
   instead of silently doing nothing.
4. Move the click logic into a small private function sendFeedbackEmail(context, ...) to keep the
   composable short.
```

**做完后手动改：** 打开 `strings.xml`，把 `feedback_email` 换成你真实的邮箱。如果不想放邮箱，就对 Agent 说 "Remove the feedback card and its strings."

**提交：**

```
fix(privacy): use a real feedback address from string resources
```

---

## 第五部分：小问题与测试

### R13：统计页和地图页用真实的世界名

```
1. views/StatsScreen.kt RecentResultRow uses SampleData.worldName() and a hard-coded
   "World ${result.categoryId}". Use the real category names from Room instead: in
   StatsViewModel (or StatsRepositoryImpl) build a map categoryId -> name from
   StatsSummary.byCategory and pass the name to each recent result. Fallback: new string
   stats_unknown_world = "World %1$d".
2. viewmodel/WorldMapViewModel.kt uses SampleData.worldName(categoryId) for the title. Take the
   name from triviaRepository.getCategories() instead; the fallback is the same stats_unknown_world
   string (resolve it in the screen, not in the ViewModel – keep a nullable worldName in the state).
3. After this, SampleData must only be used by @Preview functions and tests. Search the main
   source set and confirm.
Update StatsScreenTest / WorldMapViewModelTest for the new behaviour.
```

**提交：**

```
fix(stats): use real world names instead of sample data
```

### R14：结果页"下一关"按钮

```
viewmodel/ResultViewModel.kt: isNextUnlocked = stars >= StarRating.PASS_STARS && level < 9 uses
only THIS attempt's stars. If the learner passed this level before but failed now, LevelPolicy
already unlocks the next level on the world map, but the Result screen hides "Next level".

Fix: add progressRepository.getLevelProgress(categoryId) to the existing combine(). Best stars =
max(this result's stars, the stored stars for this level). Unlock the next level when
best stars >= StarRating.PASS_STARS and level < LevelPolicy.LEVELS_PER_WORLD (use a LevelPolicy
function if one already does this, so the rule lives in one place).

Test in ResultViewModelTest: stored progress has 3 stars for level 2, this attempt has 1 star ->
isNextLevelUnlocked = true.
```

**提交：**

```
fix(result): next-level button follows the best stars, like the world map
```

### R15：保留期限立即生效 + 题目缓存

```
1. viewmodel/SettingsViewModel.kt: changing the answer-history retention only deletes old answers
   at the next app start (MainViewModel.init). Inject PrivacyRepository and TimeProvider into
   SettingsViewModel (update di/Modules.kt and the tests). In updateAnswerRetention(period), after
   the settings are saved, call privacyRepository.deleteExpiredAnswers(timeProvider.now()) inside
   try/catch.
   Test in SettingsViewModelTest: updateAnswerRetention calls deleteExpiredAnswers (add a counter
   to FakePrivacyRepository).

2. data/database/Daos.kt QuestionDao.insertAll uses OnConflictStrategy.REPLACE with the unique
   (categoryId, text) index on QuestionEntity. A question downloaded again is deleted and
   re-inserted with used = false and a new id, so learners see repeats. Change it to
   OnConflictStrategy.IGNORE. Also add ORDER BY RANDOM() to QuestionDao.getQuestions()
   (the offline fallback that reuses questions) so replays are not always the same set.
   Query changes only – no schema change, no migration.
   Test in androidTest QuizDatabaseTest: insert a question, markAsUsed, insert it again ->
   used is still true.
```

**提交（分两次）：**

```
fix(settings): apply a shorter answer-retention period immediately
```

```
fix(database): keep the used flag when a question is downloaded again
```

### R16：清理 + 版本号 + 补统计测试

```
1. Delete views/components/StageNotice.kt and the stage_notice_title string (no longer used).
2. MainActivity.kt computes darkTheme from themeMode itself, and MainViewModel.shouldEnableDarkTheme()
   does the same thing but is unused. Keep only one: make MainActivity call
   mainViewModel.shouldEnableDarkTheme(isSystemInDarkTheme()) (adjust the signature if needed) and
   keep its test in MainViewModelTest.
3. QuizQuestApplication.kt: update the out-of-date comment about "Stage 7" – the retention clean-up
   runs in MainViewModel.init.
4. Search views/ for any remaining hard-coded user-visible text and move it to strings.xml.
5. app/build.gradle.kts: versionName = "1.0.0" (versionCode stays 1).
6. Add test/viewmodel/StatsViewModelTest.kt (JVM, MainDispatcherRule, FakeStatsRepository):
   loading -> loaded state; an empty history shows zeros; activeDaysThisWeek and last7Days are
   passed through unchanged. If StatsViewModel has no error handling, add .catch and test it.
7. Run ./gradlew testDebugUnitTest, then tell me to run ./gradlew connectedDebugAndroidTest on an
   emulator. Give me a Markdown table of all test classes (unit and androidTest), what each covers,
   and how many tests it has – I will put it in the README "Testing" section.
```

**提交（分两次）：**

```
chore: remove unused StageNotice and duplicate theme logic; version 1.0.0
```

```
test(stats): add StatsViewModel tests
```

---

## 第六部分：根据 Assessment 2 完善（A1–A9）

R1–R16 完成后，我对照你的 A2 演示（*Where You Go, What You Do*）检查了项目。检查依据：

- 第 26 页的 8 条承诺；
- 第 11–12 页的 ACS 条款和对应的开发决定；
- 第 17 页的教育行业建议。

我能读到的文件包括 README、strings.xml、Manifest、docs、build.gradle。Kotlin 源码的目录太深，无法从你的电脑直接读取，所以对照的是 11 月 18 日的 zip，再加上 R1–R16 的修改内容。

### 检查结果

| A2 承诺（第 26 页） | 现状 | 要做的 |
|---|---|---|
| 1. 完全不用位置权限 | ✅ Manifest 只有 INTERNET | — |
| 2. 进度存在本机，一键导出和删除 | ⚠️ 导出漏了"举报过的题"；导出文件里写的版本号还是 `"0.1.0"` | A1 |
| 3. 通知必须主动开启 | ✅ app 不发任何通知 | — |
| 4. 诚实的数据安全申报，写给青少年看的隐私说明 | ❌ README 删掉了 Data safety 表；README 有多处与代码不符；隐私页还有 "telemetry" 这类术语 | A4、A9 |
| 5. 不用广告和广告 SDK，每个依赖都经过审查 | ✅ README 有依赖审查表 | — |
| 6. 激励但不操纵（不羞辱、有"今天到此为止"提示） | ⚠️ 没过关时结果页只有分数，没有基于掌握程度的温和反馈 | A5 |
| 7. 默认无障碍，配色经过对比度检查 | ⚠️ 对比度没有自动测试来证明 | A6 |
| 8. 每个功能都做五步 ACS 检查 | ❌ `docs/ethics-review.md` 还是模板，但 README 写着"已记录" | A7 |

**其他依据 A2 的改进：**

- **A2：可以撤销举报。** 对应 Conduct 1.2.2(d)：用户的控制权，撤销要和操作一样容易。
- **A3：数据控制集中在一处，并默认保留更少的数据。** 依据是第 8 页 ACCC v Google 的教训，加上 Ethics 2.2(d)。
- **A8：开发日志的时间线素材。**

### README 里与代码不符的地方（A9 会修）

这些属于 A2 第 11 页 Ethics 2.1(b) 说的"误导"，评分时也容易被看出来：

| README 写的 | 代码里实际是 |
|---|---|
| 9 个世界：General Knowledge、Science & Nature、History、Geography、Entertainment、Sports、Literature、Art、Mythology | `CategoryFilter` 只允许 7 个：General Knowledge、Science & Nature、Science: Computers、Science: Mathematics、Geography、History、Animals |
| ≥80% 得 3 星，≥50% 得 2 星 | `StarRating`：≥60% 得 1 星，≥80% 得 2 星，100% 得 3 星 |
| Leitner 间隔 1/3/7/14/30 天 | `LeitnerScheduler`：1/2/4/7/14 天 |
| Settings 用 "DataStore/Room" | 项目里没有 DataStore |
| "Ethics review per feature — Recorded in docs/ethics-review.md" | 这份文件还是模板 |
| 课程对照表只有 Lecture 1–8 | 缺 Lecture 9（发布、数据安全、本地化） |
| Data safety 表 | 被删掉了（这是 A2 承诺 #4） |

### 关于"由谁来写"的提醒（学术诚信）

- `docs/ethics-review.md` 开头写着 "Written by me (not by the AI agent)"，`docs/dev-log.md` 写着 "My own notes"。
- 所以 A7 和 A8 **只让 Agent 生成草稿和素材，放进单独的新文件**。正式的那两份由你用自己的话重写。
- 你的 AI 声明里也要写上"伦理审查草稿和时间线由 AI 生成，由我改写"。
- 这本身就是 A2 讲的诚实原则（Ethics 2.1）。

---

### A1：导出完整的数据，版本号正确

```
This step follows my Assessment 2 commitment #2 ("one-tap export and delete") and ACS Ethics 2.1(b)
(no misrepresentation by omission).

1. The export misses data the app stores about the learner: the reported questions. Add
   @Serializable data class ReportedQuestion(val questionText: String, val reportedAt: Long)
   and reportedQuestions: List<ReportedQuestion> to UserDataExport. Add to FlaggedQuestionDao:
   @Query("SELECT * FROM FlaggedQuestion ORDER BY flaggedAt DESC") suspend fun getAll(): List<FlaggedQuestionEntity>
   Inject FlaggedQuestionDao into PrivacyRepositoryImpl and fill the list in exportAllDataAsJson().
2. UserDataExport has appVersion: String = "0.1.0", but the app is 1.0.0. Remove the default value.
   Give PrivacyRepositoryImpl a constructor parameter appVersion: String; in di/Modules.kt pass
   BuildConfig.VERSION_NAME. (A constructor parameter keeps the JVM tests simple.)
3. Update privacy_export_desc to: "Save a file with all your scores, answers, review cards,
   settings and reported questions."
4. Update the fakes (FakeDaos) and Koin module.

Tests: ExportSerializerTest / a PrivacyRepository test – the JSON contains reportedQuestions and the
appVersion passed in; an empty list is exported as [].
```

**提交：**

```
fix(privacy): export reported questions and the real app version

- Export now includes every kind of data the app stores (A2 commitment #2)
- appVersion comes from BuildConfig.VERSION_NAME instead of "0.1.0"
```

### A2：可以撤销举报

```
This step follows ACS Conduct 1.2.2(d) from my Assessment 2 (slide 12): keep the learner in control;
undoing must be as easy as doing. Today a reported question is hidden forever with no way back.

1. FlaggedQuestionDao: add
   @Query("SELECT COUNT(*) FROM FlaggedQuestion") fun observeCount(): Flow<Int>
   @Query("DELETE FROM FlaggedQuestion") suspend fun deleteAll()
2. PrivacyRepository: fun observeReportedCount(): Flow<Int> and suspend fun restoreReportedQuestions().
3. PrivacyViewModel / PrivacyUIState: reportedCount, showRestoreConfirmation, requestRestore(),
   confirmRestore(), dismissRestore(); new PrivacyMessage RESTORED. try/catch -> FAILED.
4. PrivacyScreen: a new card under "Questions may contain mistakes":
   title "Reported questions", text from plurals "You have hidden %1$d question" / "%1$d questions",
   OutlinedButton "Show them again" (48 dp, disabled when the count is 0).
   Confirm dialog: title "Show reported questions again?", text "They can appear in quizzes again.
   You can report them again at any time.", buttons "Cancel" (common_cancel) and "Show again" –
   equal weight, OutlinedButton, weight(1f), 48 dp. Snackbar: "Reported questions will appear again."
5. All texts in strings.xml.

Tests: PrivacyViewModelTest (count is shown; confirm calls restore and sets RESTORED; failure sets
FAILED); androidTest PrivacyScreenTest (button disabled at 0; dialog buttons; Cancel does nothing).
```

**自检：**

1. 举报一道题，打开隐私页：显示 "You have hidden 1 question"。
2. 点 "Show them again" → "Show again"：数量变成 0，这道题以后又会出现。

**提交：**

```
feat(privacy): let learners undo reported questions

- Count of hidden questions and a "Show them again" action with an equal-weight dialog
- Undo is as easy as reporting (ACS Conduct 1.2.2(d))
```

### A3：数据控制集中在一处，默认保留更少

```
This step follows two points from my Assessment 2:
- Slide 8 (ACCC v Google LLC (No 2) [2021] FCA 367): a confusing settings architecture is itself a
  privacy dark pattern. Today "what is stored" and "how long" are explained on the Privacy screen,
  but the retention control is on the Settings screen.
- Ethics 2.2(d) / slide 25: store the minimum; a DAO query deletes records after N days. For 13–17
  year olds the default should be the most private reasonable choice (best interests of the child).

1. Move the "Answer history retention" control from SettingsScreen to PrivacyScreen, directly
   under the "How long we keep it" section (same option dialog/radio style, 48 dp). PrivacyViewModel
   gets SettingsRepository and TimeProvider: updateAnswerRetention(period) saves the setting and then
   calls privacyRepository.deleteExpiredAnswers(timeProvider.now()) in try/catch (move this logic and
   its tests from SettingsViewModel). Remove the retention section and function from Settings.
2. Settings: change settings_privacy_entry_desc to "What is stored, how long we keep it, export and
   delete" so the learner knows where the control went.
3. Rename the option "Forever" to "Until I delete it" (settings_retention_forever) – it is honest
   that the data can still be deleted.
4. Default for NEW installs: answerRetention = DAYS_30 (in UserSettings and SettingsEntity defaults).
   This is only a Kotlin default, not a @ColumnInfo default, so the Room schema does not change and
   existing users keep their choice. Update privacy_retention_desc to say the default is 30 days.
5. Update Koin, fakes and tests (SettingsViewModelTest, PrivacyViewModelTest, SettingsScreenTest,
   PrivacyScreenTest).

Tests: PrivacyViewModelTest – changing retention saves it and calls deleteExpiredAnswers; a fresh
UserSettings() has DAYS_30. Confirm app/schemas did not get a new version.
```

**自检：**

1. 设置页里不再有保留期限选项，隐私页里有，而且就在"How long we keep it"下面。
2. 改成 30 天后，再导出数据：30 天前的答题记录已经不在里面了。

**提交：**

```
refactor(privacy): keep all data controls on the Privacy screen; default retention 30 days

- Retention control sits next to its explanation (lesson from ACCC v Google)
- "Until I delete it" instead of "Forever"
- More private default for new installs; no schema change
```

### A4：写给青少年看的隐私文案

```
My Assessment 2 commitment #4 promised "a plain-language privacy notice written for young readers",
and slide 4 (contextual integrity) says data should only flow in ways the user expects.
Rewrite these strings in strings.xml (short sentences, no technical words, no "telemetry",
"HTTP", "Room" or "database"):

- privacy_what_is_stored_desc: "Your stars, scores, answer history, review cards, settings and the
  questions you reported are saved only on this phone."
- privacy_never_collected_desc: "QuizQuest never asks for your name, email, age or location. It has
  no ads and does not track how you use the app."
- privacy_opentdb_title: "Where the questions come from"
- privacy_opentdb_desc: "Questions are downloaded from opentdb.com over a secure connection. The
  request only says which world, which difficulty and how many questions. Like any website,
  opentdb.com can see your internet (IP) address."
- privacy_attribution_desc: "Questions come from the Open Trivia Database (opentdb.com) and are
  shared under the CC BY-SA 4.0 licence, which allows reuse with credit."
- privacy_feedback_title: "Feedback"
- privacy_feedback_desc: "Questions or ideas? Email the developer, Yibo Pan (a JCU student). Your
  email app will share your email address – please don't include other personal information."
- privacy_snackbar_deleted: "All your data was deleted."
- privacy_snackbar_failed: "Something went wrong. Please try again."
- review_box_label: "Box %1$d of 5"
Keep the meaning of every section; do not remove any section. Update UI tests that look for the
old texts.
```

**做完后手动确认：** `feedback_email` 现在是 `yibo.pan@my.jcu.edu.au`，请确认这确实是你的学生邮箱。

**提交：**

```
fix(privacy): plain-language notice for young readers

- No technical words; explains what opentdb.com can see
- Feedback note: the email app shares the learner's address
```

### A5：结果页的温和反馈（以掌握程度为本）

```
My Assessment 2 (slide 17) promised "mastery-based rewards; no penalty or shaming", and slide 7
explains that learners under stress are the most affected by manipulative design. Today the Result
screen only shows the score, so a failed level gives no guidance.

1. ResultScreen: under the score, show ONE neutral sentence chosen by stars (string resources):
   - 3 stars: "Perfect score – you know this level well."
   - 2 stars: "Great result. You passed this level."
   - 1 star: "You passed this level. Replay it any time to earn more stars."
   - 0 stars: "Not passed yet – that's part of learning. Try this level again when you're ready."
   If the learner missed at least one question, add: "The questions you missed are now in Review."
2. No words like "failed", "wrong" or "bad"; no extra animation or sound for 0 stars. Keep the
   existing equal-weight buttons and the "done for today" cue unchanged.
3. Put the sentence in a Text with liveRegion = Polite so TalkBack reads it once.

Tests: androidTest ResultScreenTest – the 0-star state shows the "Not passed yet" text and no node
contains "fail"; the 3-star state shows the perfect-score text; the Review sentence appears only when
correctCount < totalCount.
```

**提交：**

```
feat(result): neutral, mastery-based feedback after each level

- One encouraging sentence per star result; no shaming words
- Points to Review for missed questions
```

### A6：配色对比度自动测试

```
My Assessment 2 commitment #7 promised a "contrast-checked Material 3 theme". Make that provable.

1. Add ui/theme/ContrastChecker.kt (pure Kotlin): WCAG 2.1 relative luminance on ARGB Int and
   fun ratio(foreground: Int, background: Int): Double.
2. Make LightColorScheme and DarkColorScheme in Theme.kt internal (not private) so tests can read
   them.
3. Add test/.../ui/theme/ThemeContrastTest.kt. For light AND dark assert >= 4.5:1 for:
   primary/onPrimary, primaryContainer/onPrimaryContainer, secondaryContainer/onSecondaryContainer,
   tertiaryContainer/onTertiaryContainer, errorContainer/onErrorContainer, surface/onSurface,
   surfaceVariant/onSurfaceVariant, background/onBackground; and >= 3:1 for primary/surface.
   Use Color.toArgb(). Also unit-test ContrastChecker itself (black/white = 21:1, same colour = 1:1).
4. If any pair fails, adjust the colour in Color.kt (keep the look as close as possible) – never
   lower the threshold. Tell me which colours changed.
```

**提交：**

```
test(theme): prove the colour scheme meets WCAG contrast

- ContrastChecker (pure Kotlin) and ThemeContrastTest for light and dark
```

### A7：伦理审查草稿（A2 承诺 #8）

```
My Assessment 2 commitment #8: "Ethics review per feature – the five-step ACS check from slide 13
before each release." docs/ethics-review.md is still the template, and its header says it is
written by me, not by the AI agent. So do NOT edit docs/ethics-review.md.

Create a NEW file docs/ethics-review-draft.md, clearly marked at the top:
"DRAFT generated by the AI agent as notes. Yibo rewrites each entry in his own words in
docs/ethics-review.md."

For each item in the "Features to review" list in docs/ethics-review.md, write a short draft using
the template in that file:
1. Stakeholders (ACS Conduct 1.2.1(a))
2. Values at stake, with the ACS clause (Ethics 2.1(b), 2.2(d), 2.2(h), 2.3.1; Conduct 1.2.2(c),
   1.2.2(d), 1.2.3(b), 1.2.4(c))
3. Conflict
4. Option chosen and options rejected
5. Decision & reason
Plus an "Evidence" line naming the real classes, files and test classes (e.g. RetentionPolicy,
data_extraction_rules.xml, CategoryFilter, DailyGoalTracker – no streaks, the equal-weight dialogs,
the only INTERNET permission, ThemeContrastTest). Add the new items from steps A1–A5 (complete
export, undo report, retention on the Privacy screen, plain-language notice, result feedback).
End with a "Dark-pattern checklist" table (nagging, obstruction, sneaking, interface interference,
forced action – Gray et al., 2018) showing how the app avoids each one, with evidence.
Only state facts you can see in the code; if something is not implemented, say so.
```

**做完后要自己写：** 对照草稿，在 `docs/ethics-review.md` 里用自己的话写每一条，勾掉 "Features to review" 里的方框。草稿可以删掉，也可以留着。如果留着，README 和 AI 声明里要说明它是 AI 生成的。

**提交：**

```
docs(ethics): AI-drafted notes for the five-step ACS reviews
```

自己写完正式版后：

```
docs(ethics): five-step ACS review for every feature
```

### A8：开发日志的时间线素材

```
docs/dev-log.md says it contains my own notes, so do NOT edit it. Create docs/git-timeline.md
(marked "Generated from git log by the AI agent – facts only") with a table built from
`git log --date=short --pretty=format:"%ad|%s"`: Date | Stage (from the commit message) |
What changed (one line, facts only). Group commits by day. Do not write feelings, opinions or
ethical reasoning – I will write those in dev-log.md myself.
```

**做完后要自己写：** 根据时间线，在 `dev-log.md` 里用自己的话补上每天的"做得好 / 不好"、伦理决定和感受。这是 Gibbs 反思里 Feelings 和 Evaluation 的素材。

**提交：**

```
docs: add a git timeline as notes for the development log
```

### A9：README 诚实性修正（最后做）

```
Make README.md accurate and complete. My Assessment 2 (slide 11, ACS Ethics 2.1(b)) says do not
misrepresent "directly, indirectly or by omission". Check EVERY number and claim in README.md
against the code and fix it. Known errors:
- Worlds: CategoryFilter.ALLOWED_CATEGORY_IDS has 7 categories (9 General Knowledge, 17 Science &
  Nature, 18 Science: Computers, 19 Science: Mathematics, 22 Geography, 23 History, 27 Animals) –
  not 9 worlds with Entertainment, Sports, Literature, Art, Mythology.
- Stars: StarRating gives 1 star at >= 60 %, 2 stars at >= 80 %, 3 stars at 100 %.
- Leitner intervals: LeitnerScheduler.INTERVAL_DAYS = 1, 2, 4, 7, 14 days.
- There is no DataStore; settings are stored in Room.
- Re-count the tests per class from the source files and fix the test tables.
Also:
1. Restore the section "Data safety (as it would be declared on Google Play – Lecture 9)" with a
   table: Location – No; Personal info – No; App activity (scores, answers, reported questions) –
   stored on device only, deleted by the retention setting or "Delete all my data"; Device or
   advertising IDs – No; Network – HTTPS requests to opentdb.com contain only category, difficulty,
   amount and a random session token, and opentdb.com can see the IP address. Add: "LeakCanary is
   added only to debug builds; its storage and notification permissions are not in the release
   build, which has only INTERNET."
2. Course alignment: add the Lecture 9 row (release checklist, versionName 1.0.0, BuildConfig,
   strings.xml for localisation, data safety, target audience 13–17).
3. Ethical design table: one row per Assessment 2 commitment (slide 26) with the class/file AND the
   test that shows it (e.g. ThemeContrastTest for contrast, PrivacyScreenTest for the equal-weight
   delete dialog). Add rows for A1–A5. Replace absolute claims like "no dark patterns" with
   "designed to avoid dark patterns – see the checklist in docs/ethics-review.md".
4. New short section "Laws and policies considered" (ACS Conduct 1.2.4(c)): Privacy Act 1988 and
   the Australian Privacy Principles, the Children's Online Privacy Code (OAIC), Google Play's
   Families and User Data policies and the Data safety section.
5. Keep a "Screenshots" heading with a TODO for phone and tablet, light and dark.
Do not invent features. If a claim cannot be checked in the code, remove it.
```

**提交：**

```
docs(readme): correct facts, restore data safety and map every A2 commitment to code and tests
```

---

## 全部完成后

**1. 跑一次完整检查。** 在 Agent 里粘贴：

```
Run ./gradlew testDebugUnitTest and ./gradlew lintDebug. Fix any new warnings about hard-coded
text, missing content descriptions or unused resources. Do not change behaviour.
```

**2. 手动步骤：**

- **启动图标：**
  1. 右键 `res` → New → Image Asset。
  2. Icon Type 选 **Launcher Icons (Adaptive and Legacy)**，Legacy 页签保持 Generate = Yes，点 Finish。
  3. 这样 Android 7 上也有图标。提交信息：`fix(ui): add legacy launcher icons for API 24-25`。
- **无障碍扫描：** 用 Accessibility Scanner 扫首页、答题、设置、隐私四个界面，截图放进 `docs/`。

**3. 接着做原来的阶段 9 和阶段 10**（见《QuizQuest_阶段提示词.md》）。

- README 的数据安全表、LeakCanary 说明和测试表格，A9 已经处理过了。
- 阶段 10 只需要补截图，并在 README 里确认 A7 和 A8 的草稿已经标明是 AI 生成的。

**4. 注意：** 11 月 24 日检查时，启动图标仍然只有 `mipmap-anydpi-v26`，上面第 2 条的 Image Asset 步骤还没做。

**5. push。** 把仓库分享给讲师和课程协调人。
