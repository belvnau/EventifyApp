package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.R
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.fragments.DialogEditProfileFragment
import com.example.eventifyapp.model.User
import com.example.eventifyapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private lateinit var database: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Init database & session
        database = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        // Find views
        setupViews()

        // Load user data
        loadUserProfile()

        // Setup button listeners
        setupClickListeners()
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
                // Update UI langsung
                tvProfileName.text = username
                tvProfileBio.text = bio
                tvProfileLocation.text = location

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
}