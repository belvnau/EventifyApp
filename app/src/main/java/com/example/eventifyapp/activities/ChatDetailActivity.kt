package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.ChatAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityChatDetailBinding
import com.example.eventifyapp.model.Message
import com.example.eventifyapp.repository.MessageRepository
import com.example.eventifyapp.viewmodel.MessageViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import com.example.eventifyapp.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var chatAdapter: ChatAdapter

    private var senderEmail: String = ""
    private var senderName: String = ""
    private var currentUserEmail: String = ""
    private var currentUserName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data intent (email & nama chat partner) dengan fallback untuk key lama/baru
        senderEmail = intent.getStringExtra("SENDER_EMAIL") ?: intent.getStringExtra("CHAT_PARTNER_EMAIL") ?: ""
        senderName = intent.getStringExtra("SENDER_NAME") ?: intent.getStringExtra("CHAT_PARTNER_NAME") ?: "Chat"

        if (senderEmail.isEmpty()) {
            Toast.makeText(this, "Kontak tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val database = AppDatabase.getDatabase(applicationContext)
        val sessionManager = SessionManager(this)
        currentUserEmail = sessionManager.getLoggedInEmail() ?: "poetrysa@gmail.com"

        lifecycleScope.launch {
            val user = database.userDao().getUserByEmail(currentUserEmail)
            currentUserName = user?.name ?: user?.username ?: "Syaira Poetry"
        }

        setupViews()
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeChatMessages()

        // Load riwayat chat
        viewModel.loadChatWithSender(senderEmail, currentUserEmail)
    }

    private fun setupViews() {
        binding.tvChatName.text = senderName
        
        val isCommunity = intent.getBooleanExtra("IS_COMMUNITY", false)
        val eventId = intent.getLongExtra("EVENT_ID", 0L)

        if (isCommunity && eventId != 0L) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(applicationContext)
                val event = database.eventDao().getEventById(eventId)
                val imageUrl = event?.imageUrl ?: ""
                loadImage(imageUrl, binding.ivChatAvatar, this@ChatDetailActivity)
            }
        } else {
            // Cek jika kontak dummy memiliki avatar kustom
            val customAvatarName = when (senderName) {
                "Naura Belva" -> "avatar_jennie"
                "Saddam Aditya" -> "avatar_joe"
                "Graceu Larisma" -> "avatar_camillia"
                "Saddam Mufti" -> "avatar_callum"
                "Naura, Graceu, Nasywa" -> "avatar_group"
                else -> null
            }
            if (customAvatarName != null) {
                val resId = resources.getIdentifier(customAvatarName, "drawable", packageName)
                if (resId != 0) {
                    binding.ivChatAvatar.setImageResource(resId)
                } else {
                    binding.ivChatAvatar.setImageResource(R.drawable.img_avatar_default)
                }
            } else {
                binding.ivChatAvatar.setImageResource(R.drawable.img_avatar_default)
            }
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MessageRepository(database.messageDao())
        val factory = ViewModelFactory(messageRepository = repository)
        viewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyList(), currentUserEmail)
        
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        
        binding.rvChat.apply {
            this.layoutManager = layoutManager
            this.adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            handleBackNavigation()
        }

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun handleBackNavigation() {
        val fromDetailEvent = intent.getBooleanExtra("FROM_DETAIL_EVENT", false)
        if (fromDetailEvent) {
            val intent = Intent(this, MessagesActivity::class.java).apply {
                putExtra("SELECT_TAB", "community")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        }
        finish()
    }

    override fun onBackPressed() {
        handleBackNavigation()
        super.onBackPressed()
    }

    private fun sendMessage() {
        val text = binding.etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val isCommunity = intent.getBooleanExtra("IS_COMMUNITY", false)
        val eventId = intent.getLongExtra("EVENT_ID", 0L)

        val newMessage = Message(
            senderName = currentUserName,
            senderEmail = currentUserEmail,
            receiverEmail = senderEmail, // Tetap gunakan senderEmail (yang berisi identifier group/kontak) agar chat terkelompokkan dengan benar
            message = text,
            timestamp = System.currentTimeMillis(),
            isRead = true,
            isCommunity = isCommunity,
            eventId = eventId,
            groupTitle = if (isCommunity) senderName else ""
        )

        lifecycleScope.launch {
            val insertId = viewModel.sendMessage(newMessage)
            if (insertId != -1L) {
                binding.etMessageInput.text.clear()
                viewModel.loadChatWithSender(senderEmail, currentUserEmail)
            } else {
                Toast.makeText(this@ChatDetailActivity, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeChatMessages() {
        lifecycleScope.launch {
            viewModel.chatMessages.collect { list ->
                chatAdapter.updateData(list)
                if (list.isNotEmpty()) {
                    binding.rvChat.scrollToPosition(list.size - 1)
                }
            }
        }
    }

    private fun loadImage(imageUrl: String, imageView: android.widget.ImageView, context: android.content.Context) {
        if (imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.img_avatar_default)
            return
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            lifecycleScope.launch {
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
