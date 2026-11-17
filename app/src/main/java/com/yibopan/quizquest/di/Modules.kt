package com.yibopan.quizquest.di

import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yibopan.quizquest.data.database.AnswerRecordDao
import com.yibopan.quizquest.data.database.CategoryDao
import com.yibopan.quizquest.data.database.FlaggedQuestionDao
import com.yibopan.quizquest.data.database.LevelProgressDao
import com.yibopan.quizquest.data.database.QuestionDao
import com.yibopan.quizquest.data.database.QuizDatabase
import com.yibopan.quizquest.data.database.QuizSessionDao
import com.yibopan.quizquest.data.database.ReviewCardDao
import com.yibopan.quizquest.data.database.SettingsDao
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.data.network.TriviaAPI
import com.yibopan.quizquest.data.repository.PrivacyRepository
import com.yibopan.quizquest.data.repository.PrivacyRepositoryImpl
import com.yibopan.quizquest.data.repository.ProgressRepository
import com.yibopan.quizquest.data.repository.ProgressRepositoryImpl
import com.yibopan.quizquest.data.repository.ReviewRepository
import com.yibopan.quizquest.data.repository.ReviewRepositoryImpl
import com.yibopan.quizquest.data.repository.SettingsRepository
import com.yibopan.quizquest.data.repository.SettingsRepositoryImpl
import com.yibopan.quizquest.data.repository.StatsRepository
import com.yibopan.quizquest.data.repository.StatsRepositoryImpl
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.repository.TriviaRepositoryImpl
import com.yibopan.quizquest.data.rules.AnswerShuffler
import com.yibopan.quizquest.data.rules.QuizEngine
import com.yibopan.quizquest.data.rules.SystemTimeProvider
import com.yibopan.quizquest.data.rules.TimeProvider
import com.yibopan.quizquest.viewmodel.HomeViewModel
import com.yibopan.quizquest.viewmodel.MainViewModel
import com.yibopan.quizquest.viewmodel.PrivacyViewModel
import com.yibopan.quizquest.viewmodel.QuizViewModel
import com.yibopan.quizquest.viewmodel.ResultViewModel
import com.yibopan.quizquest.viewmodel.ReviewViewModel
import com.yibopan.quizquest.viewmodel.SettingsViewModel
import com.yibopan.quizquest.viewmodel.StatsViewModel
import com.yibopan.quizquest.viewmodel.WorldMapViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

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
    single<CoroutineDispatcher> { Dispatchers.IO }
    single<TimeProvider> { SystemTimeProvider }

    // ----- Pure game rules -----
    single { AnswerShuffler() }
    single { QuizEngine(get()) }

    // ----- Lecture 5 + Lecture 8: networking (Stage 2) -----
    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(TriviaAPI.BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(TriviaAPI::class.java) }
    single { RateLimiter() }

    // ----- Lecture 7: Room database + DAOs (Stage 4) -----
    single {
        Room.databaseBuilder(
            androidContext(),
            QuizDatabase::class.java,
            "quiz-database",
        ).build()
    }
    single { get<QuizDatabase>().categoryDao() }
    single { get<QuizDatabase>().questionDao() }
    single { get<QuizDatabase>().flaggedQuestionDao() }
    single { get<QuizDatabase>().levelProgressDao() }
    single { get<QuizDatabase>().quizSessionDao() }
    single { get<QuizDatabase>().answerRecordDao() }
    single { get<QuizDatabase>().reviewCardDao() }
    single { get<QuizDatabase>().settingsDao() }

    // ----- Lecture 4: repositories (interface -> implementation) -----
    single<TriviaRepository> {
        TriviaRepositoryImpl(
            get<TriviaAPI>(),
            get<RateLimiter>(),
            get<CategoryDao>(),
            get<QuestionDao>(),
            get<FlaggedQuestionDao>(),
            get<TimeProvider>(),
            get<CoroutineDispatcher>(),
        )
    }
    single<ProgressRepository> {
        ProgressRepositoryImpl(
            get<LevelProgressDao>(),
            get<QuizSessionDao>(),
            get<AnswerRecordDao>(),
            get<TimeProvider>(),
            get<CoroutineDispatcher>(),
        )
    }
    single<ReviewRepository> {
        ReviewRepositoryImpl(
            get<ReviewCardDao>(),
            get<CoroutineDispatcher>(),
        )
    }
    single<StatsRepository> {
        StatsRepositoryImpl(
            get<QuizSessionDao>(),
            get<CategoryDao>(),
            get<TimeProvider>(),
        )
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            get<SettingsDao>(),
            get<CoroutineDispatcher>(),
        )
    }
    single<PrivacyRepository> {
        PrivacyRepositoryImpl(
            database = get(),
            settingsDao = get(),
            levelProgressDao = get(),
            quizSessionDao = get(),
            answerRecordDao = get(),
            reviewCardDao = get(),
            timeProvider = get(),
            dispatcher = get(),
        )
    }

    // ----- Lecture 4: ViewModels (add get() for each new constructor dependency) -----
    viewModel {
        MainViewModel(
            settingsRepository = get(),
            privacyRepository = get(),
            timeProvider = get(),
        )
    }
    viewModel {
        HomeViewModel(
            triviaRepository = get(),
            progressRepository = get(),
            reviewRepository = get(),
            statsRepository = get(),
            settingsRepository = get(),
            timeProvider = get(),
        )
    }
    viewModel { (categoryId: Int) ->
        WorldMapViewModel(
            categoryId = categoryId,
            progressRepository = get(),
            triviaRepository = get(),
        )
    }
    viewModel { (categoryId: Int, level: Int) ->
        QuizViewModel(
            categoryId = categoryId,
            level = level,
            triviaRepository = get(),
            progressRepository = get(),
            reviewRepository = get(),
            settingsRepository = get(),
            quizEngine = get(),
            timeProvider = get(),
        )
    }
    viewModel { (sessionId: Long, categoryId: Int, level: Int) ->
        ResultViewModel(
            sessionId = sessionId,
            categoryId = categoryId,
            level = level,
            progressRepository = get(),
            settingsRepository = get(),
            timeProvider = get(),
        )
    }
    viewModel { ReviewViewModel(reviewRepository = get(), timeProvider = get()) }
    viewModel { StatsViewModel(statsRepository = get()) }
    viewModel { SettingsViewModel(settingsRepository = get()) }
    viewModel { PrivacyViewModel(privacyRepository = get()) }
}
