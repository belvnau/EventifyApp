package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventifyapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSignIn.setOnClickListener {
            handleSignIn()
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur lupa password akan segera tersedia.", Toast.LENGTH_SHORT).show()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun handleSignIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

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

        // Login berhasil → pindah ke MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_EMAIL", email)
            putExtra("USER_NAME", email.substringBefore("@"))
            putExtra("IS_LOGGED_IN", true)
        }
        startActivity(intent)
        finish()
    }
}