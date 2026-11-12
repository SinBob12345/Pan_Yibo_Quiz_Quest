package com.yibopan.quizquest

import android.app.Application
import com.yibopan.quizquest.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application class (Lecture 4). Registered in AndroidManifest.xml with android:name.
 *
 * Stage 7 adds a one-off clean-up of old answer records here (data-retention rule, A2 slide 25).
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
