package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityDetailEventBinding
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel

    private var eventId: Long = -1
    private var eventTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        getDataFromIntent()
        bindDataToViews()
        setupInterestedButton()
        setupSeeReviewsButton()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val eventRepository = EventRepository(database.eventDao())
        val notificationRepository = NotificationRepository(database.notificationDao())

        val factory = ViewModelFactory(
            eventRepository = eventRepository,
            notificationRepository = notificationRepository
        )

        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun getDataFromIntent() {
        eventId = intent.getLongExtra("EVENT_ID", -1)
        eventTitle = intent.getStringExtra("EVENT_TITLE") ?: "Detail Event"
    }

    private fun bindDataToViews() {
        val date = intent.getStringExtra("EVENT_DATE") ?: "-"
        val location = intent.getStringExtra("EVENT_LOCATION") ?: "-"
        val price = intent.getStringExtra("EVENT_PRICE") ?: "-"
        val description = intent.getStringExtra("EVENT_DESCRIPTION") ?: "-"
        val imageUrl = intent.getStringExtra("EVENT_IMAGE") ?: ""

        binding.tvDetailTitle.text = eventTitle
        binding.tvDetailLocation.text = location
        binding.tvDetailDate.text = date
        binding.tvDetailPrice.text = price
        binding.tvDetailDesc.text = description
        binding.tvDetailFullAddress.text = "Alamat lengkap: $location"

        setEventImage(imageUrl)
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
}