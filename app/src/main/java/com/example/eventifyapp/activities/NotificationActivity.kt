package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.NotificationAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityNotificationBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        observeNotifications()
        setupBottomNavigation()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NotificationRepository(database.notificationDao())
        val factory = ViewModelFactory(notificationRepository = repository)
        viewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(emptyList()) { notif ->
            // Klik notif → tandai sudah dibaca → dot hilang
            lifecycleScope.launch {
                viewModel.markAsRead(notif.id)
            }
        }
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notificationAdapter
        }
    }

    private fun observeNotifications() {
        lifecycleScope.launch {
            viewModel.notifications.collect { notifications ->
                notificationAdapter.updateData(notifications)
            }
        }
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        navbarBinding.navNotification.setColorFilter(getColor(R.color.colorOrange))
        navbarBinding.navHome.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navChat.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navProfile.setColorFilter(getColor(R.color.gray_text))

        navbarBinding.navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            overridePendingTransition(0, 0)
        }
        navbarBinding.navChat.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            overridePendingTransition(0, 0)
        }
        navbarBinding.navNotification.setOnClickListener { }
        navbarBinding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            })
            overridePendingTransition(0, 0)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}