package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.MessageAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityMessagesBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.repository.MessageRepository
import com.example.eventifyapp.utils.SessionManager
import com.example.eventifyapp.viewmodel.MessageViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import com.example.eventifyapp.model.Event
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var messageAdapter: MessageAdapter
    private var currentUserEmail: String = ""
    private var currentTab: String = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        currentUserEmail = sessionManager.getLoggedInEmail() ?: "poetrysa@gmail.com"

        setupViewModel()
        setupRecyclerView()
        setupTabSelection()
        setupBottomNavigation()
        
        binding.btnBack.setOnClickListener {
            finish()
        }

        handleIntentExtras(intent)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val messageRepo = MessageRepository(database.messageDao())
        val factory = ViewModelFactory(messageRepository = messageRepo)
        messageViewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList()) { message ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                val partnerEmail = if (message.senderEmail.equals(currentUserEmail, ignoreCase = true)) {
                    message.receiverEmail
                } else {
                    message.senderEmail
                }
                putExtra("SENDER_EMAIL", partnerEmail)
                putExtra("CHAT_PARTNER_EMAIL", partnerEmail)
                
                val chatTitle = if (message.isCommunity) {
                    message.groupTitle
                } else if (message.groupTitle.isNotEmpty()) {
                    message.groupTitle
                } else {
                    message.senderName
                }
                putExtra("SENDER_NAME", chatTitle)
                putExtra("CHAT_PARTNER_NAME", chatTitle)
                putExtra("IS_COMMUNITY", message.isCommunity)
                putExtra("EVENT_ID", message.eventId)
            }
            startActivity(intent)
        }

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = messageAdapter
    }

    private fun setupTabSelection() {
        binding.tabAll.setOnClickListener {
            switchTab("all")
        }
        binding.tabUnread.setOnClickListener {
            switchTab("community")
        }

        lifecycleScope.launch {
            messageViewModel.conversations.collect { conversations ->
                messageAdapter.updateData(conversations)
            }
        }

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            database.eventDao().getAllEvents().collect { events ->
                val imageMap = events.associate { it.id to it.imageUrl }
                messageAdapter.updateEventImages(imageMap)
            }
        }

        switchTab("all")
    }

    private fun switchTab(tab: String) {
        currentTab = tab
        if (tab == "all") {
            binding.tabAll.setBackgroundResource(R.drawable.bg_button)
            binding.tabAll.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabUnread.background = null
            binding.tabUnread.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary))

            messageViewModel.loadLatestAllMessages(currentUserEmail)
        } else {
            binding.tabUnread.setBackgroundResource(R.drawable.bg_button)
            binding.tabUnread.setTextColor(ContextCompat.getColor(this, R.color.white))

            binding.tabAll.background = null
            binding.tabAll.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary))

            messageViewModel.loadLatestCommunityMessages(currentUserEmail)
        }
    }

    private fun handleIntentExtras(intent: Intent?) {
        val tab = intent?.getStringExtra("SELECT_TAB")
        if (tab != null) {
            switchTab(tab)
        }
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        navbarBinding.navChat.setColorFilter(getColor(R.color.colorOrange))
        navbarBinding.navHome.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navNotification.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navProfile.setColorFilter(getColor(R.color.gray_text))

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
        handleIntentExtras(intent)
    }
}
