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
                organizer = "Tech Indonesia",
                registrationUrl = "https://www.google.com"
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
                organizer = "Jakarta Arts Council",
                registrationUrl = "https://www.google.com"
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
                organizer = "F&B Indonesia",
                registrationUrl = "https://www.google.com"
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
                organizer = "+62 club",
                registrationUrl = "https://forms.gle"
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
                organizer = "Rocca Space",
                registrationUrl = "https://www.google.com"
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
                organizer = "Artket Collective",
                registrationUrl = "https://www.google.com"
            )
        )
    }
}