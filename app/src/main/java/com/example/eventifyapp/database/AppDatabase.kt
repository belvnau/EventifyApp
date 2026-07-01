package com.example.eventifyapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.eventifyapp.dao.EventDao
import com.example.eventifyapp.dao.MessageDao
import com.example.eventifyapp.dao.NotificationDao
import com.example.eventifyapp.dao.ReviewDao
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.model.Message
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.model.Review
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Event::class,
        Message::class,
        NotificationItem::class,
        Review::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reviewDao(): ReviewDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eventify_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            CoroutineScope(Dispatchers.IO).launch {

                                INSTANCE?.let {

                                    populateDatabase(it)

                                }

                            }

                        }

                    })
                    .build()

                INSTANCE = instance

                instance

            }

        }

        private suspend fun populateDatabase(database: AppDatabase) {

            database.eventDao().insertEvents(
                DataSeeder.getDummyEvents()
            )

        }

    }

}