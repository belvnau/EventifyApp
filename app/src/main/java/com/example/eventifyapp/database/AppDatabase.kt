package com.example.eventifyapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.eventifyapp.dao.*
import com.example.eventifyapp.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Event::class, Message::class, NotificationItem::class, Review::class, User::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reviewDao(): ReviewDao
    abstract fun userDao(): UserDao

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
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Pastikan data terisi jika kosong setelah migrasi
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    if (database.eventDao().getEventCount() == 0) {
                                        populateDatabase(database)
                                    }
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
            val events = DataSeeder.getDummyEvents()
            database.eventDao().insertEvents(events)
            
            database.userDao().insertUser(User(
                id = 1,
                name = "Syaira Poetry",
                username = "poetrysya",
                email = "poetrysa@gmail.com",
                password = "password123",
                bio = "Event enthusiast and creative designer.",
                location = "Jakarta, ID"
            ))

            // Seed dummy messages
            database.messageDao().insertMessage(Message(
                senderName = "Rian",
                senderEmail = "rian@gmail.com",
                message = "Halo, besok ada yang berangkat bareng ke Tech Conference 2026?",
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = false
            ))
            database.messageDao().insertMessage(Message(
                senderName = "Sarah",
                senderEmail = "sarah@gmail.com",
                message = "Kak, mau tanya untuk K-Pop Picnic nanti registrasinya di mana ya?",
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = false
            ))

            // Seed dummy notifications matching mockup
            database.notificationDao().insertNotification(NotificationItem(
                title = "Antonio Lee",
                message = "started following you",
                type = "system",
                timestamp = System.currentTimeMillis() - 10000, // 10s ago
                isRead = false
            ))
            database.notificationDao().insertNotification(NotificationItem(
                title = "Michael Clifford",
                message = "invite you on Doodle x English Meetup 2025",
                type = "invite",
                timestamp = System.currentTimeMillis() - 60000, // 1 min ago
                isRead = false
            ))
            database.notificationDao().insertNotification(NotificationItem(
                title = "You",
                message = "add Doodle x English Meetup to your event",
                type = "event",
                timestamp = System.currentTimeMillis() - 3600000, // 1 hr ago
                isRead = false
            ))
            database.notificationDao().insertNotification(NotificationItem(
                title = "Demas Handika",
                message = "liked your Artket 05 review",
                type = "event",
                timestamp = System.currentTimeMillis() - 72000000, // 20 hr ago (Yesterday)
                isRead = true
            ))
        }
    }
}
