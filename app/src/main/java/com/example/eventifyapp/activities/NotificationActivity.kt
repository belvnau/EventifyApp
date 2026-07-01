package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
            onItemClick = { notification ->
                handleNotificationClick(notification)
            },
            onDeleteClick = { notification ->
                showDeleteConfirmationDialog(notification)
            },
            onAcceptInvite = { notification ->
                handleAcceptInvitation(notification)
            },
            onRejectInvite = { notification ->
                handleRejectInvitation(notification)
            }
        )

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@NotificationActivity)
            adapter = notificationAdapter
        }
    }

    private fun setupClickListeners() {
        // Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Search Button placeholder
        binding.btnSearch.setOnClickListener {
            Toast.makeText(this, "Fitur Pencarian", Toast.LENGTH_SHORT).show()
        }

        // More Options Popup Menu (Mark All Read / Clear All)
        binding.btnMoreOptions.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "Tandai Semua Dibaca")
            popup.menu.add(0, 2, 0, "Hapus Semua")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> {
                        notificationViewModel.markAllAsRead()
                        Toast.makeText(this, "Semua ditandai dibaca", Toast.LENGTH_SHORT).show()
                        true
                    }
                    2 -> {
                        showClearAllConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        lifecycleScope.launch {
            // CRUD: Update status to read
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

    private fun showDeleteConfirmationDialog(notification: NotificationItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Notifikasi")
            .setMessage("Apakah kamu yakin ingin menghapus notifikasi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    notificationViewModel.deleteNotification(notification)
                    Toast.makeText(this@NotificationActivity, "Notifikasi dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showClearAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Notifikasi")
            .setMessage("Apakah kamu yakin ingin menghapus semua notifikasi?")
            .setPositiveButton("Hapus Semua") { _, _ ->
                notificationViewModel.deleteAllNotifications()
                Toast.makeText(this@NotificationActivity, "Semua notifikasi dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun handleAcceptInvitation(notification: NotificationItem) {
        lifecycleScope.launch {
            notificationViewModel.markAsRead(notification.id)
            Toast.makeText(this@NotificationActivity, "Undangan diterima!", Toast.LENGTH_SHORT).show()
            // Optionally, we can update message description or delete the notification
            notificationViewModel.loadNotifications()
        }
    }

    private fun handleRejectInvitation(notification: NotificationItem) {
        lifecycleScope.launch {
            notificationViewModel.markAsRead(notification.id)
            Toast.makeText(this@NotificationActivity, "Undangan ditolak", Toast.LENGTH_SHORT).show()
            notificationViewModel.loadNotifications()
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

        // Active State: Notification Tab is active (colored black/dark)
        navbarBinding.ivNotificationIcon.setColorFilter(getColor(R.color.colorPrimary))
        navbarBinding.tvNotificationLabel.setTextColor(getColor(R.color.colorPrimary))

        // Inactive States: Home, Chat, Profile (colored gray)
        navbarBinding.ivHomeIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvHomeLabel.setTextColor(getColor(R.color.gray_text))

        navbarBinding.ivChatIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvChatLabel.setTextColor(getColor(R.color.gray_text))

        navbarBinding.ivProfileIcon.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.tvProfileLabel.setTextColor(getColor(R.color.gray_text))

        // Navigation Clicks
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
