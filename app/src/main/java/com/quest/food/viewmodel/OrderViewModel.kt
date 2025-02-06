package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.model.Order
import com.quest.food.model.Quest

class OrderViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    private val questDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("quests")

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> get() = _orders

    private val _allOrders = MutableLiveData<List<Order>>()
    val allOrders: LiveData<List<Order>> get() = _allOrders

    // ✅ Conjunto para armazenar pedidos já processados e evitar duplicação
    private val processedOrders = mutableSetOf<String>()

    init {
        loadOrders()
        monitorCompletedOrders()
    }

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

    private fun monitorCompletedOrders() {
        database.orderByChild("status").equalTo("Concluído")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        order?.let {
                            if (!processedOrders.contains(it.id)) {
                                println("🔍 Pedido concluído detectado: ${it.id}")
                                processedOrders.add(it.id) // ✅ Marca como processado para evitar repetição
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun completeOrder(orderId: String) {
        if (processedOrders.contains(orderId)) {
            println("⚠️ Pedido $orderId já foi processado. Ignorando duplicação.")
            return
        }

        database.child(orderId).get().addOnSuccessListener { snapshot ->
            val order = snapshot.getValue(Order::class.java)
            if (order != null) {
                println("✅ Processando pedido ${order.id} como concluído.")
                processCompletedOrder(order)
                processedOrders.add(order.id) // ✅ Marca como processado para evitar repetição
            }
        }.addOnFailureListener {
            println("❌ Erro ao concluir pedido!")
        }
    }

    private fun processCompletedOrder(order: Order) {
        val userId = order.userId
        val purchasedItems = order.items

        questDatabase.get().addOnSuccessListener { snapshot ->
            for (questSnapshot in snapshot.children) {
                val quest = questSnapshot.getValue(Quest::class.java)
                if (quest != null && !quest.isCompleted) {
                    val matchingItemsCount = purchasedItems
                        .filter { it.categoryId == quest.rewardCategoryId }
                        .sumOf { it.quantity.toInt() } // ✅ Soma correta

                    if (matchingItemsCount > 0) {
                        println("🔄 Atualizando missão ${quest.id} para usuário $userId com +$matchingItemsCount pontos.")
                        updateQuestProgress(questSnapshot.key!!, userId, matchingItemsCount, quest.quantity)
                    }
                }
            }
        }
    }

    private fun updateQuestProgress(questId: String, userId: String, increment: Int, maxProgress: Int) {
        val questRef = questDatabase.child(questId)

        questRef.get().addOnSuccessListener { snapshot ->
            val quest = snapshot.getValue(Quest::class.java)
            if (quest != null) {
                val newProgress = (quest.currentProgress ?: 0) + increment
                val isCompleted = newProgress >= maxProgress

                val updates = mapOf(
                    "currentProgress" to newProgress,
                    "isCompleted" to isCompleted
                )

                questRef.updateChildren(updates).addOnSuccessListener {
                    println("✅ Progresso atualizado corretamente: $newProgress / $maxProgress")
                }.addOnFailureListener {
                    println("❌ Erro ao atualizar a quest: ${it.message}")
                }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        database.child(orderId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                println("📌 Pedido $orderId atualizado para status: $newStatus")
                if (newStatus == "Concluído") {
                    completeOrder(orderId) // ✅ Só chamamos `completeOrder()` uma vez
                }
                loadAllOrders()
            }
    }

    fun clearUserCart(userId: String, onSuccess: (() -> Unit)? = null, onFailure: (() -> Unit)? = null) {
        val cartRef = FirebaseDatabase.getInstance().getReference("cart").child(userId)
        cartRef.removeValue()
            .addOnSuccessListener { onSuccess?.invoke() }
            .addOnFailureListener { onFailure?.invoke() }
    }

    fun deleteOrder(orderId: String) {
        database.child(orderId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadAllOrders()
                }
            }
    }

    fun filterOrders(query: String) {
        val currentOrders = _allOrders.value ?: emptyList()
        val filteredOrders = if (query.isEmpty()) {
            currentOrders
        } else {
            currentOrders.filter { order ->
                order.id.contains(query, ignoreCase = true) ||
                        order.status.contains(query, ignoreCase = true)
            }
        }
        _allOrders.value = filteredOrders
    }

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
}
