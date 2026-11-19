package com.yibopan.quizquest

import android.app.Application
import com.yibopan.quizquest.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class (Lecture 4). Registered in AndroidManifest.xml with android:name.
 *
 * Retention clean-up of old answer records runs in [com.yibopan.quizquest.viewmodel.MainViewModel] on app startup (A2 Commitment #2).
 */
class QuizQuestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Lecture 7: Room needs the application context, so Koin must know it.
            androidContext(applicationContext)
            modules(appModules)
        }
    }
}
