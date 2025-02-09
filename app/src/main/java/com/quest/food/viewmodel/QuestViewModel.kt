package com.quest.food.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.quest.food.model.Category
import com.quest.food.model.FoodMenuItem
import com.quest.food.model.Quest
import com.quest.food.model.User

class QuestViewModel : ViewModel() {
    var selectedRewardProduct: FoodMenuItem? = null
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("quests")
    private val _quests = MutableLiveData<List<Quest>>()
    val quests: LiveData<List<Quest>> get() = _quests

    init {
        loadQuests()
    }

    // Função para carregar as missões do Firebase
    fun loadQuests() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questList = mutableListOf<Quest>()
                for (questSnapshot in snapshot.children) {
                    val quest = questSnapshot.getValue(Quest::class.java)
                    quest?.let {
                        Log.d("QuestViewModel", "Quest ID: ${it.id}, currentProgress: ${it.currentProgress}, quantity: ${it.quantity}, rewardClaimed: ${it.rewardClaimed}")
                        val updatedQuest = it.copy(rewardClaimed = it.rewardClaimed ?: false)
                        questList.add(updatedQuest)
                    }
                }
                _quests.value = questList
            }

            override fun onCancelled(error: DatabaseError) {
                _quests.value = emptyList()
                Log.e("QuestViewModel", "Erro ao carregar missões: ${error.message}")
            }
        })
    }

    // Função para buscar o produto pelo ID (Firestore)
    fun getProductById(productId: String, callback: (FoodMenuItem?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val product = document.toObject(FoodMenuItem::class.java)
                    callback(product)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    // Função para atualizar o progresso da missão
    fun updateQuestProgress(questId: String, increment: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val questRef = database.child(questId)

        questRef.get().addOnSuccessListener { snapshot ->
            val quest = snapshot.getValue(Quest::class.java)

            if (quest != null) {
                val newProgress = (quest.currentProgress ?: 0) + increment
                val completed = newProgress >= quest.quantity

                val finalProgress = if (completed) quest.quantity else newProgress

                val updates = mapOf(
                    "currentProgress" to finalProgress,
                    "completed" to completed
                )

                questRef.updateChildren(updates).addOnSuccessListener {
                    Log.d("QuestViewModel", "✅ Quest atualizada! Progresso: $finalProgress")
                }.addOnFailureListener {
                    Log.e("QuestViewModel", "❌ Erro ao atualizar a quest: ${it.message}")
                }
            }
        }
    }

    // Função para resgatar a recompensa da missão
    fun claimReward(questId: String, rewardProductName: String) {
        markRewardAsReceived(questId)
    }

    // Função para marcar a missão como concluída e adicionar XP ao usuário
    fun markQuestAsCompleted(questId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                val updatedExp = it.levelProgress + 50
                userRef.child("levelProgress").setValue(updatedExp).addOnSuccessListener {
                    Log.d("QuestViewModel", "✅ XP do usuário atualizado para: $updatedExp")
                }.addOnFailureListener { error ->
                    Log.e("QuestViewModel", "❌ Erro ao atualizar XP do usuário: ${error.message}")
                }
            }
        }

        database.child(questId).child("completed").setValue(true)
    }

    // Função para marcar a recompensa como recebida
    fun markRewardAsReceived(questId: String) {
        val questRef = database.child(questId)

        // Atualizar apenas o campo rewardClaimed para true
        val updates = mapOf(
            "rewardClaimed" to true
        )

        questRef.updateChildren(updates).addOnSuccessListener {
            Log.d("QuestViewModel", "✅ Recompensa marcada como recebida para a missão $questId no Firebase")

            // Atualizar a lista de quests localmente
            _quests.value = _quests.value?.map { existingQuest ->
                if (existingQuest.id == questId) {
                    Log.d("QuestViewModel", "Atualizando RewardClaimed para true na quest ID: $questId")
                    existingQuest.copy(rewardClaimed = true)
                } else {
                    existingQuest
                }
            }
        }.addOnFailureListener {
            Log.e("QuestViewModel", "❌ Erro ao marcar recompensa como recebida no Firebase: ${it.message}")
        }
    }

    // Função para buscar a categoria pelo ID
    fun getCategoryById(categoryId: String): LiveData<Category> {
        val categoryLiveData = MutableLiveData<Category>()

        // Supondo que você esteja usando Firebase para buscar a categoria
        FirebaseDatabase.getInstance().getReference("categories").child(categoryId)
            .get()
            .addOnSuccessListener { snapshot ->
                val category = snapshot.getValue(Category::class.java)
                categoryLiveData.value = category
            }
            .addOnFailureListener {
                Log.e("QuestViewModel", "Erro ao buscar categoria com ID: $categoryId")
                categoryLiveData.value = null
            }

        return categoryLiveData
    }

    // Função para buscar uma missão pelo ID
    fun getQuestById(questId: String): Quest? {
        return _quests.value?.find { it.id == questId } ?: run {
            Log.e("QuestViewModel", "❌ Missão não encontrada para o ID: $questId")
            null
        }
    }
}
