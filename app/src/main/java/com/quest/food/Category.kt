package com.quest.food

import android.os.Parcelable
import com.quest.food.model.MenuItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var imageUrl: String = "",
    var items: List<MenuItem> = emptyList()
) : Parcelable {
    constructor() : this("", "", "", "", emptyList()) // 🔥 Adicionamos um construtor sem argumentos
}
