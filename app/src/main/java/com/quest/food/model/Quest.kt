package com.quest.food.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Quest(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var quantity: Int = 0,
    var exp: Int = 0,
    var rewardCategoryId: String = "", // ✅ Categoria que precisa ser comprada
    var rewardProductId: String = "",
    var rewardQuantity: Int = 0,
    var rewardImageUrl: String = "",
    var imageUrl: String = "",
    var currentProgress: Int = 0, // ✅ Adiciona a contagem de progresso
    var isCompleted: Boolean = false
) {
    constructor() : this("", "", "", 0, 0, "", "", 0, "", "", 0, false) // ✅ Construtor vazio para Firebase
}
