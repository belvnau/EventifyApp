package com.example.eventifyapp.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.adapters.NotificationAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityNotificationBinding
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        observeNotifications()
    }

    private fun setupViewModel() {

        val database = AppDatabase.getDatabase(applicationContext)

        val repository = NotificationRepository(
            database.notificationDao()
        )

        val factory = ViewModelFactory(
            notificationRepository = repository
        )

        viewModel = ViewModelProvider(
            this,
            factory
        )[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {

        adapter = NotificationAdapter(

            notifications = emptyList(),

            onItemClick = {

                Toast.makeText(
                    this,
                    it.title,
                    Toast.LENGTH_SHORT
                ).show()

            },

            onAcceptClick = { notification ->

                lifecycleScope.launch {

                    viewModel.markAsRead(notification.id)

                    Toast.makeText(
                        this@NotificationActivity,
                        "Notifikasi ditandai sudah dibaca",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            },

            onRejectClick = { notification ->

                lifecycleScope.launch {

                    viewModel.deleteNotification(notification)

                    Toast.makeText(
                        this@NotificationActivity,
                        "Notifikasi dihapus",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        )

        binding.rvNotifications.layoutManager =
            LinearLayoutManager(this)

        binding.rvNotifications.adapter = adapter

    }

    private fun observeNotifications() {

        lifecycleScope.launch {

            viewModel.notifications.collect {

                adapter.updateData(it)

            }

        }

    }
}