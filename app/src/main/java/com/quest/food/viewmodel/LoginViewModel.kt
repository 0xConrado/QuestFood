package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

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
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                firebaseUser?.let { user ->
                    val userRef = database.child(user.uid)

                    // Verifica se o usuário já existe no banco de dados
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                val newUser = mapOf(
                                    "username" to (user.displayName ?: "Usuário"),
                                    "email" to (user.email ?: ""),
                                    "phone" to (user.phoneNumber ?: ""),
                                    "birthday" to "",
                                    "title" to "Novato",
                                    "level" to 1,
                                    "levelProgress" to 0,
                                    "profileImagePath" to (user.photoUrl?.toString() ?: ""),
                                    "role" to "user"
                                )
                                userRef.setValue(newUser)
                            }
                            _user.postValue(user)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _errorMessage.postValue("Erro ao buscar usuário no Firebase: ${error.message}")
                        }
                    })
                }
            } else {
                _errorMessage.postValue("Falha no login com Google: ${task.exception?.message}")
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
