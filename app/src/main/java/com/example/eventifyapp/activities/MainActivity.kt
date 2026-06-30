package com.example.eventifyapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.EventAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityMainBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private var isGridView = false

    // Permission launcher Android 13+
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        observeEvents()

        requestNotificationPermission()
    }

    // =======================
    // Permission Notification
    // =======================
    private fun requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )

            }

        }

    }

    // =======================
    // ViewModel
    // =======================
    private fun setupViewModel() {

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventRepository(database.eventDao())
        val factory = ViewModelFactory(eventRepository = repository)

        viewModel = ViewModelProvider(
            this,
            factory
        )[EventViewModel::class.java]

    }

    // =======================
    // RecyclerView
    // =======================
    private fun setupRecyclerView() {

        eventAdapter = EventAdapter(emptyList()) { event ->

            val intent = Intent(this, DetailEventActivity::class.java).apply {

                putExtra("EVENT_ID", event.id)
                putExtra("EVENT_TITLE", event.title)
                putExtra("EVENT_DATE", event.date)
                putExtra("EVENT_LOCATION", event.location)
                putExtra("EVENT_PRICE", event.price)
                putExtra("EVENT_DESCRIPTION", event.description)
                putExtra("EVENT_IMAGE", event.imageUrl)

            }

            startActivity(intent)

        }

        binding.rvEvents.layoutManager =
            LinearLayoutManager(this)

        binding.rvEvents.adapter = eventAdapter

    }

    // =======================
    // Click Listener
    // =======================
    private fun setupClickListeners() {

        binding.btnToggleLayout.setOnClickListener {
            toggleLayoutMode()
        }

    }

    // =======================
    // Toggle List/Grid
    // =======================
    private fun toggleLayoutMode() {

        isGridView = !isGridView

        if (isGridView) {

            binding.rvEvents.layoutManager =
                GridLayoutManager(this, 2)

            binding.btnToggleLayout.setImageResource(
                android.R.drawable.ic_menu_agenda
            )

            Toast.makeText(
                this,
                "Tampilan Grid",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            binding.rvEvents.layoutManager =
                LinearLayoutManager(this)

            binding.btnToggleLayout.setImageResource(
                android.R.drawable.ic_dialog_dialer
            )

            Toast.makeText(
                this,
                "Tampilan List",
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    // =======================
    // Bottom Navigation
    // =======================
    private fun setupBottomNavigation() {

        val navbarBinding =
            LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        navbarBinding.navHome.setColorFilter(
            getColor(R.color.colorOrange)
        )

        navbarBinding.navHome.setOnClickListener {}

        navbarBinding.navChat.setOnClickListener {
            startActivity(Intent(this, MessagesActivity::class.java))
        }

        navbarBinding.navNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        navbarBinding.navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    // =======================
    // Observe Data
    // =======================
    private fun observeEvents() {

        lifecycleScope.launch {

            viewModel.allEvents.collect { events ->

                if (events.isNotEmpty()) {
                    eventAdapter.updateData(events)
                }

            }

        }

    }

}