package com.quest.food.model

// Classe de modelo para representar os itens do menu do perfil
data class ProfileMenuItem(
    val title: String,       // Título do item do menu (ex: "Histórico de Pedidos")
    val iconRes: Int         // ID do recurso do ícone associado ao item
)
