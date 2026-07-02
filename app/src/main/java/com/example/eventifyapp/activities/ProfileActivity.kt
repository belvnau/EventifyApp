package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.EventAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivityProfileBinding
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.example.eventifyapp.fragments.DialogEditProfileFragment
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.model.User
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.utils.SessionManager
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    private lateinit var eventViewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private var allEvents: List<Event> = emptyList()
    private var currentTab = "GOING" // default checked in xml is btnTabGoing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init database & session
        database = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        setupViewModel()
        setupRecyclerView()
        setupTabListeners()
        setupBottomNavigation()

        // Load user data
        loadUserProfile()

        // Setup button listeners
        setupClickListeners()

        // Observe favorite/all events from ViewModel
        observeEvents()
    }

    private fun setupViewModel() {
        val eventRepo = EventRepository(database.eventDao())
        val factory = ViewModelFactory(eventRepository = eventRepo)
        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
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
        binding.rvProfileEvents.layoutManager = LinearLayoutManager(this)
        binding.rvProfileEvents.adapter = eventAdapter
    }

    private fun setupTabListeners() {
        binding.toggleGroupEvents.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnTabGoing -> {
                        currentTab = "GOING"
                        filterAndDisplayEvents()
                    }
                    R.id.btnTabSaved -> {
                        currentTab = "SAVED"
                        filterAndDisplayEvents()
                    }
                    R.id.btnTabPast -> {
                        currentTab = "PAST"
                        filterAndDisplayEvents()
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            eventViewModel.allEvents.collect { events ->
                allEvents = events
                filterAndDisplayEvents()
            }
        }
    }

    private fun filterAndDisplayEvents() {
        val filteredEvents = when (currentTab) {
            "SAVED" -> allEvents.filter { it.isFavorite }
            "GOING" -> emptyList()
            "PAST" -> emptyList()
            else -> emptyList()
        }

        eventAdapter.updateData(filteredEvents)

        if (filteredEvents.isEmpty()) {
            binding.rvProfileEvents.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvProfileEvents.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
        }
    }

    private fun loadUserProfile() {
        val loggedInEmail = sessionManager.getLoggedInEmail()
        if (loggedInEmail != null) {
            lifecycleScope.launch {
                database.userDao().getUserByEmailFlow(loggedInEmail).collect { user ->
                    if (user != null) {
                        currentUser = user
                        binding.tvProfileName.text = if (user.username.isNotEmpty()) user.username else user.name
                        binding.tvProfileEmail.text = user.email
                        binding.tvProfileBio.text = user.bio
                        binding.tvProfileLocation.text = user.location
                        binding.tvProfileJoinedDate.text = user.joinedDate
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            openEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            handleLogout()
        }
    }

    private fun handleLogout() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun openEditProfileDialog() {
        val dialog = DialogEditProfileFragment()
        dialog.arguments = Bundle().apply {
            putString("name", currentUser?.name ?: "")
            putString("username", currentUser?.username ?: "")
            putString("bio", currentUser?.bio ?: "")
            putString("location", currentUser?.location ?: "")
        }
        dialog.listener = object : DialogEditProfileFragment.OnSaveListener {
            override fun onSave(name: String, username: String, bio: String, location: String) {
                // Update UI langsung
                binding.tvProfileName.text = username
                binding.tvProfileBio.text = bio
                binding.tvProfileLocation.text = location

                // Save ke database
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedUser = currentUser?.copy(
                        name = name,
                        username = username,
                        bio = bio,
                        location = location
                    )
                    if (updatedUser != null) {
                        database.userDao().updateUser(updatedUser)
                    }
                }
            }
        }
        dialog.show(supportFragmentManager, "EditProfile")
    }

    private fun setupBottomNavigation() {
        val navbarBinding = LayoutNavbarBinding.bind(binding.bottomNavbar.root)

        // Set active icon for Profile
        navbarBinding.navProfile.setColorFilter(getColor(R.color.colorOrange))
        navbarBinding.navHome.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navChat.setColorFilter(getColor(R.color.gray_text))
        navbarBinding.navNotification.setColorFilter(getColor(R.color.gray_text))

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
            val intent = Intent(this, NotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        navbarBinding.navProfile.setOnClickListener {
            // Already here
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}