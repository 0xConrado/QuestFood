package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.Order
import com.google.firebase.database.DatabaseError

class OrderViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("orders")
    private val auth = FirebaseAuth.getInstance()

    private val _userOrders = MutableLiveData<List<Order>>()
    val userOrders: LiveData<List<Order>> get() = _userOrders

    private val _allOrders = MutableLiveData<List<Order>>()
    val allOrders: LiveData<List<Order>> get() = _allOrders

    // Função para carregar os pedidos do usuário
    fun loadUserOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("OrderViewModel", "Usuário não autenticado")
            _userOrders.value = emptyList()
            return
        }

        Log.d("OrderViewModel", "Buscando pedidos para o userId: $userId")

        database.orderByChild("userId").equalTo(userId).get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                Log.d("OrderViewModel", "Pedidos carregados: $orders")
                _userOrders.value = orders
            }
            .addOnFailureListener { exception ->
                Log.e("OrderViewModel", "Erro ao carregar pedidos", exception)
                _userOrders.value = emptyList()
            }
    }

    // Função para carregar todos os pedidos (não filtrados)
    fun loadAllOrders() {
        database.get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.children.mapNotNull { it.getValue(Order::class.java) }
                _allOrders.value = orders
            }
            .addOnFailureListener { exception ->
                _allOrders.value = emptyList()
                exception.printStackTrace()
            }
    }

    fun loadSpecificOrder(orderId: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child(orderId).get().addOnSuccessListener { snapshot ->
            val order = snapshot.getValue(Order::class.java)
            if (order?.userId == userId) {
                _userOrders.value = listOf(order)
            }
        }
    }

    // Função para atualizar o status do pedido
    fun updateOrderStatus(orderId: String, newStatus: String) {
        database.child(orderId).child("status").setValue(newStatus)
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Função para excluir um pedido
    fun deleteOrder(orderId: String) {
        database.child(orderId).removeValue()
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Função para atualizar a avaliação de um pedido
    fun updateOrderRating(orderId: String, rating: Int) {
        database.child(orderId).child("rating").setValue(rating)
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Função para atualizar a contestação de um pedido
    fun updateOrderDispute(orderId: String, dispute: String) {
        database.child(orderId).child("dispute").setValue(dispute)
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    // Função para limpar o carrinho do usuário
    fun clearUserCart(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cartDatabase = FirebaseDatabase.getInstance().getReference("cart").child(userId)
        cartDatabase.removeValue().addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
}
