package com.yibopan.quizquest.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.yibopan.quizquest.R

class AndroidSoundPlayer(context: Context) : SoundPlayer {
    private val soundPool: SoundPool
    private val correctSoundId: Int
    private val wrongSoundId: Int

    init {
        val appContext = context.applicationContext
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        correctSoundId = soundPool.load(appContext, R.raw.sfx_correct, 1)
        wrongSoundId = soundPool.load(appContext, R.raw.sfx_wrong, 1)
    }

    override fun playCorrect() {
        if (correctSoundId != 0) {
            soundPool.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    override fun playWrong() {
        if (wrongSoundId != 0) {
            soundPool.play(wrongSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    override fun release() {
        soundPool.release()
    }
}
