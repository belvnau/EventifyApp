package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.adapters.ReviewAdapter
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.viewmodel.ReviewViewModel
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityDetailEventBinding
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var reviewPreviewAdapter: ReviewAdapter

    private var eventId: Long = -1
    private var eventTitle: String = ""
    private var registrationUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        getDataFromIntent()
        bindDataToViews()
        setupToolbar()
        setupInterestedButton()
        setupSeeReviewsButton()
        setupJoinButton()
        setupReviewPreview()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val eventRepository = EventRepository(database.eventDao())
        val notificationRepository = NotificationRepository(database.notificationDao())
        val reviewRepository = ReviewRepository(database.reviewDao())  // baru

        val factory = ViewModelFactory(
            eventRepository = eventRepository,
            notificationRepository = notificationRepository,
            reviewRepository = reviewRepository  // baru
        )

        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
        reviewViewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]  // baru
    }

    private fun getDataFromIntent() {
        eventId = intent.getLongExtra("EVENT_ID", -1)
        eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Detail Event"
        registrationUrl = intent.getStringExtra("EVENT_REGISTRATION_URL") ?: ""
    }

    private fun bindDataToViews() {
        val date = intent.getStringExtra("EVENT_DATE") ?: "-"
        val location = intent.getStringExtra("EVENT_LOCATION") ?: "-"
        val price = intent.getStringExtra("EVENT_PRICE") ?: "-"
        val description = intent.getStringExtra("EVENT_DESCRIPTION") ?: "-"
        val imageUrl = intent.getStringExtra("EVENT_IMAGE") ?: ""

        binding.tvDetailTitle.text = eventTitle
        binding.tvDetailLocation.text = location
        binding.tvDetailDate.text = formatDate(date)
        binding.tvDetailPrice.text = price
        binding.tvDetailDesc.text = description
        binding.tvDetailFullAddress.text = "Alamat lengkap: $location"
        
        // Cek apakah tvParticipantCount ada di binding sebelum set text
        // (ID ini ada di layout versi modern yang kita buat)
        try {
            val participantView = binding::class.java.getMethod("getTvParticipantCount").invoke(binding) as? android.widget.TextView
            participantView?.text = "180 Participants"
        } catch (e: Exception) {
            // Abaikan jika tidak ada
        }

        setEventImage(imageUrl)
    }

    private fun setupToolbar() {
        // Tombol back di toolbar agar benar-benar kembali (finish)
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            val date = parser.parse(dateString)
            if (date != null) formatter.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun setEventImage(imageUrl: String) {
        val resourceId = resources.getIdentifier(imageUrl, "drawable", packageName)
        if (resourceId != 0) {
            binding.ivDetailImage.setImageResource(resourceId)
        } else {
            val defaultResId = resources.getIdentifier("img_artket", "drawable", packageName)
            binding.ivDetailImage.setImageResource(defaultResId)
        }
    }

    private fun setupInterestedButton() {
        binding.btnInterested.setOnClickListener {
            if (eventId == -1L) {
                Toast.makeText(this, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            markEventAsInterested()
        }
    }

    private fun markEventAsInterested() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)

            if (event == null) {
                Toast.makeText(this@DetailEventActivity, "Gagal memuat event", Toast.LENGTH_SHORT).show()
                return@launch
            }

            eventViewModel.toggleFavorite(event.id, event.isFavorite)

            notificationViewModel.addNotification(
                NotificationItem(
                    title = "Interested",
                    message = "Kamu tertarik pada event ini!",
                    type = "event",
                    eventId = event.id
                )
            )

            Toast.makeText(
                this@DetailEventActivity,
                "Kamu tertarik pada event ini!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupSeeReviewsButton() {
        binding.tvSeeReviews.setOnClickListener {
            val intent = Intent(this, ReviewsActivity::class.java).apply {
                putExtra("EVENT_ID", eventId)
            }
            startActivity(intent)
        }
    }

    private fun setupJoinButton() {
        binding.btnJoin.setOnClickListener {
            if (registrationUrl.isNotEmpty()) {
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Pendaftaran Eksternal")
                    .setMessage("Kamu akan diarahkan ke link pendaftaran di luar aplikasi:\n$registrationUrl\n\nApakah kamu ingin melanjutkan?")
                    .setPositiveButton("Join") { dialog, _ ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(registrationUrl))
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Link tidak valid", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Toast.makeText(this, "Link pendaftaran belum tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupReviewPreview() {
        reviewPreviewAdapter = ReviewAdapter(reviews = emptyList())
        binding.rvReviewPreview.apply {
            layoutManager = LinearLayoutManager(
                this@DetailEventActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = reviewPreviewAdapter
        }


        if (eventId != -1L) {
            reviewViewModel.loadReviews(eventId)
            lifecycleScope.launch {
                reviewViewModel.reviews.collect { reviews ->
                    reviewPreviewAdapter.updateData(newReviews = reviews.take(n = 3))
                }
            }
        }
    }
}
