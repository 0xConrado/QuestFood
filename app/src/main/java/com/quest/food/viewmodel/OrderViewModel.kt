package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.model.Order

class OrderViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    private val cartDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("cart")

    // LiveData para pedidos do usuário atual
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders

    // LiveData para todos os pedidos (uso de administrador)
    private val _allOrders = MutableLiveData<List<Order>>()
    val allOrders: LiveData<List<Order>> get() = _allOrders

    init {
        loadOrders()  // Carregar pedidos do usuário ao iniciar
    }

    // Carrega os pedidos do usuário atual
    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ordersList = mutableListOf<Order>()
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        order?.let { ordersList.add(it) }
                    }
                    _orders.value = ordersList
                }

                override fun onCancelled(error: DatabaseError) {
                    _orders.value = emptyList()
                }
            })
    }

    // Carrega todos os pedidos (para administradores)
    fun loadAllOrders() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allOrdersList = mutableListOf<Order>()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    order?.let { allOrdersList.add(it) }
                }
                _allOrders.value = allOrdersList
            }

            override fun onCancelled(error: DatabaseError) {
                _allOrders.value = emptyList()
            }
        })
    }

    // Atualiza o status de um pedido
    fun updateOrderStatus(orderId: String, newStatus: String) {
        database.child(orderId).child("status").setValue(newStatus)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadAllOrders()  // Atualiza a lista de pedidos após alteração
                }
            }
    }

    // Deleta um pedido específico
    fun deleteOrder(orderId: String) {
        database.child(orderId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadAllOrders()  // Atualiza a lista após exclusão
                }
            }
    }

    // Limpa o carrinho do usuário após o checkout
    fun clearUserCart(
        userId: String,
        onSuccess: (() -> Unit)? = null,
        onFailure: (() -> Unit)? = null
    ) {
        val cartDatabase = FirebaseDatabase.getInstance().getReference("cart")
        cartDatabase.child(userId).removeValue()
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { onFailure?.invoke() }
    }

}
