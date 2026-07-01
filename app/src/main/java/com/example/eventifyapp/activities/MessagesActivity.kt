package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.eventifyapp.R
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.adapters.MessageAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityMessagesBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.repository.MessageRepository
import com.example.eventifyapp.viewmodel.MessageViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var messageAdapter: MessageAdapter

    private var isTabAllSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupTabListeners()
        setupBottomNavigation()
        observeConversations()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke halaman ini
        loadConversations()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MessageRepository(database.messageDao())
        val factory = ViewModelFactory(messageRepository = repository)
        viewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(emptyList()) { message ->
            // Saat percakapan diklik, tandai pesan terakhir sebagai sudah dibaca
            lifecycleScope.launch {
                viewModel.markAsRead(message.id)

                val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                val currentUserEmail = sharedPrefs.getString("USER_EMAIL", "nasywa@example.com") ?: "nasywa@example.com"
                
                val chatPartnerEmail = if (message.senderEmail == currentUserEmail) message.receiverEmail else message.senderEmail
                val chatPartnerName = if (message.senderEmail == currentUserEmail) "Admin" else message.senderName

                val chatIntent = Intent(this@MessagesActivity, ChatDetailActivity::class.java).apply {
                    putExtra("SENDER_EMAIL", chatPartnerEmail)
                    putExtra("SENDER_NAME", chatPartnerName)
                }
                startActivity(chatIntent)
            }
        }

        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@MessagesActivity)
            adapter = messageAdapter
        }
    }

    private fun setupTabListeners() {
        binding.tabAll.setOnClickListener {
            if (!isTabAllSelected) {
                isTabAllSelected = true
                updateTabUi()
                loadConversations()
            }
        }

        binding.tabUnread.setOnClickListener {
            if (isTabAllSelected) {
                isTabAllSelected = false
                updateTabUi()
                loadConversations()
            }
        }
    }

    private fun updateTabUi() {
        if (isTabAllSelected) {
            binding.tabAll.setTextColor(getColor(R.color.colorOrange))
            binding.tabAll.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabUnread.setTextColor(getColor(R.color.colorTextSecondary))
            binding.tabUnread.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.tabUnread.setTextColor(getColor(R.color.colorOrange))
            binding.tabUnread.setTypeface(null, android.graphics.Typeface.BOLD)
            binding.tabAll.setTextColor(getColor(R.color.colorTextSecondary))
            binding.tabAll.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun loadConversations() {
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val currentUserEmail = sharedPrefs.getString("USER_EMAIL", "nasywa@example.com") ?: "nasywa@example.com"
        if (isTabAllSelected) {
            viewModel.loadAllConversations(currentUserEmail)
        } else {
            viewModel.loadUnreadConversations(currentUserEmail)
        }
    }

    private fun observeConversations() {
        lifecycleScope.launch {
            viewModel.conversations.collect { list ->
                messageAdapter.updateData(list)
            }
        }
    }


    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        // Set active icon for Chat
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
    }
}
