package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.User

class UserViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("users")
    val auth = FirebaseAuth.getInstance()

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = _userData

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // ✅ Carrega todos os usuários (para Admins)
    fun loadAllUsers() {
        database.get().addOnSuccessListener { snapshot ->
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            _allUsers.value = users
        }
    }

    // ✅ Obtém dados de um usuário específico por ID
    fun getUserById(userId: String, callback: (User?) -> Unit) {
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            callback(user)
        }.addOnFailureListener {
            callback(null)
        }
    }

    // ✅ Carrega os dados do usuário logado
    fun loadUserData() {
        val userId = getCurrentUserId() ?: return
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            _userData.value = user
        }.addOnFailureListener {
            _userData.value = null
        }
    }

    // ✅ Adiciona experiência ao usuário quando ele completa uma missão
    fun addExperience(userId: String, exp: Int, onComplete: (() -> Unit)? = null) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val updatedExp = it.levelProgress + exp
                userRef.child("levelProgress").setValue(updatedExp)
                    .addOnSuccessListener {
                        println("✅ XP atualizado para: $updatedExp")
                        onComplete?.invoke()
                    }
            }
        }
    }

}
