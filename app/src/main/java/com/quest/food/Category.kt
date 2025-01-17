// Classe Category.kt
package com.quest.food

import java.io.Serializable

// Representa uma categoria com título, subtítulo e URL da imagem
data class Category(
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = ""
) : Serializable
