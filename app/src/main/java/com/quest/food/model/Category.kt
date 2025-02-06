package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var imageUrl: String = "",
    val items: Map<String, MenuItem> = emptyMap()  // A propriedade 'items' deve ser um Map
) : Parcelable {
    constructor() : this("", "", "", "", emptyMap())  // ✅ Construtor atualizado
}
