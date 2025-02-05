package com.quest.food.model

data class Quest(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var quantity: Int = 0,
    var exp: Int = 0,
    var rewardCategoryId: String = "",
    var rewardProductId: String = "",
    var rewardQuantity: Int = 0,
    var rewardImageUrl: String = "",
    var imageUrl: String = "",
    var isCompleted: Boolean = false
)
