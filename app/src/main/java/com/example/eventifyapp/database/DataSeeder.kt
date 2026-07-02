package com.example.eventifyapp.database

import com.example.eventifyapp.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DataSeeder {

    private fun getDynamicDate(offsetDays: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        return sdf.format(cal.time)
    }

    fun getDummyEvents(): List<Event> {
        return listOf(
            Event(
                id = 0,
                title = "Tech Conference 2026",
                description = "Annual technology conference featuring latest innovations in AI, ML, and Cloud Computing.",
                location = "Jakarta Convention Center",
                date = getDynamicDate(15),
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
                date = getDynamicDate(20),
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
                date = getDynamicDate(25),
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
                date = getDynamicDate(30),
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
                date = getDynamicDate(10),
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
                date = getDynamicDate(5),
                time = "09:00 - 20:00",
                price = "Free Entry",
                category = "Art",
                imageUrl = "img_artket",
                organizer = "Artket Collective",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Reading Book Together",
                description = "Mari berkumpul dan membaca buku bersama di taman kota. Bawa buku favoritmu dan nikmati sore hari yang damai dengan sesama pecinta buku.",
                location = "Taman Suropati, Jakarta",
                date = getDynamicDate(0),
                time = "15:00 - 18:00",
                price = "Free",
                category = "Hobby",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR-Hmqkn1tSAnNA-MgcH9HUi7xGepAhRSgp-0feHoxalXtBd06UH6ekjw&s=10",
                organizer = "Bookish Club",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Jakarta Midnight Concert",
                description = "Konser musik malam spektakuler menghadirkan band indie papan atas di jantung kota Jakarta.",
                location = "M Bloc Space, Jakarta",
                date = getDynamicDate(0),
                time = "20:00 - 23:30",
                price = "Rp 150.000",
                category = "Music",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSR2Xr5jCWfukEEnXV7fRsYRe-mmK5u7y4FOi5VQ_i6PBodEwZX_8CNTHs&s=10",
                organizer = "M Bloc Creative",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Jakarta Fun Marathon 5K",
                description = "Lari santai 5 kilometer menyusuri jalanan protokol Jakarta. Cocok untuk semua tingkat kebugaran, dapatkan medali finisher menarik!",
                location = "Sudirman-Thamrin, Jakarta",
                date = getDynamicDate(1),
                time = "06:00 - 09:00",
                price = "Rp 75.000",
                category = "Sport",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRaVG5EKSAz2vsSkpqJ1IO3ZSn954NnRy4vBJLA10BFoA&s=10",
                organizer = "Jakarta Runners",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "AI Workshop: Generative AI for Beginners",
                description = "Workshop interaktif mempelajari dasar-dasar Generative AI dan bagaimana memanfaatkannya untuk produktivitas kerja sehari-hari.",
                location = "Co-working Space Kuningan, Jakarta",
                date = getDynamicDate(1),
                time = "10:00 - 15:00",
                price = "Rp 100.000",
                category = "Technology",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=500",
                organizer = "AI Indonesia",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Healthy Vegan Cooking Class",
                description = "Belajar memasak makanan vegan yang lezat, bernutrisi tinggi, dan mudah disiapkan di rumah bersama Chef profesional.",
                location = "Almond Zucchini Cooking Studio, Jakarta",
                date = getDynamicDate(3),
                time = "10:00 - 13:00",
                price = "Rp 350.000",
                category = "Food",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSo7Ih3ldTrW6Kpn8InnisUBIM1WC9ZBW3Z3EoSCpPX6dvn0PSZmYeIFcU&s=10",
                organizer = "Vegan Culinary Academy",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Pottery Making Workshop",
                description = "Rasakan pengalaman membuat kerajinan keramik sendiri dengan tanah liat. Hasil karyamu dapat dibawa pulang setelah dibakar.",
                location = "Museum Seni Rupa dan Keramik, Jakarta",
                date = getDynamicDate(4),
                time = "13:00 - 16:00",
                price = "Rp 200.000",
                category = "Art",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRb-dbOdDwRvB0-PpoYh_hUBGYScs661bOmcOD6pa0d7ZEMSQk2wSX7gQ8Z&s=10",
                organizer = "Ceramics Hub",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Bouldering & Wall Climbing Session",
                description = "Sesi memanjat dinding bersama komunitas. Pelatih berpengalaman akan mendampingi dan memberikan instruksi keamanan.",
                location = "Indoclimb FX Sudirman, Jakarta",
                date = getDynamicDate(6),
                time = "16:00 - 19:00",
                price = "Rp 120.000",
                category = "Sport",
                imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=500",
                organizer = "Climbers Community",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Creative Writing & Poetry Night",
                description = "Malam apresiasi sastra. Tulis dan bacakan puisi atau cerita pendek karyamu di hadapan komunitas pecinta sastra.",
                location = "Perpustakaan Jakarta, Cikini",
                date = getDynamicDate(7),
                time = "18:30 - 21:00",
                price = "Free Entry",
                category = "Hobby",
                imageUrl = "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=500",
                organizer = "Jakarta Writing Club",
                registrationUrl = "https://www.google.com"
            ),
            Event(
                id = 0,
                title = "Virtual Reality Gaming Gathering",
                description = "Kumpul bersama penggemar VR gaming. Coba berbagai game VR terbaru dan ikuti turnamen mini berhadiah menarik.",
                location = "VR Zone Grand Indonesia, Jakarta",
                date = getDynamicDate(8),
                time = "14:00 - 17:00",
                price = "Rp 80.000",
                category = "Fun",
                imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSuSF11bB_QlmCBVkyxBFBfSufEt-fTtZsJALBRCuvmcQNqdMW3M5OJgeIW&s=10",
                organizer = "VR Gamers Indonesia",
                registrationUrl = "https://www.google.com"
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