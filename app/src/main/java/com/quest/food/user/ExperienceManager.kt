package com.quest.food.user

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object ExperienceManager {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    /**
     * Adiciona experiência ao usuário e verifica se ele sobe de nível.
     */
    fun addExperience(userId: String, xpGained: Int, onUpdate: (Int, Int) -> Unit) {
        val userRef = database.child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) return@addOnSuccessListener

            var level = snapshot.child("level").getValue(Int::class.java) ?: 1
            var levelProgress = snapshot.child("levelProgress").getValue(Int::class.java) ?: 0

            levelProgress += xpGained
            val xpRequired = level * 100

            while (levelProgress >= xpRequired) {
                levelProgress -= xpRequired
                level++
            }

            val updates = mapOf(
                "level" to level,
                "levelProgress" to levelProgress
            )

            userRef.updateChildren(updates).addOnSuccessListener {
                onUpdate(level, levelProgress)
            }
        }
    }
}
