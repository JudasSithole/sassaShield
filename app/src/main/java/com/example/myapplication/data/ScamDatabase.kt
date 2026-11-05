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
                // TODO: Replace fallbackToDestructiveMigration with proper Room migrations once
                // room-runtime dependency is resolved. Destructive migration wipes user data on
                // schema changes; fallbackToDestructiveMigration(false) prevents data loss but will
                // fail to open if the schema version mismatches.
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
