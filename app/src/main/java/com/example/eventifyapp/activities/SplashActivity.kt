package com.example.eventifyapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.eventifyapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay 2 detik pakai Coroutine
        lifecycleScope.launch {
            delay(2000)

            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            intent.putExtra("FROM_SPLASH", true)
            intent.putExtra("APP_VERSION", "1.0")
            startActivity(intent)
            finish()
        }
    }
}