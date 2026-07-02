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
    version = 9,
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
            val insertedIds = mutableListOf<Long>()
            for (event in events) {
                val id = database.eventDao().insertEvent(event)
                insertedIds.add(id)
                // Tambah dummy notifikasi
                val notifications = DataSeeder.getDummyNotifications()
                for (notif in notifications) {
                    database.notificationDao().insertNotification(notif)
                }
            }
            
            database.userDao().insertUser(User(
                id = 1,
                name = "Syaira Poetry",
                username = "poetrysya",
                email = "poetrysa@gmail.com",
                password = "password123",
                bio = "Event enthusiast and creative designer.",
                location = "Jakarta, ID"
            ))

            val cal = java.util.Calendar.getInstance()
            for (eventId in insertedIds) {
                // Review 1: Christian Bale (16 Sep, 2025)
                cal.set(2025, java.util.Calendar.SEPTEMBER, 16, 10, 0, 0)
                database.reviewDao().insertReview(Review(
                    eventId = eventId,
                    reviewerName = "Christian Bale",
                    rating = 4.0f,
                    comment = "The event was so much fun. I met many people with similar interest.",
                    timestamp = cal.timeInMillis
                ))
                
                // Review 2: Sebastian Tet (07 Jul, 2025)
                cal.set(2025, java.util.Calendar.JULY, 7, 14, 30, 0)
                database.reviewDao().insertReview(Review(
                    eventId = eventId,
                    reviewerName = "Sebastian Tet",
                    rating = 4.8f,
                    comment = "The event was well-organized, and I liked how easy it was.",
                    timestamp = cal.timeInMillis
                ))
                
                // Review 3: Saddam Mufti (14 Feb, 2025)
                cal.set(2025, java.util.Calendar.FEBRUARY, 14, 9, 15, 0)
                database.reviewDao().insertReview(Review(
                    eventId = eventId,
                    reviewerName = "Saddam Mufti",
                    rating = 3.0f,
                    comment = "Worth it banget! Penjelasan materinya gampang dimengerti buat pemula.",
                    timestamp = cal.timeInMillis
                ))
                
                // Review 4: Naura Belva (27 Jul, 2025)
                cal.set(2025, java.util.Calendar.JULY, 27, 16, 45, 0)
                database.reviewDao().insertReview(Review(
                    eventId = eventId,
                    reviewerName = "Naura Belva",
                    rating = 4.2f,
                    comment = "However, I felt that some parts of the event were a bit too crowded.",
                    timestamp = cal.timeInMillis
                ))
                
                // Review 5: Saddam Aditya (05 Jan, 2025)
                cal.set(2025, java.util.Calendar.JANUARY, 5, 11, 20, 0)
                database.reviewDao().insertReview(Review(
                    eventId = eventId,
                    reviewerName = "Saddam Aditya",
                    rating = 4.1f,
                    comment = "Best event of the year! Gak sabar buat ikutan event selanjutnya.",
                    timestamp = cal.timeInMillis
                ))
            }

            // Seed Messages
            val myEmail = "poetrysa@gmail.com"
            val now = System.currentTimeMillis()

            // Private Chats
            database.messageDao().insertMessage(Message(
                senderName = "Naura Belva",
                senderEmail = "naura@gmail.com",
                receiverEmail = myEmail,
                message = "Nice to hear from you! This is very exciting.",
                timestamp = now - 600000,
                isRead = false,
                isCommunity = false
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Saddam Aditya",
                senderEmail = "saddam_aditya@gmail.com",
                receiverEmail = myEmail,
                message = "You know what to do :D",
                timestamp = now - 1800000,
                isRead = false,
                isCommunity = false
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Graceu Larisma",
                senderEmail = "graceu@gmail.com",
                receiverEmail = myEmail,
                message = "This is so nice! I'm glad for you",
                timestamp = now - 3600000,
                isRead = false,
                isCommunity = false
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Saddam Mufti",
                senderEmail = "saddam_mufti@gmail.com",
                receiverEmail = myEmail,
                message = "Interesting 😜",
                timestamp = now - 7200000,
                isRead = false,
                isCommunity = false
            ))

            // Circle Group Chat
            database.messageDao().insertMessage(Message(
                senderName = "Graceu Larisma",
                senderEmail = "circle_1@gmail.com",
                receiverEmail = myEmail,
                message = "Guys let's organize again!",
                timestamp = now - 14400000,
                isRead = true,
                isCommunity = false,
                groupTitle = "Naura, Graceu, Nasywa"
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Syaira Poe",
                senderEmail = "syaira_poe@gmail.com",
                receiverEmail = myEmail,
                message = "Where are u from?",
                timestamp = now - 28800000,
                isRead = false,
                isCommunity = false
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Nasywa Sasikirana",
                senderEmail = "nasywa_sasi@gmail.com",
                receiverEmail = myEmail,
                message = "Hi!!",
                timestamp = now - 43200000,
                isRead = true,
                isCommunity = false
            ))

            // Community Chats
            database.messageDao().insertMessage(Message(
                senderName = "Jakarta Arts Council",
                senderEmail = "community_event_2",
                receiverEmail = "",
                message = "Halo teman-teman! Selamat bergabung di grup komunitas Jazz Music Festival. Silakan gunakan grup ini untuk berdiskusi.",
                timestamp = now - 3600000,
                isRead = true,
                isCommunity = true,
                eventId = 2,
                groupTitle = "Jazz Music Festival Community"
            ))

            database.messageDao().insertMessage(Message(
                senderName = "Tech Indonesia",
                senderEmail = "community_event_1",
                receiverEmail = "",
                message = "Halo dev! Selamat datang di grup komunitas Tech Conference 2026.",
                timestamp = now - 7200000,
                isRead = true,
                isCommunity = true,
                eventId = 1,
                groupTitle = "Tech Conference 2026 Community"
            ))
        }
    }
}
