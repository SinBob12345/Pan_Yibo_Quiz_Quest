package com.yibopan.quizquest.data.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.yibopan.quizquest.R

/** Interface for playing sound effects (Stage 8). */
interface SoundPlayer {
    fun playCorrect()
    fun playWrong()
    fun release()
}

class AndroidSoundPlayer(context: Context) : SoundPlayer {
    private val soundPool: SoundPool
    private val correctSoundId: Int
    private val wrongSoundId: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        correctSoundId = soundPool.load(context, R.raw.sfx_correct, 1)
        wrongSoundId = soundPool.load(context, R.raw.sfx_wrong, 1)
    }

    override fun playCorrect() {
        soundPool.play(correctSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    override fun playWrong() {
        soundPool.play(wrongSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    override fun release() {
        soundPool.release()
    }
}
