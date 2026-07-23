package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScamMessage::class, ScamCall::class, ManualBlock::class], version = 3, exportSchema = false)
abstract class ScamDatabase : RoomDatabase() {
    abstract fun scamDao(): ScamDao

    companion object {
        @Volatile
        private var INSTANCE: ScamDatabase? = null

        fun getDatabase(context: Context): ScamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScamDatabase::class.java,
                    "scam_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
