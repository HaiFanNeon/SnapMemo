package com.example.snapmemo.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.snapmemo.MainActivity
import com.example.snapmemo.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val serverUrl = binding.etServerUrl.text?.toString()?.trim() ?: ""
            val username = binding.etUsername.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""

            if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.signIn(serverUrl, username, password)
        }

        binding.btnOfflineMode.setOnClickListener {
            viewModel.useOfflineMode()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = !state.isLoading
                binding.btnOfflineMode.isEnabled = !state.isLoading

                state.errorMessage?.let { msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                if (state.isSuccess) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}
