package com.quest.food.user

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object TitleManager {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    // Mapeia níveis para títulos desbloqueáveis
    private val levelTitles = mapOf(
        1 to "Novato",
        5 to "Aventureiro",
        10 to "Explorador",
        15 to "Mestre da Jornada",
        20 to "Lenda Viva"
    )

    /**
     * Atualiza o título do usuário baseado no nível atual.
     */
    fun updateTitle(userId: String, level: Int, onUpdate: (String) -> Unit) {
        val userRef = database.child(userId)

        val newTitle = levelTitles.entries.lastOrNull { it.key <= level }?.value ?: "Desconhecido"

        userRef.child("title").setValue(newTitle).addOnSuccessListener {
            onUpdate(newTitle) // Atualiza o título na UI
        }
    }
}
