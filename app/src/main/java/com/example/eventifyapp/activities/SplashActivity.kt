package com.example.eventifyapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.R
import com.example.eventifyapp.utils.NotificationHelper
import com.example.eventifyapp.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    // Launcher untuk request permission notifikasi
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Apapun hasilnya (granted/denied), lanjut ke halaman berikutnya
        navigateToNext()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Buat notification channel dulu (wajib untuk Android 8+)
        NotificationHelper.createNotificationChannel(this)

        lifecycleScope.launch {
            delay(2000)
            checkNotificationPermission()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                // Sudah ada permission → langsung lanjut
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    navigateToNext()
                }
                // Belum ada permission → minta izin ke user
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android < 13 tidak perlu request permission runtime
            navigateToNext()
        }
    }

    private fun navigateToNext() {
        val sessionManager = SessionManager(this)
        val intent = if (sessionManager.isLoggedIn()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java).apply {
                putExtra("FROM_SPLASH", true)
                putExtra("APP_VERSION", "1.0")
            }
        }
        startActivity(intent)
        finish()
    }
}