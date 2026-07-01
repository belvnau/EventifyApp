package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventifyapp.R
import com.example.eventifyapp.adapters.EventAdapter
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.fragments.DialogEditProfileFragment
import com.example.eventifyapp.model.Event
import com.example.eventifyapp.model.User
import com.example.eventifyapp.repository.EventRepository
import com.example.eventifyapp.repository.NotificationRepository
import com.example.eventifyapp.utils.SessionManager
import com.example.eventifyapp.viewmodel.EventViewModel
import com.example.eventifyapp.viewmodel.NotificationViewModel
import com.example.eventifyapp.viewmodel.ViewModelFactory
import com.example.eventifyapp.databinding.LayoutNavbarBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfileBio: TextView
    private lateinit var tvProfileLocation: TextView
    private lateinit var tvProfileJoinedDate: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnBack: ImageButton
    private lateinit var ivProfileImage: ShapeableImageView
    private lateinit var btnLogout: MaterialButton
    
    private lateinit var rvProfileEvents: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var toggleGroupEvents: MaterialButtonToggleGroup

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private lateinit var eventViewModel: EventViewModel
    private lateinit var notificationViewModel: NotificationViewModel
    private lateinit var eventAdapter: EventAdapter
    
    private var currentUser: User? = null
    private var allEvents: List<Event> = emptyList()
    private var selectedTab = "going" // Default tab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        database = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        setupViewModel()
        setupViews()
        setupRecyclerView()
        setupTabListeners()
        setupClickListeners()
        setupBottomNavigation()

        loadUserProfile()
        observeEvents()
        observeNotifications()
    }

    private fun setupViewModel() {
        val eventRepo = EventRepository(database.eventDao())
        val notificationRepo = NotificationRepository(database.notificationDao())
        val factory = ViewModelFactory(
            eventRepository = eventRepo,
            notificationRepository = notificationRepo
        )
        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
        notificationViewModel = ViewModelProvider(this, factory)[NotificationViewModel::class.java]
    }

    private fun setupViews() {
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfileBio = findViewById(R.id.tvProfileBio)
        tvProfileLocation = findViewById(R.id.tvProfileLocation)
        tvProfileJoinedDate = findViewById(R.id.tvProfileJoinedDate)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnBack = findViewById(R.id.btnBack)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnLogout = findViewById(R.id.btnLogout)
        
        rvProfileEvents = findViewById(R.id.rvProfileEvents)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        toggleGroupEvents = findViewById(R.id.toggleGroupEvents)
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(emptyList(), isGridView = false) { event ->
            val intent = Intent(this, DetailEventActivity::class.java).apply {
                putExtra("EVENT_ID", event.id)
            }
            startActivity(intent)
        }
        rvProfileEvents.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = eventAdapter
        }
    }

    private fun setupTabListeners() {
        toggleGroupEvents.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnTabGoing -> selectedTab = "going"
                    R.id.btnTabSaved -> selectedTab = "saved"
                    R.id.btnTabPast -> selectedTab = "past"
                }
                filterAndDisplayEvents()
            }
        }
    }

    private fun filterAndDisplayEvents() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val filtered = when (selectedTab) {
            "going" -> allEvents.filter { it.isJoined }
            "saved" -> allEvents.filter { it.isFavorite }
            "past" -> allEvents.filter { 
                try {
                    it.date < todayStr
                } catch (e: Exception) {
                    false
                }
            }
            else -> emptyList()
        }

        eventAdapter.updateData(filtered)

        if (filtered.isEmpty()) {
            rvProfileEvents.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvProfileEvents.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun loadUserProfile() {
        val loggedInEmail = sessionManager.getLoggedInEmail()
        if (loggedInEmail != null) {
            lifecycleScope.launch {
                database.userDao().getUserByEmailFlow(loggedInEmail).collect { user ->
                    if (user != null) {
                        currentUser = user
                        tvProfileName.text = if (user.username.isNotEmpty()) user.username else user.name
                        tvProfileEmail.text = user.email
                        tvProfileBio.text = user.bio
                        tvProfileLocation.text = user.location
                        tvProfileJoinedDate.text = user.joinedDate
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            eventViewModel.allEvents.collect { list ->
                allEvents = list
                filterAndDisplayEvents()
            }
        }
    }

    private fun observeNotifications() {
        lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                val navbarBinding = LayoutNavbarBinding.bind(findViewById(R.id.bottomNavbar))
                if (count > 0) {
                    navbarBinding.tvNotificationBadge.visibility = View.VISIBLE
                    navbarBinding.tvNotificationBadge.text = count.toString()
                } else {
                    navbarBinding.tvNotificationBadge.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnEditProfile.setOnClickListener {
            openEditProfileDialog()
        }

        btnLogout.setOnClickListener {
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
                tvProfileName.text = username
                tvProfileBio.text = bio
                tvProfileLocation.text = location

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
        val navbarBinding = LayoutNavbarBinding.bind(findViewById(R.id.bottomNavbar))

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

    override fun onResume() {
        super.onResume()
        if (::notificationViewModel.isInitialized) {
            notificationViewModel.loadUnreadCount()
        }
        if (::eventViewModel.isInitialized) {
            eventViewModel.loadAllEvents()
        }
    }
}