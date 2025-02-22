package com.example.labyrinthmarblegame

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarbleGameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: MarbleGameScore): Unit

    // Use Flow<> to avoid Cursor conversion issues
    @Query("SELECT * FROM marble_game_score WHERE level = :level ORDER BY completionTime ASC LIMIT 1")
    fun getBestScoreForLevel(level: Int): Flow<MarbleGameScore?>

    @Query("SELECT * FROM marble_game_score ORDER BY completionTime ASC")
    fun getAllScores(): Flow<List<MarbleGameScore>>

    @Query("DELETE FROM marble_game_score")
    suspend fun clearScores(): Unit
}
