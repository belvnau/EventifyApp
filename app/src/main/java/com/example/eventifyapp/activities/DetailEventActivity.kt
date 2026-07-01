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
import java.text.SimpleDateFormat
import java.util.Locale

class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding

    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel

    private var eventId: Long = -1
    private var eventTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        getIntentData()
        bindData()
        setupToolbar()
        setupInterestedButton()
        setupReviewButton()
    }

    private fun setupViewModel() {

        val database = AppDatabase.getDatabase(applicationContext)

        val eventRepository =
            EventRepository(database.eventDao())

        val notificationRepository =
            NotificationRepository(database.notificationDao())

        val factory = ViewModelFactory(
            eventRepository = eventRepository,
            notificationRepository = notificationRepository
        )

        eventViewModel =
            ViewModelProvider(this, factory)[EventViewModel::class.java]

        notificationViewModel =
            ViewModelProvider(this, factory)[NotificationViewModel::class.java]

    }

    private fun getIntentData() {

        eventId =
            intent.getLongExtra("EVENT_ID", -1)

        eventTitle =
            intent.getStringExtra("EVENT_TITLE") ?: ""

    }

    private fun bindData() {

        binding.tvDetailTitle.text = eventTitle

        binding.tvDetailLocation.text =
            intent.getStringExtra("EVENT_LOCATION")

        binding.tvDetailPrice.text =
            intent.getStringExtra("EVENT_PRICE")

        binding.tvDetailDesc.text =
            intent.getStringExtra("EVENT_DESCRIPTION")

        binding.tvDetailDate.text =
            formatDate(
                intent.getStringExtra("EVENT_DATE") ?: ""
            )

    }

    private fun setupToolbar() {

        binding.toolbarDetail.setNavigationOnClickListener {

            finish()

        }

    }

    private fun formatDate(date: String): String {

        return try {

            val parser =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val formatter =
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            formatter.format(parser.parse(date)!!)

        } catch (e: Exception) {

            date

        }

    }

    private fun setupInterestedButton() {

        binding.btnInterested.setOnClickListener {

            lifecycleScope.launch {

                val event =
                    eventViewModel.getEventById(eventId)

                if (event != null) {

                    eventViewModel.toggleFavorite(
                        event.id,
                        event.isFavorite
                    )

                    notificationViewModel.addNotification(

                        NotificationItem(

                            title = "Event Tertarik",

                            message = "Kamu tertarik pada event ${event.title}",

                            type = "event",

                            eventId = event.id

                        )

                    )

                    Toast.makeText(
                        this@DetailEventActivity,
                        "Berhasil ditambahkan ke notifikasi",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

    }

    private fun setupReviewButton() {

        binding.tvSeeReviews.setOnClickListener {

            val intent =
                Intent(this, ReviewsActivity::class.java)

            intent.putExtra("EVENT_ID", eventId)

            startActivity(intent)

        }

    }

}