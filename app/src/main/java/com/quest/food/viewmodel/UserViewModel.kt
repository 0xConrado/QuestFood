package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.Address
import com.quest.food.model.User

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _address = MutableLiveData<Address?>()
    val address: LiveData<Address?> get() = _address

    init {
        loadUserData()
    }

    fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val userData = snapshot.getValue(User::class.java)?.copy(id = userId) // ✅ Corrigido aqui
            val addressData = snapshot.child("address").getValue(Address::class.java)

            _user.value = userData
            _address.value = addressData

            Log.d("UserViewModel", "Usuário carregado: ${userData?.username}, Endereço: ${addressData?.street}")
        }.addOnFailureListener { error ->
            Log.e("UserViewModel", "Erro ao carregar dados: ${error.message}")
        }
    }
}
