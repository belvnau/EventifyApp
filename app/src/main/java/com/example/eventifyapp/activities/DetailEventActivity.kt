package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.R
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityDetailEventBinding
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ReviewViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var reviewViewModel: ReviewViewModel

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
        observeReviews()

        if (eventId != -1L) {
            reviewViewModel.loadReviews(eventId)
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val eventRepository = EventRepository(database.eventDao())
        val notificationRepository = NotificationRepository(database.notificationDao())
        val reviewRepository = ReviewRepository(database.reviewDao())

        val factory = ViewModelFactory(
            eventRepository = eventRepository,
            notificationRepository = notificationRepository,
            reviewRepository = reviewRepository
        )

        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
        reviewViewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
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
        
        try {
            val participantView = binding::class.java.getMethod("getTvParticipantCount").invoke(binding) as? android.widget.TextView
            participantView?.text = "180 Participants"
        } catch (e: Exception) {
            // Abaikan jika tidak ada
        }

        setEventImage(imageUrl)
    }

    private fun setupToolbar() {
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
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            lifecycleScope.launch {
                val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val url = java.net.URL(imageUrl)
                        val connection = url.openConnection()
                        connection.doInput = true
                        connection.connect()
                        val input = connection.getInputStream()
                        android.graphics.BitmapFactory.decodeStream(input)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    binding.ivDetailImage.setImageBitmap(bitmap)
                } else {
                    val defaultResId = resources.getIdentifier("img_artket", "drawable", packageName)
                    binding.ivDetailImage.setImageResource(defaultResId)
                }
            }
        } else {
            val resourceId = resources.getIdentifier(imageUrl, "drawable", packageName)
            if (resourceId != 0) {
                binding.ivDetailImage.setImageResource(resourceId)
            } else {
                val defaultResId = resources.getIdentifier("img_artket", "drawable", packageName)
                binding.ivDetailImage.setImageResource(defaultResId)
            }
        }
    }

    private fun setupInterestedButton() {
        if (eventId != -1L) {
            lifecycleScope.launch {
                val event = eventViewModel.getEventById(eventId)
                if (event != null) {
                    updateInterestedButtonState(event.isFavorite)
                }
            }
        }

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

            val newStatus = !event.isFavorite
            eventViewModel.toggleFavorite(event.id, event.isFavorite)
            updateInterestedButtonState(newStatus)

            val message = if (newStatus) {
                notificationViewModel.addNotification(
                    NotificationItem(
                        title = "Interested",
                        message = "Kamu tertarik pada event: ${event.title}!",
                        type = "event",
                        eventId = event.id
                    )
                )
                "Kamu tertarik pada event ini!"
            } else {
                "Dihapus dari daftar disimpan"
            }

            Toast.makeText(
                this@DetailEventActivity,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateInterestedButtonState(isFavorite: Boolean) {
        if (isFavorite) {
            binding.btnInterested.text = "Saved"
            binding.btnInterested.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnInterested.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorOrange)
            )
        } else {
            binding.btnInterested.text = "Interested"
            binding.btnInterested.setTextColor(ContextCompat.getColor(this, R.color.colorOrange))
            binding.btnInterested.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorOrangeSoft)
            )
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

    private fun observeReviews() {
        lifecycleScope.launch {
            reviewViewModel.reviews.collect { reviews ->
                binding.llReviewsContainer.removeAllViews()
                val displayReviews = reviews.take(2)
                for (review in displayReviews) {
                    val cardView = LayoutInflater.from(this@DetailEventActivity)
                        .inflate(R.layout.item_detail_review, binding.llReviewsContainer, false)
                    
                    val tvName = cardView.findViewById<TextView>(R.id.tvReviewerName)
                    val tvRating = cardView.findViewById<TextView>(R.id.tvReviewRating)
                    val tvDate = cardView.findViewById<TextView>(R.id.tvReviewDate)
                    
                    tvName.text = review.reviewerName
                    
                    val ratingStr = String.format(Locale.US, "%.1f", review.rating)
                    val stars = "★".repeat(review.rating.toInt())
                    tvRating.text = "$ratingStr $stars"
                    
                    val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
                    tvDate.text = sdf.format(Date(review.timestamp))
                    
                    binding.llReviewsContainer.addView(cardView)
                }
            }
        }
    }
}
