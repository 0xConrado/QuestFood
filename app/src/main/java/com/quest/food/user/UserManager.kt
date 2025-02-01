package com.quest.food.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object UserManager {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Adiciona experiência ao usuário e verifica se ele sobe de nível.
     */
    fun addExperience(userId: String, xpGained: Int, onUpdate: (Int, Int, String) -> Unit) {
        ExperienceManager.addExperience(userId, xpGained) { level, progress ->
            TitleManager.updateTitle(userId, level) { newTitle ->
                onUpdate(level, progress, newTitle)
            }
        }
    }

    /**
     * Retorna o ID do usuário autenticado
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}