package com.quest.food.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.quest.food.model.Quest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuestViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().getReference("quests")

    private val _quests = MutableLiveData<List<Quest>>()
    val quests: LiveData<List<Quest>> get() = _quests

    init {
        loadQuests()
    }

    fun loadQuests() {
        database.get().addOnSuccessListener { snapshot ->
            val questList = snapshot.children.mapNotNull { it.getValue(Quest::class.java) }
            _quests.value = questList
        }.addOnFailureListener {
            _quests.value = emptyList() // Caso haja erro
        }
    }

    // Adicionar nova missão
    fun addQuest(quest: Quest) {
        val questRef = database.push()
        quest.id = questRef.key ?: ""
        questRef.setValue(quest).addOnCompleteListener {
            loadQuests()  // Atualiza a lista de missões após salvar
        }
    }
}



