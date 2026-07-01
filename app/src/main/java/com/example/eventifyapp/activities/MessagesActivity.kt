package com.example.eventifyapp.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.MessageAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityMessagesBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.model.Message
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.MessageRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.MessageViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var messageAdapter: MessageAdapter

    private var selectedTab = "all" // Default tab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupTabListeners()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val messageRepo = MessageRepository(database.messageDao())
        val notificationRepo = NotificationRepository(database.notificationDao())
        
        val factory = ViewModelFactory(
            messageRepository = messageRepo,
            notificationRepository = notificationRepo
        )
        
        messageViewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList()) { message ->
            showChatDetailDialog(message)
        }
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MessagesActivity)
            adapter = messageAdapter
        }
    }

    private fun setupTabListeners() {
        binding.tabAll.setOnClickListener {
            selectedTab = "all"
            updateTabStyles()
            messageViewModel.loadAllConversations()
        }

        binding.tabUnread.setOnClickListener {
            selectedTab = "unread"
            updateTabStyles()
            messageViewModel.loadUnreadConversations()
        }
    }

    private fun updateTabStyles() {
        if (selectedTab == "all") {
            binding.tabAll.setTextColor(getColor(R.color.colorOrange))
            binding.tabAll.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabUnread.setTextColor(getColor(R.color.gray_text))
            binding.tabUnread.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.tabAll.setTextColor(getColor(R.color.gray_text))
            binding.tabAll.setTypeface(null, android.graphics.Typeface.NORMAL)
            binding.tabUnread.setTextColor(getColor(R.color.colorOrange))
            binding.tabUnread.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun observeViewModel() {
        // Observe conversation threads list
        lifecycleScope.launch {
            messageViewModel.conversations.collect { list ->
                messageAdapter.updateData(list)
            }
        }

        // Observe navbar notification count badge
        lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)
                if (count > 0) {
                    navbarBinding.tvNotificationBadge.visibility = View.VISIBLE
                    navbarBinding.tvNotificationBadge.text = count.toString()
                } else {
                    navbarBinding.tvNotificationBadge.visibility = View.GONE
                }
            }
        }
    }

    private fun showChatDetailDialog(conversation: Message) {
        lifecycleScope.launch {
            // CRUD: Update message status to read
            messageViewModel.markAsRead(conversation.id)
            if (selectedTab == "all") {
                messageViewModel.loadAllConversations()
            } else {
                messageViewModel.loadUnreadConversations()
            }
            notificationViewModel.loadUnreadCount()
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chat dengan ${conversation.senderName}")
        builder.setMessage("Pesan Terakhir:\n${conversation.message}")

        val input = EditText(this)
        input.hint = "Ketik balasan..."
        builder.setView(input)

        builder.setPositiveButton("Kirim") { _, _ ->
            val replyText = input.text.toString().trim()
            if (replyText.isNotEmpty()) {
                lifecycleScope.launch {
                    messageViewModel.sendMessage(
                        Message(
                            senderName = "Me",
                            senderEmail = "poetrysa@gmail.com",
                            message = replyText,
                            isRead = true
                        )
                    )

                    // CRUD: Create notification
                    notificationViewModel.addNotification(
                        NotificationItem(
                            title = "Pesan Terkirim",
                            message = "Balasan terkirim ke ${conversation.senderName}: $replyText",
                            type = "message"
                        )
                    )

                    Toast.makeText(this@MessagesActivity, "Balasan terkirim!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Tutup", null)
        builder.show()
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        // Active State: Chat Tab is active
        navbarBinding.ivChatIcon.setColorFilter(getColor(R.color.colorPrimary))
        navbarBinding.tvChatLabel.setTextColor(getColor(R.color.colorPrimary))

        // Inactive States: Home, Notification, Profile
        navbarBinding.ivHomeIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvHomeLabel.setTextColor(getColor(R.color.gray_text))

        navbarBinding.ivNotificationIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvNotificationLabel.setTextColor(getColor(R.color.gray_text))

        navbarBinding.ivProfileIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvProfileLabel.setTextColor(getColor(R.color.gray_text))

        navbarBinding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navbarBinding.navChat.setOnClickListener {
            // Already here
        }

        navbarBinding.navNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navbarBinding.navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (selectedTab == "all") {
            messageViewModel.loadAllConversations()
        } else {
            messageViewModel.loadUnreadConversations()
        }
        if (::notificationViewModel.isInitialized) {
            notificationViewModel.loadUnreadCount()
        }
    }
}
