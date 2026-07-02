package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.databinding.ItemMessageListBinding
import com.example.eventifyapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.eventifyapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageAdapter(
    private var messages: List<Message> = emptyList(),
    private val onItemClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var eventImageMap: Map<Long, String> = emptyMap()

    class MessageViewHolder(val binding: ItemMessageListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        val chatTitle = if (message.groupTitle.isNotEmpty()) {
            message.groupTitle
        } else {
            message.senderName
        }
        holder.binding.tvSenderName.text = chatTitle
        holder.binding.tvMessagePreview.text = message.message
        
        // Format timestamp to readable time
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.binding.tvMessageTime.text = sdf.format(Date(message.timestamp))

        // Toggle unread dot visibility
        if (message.isRead) {
            holder.binding.viewUnreadDot.visibility = View.GONE
        } else {
            holder.binding.viewUnreadDot.visibility = View.VISIBLE
        }

        // Set default, dynamic, or custom avatar
        val context = holder.itemView.context
        holder.binding.tvAvatarLetter.visibility = View.GONE
        holder.binding.ivSenderAvatar.visibility = View.VISIBLE
        holder.binding.layoutAvatarContainer.setCardBackgroundColor(
            androidx.core.content.ContextCompat.getColor(context, R.color.colorBackground)
        )

        if (message.isCommunity) {
            val imageUrl = eventImageMap[message.eventId] ?: ""
            loadImage(imageUrl, holder.binding.ivSenderAvatar, context)
        } else {
            // Check for specific dummy contacts
            val customAvatarName = when (chatTitle) {
                "Naura Belva" -> "avatar_jennie"
                "Saddam Aditya" -> "avatar_joe"
                "Graceu Larisma" -> "avatar_camillia"
                "Saddam Mufti" -> "avatar_callum"
                "Naura, Graceu, Nasywa" -> "avatar_group"
                else -> null
            }

            if (customAvatarName != null) {
                val resId = context.resources.getIdentifier(customAvatarName, "drawable", context.packageName)
                if (resId != 0) {
                    holder.binding.ivSenderAvatar.setImageResource(resId)
                } else {
                    holder.binding.ivSenderAvatar.setImageResource(R.drawable.img_avatar_default)
                }
            } else {
                // Show dynamic letter avatar
                holder.binding.ivSenderAvatar.visibility = View.GONE
                holder.binding.tvAvatarLetter.visibility = View.VISIBLE
                
                val firstLetter = if (chatTitle.isNotEmpty()) chatTitle.first().uppercaseChar().toString() else "?"
                holder.binding.tvAvatarLetter.text = firstLetter

                val bgColors = listOf("#FF6B35", "#2A9D8F", "#E91E63", "#2196F3", "#9C27B0", "#E76F51")
                val colorHex = bgColors[Math.abs(chatTitle.hashCode()) % bgColors.size]
                holder.binding.layoutAvatarContainer.setCardBackgroundColor(android.graphics.Color.parseColor(colorHex))
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateData(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun updateEventImages(images: Map<Long, String>) {
        eventImageMap = images
        notifyDataSetChanged()
    }

    private fun loadImage(imageUrl: String, imageView: android.widget.ImageView, context: android.content.Context) {
        if (imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.img_avatar_default)
            return
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            CoroutineScope(Dispatchers.Main).launch {
                val bitmap = withContext(Dispatchers.IO) {
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
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.img_avatar_default)
                }
            }
        } else {
            val resourceId = context.resources.getIdentifier(imageUrl, "drawable", context.packageName)
            if (resourceId != 0) {
                imageView.setImageResource(resourceId)
            } else {
                imageView.setImageResource(R.drawable.img_avatar_default)
            }
        }
    }
}
