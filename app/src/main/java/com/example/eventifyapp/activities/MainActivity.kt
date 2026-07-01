package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
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
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private var allEventsList: List<Event> = emptyList()
    private var isGridView = false // Default List View (1 column)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserGreeting()
        setupViewModel()
        setupRecyclerView()
        setupFilterTabs()
        setupClickListeners()
        setupBottomNavigation()
        observeEvents()
    }

    private fun setupUserGreeting() {
        val username = intent.getStringExtra("USER_NAME") ?: "Poetrysya"
        binding.tvUsername.text = username
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EventRepository(database.eventDao())
        val factory = ViewModelFactory(eventRepository = repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList(), isGridView) { event ->
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

        updateRecyclerViewLayout()
        binding.rvEvents.adapter = eventAdapter
    }

    private fun updateRecyclerViewLayout() {
        if (isGridView) {
            binding.rvEvents.layoutManager = GridLayoutManager(this, 2)
            binding.btnToggleLayout.setImageResource(android.R.drawable.ic_menu_agenda) // Icon for List
        } else {
            binding.rvEvents.layoutManager = LinearLayoutManager(this)
            binding.btnToggleLayout.setImageResource(android.R.drawable.ic_dialog_dialer) // Icon for Grid
        }
        eventAdapter.setLayoutMode(isGridView)
    }

    private fun setupClickListeners() {
        binding.btnToggleLayout.setOnClickListener {
            isGridView = !isGridView
            updateRecyclerViewLayout()
        }
    }

    private fun setupFilterTabs() {
        binding.tabUpcoming.setOnClickListener { updateFilterUI("upcoming") }
        binding.tabToday.setOnClickListener { updateFilterUI("today") }
        binding.tabTomorrow.setOnClickListener { updateFilterUI("tomorrow") }
        binding.tabWeekend.setOnClickListener { updateFilterUI("weekend") }
        
        updateFilterUI("upcoming")
    }

    private fun updateFilterUI(filter: String) {
        resetTabStyle(binding.tabUpcoming)
        resetTabStyle(binding.tabToday)
        resetTabStyle(binding.tabTomorrow)
        resetTabStyle(binding.tabWeekend)

        when (filter) {
            "upcoming" -> {
                setActiveTabStyle(binding.tabUpcoming)
                filterEvents("upcoming")
            }
            "today" -> {
                setActiveTabStyle(binding.tabToday)
                filterEvents("today")
            }
            "tomorrow" -> {
                setActiveTabStyle(binding.tabTomorrow)
                filterEvents("tomorrow")
            }
            "weekend" -> {
                setActiveTabStyle(binding.tabWeekend)
                filterEvents("weekend")
            }
        }
    }

    private fun resetTabStyle(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary))
        textView.background = null
    }

    private fun setActiveTabStyle(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.setBackgroundResource(R.drawable.bg_button)
    }

    private fun filterEvents(filter: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        
        val filtered = when (filter) {
            "today" -> allEventsList.filter { it.date == todayStr }
            "tomorrow" -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val tomorrowStr = sdf.format(cal.time)
                allEventsList.filter { it.date == tomorrowStr }
            }
            "weekend" -> {
                allEventsList.filter { 
                    val cal = Calendar.getInstance()
                    val date = try { sdf.parse(it.date) } catch (e: Exception) { null }
                    if (date != null) {
                        cal.time = date
                        val day = cal.get(Calendar.DAY_OF_WEEK)
                        day == Calendar.SATURDAY || day == Calendar.SUNDAY
                    } else false
                }
            }
            else -> allEventsList
        }
        eventAdapter.updateData(filtered)
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)
        navbarBinding.navHome.setColorFilter(getColor(R.color.colorOrange))
        
        navbarBinding.navChat.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
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

    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.allEvents.collect { events ->
                if (events.isNotEmpty()) {
                    allEventsList = events
                    updateFilterUI("upcoming")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        setupUserGreeting()
    }
}
