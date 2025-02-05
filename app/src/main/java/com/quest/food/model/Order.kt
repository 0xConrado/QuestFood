package com.quest.food.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val userName: String = "", // Nome do usuário
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Aguardando Aprovação",
    val paymentMethod: String = "",
    val deliveryOption: String = "",
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryFee: Double = 0.0 // Novo campo para taxa de entrega
)
