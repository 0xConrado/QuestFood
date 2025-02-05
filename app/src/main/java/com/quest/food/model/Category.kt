package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var imageUrl: String = "",
    var items: Map<String, MenuItem> = emptyMap()  // ✅ Alterado para Map
) : Parcelable {
    constructor() : this("", "", "", "", emptyMap())  // ✅ Construtor atualizado
}
