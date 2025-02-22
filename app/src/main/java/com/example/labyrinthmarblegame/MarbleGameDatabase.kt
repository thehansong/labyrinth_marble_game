package com.example.labyrinthmarblegame

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MarbleGameScore::class], version = 1, exportSchema = false)
abstract class MarbleGameDatabase : RoomDatabase() {

    abstract fun marbleGameDao(): MarbleGameDao

    companion object {
        @Volatile
        private var INSTANCE: MarbleGameDatabase? = null

        fun getDatabase(context: Context): MarbleGameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarbleGameDatabase::class.java,
                    "marble_game_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
