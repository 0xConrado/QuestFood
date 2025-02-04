package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.User

class UserViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    private val _allUsers = MutableLiveData<List<User>>()
    val allUsers: LiveData<List<User>> get() = _allUsers

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> get() = _userData

    fun loadAllUsers() {
        database.get().addOnSuccessListener { snapshot ->
            val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
            _allUsers.value = users
        }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            callback(user)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            _userData.value = user
        }.addOnFailureListener {
            _userData.value = null
        }
    }
}