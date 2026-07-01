package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.eventifyapp.databinding.ItemEventCardBinding
import com.example.eventifyapp.databinding.ItemEventGridBinding
import com.example.eventifyapp.model.Event
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(
    private var events: List<Event> = emptyList(),
    private var isGridView: Boolean = false,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
    }

    class EventViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = if (viewType == VIEW_TYPE_GRID) {
            ItemEventGridBinding.inflate(inflater, parent, false)
        } else {
            ItemEventCardBinding.inflate(inflater, parent, false)
        }
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val context = holder.itemView.context

        if (holder.binding is ItemEventCardBinding) {
            val b = holder.binding
            b.tvEventTitle.text = event.title
            val formattedDate = formatDate(event.date)
            b.tvEventDetails.text = "$formattedDate • ${event.location}"
            
            // Reverting to dummy since participantCount was removed from model
            b.tvEventMembers.text = "180 Members joined"
            
            loadImage(event.imageUrl, b.ivEventImage, context)
            
            b.btnJoinNow.setOnClickListener {
                // Handle Join (Dummy)
            }
        } else if (holder.binding is ItemEventGridBinding) {
            val b = holder.binding
            b.tvEventTitle.text = event.title
            loadImage(event.imageUrl, b.ivEventImage, context)
        }

        holder.itemView.setOnClickListener {
            onItemClick(event)
        }
    }

    private fun loadImage(imageUrl: String, imageView: android.widget.ImageView, context: android.content.Context) {
        val resourceId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
        if (resourceId != 0) {
            imageView.setImageResource(resourceId)
        } else {
            val defaultResId = context.resources.getIdentifier("img_artket", "drawable", context.packageName)
            imageView.setImageResource(defaultResId)
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

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    fun setLayoutMode(isGrid: Boolean) {
        this.isGridView = isGrid
        notifyDataSetChanged()
    }
}
