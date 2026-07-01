package com.example.eventifyapp.activities

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
import kotlinx.coroutines.launch

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

        // Ambil data intent (email & nama chat partner)
        senderEmail = intent.getStringExtra("SENDER_EMAIL") ?: ""
        senderName = intent.getStringExtra("SENDER_NAME") ?: "Chat"

        if (senderEmail.isEmpty()) {
            Toast.makeText(this, "Kontak tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ambil data user yang sedang login dari SharedPreferences
        val sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        currentUserEmail = sharedPrefs.getString("USER_EMAIL", "nasywa@example.com") ?: "nasywa@example.com"
        currentUserName = sharedPrefs.getString("USER_NAME", "Nasywa") ?: "Nasywa"

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
        
        // Load default avatar
        val avatarResId = resources.getIdentifier("img_avatar_default", "drawable", packageName)
        if (avatarResId != 0) {
            binding.ivChatAvatar.setImageResource(avatarResId)
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MessageRepository(database.messageDao())
        val factory = ViewModelFactory(messageRepository = repository)
        viewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // ChatAdapter butuh list pesan & email user saat ini untuk bedakan bubble kiri/kanan
        chatAdapter = ChatAdapter(emptyList(), currentUserEmail)
        
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Membuat chat scroll otomatis ke bawah saat dibuka
        }
        
        binding.rvChat.apply {
            this.layoutManager = layoutManager
            this.adapter = chatAdapter
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = binding.etMessageInput.text.toString().trim()
        if (text.isEmpty()) return

        val newMessage = Message(
            senderName = currentUserName,
            senderEmail = currentUserEmail,
            receiverEmail = senderEmail,
            message = text,
            timestamp = System.currentTimeMillis(),
            isRead = true // Pesan yang dikirim langsung ditandai terbaca untuk pengirim
        )

        lifecycleScope.launch {
            val insertId = viewModel.sendMessage(newMessage)
            if (insertId != -1L) {
                binding.etMessageInput.text.clear()
                // Auto scroll ke bawah setelah kirim pesan
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
}
