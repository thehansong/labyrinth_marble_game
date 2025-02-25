package com.example.labyrinthmarblegame

import android.content.Context
import android.media.SoundPool
import android.media.AudioAttributes
import android.media.MediaPlayer

lateinit var bgmPlayer: MediaPlayer
lateinit var soundPool: SoundPool

var wallCollisionSoundId: Int = 0
var gameClearedSoundId: Int = 0
var gameRestartSoundId: Int = 0
var nextLevelSoundId: Int = 0

fun initBGM(context: Context) {
    bgmPlayer = MediaPlayer.create(context, R.raw.game_bgm)
    bgmPlayer.isLooping = true
    bgmPlayer.setVolume(0.5f, 0.5f)
}

fun initSounds(context: Context) {
    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(audioAttributes)
        .build()

    // Load sound resources
    wallCollisionSoundId = soundPool.load(context, R.raw.collision_sfx, 1)
    gameClearedSoundId = soundPool.load(context, R.raw.gamecleared_sfx, 1)
    gameRestartSoundId = soundPool.load(context, R.raw.gamerestart_sfx, 1)
    nextLevelSoundId = soundPool.load(context, R.raw.nextlevel_sfx, 1)
}