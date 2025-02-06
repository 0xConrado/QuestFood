package com.quest.food.ui.quest

import android.os.Bundle
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
        val view = inflater.inflate(R.layout.fragment_quest, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewQuests)
        createQuestButton = view.findViewById(R.id.createQuestButton)

        questViewModel = ViewModelProvider(this).get(QuestViewModel::class.java)

        recyclerView.layoutManager = LinearLayoutManager(context)

        // ✅ Ajustado para atualizar o progresso corretamente e abrir o RewardPopupFragment quando necessário
        questAdapter = QuestAdapter(
            emptyList(),
            onQuestProgressUpdate = { questId, progress ->
                questViewModel.updateQuestProgress(questId, progress)
            },
            onQuestCompleted = { quest ->
                showRewardPopup(quest)
            }
        )

        recyclerView.adapter = questAdapter

        questViewModel.quests.observe(viewLifecycleOwner) { quests ->
            questAdapter.updateQuests(quests)
        }

        createQuestButton.setOnClickListener {
            val popupQuestFragment = PopupQuestFragment()
            popupQuestFragment.show(childFragmentManager, "popupQuest")
        }

        return view
    }

    private fun showRewardPopup(quest: Quest) {
        if (!isAdded || quest.id.isEmpty()) {
            return
        }

        val popup = RewardPopupFragment.newInstance(
            questId = quest.id, // ✅ Agora passamos corretamente o questId
            rewardProductId = quest.rewardProductId,
            rewardQuantity = quest.rewardQuantity,
            expGain = quest.exp,
            rewardImageUrl = quest.rewardImageUrl
        )
        popup.show(childFragmentManager, "rewardPopup")
    }
}
