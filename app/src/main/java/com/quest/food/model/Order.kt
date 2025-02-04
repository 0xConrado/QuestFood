package com.quest.food.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "", // Adicionado o nome do usuário
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Aguardando Aprovação",
    val paymentMethod: String = "",
    val deliveryOption: String = "",
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

