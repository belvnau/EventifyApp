package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.EventAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityCategoryBinding
import com.example.eventifyapp.model.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var database: AppDatabase
    private lateinit var eventAdapter: EventAdapter

    private var activeTab: TextView? = null
    private var collectJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupToolbar()
        setupEventsRecyclerView()
        setupTabClickListeners()

        // Select "All" tab by default
        selectTab(binding.tabAll, "All")
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupEventsRecyclerView() {
        eventAdapter = EventAdapter(emptyList(), false) { event ->
            val intent = Intent(this, DetailEventActivity::class.java).apply {
                putExtra("EVENT_ID", event.id)
                putExtra("EVENT_TITLE", event.title)
                putExtra("EVENT_DATE", event.date)
                putExtra("EVENT_LOCATION", event.location)
                putExtra("EVENT_PRICE", event.price)
                putExtra("EVENT_DESCRIPTION", event.description)
                putExtra("EVENT_IMAGE", event.imageUrl)
                putExtra("EVENT_REGISTRATION_URL", event.registrationUrl)
            }
            startActivity(intent)
        }

        binding.rvCategoryEvents.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryEvents.adapter = eventAdapter
    }

    private fun setupTabClickListeners() {
        binding.tabAll.setOnClickListener { selectTab(binding.tabAll, "All") }
        binding.tabHobby.setOnClickListener { selectTab(binding.tabHobby, "Hobby") }
        binding.tabTech.setOnClickListener { selectTab(binding.tabTech, "Technology") }
        binding.tabMusic.setOnClickListener { selectTab(binding.tabMusic, "Music") }
        binding.tabFood.setOnClickListener { selectTab(binding.tabFood, "Food") }
        binding.tabHealth.setOnClickListener { selectTab(binding.tabHealth, "Health") }
        binding.tabArt.setOnClickListener { selectTab(binding.tabArt, "Art") }
        binding.tabSport.setOnClickListener { selectTab(binding.tabSport, "Sport") }
        binding.tabFun.setOnClickListener { selectTab(binding.tabFun, "Fun") }
    }

    private fun selectTab(tab: TextView, categoryName: String) {
        if (activeTab == tab) return

        // Reset old tab
        activeTab?.let { resetTabStyle(it) }

        // Set new active tab
        activeTab = tab
        setActiveTabStyle(tab)

        // Load filtered data
        loadEvents(categoryName)
    }

    private fun resetTabStyle(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary))
        textView.background = null
    }

    private fun setActiveTabStyle(textView: TextView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        textView.setBackgroundResource(R.drawable.bg_button)
    }

    private fun loadEvents(categoryName: String) {
        collectJob?.cancel()
        collectJob = lifecycleScope.launch {
            val flow = if (categoryName == "All") {
                database.eventDao().getAllEvents()
            } else {
                database.eventDao().getEventsByCategory(categoryName)
            }

            flow.collect { events ->
                eventAdapter.updateData(events)

                if (events.isEmpty()) {
                    binding.rvCategoryEvents.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvCategoryEvents.visibility = View.VISIBLE
                    binding.layoutEmptyState.visibility = View.GONE
                }
            }
        }
    }
}
