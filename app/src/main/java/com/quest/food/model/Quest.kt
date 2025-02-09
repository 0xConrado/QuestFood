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
    var rewardCategoryNames: String = "", // Novo campo para armazenar os nomes das categorias
    var rewardProductId: String = "",
    var rewardProductName: String = "",
    var rewardQuantity: Int = 0,
    var rewardImageUrl: String = "",
    var imageUrl: String = "",
    var currentProgress: Int = 0, // ✅ Adiciona a contagem de progresso
    var completed: Boolean = false,
    val rewardClaimed: Boolean = false // Campo para controlar se a recompensa já foi recebida
) {
    constructor() : this("", "", "", 0, 0, "", "", "", "", 0, "", "", 0, false, false) // ✅ Construtor vazio para Firebase
}
