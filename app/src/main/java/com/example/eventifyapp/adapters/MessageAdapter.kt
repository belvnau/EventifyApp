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

class MessageAdapter(
    private var messages: List<Message> = emptyList(),
    private val onItemClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

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
        holder.binding.tvSenderName.text = message.senderName
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

        // Set default avatar
        val context = holder.itemView.context
        val avatarResId = context.resources.getIdentifier("img_avatar_default", "drawable", context.packageName)
        if (avatarResId != 0) {
            holder.binding.ivSenderAvatar.setImageResource(avatarResId)
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
}
