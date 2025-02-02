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
    var items: Map<String, MenuItem> = emptyMap()  // ✅ Alterado para Map
) : Parcelable {
    constructor() : this("", "", "", "", emptyMap())  // ✅ Construtor atualizado
}
