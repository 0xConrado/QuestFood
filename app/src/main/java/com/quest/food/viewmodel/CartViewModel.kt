package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.CartItem

class CartViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("cart")
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        database.get().addOnSuccessListener { snapshot ->
            val items = mutableListOf<CartItem>()
            snapshot.children.forEach { child ->
                val item = child.getValue(CartItem::class.java)
                item?.let { items.add(it) }
            }
            _cartItems.value = items
        }
    }

    fun addToCart(item: CartItem) {
        val newItemRef = database.push()
        item.id = newItemRef.key ?: ""
        newItemRef.setValue(item)
        loadCartItems() // Atualiza o carrinho
    }

    fun updateCartItem(item: CartItem) {
        database.child(item.id).setValue(item)
        loadCartItems()
    }

    fun removeFromCart(item: CartItem) {
        database.child(item.id).removeValue()
        loadCartItems()
    }
}
