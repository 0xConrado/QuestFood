package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loginWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Por favor, preencha todos os campos."
            return
        }

        _loading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _loading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    fun loginAnonymously() {
        _loading.value = true
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                _loading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                } else {
                    handleAuthError(task.exception)
                }
            }
    }

    fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _loading.value = true

        val currentUser = auth.currentUser
        if (currentUser != null) {
            // O usuário já está logado, vincular a conta do Google ao login existente
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        _user.value = auth.currentUser
                        Log.d("LoginViewModel", "Conta do Google vinculada com sucesso!")
                    } else {
                        _errorMessage.value = "Erro ao vincular conta do Google: ${task.exception?.message}"
                    }
                }
        } else {
            // Usuário não logado, realizar login normal com Google
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    _loading.value = false
                    if (task.isSuccessful) {
                        _user.value = auth.currentUser
                    } else {
                        _errorMessage.value = "Erro no login com Google: ${task.exception?.message}"
                    }
                }
        }
    }

    fun logOut() {
        auth.signOut()
        _user.value = null
    }

    private fun handleAuthError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "E-mail ou senha incorretos!"
            exception?.message?.contains("The password is invalid") == true -> "Senha incorreta!"
            exception?.message?.contains("There is no user record") == true -> "Usuário não encontrado!"
            exception?.message?.contains("A network error") == true -> "Erro de conexão! Verifique sua internet."
            else -> "Erro ao autenticar. Tente novamente."
        }
        _errorMessage.value = errorMessage
    }
}
