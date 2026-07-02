# EventifyApp 🎉

Eventify adalah aplikasi papan pengumuman digital yang mengumpulkan informasi kegiatan komunitas, hobi, pengembangan diri, dan volunteer dalam satu platform terpusat — mudah ditemukan, terorganisir, dan sesuai lokasi serta minat pengguna.

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Database:** Room (SQLite)
- **Architecture:** MVVM (ViewModel + LiveData)
- **UI:** RecyclerView, CardView, Fragment, Material Design
- **Async:** Coroutines
- **Other:** Broadcast Receiver, Notification Manager

---

## 📁 Struktur Project

```
EventifyApp/
└── app/src/main/
    ├── java/com/example/eventifyapp/
    │   ├── activities/
    │   │   ├── CategoryActivity.kt
    │   │   ├── ChatDetailActivity.kt
    │   │   ├── DetailEventActivity.kt
    │   │   ├── LoginActivity.kt
    │   │   ├── MainActivity.kt
    │   │   ├── MessagesActivity.kt
    │   │   ├── NotificationActivity.kt
    │   │   ├── ProfileActivity.kt
    │   │   ├── ReviewsActivity.kt
    │   │   ├── SignUpActivity.kt
    │   │   └── SplashActivity.kt
    │   ├── adapters/
    │   │   ├── ChatAdapter.kt
    │   │   ├── EventAdapter.kt
    │   │   ├── MessageAdapter.kt
    │   │   ├── NotificationAdapter.kt
    │   │   └── ReviewAdapter.kt
    │   ├── dao/
    │   │   ├── EventDao.kt
    │   │   ├── MessageDao.kt
    │   │   ├── NotificationDao.kt
    │   │   ├── ReviewDao.kt
    │   │   └── UserDao.kt
    │   ├── database/
    │   │   ├── AppDatabase.kt
    │   │   ├── Converters.kt
    │   │   └── DataSeeder.kt
    │   ├── fragments/
    │   │   └── DialogEditProfileFragment.kt
    │   ├── model/
    │   │   ├── Event.kt
    │   │   ├── Message.kt
    │   │   ├── NotificationItem.kt
    │   │   ├── Review.kt
    │   │   └── User.kt
    │   ├── repository/
    │   │   ├── EventRepository.kt
    │   │   ├── MessageRepository.kt
    │   │   ├── NotificationRepository.kt
    │   │   ├── ReviewRepository.kt
    │   │   └── UserRepository.kt
    │   ├── utils/
    │   │   ├── NotificationHelper.kt
    │   │   └── SessionManager.kt
    │   └── viewmodel/
    │       ├── EventViewModel.kt
    │       ├── MessageViewModel.kt
    │       ├── NotificationViewModel.kt
    │       ├── ReviewViewModel.kt
    │       ├── UserViewModel.kt
    │       └── ViewModelFactory.kt
    └── res/
        ├── drawable/       ← custom avatars, default icons, background gradients, and vector graphics
        ├── layout/         ← XML layouts for activities, adapters list items, popups, and dialogs
        ├── menu/           ← navbar bottom menu items
        └── values/         ← theme styles, custom colors, layout attributes, and text string resources
```

---

## 🎨 Warna Utama

| Nama | Hex |
|------|-----|
| Navy Dark | `#1A2340` |
| Orange | `#FF6B35` |
| Background | `#F5F6FA` |
| Surface | `#FFFFFF` |
| Text Primary | `#1A2340` |
| Text Secondary | `#7B8194` |

---

## 🚀 Cara Clone & Setup

```bash
# 1. Clone repo
git clone https://github.com/belvnau/EventifyApp.git

# 2. Buat branch sesuai bagian
git checkout -b feat/nama-bagianmu

# 3. Sebelum mulai kerja, selalu pull dulu
git pull

# 4. Setelah selesai coding, push
git add .
git commit -m "feat: deskripsi singkat yang kamu kerjain"
git push origin feat/nama-bagianmu
```

---

## 📋 Alur Aplikasi

```
Splash → Login / Sign Up → Home (RecyclerView Event)
                                    ↓
                            Detail Event → Interested → Notifikasi
                                    ↓
                              Reviews (Add Review)
                            
Home → Messages → Chat Detail (bubble chat)
Home → Notification (Accept / Reject / Hapus)
Home → Profile (Edit Profil)
```

---

## 📋 CRUD Coverage

| Fitur | Create | Read | Update | Delete |
|-------|--------|------|--------|--------|
| Event | seed data | Homepage | - | - |
| Chat | kirim pesan | ChatDetail | - | - |
| Review | Add Review | ReviewsActivity | - | - |
| Notification | auto saat Interested | NotificationActivity | Accept/Reject | hapus notif |
| User/Profile | - | ProfileActivity | Edit Profile | - |

---

## ⚙️ Persyaratan

- Android Studio Hedgehog atau lebih baru
- Min SDK: 24
- Target SDK: 34
- Kotlin 1.9+
