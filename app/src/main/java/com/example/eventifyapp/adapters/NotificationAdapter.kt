package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.databinding.ItemNotificationBinding
import com.example.eventifyapp.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<NotificationItem> = emptyList(),
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.tvNotificationTitle.text = notification.title
        holder.binding.tvNotificationMessage.text = notification.message
        
        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.binding.tvNotificationTime.text = sdf.format(Date(notification.timestamp))

        // Set unread dot visibility
        if (notification.isRead) {
            holder.binding.viewNotifUnreadDot.visibility = View.GONE
        } else {
            holder.binding.viewNotifUnreadDot.visibility = View.VISIBLE
        }

        // Optional: change icon background based on notification type
        val context = holder.itemView.context
        val orangeSoft = context.getColor(context.resources.getIdentifier("colorOrangeSoft", "color", context.packageName))
        val primaryBlueSoft = context.getColor(context.resources.getIdentifier("colorBackground", "color", context.packageName))
        
        when (notification.type.lowercase(Locale.getDefault())) {
            "event" -> {
                holder.binding.cvNotifIconBg.setCardBackgroundColor(orangeSoft)
                holder.binding.ivNotificationIcon.setImageResource(
                    context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                )
            }
            "message" -> {
                holder.binding.cvNotifIconBg.setCardBackgroundColor(primaryBlueSoft)
                holder.binding.ivNotificationIcon.setImageResource(
                    android.R.drawable.stat_notify_chat
                )
            }
            else -> { // system
                holder.binding.cvNotifIconBg.setCardBackgroundColor(primaryBlueSoft)
                holder.binding.ivNotificationIcon.setImageResource(
                    context.resources.getIdentifier("ic_notification", "drawable", context.packageName)
                )
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
