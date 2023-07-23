package com.dicoding.dicodingstoryapplication.view.authentication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.dicoding.dicodingstoryapplication.databinding.ActivityAuthenticationBinding
import com.dicoding.dicodingstoryapplication.view.login.LoginActivity
import com.dicoding.dicodingstoryapplication.view.signup.SignupActivity

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        playAnimation()
        setupAction()
    }

    // Play logo animation
    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.ivPageIcon, "rotation", 0f, 360f).apply {
            duration = ROTATE_DURATION
            repeatCount = ObjectAnimator.INFINITE
        }.start()

        val title = createAnimation(binding.tvAppTitle)
        val desc = createAnimation(binding.pageDescription)
        val btnLogin = createAnimation(binding.btnLogin)
        val btnSignup = createAnimation(binding.btnSignup)

        val button = AnimatorSet().apply {
            playTogether(btnLogin, btnSignup)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, button)
            startDelay = DELAY_DURATION
            start()
        }
    }

    // Create animation
    private fun createAnimation(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1f).setDuration(DELAY_DURATION)
    }

    // Basic view setup
    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    // Button setup
    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    companion object {
        private const val ROTATE_DURATION: Long = 3000
        private const val DELAY_DURATION: Long = 500
    }
}