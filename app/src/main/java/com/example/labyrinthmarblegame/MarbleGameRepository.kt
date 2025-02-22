package com.example.labyrinthmarblegame

import kotlinx.coroutines.flow.Flow

class MarbleGameRepository(private val marbleGameDao: MarbleGameDao) {
    val topHighScores: Flow<List<MarbleGameScore>> = marbleGameDao.getAllScores()

    fun getBestScoreForLevel(level: Int): Flow<MarbleGameScore?> {
        return marbleGameDao.getBestScoreForLevel(level)
    }

    suspend fun insertScore(score: MarbleGameScore) {
        marbleGameDao.insertScore(score)
    }

    suspend fun clearScores() {
        marbleGameDao.clearScores()
    }
}
