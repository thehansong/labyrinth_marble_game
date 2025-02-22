package com.example.labyrinthmarblegame

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marble_game_score")
data class MarbleGameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val level: Int,
    val completionTime: Long,
    val playerName: String
)
