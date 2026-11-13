package com.yibopan.quizquest.di

import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.yibopan.quizquest.data.database.QuizDatabase
import com.yibopan.quizquest.data.network.RateLimiter
import com.yibopan.quizquest.data.network.TriviaAPI
import com.yibopan.quizquest.data.repository.TriviaRepository
import com.yibopan.quizquest.data.repository.TriviaRepositoryImpl
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
    single<TriviaRepository> { TriviaRepositoryImpl(get<TriviaAPI>(), get<RateLimiter>(), get<CoroutineDispatcher>()) }
    // TODO(Stage 4) ProgressRepository, ReviewRepository, StatsRepository, SettingsRepository
    // TODO(Stage 7) PrivacyRepository

    // ----- Lecture 4: ViewModels (add get() for each new constructor dependency) -----
    viewModel { HomeViewModel() }
    viewModel { (categoryId: Int) -> WorldMapViewModel(categoryId = categoryId) }
    viewModel { (categoryId: Int, level: Int) -> QuizViewModel(categoryId = categoryId, level = level) }
    viewModel { (sessionId: Long, categoryId: Int, level: Int) ->
        ResultViewModel(sessionId = sessionId, categoryId = categoryId, level = level)
    }
    viewModel { ReviewViewModel() }
    viewModel { StatsViewModel() }
    viewModel { SettingsViewModel() }
    viewModel { PrivacyViewModel() }
}
