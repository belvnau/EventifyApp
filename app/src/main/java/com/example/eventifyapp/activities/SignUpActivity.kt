package com.example.eventifyapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.database.AppDatabase
import com.example.eventifyapp.databinding.ActivitySignUpBinding
import com.example.eventifyapp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            handleSignUp()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun handleSignUp() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            binding.etUsername.error = "Username tidak boleh kosong"
            binding.etUsername.requestFocus()
            return
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email tidak boleh kosong"
            binding.etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "Password tidak boleh kosong"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Password tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return
        }

        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val existingUser = withContext(Dispatchers.IO) {
                db.userDao().getUserByEmail(email)
            }
            if (existingUser != null) {
                binding.etEmail.error = "Email sudah terdaftar"
                binding.etEmail.requestFocus()
                return@launch
            }

            val currentDate = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
            val newUser = User(
                name = username,
                username = username,
                email = email,
                password = password,
                bio = "Hello! I am a new Eventify user.",
                location = "Jakarta, ID",
                joinedDate = "Joined $currentDate"
            )

            withContext(Dispatchers.IO) {
                db.userDao().insertUser(newUser)
            }

            Toast.makeText(this@SignUpActivity, "Akun berhasil dibuat! Silakan login.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}