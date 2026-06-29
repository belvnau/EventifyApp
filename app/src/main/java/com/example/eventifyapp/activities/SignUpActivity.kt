package com.example.eventifyapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventifyapp.databinding.ActivitySignUpBinding

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
        val name = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            binding.etFullName.error = "Nama lengkap tidak boleh kosong"
            binding.etFullName.requestFocus()
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

        Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
        finish()
    }
}