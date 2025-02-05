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

            // Adiciona o XP ganho ao progresso atual
            levelProgress += xpGained

            // Calcula o XP necessário para o próximo nível
            var xpRequired = level * 100  // Cada nível exige 100 XP para subir

            // Se o progresso ultrapassar o necessário, o nível sobe e o progresso é recalculado
            while (levelProgress >= xpRequired) {
                levelProgress -= xpRequired  // Subtrai o XP necessário para o próximo nível
                level++  // O usuário sobe de nível
                xpRequired = level * 100  // Atualiza o XP necessário para o próximo nível
            }

            // Atualiza os dados do usuário no Firebase
            val updates = mapOf(
                "level" to level,
                "levelProgress" to levelProgress
            )

            userRef.updateChildren(updates).addOnSuccessListener {
                onUpdate(level, levelProgress)  // Atualiza a interface com o novo nível e progresso
            }
        }
    }
}
