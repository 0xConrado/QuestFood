package com.quest.food

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.quest.food.viewmodel.SplashViewModel

class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Verifica autenticação e redireciona após 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (splashViewModel.isUserLoggedIn()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2000) // Delay de 2 segundos para a Splash Screen
    }
}
