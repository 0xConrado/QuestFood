package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.model.Quest
import com.quest.food.model.User

class QuestViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("quests")
    private val _quests = MutableLiveData<List<Quest>>()
    val quests: LiveData<List<Quest>> get() = _quests

    init {
        loadQuests()
    }

    fun loadQuests() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questList = mutableListOf<Quest>()
                for (questSnapshot in snapshot.children) {
                    val quest = questSnapshot.getValue(Quest::class.java)
                    quest?.let { questList.add(it) }
                }
                _quests.value = questList
            }

            override fun onCancelled(error: DatabaseError) {
                _quests.value = emptyList()
            }
        })
    }

    fun updateQuestProgress(questId: String, increment: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val questRef = database.child(questId)

        questRef.get().addOnSuccessListener { snapshot ->
            val quest = snapshot.getValue(Quest::class.java)

            if (quest != null) {
                val newProgress = (quest.currentProgress ?: 0) + increment
                val isCompleted = newProgress >= quest.quantity

                // ✅ Garantimos que o progresso não ultrapasse o máximo
                val finalProgress = if (isCompleted) quest.quantity else newProgress

                val updates = mapOf(
                    "currentProgress" to finalProgress,
                    "completed" to isCompleted // ✅ Usamos apenas "completed", removemos "isCompleted"
                )

                questRef.updateChildren(updates).addOnSuccessListener {
                    println("✅ Quest atualizada! Progresso: $finalProgress")
                }.addOnFailureListener {
                    println("❌ Erro ao atualizar a quest: ${it.message}")
                }
            }
        }
    }

    fun markQuestAsCompleted(questId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val updatedExp = it.levelProgress + 50
                userRef.child("levelProgress").setValue(updatedExp)
            }
        }

        database.child(questId).child("completed").setValue(true) // ✅ Mantemos apenas "completed"
    }
}
