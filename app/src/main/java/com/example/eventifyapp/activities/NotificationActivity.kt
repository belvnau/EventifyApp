package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.NotificationAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityNotificationBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.model.NotificationItem
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NotificationRepository(database.notificationDao())
        val factory = ViewModelFactory(notificationRepository = repository)
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            notifications = emptyList(),
            onItemClick = { notification ->
                handleNotificationClick(notification)
            },
            onDeleteClick = { notification ->
                handleNotificationDelete(notification)
            }
        )

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notificationAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnClearAll.setOnClickListener {
            notificationViewModel.deleteAllNotifications()
            Toast.makeText(this, "Semua notifikasi dihapus", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        lifecycleScope.launch {
            // CRUD: Update status to read in local database
            notificationViewModel.markAsRead(notification.id)
            
            // Navigate if contains eventId
            if (notification.eventId != null) {
                val intent = Intent(this@NotificationActivity, DetailEventActivity::class.java).apply {
                    putExtra("EVENT_ID", notification.eventId)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@NotificationActivity, notification.title, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleNotificationDelete(notification: NotificationItem) {
        lifecycleScope.launch {
            // CRUD: Delete notification
            notificationViewModel.deleteNotification(notification)
            Toast.makeText(this@NotificationActivity, "Notifikasi dihapus", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        // Observe list
        lifecycleScope.launch {
            notificationViewModel.notifications.collect { list ->
                notificationAdapter.updateData(list)
                if (list.isEmpty()) {
                    binding.rvNotifications.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvNotifications.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                }
            }
        }

        // Observe badge count
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

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        // Set active icon for Notification
        navbarBinding.navNotification.setColorFilter(getColor(R.color.colorOrange))
        navbarBinding.navHome.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navChat.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navProfile.setColorFilter(getColor(R.color.gray_text))

        navbarBinding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navbarBinding.navChat.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navbarBinding.navNotification.setOnClickListener {
            // Already here
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
        notificationViewModel.loadNotifications()
        notificationViewModel.loadUnreadCount()
    }
}
