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
import com.example.eventifyapp.model.Message
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

        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(applicationContext)
            val count = database.messageDao().getPrivateMessageCountForUser(currentUserEmail)
            if (count == 0) {
                seedMockMessagesForUser(database, currentUserEmail)
            }
        }

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
        messageAdapter = MessageAdapter(emptyList(), currentUserEmail) { message ->
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

    private suspend fun seedMockMessagesForUser(database: AppDatabase, myEmail: String) {
        val now = System.currentTimeMillis()
        val messagesToSeed = listOf(
            Message(
                senderName = "Naura Belva",
                senderEmail = "naura@gmail.com",
                receiverEmail = myEmail,
                message = "Nice to hear from you! This is very exciting.",
                timestamp = now - 600000,
                isRead = false,
                isCommunity = false
            ),
            Message(
                senderName = "Saddam Aditya",
                senderEmail = "saddam_aditya@gmail.com",
                receiverEmail = myEmail,
                message = "You know what to do :D",
                timestamp = now - 1800000,
                isRead = false,
                isCommunity = false
            ),
            Message(
                senderName = "Graceu Larisma",
                senderEmail = "graceu@gmail.com",
                receiverEmail = myEmail,
                message = "This is so nice! I'm glad for you",
                timestamp = now - 3600000,
                isRead = false,
                isCommunity = false
            ),
            Message(
                senderName = "Saddam Mufti",
                senderEmail = "saddam_mufti@gmail.com",
                receiverEmail = myEmail,
                message = "Interesting 😜",
                timestamp = now - 7200000,
                isRead = false,
                isCommunity = false
            ),
            Message(
                senderName = "Graceu Larisma",
                senderEmail = "circle_1@gmail.com",
                receiverEmail = myEmail,
                message = "Guys let's organize again!",
                timestamp = now - 14400000,
                isRead = true,
                isCommunity = false,
                groupTitle = "Naura, Graceu, Nasywa"
            ),
            Message(
                senderName = "Syaira Poe",
                senderEmail = "syaira_poe@gmail.com",
                receiverEmail = myEmail,
                message = "Where are u from?",
                timestamp = now - 28800000,
                isRead = false,
                isCommunity = false
            ),
            Message(
                senderName = "Nasywa Sasikirana",
                senderEmail = "nasywa_sasi@gmail.com",
                receiverEmail = myEmail,
                message = "Hi!!",
                timestamp = now - 43200000,
                isRead = true,
                isCommunity = false
            )
        )

        for (msg in messagesToSeed) {
            if (!msg.senderEmail.equals(myEmail, ignoreCase = true)) {
                database.messageDao().insertMessage(msg)
            }
        }
    }
}
