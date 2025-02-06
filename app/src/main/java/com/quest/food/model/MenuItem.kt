package com.quest.food.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Enum para definir os tipos de menu (por enquanto, só CATEGORY e ITEM)
enum class MenuType {
    CATEGORY, ITEM
}

// Classe pai genérica para todos os menus
@Parcelize
sealed class MenuItem(val type: MenuType) : Parcelable

// Modelo para categorias (Ex: Bebidas, Hambúrgueres)
@Parcelize
data class CategoryMenuItem(
    var id: String = "",
    var title: String = "",
    var subtitle: String = "",
    var imageUrl: String = "",
    var items: Map<String, MenuItem> = emptyMap() // Armazena os itens da categoria
) : MenuItem(MenuType.CATEGORY) {
    // Construtor sem argumentos para o Firebase
    constructor() : this("", "", "", "", emptyMap())
}

// Modelo para itens do menu (Ex: Coca-Cola, X-Burger)
@Parcelize
data class FoodMenuItem(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var oldPrice: Double = 0.0,
    var imageUrl: String = "",
    var isPromo: Boolean = false,
    var isBestSeller: Boolean = false
) : MenuItem(MenuType.ITEM) {
    constructor() : this("", "", "", 0.0, 0.0, "", false, false)
}

// Classe de modelo para representar os itens do menu do perfil
@Parcelize
data class ProfileMenuItem(
    val title: String,       // Título do item do menu (ex: "Histórico de Pedidos")
    val iconRes: Int         // ID do recurso do ícone associado ao item
) : MenuItem(MenuType.ITEM)
