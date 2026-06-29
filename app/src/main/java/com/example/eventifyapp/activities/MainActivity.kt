package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityMainBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import com.example.eventifyapp.adapters.EventAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private var isGridView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        setupBottomNavigation()
        observeEvents()
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventRepository(database.eventDao())
        val factory = ViewModelFactory(eventRepository = repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList()) { event ->
            // Ketika event diklik → buka DetailEventActivity
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

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = eventAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnToggleLayout.setOnClickListener {
            toggleLayoutMode()
        }
    }

    private fun toggleLayoutMode() {
        isGridView = !isGridView
        if (isGridView) {
            binding.rvEvents.layoutManager = GridLayoutManager(this, 2)
            binding.btnToggleLayout.setImageResource(android.R.drawable.ic_menu_agenda) // Ganti ke ikon List
            Toast.makeText(this, "Tampilan Grid", Toast.LENGTH_SHORT).show()
        } else {
            binding.rvEvents.layoutManager = LinearLayoutManager(this)
            binding.btnToggleLayout.setImageResource(android.R.drawable.ic_dialog_dialer) // Ganti ke ikon Grid
            Toast.makeText(this, "Tampilan List", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        // Karena layout_navbar menggunakan <include>, kita bind element di dalamnya
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        // Set ikon aktif untuk halaman Home (warna oranye)
        navbarBinding.navHome.setColorFilter(getColor(com.example.eventifyapp.R.color.colorOrange))

        navbarBinding.navHome.setOnClickListener {
            // Sudah di Home, tidak perlu pindah
        }

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