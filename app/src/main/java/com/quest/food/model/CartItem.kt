package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    var id: String = "",
    var productId: String = "",
    var productName: String = "",
    var categoryName: String = "", // ✅ Nome da Categoria adicionado
    var quantity: Int = 1,
    var price: Double = 0.00,
    var selectedIngredients: List<String> = emptyList(),
    var timestamp: Long = System.currentTimeMillis(), // Para controle do tempo no carrinho
    var categoryId: String = "" // Mantido o categoryId
) : Parcelable
