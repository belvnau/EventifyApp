package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.R
import com.example.eventifyapp.databinding.ItemReviewBinding
import com.example.eventifyapp.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter(
    private var reviews: List<Review> = emptyList(),
    private val onLikeClicked: ((Review) -> Unit)? = null
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]

        holder.binding.tvReviewerName.text = review.reviewerName
        holder.binding.tvReviewComment.text = review.comment
        holder.binding.rbReviewRating.rating = review.rating
        holder.binding.tvRatingText.text = String.format(Locale.US, "%.1f rating", review.rating)

        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
        holder.binding.tvReviewDate.text = sdf.format(Date(review.timestamp))

        // Like UI
        // Like UI
        holder.binding.tvLikeCount.text = "${review.likeCount} Likes"
        if (review.isLikedByUser) {
            holder.binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_filled)
            holder.binding.ivLikeIcon.clearColorFilter() // hapus color filter lama
        } else {
            holder.binding.ivLikeIcon.setImageResource(R.drawable.ic_heart_outline)
            holder.binding.ivLikeIcon.clearColorFilter()
        }

        holder.binding.llLikeContainer.setOnClickListener {
            onLikeClicked?.invoke(review)
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}