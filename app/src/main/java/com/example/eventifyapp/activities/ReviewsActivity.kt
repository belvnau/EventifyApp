package com.example.eventifyapp.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.ReviewAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityReviewsBinding
import com.example.eventifyapp.model.Review
import com.example.eventifyapp.repository.ReviewRepository
import com.example.eventifyapp.utils.SessionManager
import com.example.eventifyapp.viewmodel.ReviewViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
        setupToolbar()
        observeReviews()

        if (eventId != -1L) {
            viewModel.loadReviews(eventId)
            viewModel.loadAverageRating(eventId)
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
                binding.tvTotalReviews.text = "${reviews.size} Reviews"
            }
        }
        lifecycleScope.launch {
            viewModel.averageRating.collect { avg ->
                binding.tvAverageRating.text = String.format(Locale.US, "%.1f", avg)
                binding.rbAverageStars.rating = avg
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

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showAddReviewDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_review, null)
        val rbRating = dialogView.findViewById<RatingBar>(R.id.rbDialogRating)
        val etComment = dialogView.findViewById<EditText>(R.id.etReviewComment)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Kirim") { _, _ ->
                val comment = etComment.text.toString().trim()
                val rating = rbRating.rating

                if (comment.isEmpty()) {
                    Toast.makeText(this, "Komentar wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val sessionManager = SessionManager(this@ReviewsActivity)
                    val email = sessionManager.getLoggedInEmail()
                    val database = AppDatabase.getDatabase(applicationContext)
                    
                    val userName = if (email != null) {
                        val user = withContext(Dispatchers.IO) {
                            database.userDao().getUserByEmail(email)
                        }
                        user?.name ?: user?.username ?: "User"
                    } else {
                        "User"
                    }

                    viewModel.addReview(
                        Review(
                            eventId = eventId,
                            reviewerName = userName,
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
}
