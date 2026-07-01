package com.example.eventifyapp.database

import com.example.eventifyapp.model.Event

object DataSeeder {

    fun getDummyEvents(): List<Event> {
        return listOf(
            Event(
                id = 0,
                title = "Tech Conference 2026",
                description = "Annual technology conference featuring latest innovations in AI, ML, and Cloud Computing.",
                location = "Jakarta Convention Center",
                date = "2026-07-15",
                time = "09:00 - 17:00",
                price = "Rp 500.000",
                category = "Technology",
                imageUrl = "img_tech",
                organizer = "Tech Indonesia"
            ),
            Event(
                id = 0,
                title = "Jazz Music Festival",
                description = "A night of smooth jazz featuring international and local artists.",
                location = "Taman Ismail Marzuki",
                date = "2026-07-20",
                time = "19:00 - 23:00",
                price = "Rp 250.000",
                category = "Music",
                imageUrl = "img_jazzfest",
                organizer = "Jakarta Arts Council"
            ),
            Event(
                id = 0,
                title = "Food & Beverage Expo",
                description = "Explore culinary delights from hundreds of vendors.",
                location = "JIExpo Kemayoran",
                date = "2026-07-25",
                time = "10:00 - 21:00",
                price = "Rp 100.000",
                category = "Food",
                imageUrl = "img_fnb",
                organizer = "F&B Indonesia"
            ),
            Event(
                id = 0,
                title = "K-Pop Picnic",
                description = "K-Pop Picnic merupakan acara untuk mempertemukan para penggemar SEVENTEEN dalam suasana piknik yang santai. Peserta dapat berbagi cerita dan pengalaman, mengikuti berbagai permainan seru, serta menjalin pertemanan baru dengan sesama CARAT.",
                location = "Tebet Ecopark",
                date = "2026-08-01",
                time = "13:00 - 17:00",
                price = "Free",
                category = "Fun",
                imageUrl = "img_kpoppicnic",
                organizer = "+62 club"
            ),
            Event(
                id = 0,
                title = "Morning Yoga at GBK",
                description = "Start your day with a refreshing outdoor yoga session. Suitable for beginners and experienced participants alike.",
                location = "Gelora Bung Karno",
                date = "2026-08-10",
                time = "06:00 - 08:00",
                price = "Rp 100.000",
                category = "Health",
                imageUrl = "img_yoga",
                organizer = "Rocca Space"
            ),
            Event(
                id = 0,
                title = "Art Market",
                description = "Find unique artworks, handmade crafts, and creative gifts from local artists at this annual market.",
                location = "M Bloc Space",
                date = "2026-08-15",
                time = "09:00 - 20:00",
                price = "Free Entry",
                category = "Art",
                imageUrl = "img_artket",
                organizer = "Artket Collective"
            )
        )
    }

    fun getDummyMessages(currentUserEmail: String): List<com.example.eventifyapp.model.Message> {
        val now = System.currentTimeMillis()
        return listOf(
            com.example.eventifyapp.model.Message(
                senderName = "Jane Doe",
                senderEmail = "jane.doe@example.com",
                receiverEmail = currentUserEmail,
                message = "Hi, are you attending the Tech Conference tomorrow?",
                timestamp = now - 3600000, // 1 hour ago
                isRead = false
            ),
            com.example.eventifyapp.model.Message(
                senderName = "Mark Smith",
                senderEmail = "mark.smith@example.com",
                receiverEmail = currentUserEmail,
                message = "Hey, do you know where GBK (Gelora Bung Karno) is?",
                timestamp = now - 7200000, // 2 hours ago
                isRead = true
            ),
            com.example.eventifyapp.model.Message(
                senderName = "Alice Johnson",
                senderEmail = "alice.johnson@example.com",
                receiverEmail = currentUserEmail,
                message = "The K-Pop Picnic is going to be so much fun!",
                timestamp = now - 10800000, // 3 hours ago
                isRead = false
            )
        )
    }
}