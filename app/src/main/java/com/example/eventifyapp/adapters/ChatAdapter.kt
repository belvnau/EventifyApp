package com.example.eventifyapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.databinding.ItemChatBubbleLeftBinding
import com.example.eventifyapp.databinding.ItemChatBubbleRightBinding
import com.example.eventifyapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var chatMessages: List<Message> = emptyList(),
    private val currentUserEmail: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]
        // Membandingkan email pengirim dengan email user yang sedang login
        return if (message.senderEmail.equals(currentUserEmail, ignoreCase = true)) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    class SentViewHolder(val binding: ItemChatBubbleRightBinding) : RecyclerView.ViewHolder(binding.root)
    class ReceivedViewHolder(val binding: ItemChatBubbleLeftBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemChatBubbleRightBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentViewHolder(binding)
        } else {
            val binding = ItemChatBubbleLeftBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessages[position]
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = sdf.format(Date(message.timestamp))

        if (holder is SentViewHolder) {
            holder.binding.tvChatMessage.text = message.message
            holder.binding.tvChatTime.text = formattedTime
        } else if (holder is ReceivedViewHolder) {
            holder.binding.tvChatMessage.text = message.message
            holder.binding.tvChatTime.text = formattedTime
            if (message.isCommunity) {
                holder.binding.tvSenderName.text = message.senderName
                holder.binding.tvSenderName.visibility = android.view.View.VISIBLE
            } else {
                holder.binding.tvSenderName.visibility = android.view.View.GONE
            }
        }
    }

    override fun getItemCount(): Int = chatMessages.size

    fun updateData(newMessages: List<Message>) {
        chatMessages = newMessages
        notifyDataSetChanged()
    }
}
