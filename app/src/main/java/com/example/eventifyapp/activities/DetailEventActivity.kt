package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.R
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
import java.util.Locale
import java.util.Date

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
        setupToolbar()
        setupInterestedButton()
        setupJoinButton()
        setupSeeReviewsButton()
        loadEventDetails()
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
        binding.tvDetailDate.text = formatDate(date)
        binding.tvDetailPrice.text = price
        binding.tvDetailDesc.text = description
        binding.tvDetailFullAddress.text = "Alamat lengkap: $location"
        
        try {
            val participantView = binding::class.java.getMethod("getTvParticipantCount").invoke(binding) as? android.widget.TextView
            participantView?.text = "180 Participants"
        } catch (e: Exception) {
            // Ignore
        }

        setEventImage(imageUrl)
    }

    private fun loadEventDetails() {
        lifecycleScope.launch {
            if (eventId == -1L) return@launch
            val event = eventViewModel.getEventById(eventId)
            if (event != null) {
                // Fallback: If not opened via MainActivity, fill layout with DB values
                if (!intent.hasExtra("EVENT_TITLE")) {
                    eventTitle = event.title
                    binding.tvDetailTitle.text = event.title
                    binding.tvDetailLocation.text = event.location
                    binding.tvDetailDate.text = formatDate(event.date)
                    binding.tvDetailPrice.text = event.price
                    binding.tvDetailDesc.text = event.description
                    binding.tvDetailFullAddress.text = "Alamat lengkap: ${event.location}"
                    setEventImage(event.imageUrl)
                }

                // Update button states
                updateButtonStates(event.isFavorite, event.isJoined)
            }
        }
    }

    private fun updateButtonStates(isFavorite: Boolean, isJoined: Boolean) {
        // Interested / Favorite Button
        if (isFavorite) {
            binding.btnInterested.setBackgroundColor(getColor(R.color.colorOrange))
            binding.btnInterested.setTextColor(getColor(R.color.white))
        } else {
            binding.btnInterested.setBackgroundColor(getColor(R.color.colorOrangeSoft))
            binding.btnInterested.setTextColor(getColor(R.color.colorOrange))
        }

        // Join Button
        if (isJoined) {
            binding.btnJoin.text = "Joined"
            binding.btnJoin.setBackgroundColor(getColor(R.color.colorTextSecondary))
        } else {
            binding.btnJoin.text = "Join Now"
            binding.btnJoin.setBackgroundColor(getColor(R.color.colorNavyDark))
        }
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
            toggleInterestedState()
        }
    }

    private fun toggleInterestedState() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId) ?: return@launch
            val nextStatus = !event.isFavorite
            
            eventViewModel.toggleFavorite(event.id, event.isFavorite)
            
            // Create notification if favorited
            if (nextStatus) {
                notificationViewModel.addNotification(
                    NotificationItem(
                        title = "Event Terfavorit",
                        message = "Kamu menyukai event: ${event.title}",
                        type = "event",
                        eventId = event.id
                    )
                )
                Toast.makeText(this@DetailEventActivity, "Ditambahkan ke Saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@DetailEventActivity, "Dihapus dari Saved", Toast.LENGTH_SHORT).show()
            }
            
            loadEventDetails()
        }
    }

    private fun setupJoinButton() {
        binding.btnJoin.setOnClickListener {
            if (eventId == -1L) {
                Toast.makeText(this, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            toggleJoinState()
        }
    }

    private fun toggleJoinState() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId) ?: return@launch
            val nextStatus = !event.isJoined
            
            eventViewModel.toggleJoin(event.id, event.isJoined)
            
            // Create notification if joined
            if (nextStatus) {
                notificationViewModel.addNotification(
                    NotificationItem(
                        title = "Bergabung ke Event",
                        message = "Kamu telah mendaftar ke event: ${event.title}",
                        type = "event",
                        eventId = event.id
                    )
                )
                Toast.makeText(this@DetailEventActivity, "Berhasil bergabung ke event!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@DetailEventActivity, "Batal bergabung dari event", Toast.LENGTH_SHORT).show()
            }
            
            loadEventDetails()
        }
    }

    private fun setupSeeReviewsButton() {
        binding.tvSeeReviews.setOnClickListener {
            val intent = Intent(this, ReviewsActivity::class.java).apply {
                putExtra("EVENT_ID", eventId)
                putExtra("EVENT_TITLE", eventTitle)
            }
            startActivity(intent)
        }
    }
}
