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
import com.quest.food.viewmodel.QuestViewModel
import com.quest.food.ui.popup.PopupQuestFragment  // ✅ Importação corrigida

class QuestFragment : Fragment() {

    private lateinit var questViewModel: QuestViewModel
    private lateinit var questAdapter: QuestAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var createQuestButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_quest, container, false)

        recyclerView = binding.findViewById(R.id.recyclerViewQuests)
        createQuestButton = binding.findViewById(R.id.createQuestButton)

        questViewModel = ViewModelProvider(this).get(QuestViewModel::class.java)

        recyclerView.layoutManager = LinearLayoutManager(context)
        questAdapter = QuestAdapter()
        recyclerView.adapter = questAdapter

        questViewModel.quests.observe(viewLifecycleOwner) { quests ->
            questAdapter.submitList(quests)
        }

        createQuestButton.setOnClickListener {
            val popupQuestFragment = PopupQuestFragment()  // ✅ Nome corrigido
            popupQuestFragment.show(childFragmentManager, "popupQuest")
        }

        return binding
    }
}
