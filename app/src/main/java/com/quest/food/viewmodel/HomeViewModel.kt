package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.User
import com.quest.food.model.CategoryMenuItem

class HomeViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("categories")

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> get() = _isAdmin

    private val _categories = MutableLiveData<List<CategoryMenuItem>>()
    val categories: LiveData<List<CategoryMenuItem>> get() = _categories

    init {
        loadUserData()
        loadCategories()
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        val userDatabase = FirebaseDatabase.getInstance().getReference("users")
        userDatabase.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                _user.value = user
                _isAdmin.value = user?.role == "admin"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadCategories() {
        database.get().addOnSuccessListener { snapshot ->
            val categoriesList = mutableListOf<CategoryMenuItem>()
            for (child in snapshot.children) {
                val category = child.getValue(CategoryMenuItem::class.java)
                category?.id = child.key ?: "" // Atribui o ID da chave do Firebase
                if (category != null) categoriesList.add(category)
            }
            _categories.value = categoriesList
        }.addOnFailureListener {
            _categories.value = emptyList()
        }
    }

    fun addCategory(category: CategoryMenuItem) {
        val newCategoryRef = database.push()
        category.id = newCategoryRef.key ?: ""  // Atribui o ID gerado pelo Firebase

        newCategoryRef.setValue(category).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadCategories()
            }
        }
    }

    fun updateCategory(category: CategoryMenuItem) {
        if (category.id.isNotEmpty()) {
            database.child(category.id).setValue(category)
        }
    }

    fun deleteCategory(category: CategoryMenuItem) {
        if (category.id.isNotEmpty()) {
            database.child(category.id).removeValue()
        }
    }
}
