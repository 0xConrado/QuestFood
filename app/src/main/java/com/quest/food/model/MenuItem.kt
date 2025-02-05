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
    var imageUrl: String = ""
) : MenuItem(MenuType.CATEGORY)

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
) : MenuItem(MenuType.ITEM)

// Classe de modelo para representar os itens do menu do perfil
data class ProfileMenuItem(
    val title: String,       // Título do item do menu (ex: "Histórico de Pedidos")
    val iconRes: Int         // ID do recurso do ícone associado ao item
) : MenuItem(MenuType.ITEM)
