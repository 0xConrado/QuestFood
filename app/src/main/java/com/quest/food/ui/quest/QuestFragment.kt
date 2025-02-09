package com.quest.food.ui.quest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.adapter.QuestAdapter
import com.quest.food.model.Quest
import com.quest.food.ui.popup.PopupQuestFragment
import com.quest.food.ui.popup.RewardPopupFragment
import com.quest.food.viewmodel.QuestViewModel

class QuestFragment : Fragment() {

    private lateinit var questViewModel: QuestViewModel
    private lateinit var questAdapter: QuestAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var createQuestButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializando o ViewModel
        questViewModel = ViewModelProvider(requireActivity()).get(QuestViewModel::class.java)

        // Forçar a atualização das quests
        questViewModel.loadQuests()

        val view = inflater.inflate(R.layout.fragment_quest, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewQuests)
        createQuestButton = view.findViewById(R.id.createQuestButton)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // Inicializar o adapter
        questAdapter = QuestAdapter(
            emptyList(),
            onQuestProgressUpdate = { _, _ -> }, // Ignora atualizações desnecessárias
            onQuestCompleted = { quest ->
                showRewardPopup(quest) // Exibe o RewardPopupFragment
            }
        )

        recyclerView.adapter = questAdapter

        // Observar as quests do ViewModel
        questViewModel.quests.observe(viewLifecycleOwner) { quests ->
            Log.d("QuestFragment", "Atualizando RecyclerView com ${quests.size} quests")
            questAdapter.updateQuests(quests)
        }

        // Botão para criar uma nova quest
        createQuestButton.setOnClickListener {
            val popupQuestFragment = PopupQuestFragment()
            popupQuestFragment.show(childFragmentManager, "popupQuest")
        }

        return view
    }

    private fun showRewardPopup(quest: Quest) {
        if (!isAdded || quest.id.isEmpty() || quest.rewardClaimed) {
            return
        }

        // Pegando o nome da recompensa diretamente da instância da quest
        val rewardProductName = quest.rewardProductName ?: "Produto de Recompensa"

        // Pegando o nome da categoria diretamente da instância da quest
        val rewardCategoryNames = quest.rewardCategoryNames ?: "Categoria de Recompensa Não Definida"

        val popup = RewardPopupFragment.newInstance(
            questId = quest.id,
            rewardProductId = quest.rewardProductId,
            rewardProductName = rewardProductName,
            rewardQuantity = quest.rewardQuantity,
            expGain = quest.exp.toDouble(),
            rewardImageUrl = quest.rewardImageUrl,
            rewardCategoryNames = rewardCategoryNames // Passando o nome da categoria para o fragmento
        )

        popup.show(childFragmentManager, "rewardPopup")
    }
}
