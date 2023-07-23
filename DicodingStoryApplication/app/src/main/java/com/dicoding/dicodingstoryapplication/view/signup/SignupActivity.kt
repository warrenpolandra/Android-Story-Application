package com.dicoding.dicodingstoryapplication.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstoryapplication.databinding.ActivitySignupBinding
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.dicoding.dicodingstoryapplication.model.UserRegisterModel
import com.dicoding.dicodingstoryapplication.view.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var signUpViewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()
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

    // ViewModel setup
    private fun setupViewModel() {
        signUpViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[SignUpViewModel::class.java]

        signUpViewModel.finishActivity.observe(this) {
            finish()
        }

        signUpViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    // Button setup
    private fun setupAction() {
        binding.btnSignup.setOnClickListener {
            val name = binding.edRegisterName
            val email = binding.edRegisterEmail
            val password = binding.edRegisterPassword

            if (name.text.toString().isEmpty())
                name.error =  "Enter your name!"

            if (email.text.toString().isEmpty())
                email.error = "Enter your email!"

            if (password.text.toString().isEmpty()){
                password.error = "Enter your password!"
            }

            if (name.error == null && email.error == null && password.error == null) {
                val user = UserRegisterModel(name.text.toString(), email.text.toString(), password.text.toString())
                signUpViewModel.signupUser(user, this)
            }
        }
    }

    // Show loading status
    private fun showLoading(isLoading: Boolean) {
        val nameLayout = binding.editTextLayoutName
        val emailLayout = binding.editTextLayoutEmail
        val passwordLayout = binding.editTextLayoutPassword
        val progressBar = binding.progressBar
        val signupButton = binding.btnSignup

        if (isLoading) {
            nameLayout.isEnabled = false
            emailLayout.isEnabled = false
            passwordLayout.isEnabled = false
            signupButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
        } else {
            nameLayout.isEnabled = true
            emailLayout.isEnabled = true
            passwordLayout.isEnabled = true
            signupButton.isEnabled = true
            progressBar.visibility = View.GONE
        }
    }

    // Play animation
    private fun playAnimation() {
        val title = createAnimation(binding.tvTitle)
        val desc = createAnimation(binding.tvDescription)
        val nameTitle = createAnimation(binding.tvNameTitle)
        val nameLayout = createAnimation(binding.editTextLayoutName)
        val nameEdit = createAnimation(binding.edRegisterName)
        val emailTitle = createAnimation(binding.tvEmailTitle)
        val emailLayout = createAnimation(binding.editTextLayoutEmail)
            val emailEdit = createAnimation(binding.edRegisterEmail)
        val passTitle = createAnimation(binding.tvPasswordTitle)
        val passLayout = createAnimation(binding.editTextLayoutPassword)
        val passEdit = createAnimation(binding.edRegisterPassword)
        val button = createAnimation(binding.btnSignup)

        val name = AnimatorSet().apply {
            playTogether(nameTitle, nameLayout, nameEdit)
        }

        val email = AnimatorSet().apply {
            playTogether(emailTitle, emailLayout, emailEdit)
        }

        val password = AnimatorSet().apply {
            playTogether(passTitle, passLayout, passEdit)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, name, email, password, button)
            startDelay = DELAY_DURATION
            start()
        }
    }

    // Create animation
    private fun createAnimation(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1f).setDuration(DELAY_DURATION)
    }

    companion object {
        private const val DELAY_DURATION: Long = 500
    }
}