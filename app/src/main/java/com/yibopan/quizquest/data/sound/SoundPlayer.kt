package com.yibopan.quizquest.data.sound

/** Interface for playing sound effects (Stage 8). */
interface SoundPlayer {
    fun playCorrect()
    fun playWrong()
    fun release()
}
