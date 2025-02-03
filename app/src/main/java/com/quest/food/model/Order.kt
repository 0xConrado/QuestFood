package com.quest.food.model

data class Order(
    var id: String = "",              // ID do pedido gerado pelo Firebase
    val userId: String = "",          // ID do usuário (UID do Firebase)
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,          // Valor total do pedido
    val status: String = "Aguardando Aprovação", // Status inicial do pedido
    val paymentMethod: String = "",   // Metodo de pagamento (Dinheiro, Cartão, etc.)
    val deliveryOption: String = "",  // Delivery ou Retirada
    val observation: String = "",     // Observação opcional do pedido
    val timestamp: Long = System.currentTimeMillis(), // Data/hora do pedido ✅ corrigido
    val rating: Int? = null,          // Avaliação do pedido (opcional)
    val dispute: String? = null       // Contestação do pedido (opcional)
)
