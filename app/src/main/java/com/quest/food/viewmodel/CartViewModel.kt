package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.CartItem

class CartViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("cart")
    private val auth = FirebaseAuth.getInstance()
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val items = mutableListOf<CartItem>()
            snapshot.children.forEach { child ->
                val item = child.getValue(CartItem::class.java)
                item?.let { items.add(it) }
            }
            _cartItems.value = items
        }
    }

    fun addToCart(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        val newItemRef = database.child(userId).push()
        item.id = newItemRef.key ?: ""
        newItemRef.setValue(item).addOnSuccessListener { loadCartItems() }
    }

    fun updateCartItem(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child(item.id).setValue(item).addOnSuccessListener { loadCartItems() }
    }

    fun removeFromCart(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child(item.id).removeValue().addOnSuccessListener { loadCartItems() }
    }

    fun clearCart(onSuccess: () -> Unit, onFailure: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).removeValue().addOnSuccessListener {
            _cartItems.value = emptyList()
            onSuccess()
        }.addOnFailureListener {
            onFailure()
        }
    }
}