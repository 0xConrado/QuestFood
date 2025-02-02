package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductItem(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    val originalPrice: Double? = null,
    var imageUrl: String = "",
    var isPromotion: Boolean = false,
    var isBestSeller: Boolean = false,
    var ingredients: List<String> = emptyList() // Adicionado o campo ingredients
) : Parcelable

