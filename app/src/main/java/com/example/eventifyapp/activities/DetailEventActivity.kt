package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.ReviewAdapter
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.viewmodel.ReviewViewModel
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityDetailEventBinding
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.model.Message
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.repository.MessageRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.utils.NotificationHelper
import com.example.eventifyapp.viewmodel.MessageViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var reviewViewModel: ReviewViewModel
    private lateinit var messageViewModel: MessageViewModel
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
        setupJoinCommunityButton()
        setupReviewPreview()
        checkCommunityButtonState()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val eventRepository = EventRepository(database.eventDao())
        val notificationRepository = NotificationRepository(database.notificationDao())
        val reviewRepository = ReviewRepository(database.reviewDao())
        val messageRepository = MessageRepository(database.messageDao())

        val factory = ViewModelFactory(
            eventRepository = eventRepository,
            notificationRepository = notificationRepository,
            reviewRepository = reviewRepository,
            messageRepository = messageRepository
        )

        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
        reviewViewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
        messageViewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
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
            binding.btnInterested.setOnClickListener {
                if (eventId == -1L) {
                    Toast.makeText(this, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                markEventAsInterested()
            }
        }
    }

    private fun markEventAsInterested() {
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)

            if (event == null) {
                Toast.makeText(this@DetailEventActivity, "Gagal memuat event", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val newFavorite = !event.isFavorite
            eventViewModel.toggleFavorite(event.id, event.isFavorite)
            
            updateInterestedButtonState(newFavorite)
            updateJoinCommunityButtonState(newFavorite)

            if (newFavorite) {
                notificationViewModel.addNotification(
                    NotificationItem(
                        title = "Interested",
                        message = "Kamu menambahkan ${event.title} ke daftar event kamu!",
                        type = "event",
                        eventId = event.id
                    )
                )

                // Kirim Android system notification
                NotificationHelper.sendInterestedNotification(
                    context = this@DetailEventActivity,
                    eventTitle = event.title
                )

                Toast.makeText(this@DetailEventActivity, "Kamu tertarik pada event ini!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@DetailEventActivity, "Batal tertarik pada event ini", Toast.LENGTH_SHORT).show()
            }
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

    private fun updateJoinCommunityButtonState(isFavorite: Boolean) {
        if (isFavorite) {
            binding.btnJoinCommunity.isEnabled = true
            binding.btnJoinCommunity.alpha = 1.0f
            binding.btnJoinCommunity.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorNavyDark)
            )
        } else {
            binding.btnJoinCommunity.isEnabled = false
            binding.btnJoinCommunity.alpha = 0.5f
            binding.btnJoinCommunity.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.gray_text)
            )
        }
    }

    private fun checkCommunityButtonState() {
        if (eventId != -1L) {
            lifecycleScope.launch {
                val event = eventViewModel.getEventById(eventId)
                if (event != null) {
                    updateInterestedButtonState(event.isFavorite)
                    updateJoinCommunityButtonState(event.isFavorite)
                }
            }
        }
    }

    private fun setupJoinCommunityButton() {
        binding.btnJoinCommunity.setOnClickListener {
            showSuccessJoinDialog()
        }
    }

    private fun showSuccessJoinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success_join, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnStartChatting = dialogView.findViewById<android.widget.Button>(R.id.btnStartChatting)
        val btnCancelJoin = dialogView.findViewById<android.widget.TextView>(R.id.btnCancelJoin)
        val tvDialogMessage = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogMessage)

        tvDialogMessage.text = "Kamu sudah terhubung ke grup komunitas untuk event \"$eventTitle\". Yuk mulai ngobrol dengan sesama peserta!"

        btnStartChatting.setOnClickListener {
            dialog.dismiss()
            joinCommunityAndNavigate()
        }

        btnCancelJoin.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun joinCommunityAndNavigate() {
        val communityEmail = "community_event_$eventId"
        lifecycleScope.launch {
            val event = eventViewModel.getEventById(eventId)
            val organizerName = event?.organizer ?: "Organizer"
            val database = AppDatabase.getDatabase(applicationContext)

            // Periksa apakah pesan komunitas sudah ada
            val existingMessages = database.messageDao().getChatMessages(communityEmail, "").first()
            if (existingMessages.isEmpty()) {
                val welcomeMessage = Message(
                    senderName = organizerName,
                    senderEmail = communityEmail,
                    receiverEmail = "",
                    message = "Halo teman-teman! Selamat bergabung di grup komunitas $eventTitle. Silakan gunakan grup ini untuk berdiskusi.",
                    timestamp = System.currentTimeMillis() - 1000,
                    isRead = true,
                    isCommunity = true,
                    eventId = eventId,
                    groupTitle = "$eventTitle Community"
                )
                database.messageDao().insertMessage(welcomeMessage)
            }

            // Arahkan ke ChatDetailActivity
            val intent = Intent(this@DetailEventActivity, ChatDetailActivity::class.java).apply {
                putExtra("SENDER_EMAIL", communityEmail)
                putExtra("SENDER_NAME", "$eventTitle Community")
                putExtra("IS_COMMUNITY", true)
                putExtra("EVENT_ID", eventId)
                putExtra("FROM_DETAIL_EVENT", true)
            }
            startActivity(intent)
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
        reviewPreviewAdapter = ReviewAdapter(reviews = emptyList()) { review ->
            reviewViewModel.toggleLikeReview(review)
        }
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
