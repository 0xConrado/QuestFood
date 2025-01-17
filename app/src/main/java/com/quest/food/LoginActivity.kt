package com.quest.food

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.quest.food.ui.popup.PopupRegisterFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Configurar o Login com o Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Obtido do Firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Registrar o launcher para o login com Google
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Log.w("GoogleSignIn", "Google sign in failed", e)
                    Toast.makeText(this, "Erro ao fazer login com Google.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Login com Google cancelado.", Toast.LENGTH_SHORT).show()
            }
        }

        // Referências aos campos no layout
        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val loginButton: Button = findViewById(R.id.buttonLogin)
        val googleLoginButton: Button = findViewById(R.id.buttonLoginGoogle)
        val guestLoginButton: Button = findViewById(R.id.buttonLoginAnonimo)
        val registerTextView: TextView = findViewById(R.id.textViewRegistrar)
        val forgotPasswordTextView: TextView = findViewById(R.id.textViewEsqueceuSenha)

        // Botão de login com email/senha
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                loginWithEmailAndPassword(email, password)
            }
        }

        // Botão de login com Google
        googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        // Botão de login anônimo
        guestLoginButton.setOnClickListener {
            loginAnonymously()
        }

        // Link para criar uma nova conta
        registerTextView.setOnClickListener {
            val registerPopup = PopupRegisterFragment()
            registerPopup.show(supportFragmentManager, "RegisterPopup")
        }

        // Link para recuperar a senha
        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Recuperação de senha não está implementada ainda.", Toast.LENGTH_SHORT).show()
        }
    }

    // Login com Google
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent) // Usando o launcher atualizado
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Log dos dados do usuário autenticado
                    Log.d("FirebaseAuth", "Usuário logado: ${user?.displayName}")
                    Log.d("FirebaseAuth", "Email: ${user?.email}")
                    Log.d("FirebaseAuth", "Foto do perfil: ${user?.photoUrl}")

                    // Exemplo de exibição dos dados
                    Toast.makeText(
                        this,
                        "Bem-vindo, ${user?.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateToHome()
                } else {
                    Log.e("FirebaseAuth", "Erro ao autenticar com Firebase: ${task.exception}")
                    Toast.makeText(this, "Erro ao autenticar com Google.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Valida os campos de entrada
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            showError("Por favor, insira seu email.")
            return false
        }
        if (password.isEmpty()) {
            showError("Por favor, insira sua senha.")
            return false
        }
        return true
    }

    // Realiza login com email e senha
    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    showError("Falha no login: ${task.exception?.message}")
                }
            }
    }

    // Realiza login anônimo
    private fun loginAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logado como visitante.", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    showError("Falha no login anônimo: ${task.exception?.message}")
                }
            }
    }

    // Mostra uma mensagem de erro
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Navega para a tela principal após o login
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}