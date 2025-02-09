package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.quest.food.model.CategoryMenuItem

class CategoryViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("categories")

    private val _categories = MutableLiveData<List<CategoryMenuItem>>()
    val categories: LiveData<List<CategoryMenuItem>> get() = _categories

    init {
        loadCategories()
    }

    fun loadCategories() {
        database.get().addOnSuccessListener { snapshot ->
            val categoriesList = mutableListOf<CategoryMenuItem>()
            for (child in snapshot.children) {
                val category = child.getValue(CategoryMenuItem::class.java)
                category?.id = child.key ?: ""
                if (category != null) categoriesList.add(category)
            }
            _categories.value = categoriesList
        }.addOnFailureListener {
            Log.e("CategoryViewModel", "Erro ao carregar categorias: ${it.message}")
            _categories.value = emptyList()
        }
    }

    fun addCategory(category: CategoryMenuItem) {
        val newCategoryRef = database.push()
        category.id = newCategoryRef.key ?: ""

        newCategoryRef.setValue(category).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadCategories()
            }
        }
    }

    fun updateCategory(category: CategoryMenuItem) {
        if (category.id.isNotEmpty()) {
            val updates = mapOf(
                "title" to category.title,
                "subtitle" to category.subtitle,
                "imageUrl" to category.imageUrl
            )

            database.child(category.id).updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        loadCategories()
                    }
                }
                .addOnFailureListener {
                    Log.e("CategoryViewModel", "Erro ao atualizar categoria: ${it.message}")
                }
        }
    }

    // Função para verificar se o usuário é admin
    fun isAdmin(userRole: String?): Boolean {
        return userRole?.equals("admin", ignoreCase = true) == true
    }

    fun deleteCategory(category: CategoryMenuItem) {
        if (category.id.isNotEmpty()) {
            database.child(category.id).removeValue().addOnCompleteListener {
                loadCategories() // Recarregar após deletar
            }
        }
    }
}
