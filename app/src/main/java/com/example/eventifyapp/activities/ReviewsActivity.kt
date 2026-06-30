package com.example.eventifyapp.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.ReviewAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityReviewsBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.model.Review
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.viewmodel.ReviewViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewsBinding
    private lateinit var viewModel: ReviewViewModel
    private lateinit var reviewAdapter: ReviewAdapter
    private var eventId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventId = intent.getLongExtra("EVENT_ID", -1)

        setupViewModel()
        setupRecyclerView()
        setupAddReviewButton()
        setupBottomNavigation()
        observeReviews()

        if (eventId != -1L) {
            viewModel.loadReviews(eventId)
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ReviewRepository(database.reviewDao())
        val factory = ViewModelFactory(reviewRepository = repository)
        viewModel = ViewModelProvider(this, factory)[ReviewViewModel::class.java]
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(emptyList())
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(this@ReviewsActivity)
            adapter = reviewAdapter
        }
    }

    private fun observeReviews() {
        lifecycleScope.launch {
            viewModel.reviews.collect { reviews ->
                reviewAdapter.updateData(reviews)
            }
        }
    }

    private fun setupAddReviewButton() {
        binding.btnAddReview.setOnClickListener {
            if (eventId == -1L) {
                Toast.makeText(this, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showAddReviewDialog()
        }
    }

    private fun showAddReviewDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null)
        val etName = dialogView.findViewById<EditText>(R.id.etReviewerName)
        val rbRating = dialogView.findViewById<RatingBar>(R.id.rbDialogRating)
        val etComment = dialogView.findViewById<EditText>(R.id.etReviewComment)

        AlertDialog.Builder(this)
            .setTitle("Tambah Review")
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val name = etName.text.toString().trim()
                val comment = etComment.text.toString().trim()
                val rating = rbRating.rating

                if (name.isEmpty() || comment.isEmpty()) {
                    Toast.makeText(this, "Nama dan komentar wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    viewModel.addReview(
                        Review(
                            eventId = eventId,
                            reviewerName = name,
                            rating = rating,
                            comment = comment
                        )
                    )
                    Toast.makeText(this@ReviewsActivity, "Review ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)
        navbarBinding.navChat.setOnClickListener {
            startActivity(android.content.Intent(this, MessagesActivity::class.java))
        }
        navbarBinding.navNotification.setOnClickListener {
            startActivity(android.content.Intent(this, NotificationActivity::class.java))
        }
        navbarBinding.navProfile.setOnClickListener {
            startActivity(android.content.Intent(this, ProfileActivity::class.java))
        }
        navbarBinding.navHome.setOnClickListener {
            startActivity(android.content.Intent(this, MainActivity::class.java))
        }
    }
}