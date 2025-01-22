package com.quest.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.quest.food.databinding.ActivityLoginBinding
import com.quest.food.ui.popup.PopupRegisterFragment
import com.quest.food.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar layout com ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar login com Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Observadores do ViewModel
        loginViewModel.user.observe(this, Observer { user ->
            if (user != null) navigateToMain()
        })

        loginViewModel.errorMessage.observe(this, Observer { message ->
            message?.let { showToast(it) }
        })

        loginViewModel.loading.observe(this, Observer { isLoading ->
            binding.loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        // Configurar botões
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            loginViewModel.loginWithEmail(email, password)
        }

        binding.buttonLoginGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.buttonLoginAnonimo.setOnClickListener {
            loginViewModel.loginAnonymously()
        }

        binding.textViewRegistrar.setOnClickListener {
            val registerPopup = PopupRegisterFragment()
            registerPopup.show(supportFragmentManager, "RegisterPopup")
        }

        binding.textViewEsqueceuSenha.setOnClickListener {
            showToast("Recuperação de senha ainda não implementada.")
        }
    }

    // Launcher para iniciar o fluxo de Login com o Google (substitui startActivityForResult)
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                loginViewModel.loginWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google Sign-In falhou", e)
                showToast("Falha no login: ${e.message}")
            }
        } else {
            showToast("Login cancelado")
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
