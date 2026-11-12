package com.yibopan.quizquest.di

import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import com.yibopan.quizquest.viewmodel.HomeViewModel
import com.yibopan.quizquest.viewmodel.PrivacyViewModel
import com.yibopan.quizquest.viewmodel.QuizViewModel
import com.yibopan.quizquest.viewmodel.ResultViewModel
import com.yibopan.quizquest.viewmodel.ReviewViewModel
import com.yibopan.quizquest.viewmodel.SettingsViewModel
import com.yibopan.quizquest.viewmodel.StatsViewModel
import com.yibopan.quizquest.viewmodel.WorldMapViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * All Koin definitions of the app (Lecture 4: "Create a new package named di ... Modules").
 *
 * - `single` = one shared instance (API, database, DAOs, repositories).
 * - `viewModel` = a new ViewModel per screen that is cleared with that screen. (Lecture 4 used
 *   `single { PetsViewModel(get()) }`; with several screens `viewModel {}` is the correct Koin scope.)
 * - Navigation arguments reach ViewModels through `koinViewModel(parameters = { parametersOf(...) })`.
 */
val appModules = module {

    // ----- Lecture 5: dispatcher for repositories (injected so tests can replace it) -----
    single { Dispatchers.IO }
    single<TimeProvider> { SystemTimeProvider }

    // ----- Lecture 5 + Lecture 8: networking (Stage 2) -----
    // TODO(Stage 2) single { OkHttpClient } with connect/read timeouts
    // TODO(Stage 2) single { Retrofit } with Json { ignoreUnknownKeys = true }, the converter factory,
    //               client(get()) and TriviaAPI.BASE_URL
    // TODO(Stage 2) single { get<Retrofit>().create(TriviaAPI::class.java) }
    // TODO(Stage 2) single { RateLimiter() }

    // ----- Lecture 7: Room database + DAOs (Stage 4) -----
    // TODO(Stage 4) single { Room.databaseBuilder(androidContext(), QuizDatabase::class.java, "quiz-database").build() }
    // TODO(Stage 4) single { get<QuizDatabase>().questionDao() }  ... one line per DAO

    // ----- Lecture 4: repositories (interface -> implementation) -----
    // TODO(Stage 2) single<TriviaRepository> { TriviaRepositoryImpl(get(), get(), get()) }
    // TODO(Stage 4) ProgressRepository, ReviewRepository, StatsRepository, SettingsRepository
    // TODO(Stage 7) PrivacyRepository

    // ----- Lecture 4: ViewModels (add get() for each new constructor dependency) -----
    viewModel { HomeViewModel() }
    viewModel { (categoryId: Int) -> WorldMapViewModel(categoryId = categoryId) }
    viewModel { (categoryId: Int, level: Int) -> QuizViewModel(categoryId = categoryId, level = level) }
    viewModel { (sessionId: Long) -> ResultViewModel(sessionId = sessionId) }
    viewModel { ReviewViewModel() }
    viewModel { StatsViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { PrivacyViewModel() }
}
