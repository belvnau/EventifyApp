package com.example.eventifyapp.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.R
import com.example.eventifyapp.databinding.ItemNotificationBinding
import com.example.eventifyapp.databinding.ItemNotificationHeaderBinding
import com.example.eventifyapp.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

sealed class NotificationListItem {
    data class Header(val title: String) : NotificationListItem()
    data class Content(val notification: NotificationItem) : NotificationListItem()
}

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit,
    private val onDeleteClick: (NotificationItem) -> Unit,
    private val onAcceptInvite: (NotificationItem) -> Unit = {},
    private val onRejectInvite: (NotificationItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
    }

    private var itemsList: List<NotificationListItem> = emptyList()

    override fun getItemViewType(position: Int): Int {
        return when (itemsList[position]) {
            is NotificationListItem.Header -> VIEW_TYPE_HEADER
            is NotificationListItem.Content -> VIEW_TYPE_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val binding = ItemNotificationHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ContentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemsList[position]
        if (holder is HeaderViewHolder && item is NotificationListItem.Header) {
            holder.binding.tvHeaderTitle.text = item.title
        } else if (holder is ContentViewHolder && item is NotificationListItem.Content) {
            val notification = item.notification
            val context = holder.itemView.context
            
            // Format HTML to display: [Title/SenderName] in bold + [Message] in normal text
            val formattedMsg = "<b>${notification.title}</b> ${notification.message}"
            holder.binding.tvNotificationMessage.text = Html.fromHtml(formattedMsg, Html.FROM_HTML_MODE_LEGACY)
            
            // Format time (e.g. Just now, 1 hr ago, or formatted date)
            holder.binding.tvNotificationTime.text = getRelativeTime(notification.timestamp)

            // Toggle unread indicator dot
            if (notification.isRead) {
                holder.binding.viewNotifUnreadDot.visibility = View.GONE
            } else {
                holder.binding.viewNotifUnreadDot.visibility = View.VISIBLE
            }

            // Set Avatar based on notification type
            val avatarResName = when (notification.type.lowercase(Locale.getDefault())) {
                "event" -> "img_artket"
                "message" -> "img_avatar_default"
                else -> "img_avatar_default"
            }
            val avatarResId = context.resources.getIdentifier(avatarResName, "drawable", context.packageName)
            if (avatarResId != 0) {
                holder.binding.ivNotificationAvatar.setImageResource(avatarResId)
            }

            // Bind invitation buttons (e.g., if title/message contains "invite" or type is "invite")
            val isInvite = notification.type.lowercase(Locale.getDefault()) == "invite" || 
                           notification.message.lowercase(Locale.getDefault()).contains("invite")
            if (isInvite) {
                holder.binding.layoutActionButtons.visibility = View.VISIBLE
                holder.binding.btnAccept.setOnClickListener {
                    onAcceptInvite(notification)
                }
                holder.binding.btnReject.setOnClickListener {
                    onRejectInvite(notification)
                }
            } else {
                holder.binding.layoutActionButtons.visibility = View.GONE
            }

            // Single click to open detail and mark as read
            holder.itemView.setOnClickListener {
                onItemClick(notification)
            }

            // Long click to trigger deletion
            holder.itemView.setOnLongClickListener {
                onDeleteClick(notification)
                true
            }
        }
    }

    override fun getItemCount(): Int = itemsList.size

    fun updateData(newNotifications: List<NotificationItem>) {
        val displayItems = mutableListOf<NotificationListItem>()
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        
        // Group list by categories
        val unreadList = newNotifications.filter { !it.isRead }
        val readList = newNotifications.filter { it.isRead }
        
        val todayList = readList.filter { sdf.format(Date(it.timestamp)) == todayStr }
        val yesterdayList = readList.filter { sdf.format(Date(it.timestamp)) == yesterdayStr }
        val earlierList = readList.filter { 
            val d = sdf.format(Date(it.timestamp))
            d != todayStr && d != yesterdayStr
        }
        
        if (unreadList.isNotEmpty()) {
            displayItems.add(NotificationListItem.Header("Unread"))
            displayItems.addAll(unreadList.map { NotificationListItem.Content(it) })
        }
        
        if (todayList.isNotEmpty()) {
            displayItems.add(NotificationListItem.Header("Today"))
            displayItems.addAll(todayList.map { NotificationListItem.Content(it) })
        }
        
        if (yesterdayList.isNotEmpty()) {
            displayItems.add(NotificationListItem.Header("Yesterday"))
            displayItems.addAll(yesterdayList.map { NotificationListItem.Content(it) })
        }
        
        if (earlierList.isNotEmpty()) {
            displayItems.add(NotificationListItem.Header("Earlier"))
            displayItems.addAll(earlierList.map { NotificationListItem.Content(it) })
        }
        
        itemsList = displayItems
        notifyDataSetChanged()
    }

    private fun getRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000} min ago"
            diff < 86400000 -> "${diff / 3600000} hr ago"
            else -> "${diff / 86400000} days ago"
        }
    }

    class HeaderViewHolder(val binding: ItemNotificationHeaderBinding) : RecyclerView.ViewHolder(binding.root)
    class ContentViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
}
