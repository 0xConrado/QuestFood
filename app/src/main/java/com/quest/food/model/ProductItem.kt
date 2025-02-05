package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.PropertyName

@Parcelize
data class ProductItem(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    val originalPrice: Double? = null,
    var imageUrl: String = "",
    @get:PropertyName("promotion") @set:PropertyName("promotion")  // ✅ Anotação para o Firebase
    var isPromotion: Boolean = false,
    var isBestSeller: Boolean = false,
    var ingredients: List<String> = emptyList(),
    var categoryId: String = "" // Adicionado o campo categoryId
) : Parcelable
