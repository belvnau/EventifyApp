package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eventifyapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("USER_NAME") ?: "Grace Larisma"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "grace@email.com"

        binding.tvName.text = userName
        binding.tvEmail.text = userEmail

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }

        binding.btnGoing.setOnClickListener {
            binding.tvStatus.text = "Daftar Event Going"
        }

        binding.btnSaved.setOnClickListener {
            binding.tvStatus.text = "Daftar Event Saved"
        }

        binding.btnPast.setOnClickListener {
            binding.tvStatus.text = "Daftar Event Past"
        }
    }
}