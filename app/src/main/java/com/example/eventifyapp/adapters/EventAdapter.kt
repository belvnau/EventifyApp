package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.databinding.ItemEventCardBinding
import com.example.eventifyapp.model.Event

class EventAdapter(
    private var events: List<Event> = emptyList(),
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(val binding: ItemEventCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.binding.tvEventTitle.text = event.title
        holder.binding.tvEventDate.text = event.date
        holder.binding.tvEventLocation.text = event.location
        holder.binding.tvEventPrice.text = event.price

        val context = holder.itemView.context
        val resourceId = context.resources.getIdentifier(
            event.imageUrl,
            "drawable",
            context.packageName
        )
        if (resourceId != 0) {
            holder.binding.ivEventImage.setImageResource(resourceId)
        } else {
            // Fallback placeholder
            val defaultResId = context.resources.getIdentifier("img_artket", "drawable", context.packageName)
            if (defaultResId != 0) {
                holder.binding.ivEventImage.setImageResource(defaultResId)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(event)
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateData(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }
}
