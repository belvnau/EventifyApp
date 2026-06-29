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
    │   │   ├── SplashActivity.kt
    │   │   ├── LoginActivity.kt
    │   │   ├── SignUpActivity.kt
    │   │   ├── MainActivity.kt
    │   │   ├── DetailEventActivity.kt
    │   │   ├── MessagesActivity.kt
    │   │   ├── ChatDetailActivity.kt
    │   │   ├── ReviewsActivity.kt
    │   │   ├── ProfileActivity.kt
    │   │   └── NotificationActivity.kt
    │   ├── adapters/
    │   │   ├── EventAdapter.kt
    │   │   ├── MessageAdapter.kt
    │   │   ├── ChatAdapter.kt
    │   │   ├── ReviewAdapter.kt
    │   │   └── NotificationAdapter.kt
    │   ├── model/
    │   │   ├── Event.kt
    │   │   ├── Message.kt
    │   │   ├── Chat.kt
    │   │   ├── Review.kt
    │   │   ├── NotificationItem.kt
    │   │   └── User.kt
    │   ├── dao/
    │   │   ├── EventDao.kt
    │   │   ├── MessageDao.kt
    │   │   ├── ChatDao.kt
    │   │   ├── ReviewDao.kt
    │   │   ├── NotificationDao.kt
    │   │   └── UserDao.kt
    │   ├── database/
    │   │   └── AppDatabase.kt
    │   ├── repository/
    │   │   ├── EventRepository.kt
    │   │   ├── MessageRepository.kt
    │   │   ├── ChatRepository.kt
    │   │   ├── ReviewRepository.kt
    │   │   ├── NotificationRepository.kt
    │   │   └── UserRepository.kt
    │   ├── viewmodel/
    │   │   ├── EventViewModel.kt
    │   │   ├── ChatViewModel.kt
    │   │   └── NotificationViewModel.kt
    │   ├── fragments/
    │   │   └── HomeFragment.kt
    │   └── receiver/
    │       └── EventBroadcastReceiver.kt
    └── res/
        ├── layout/         ← semua layout XML (activity & item)
        ├── drawable/       ← icon, background, gambar
        ├── menu/           ← bottom nav menu
        └── values/         ← colors, strings, themes
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