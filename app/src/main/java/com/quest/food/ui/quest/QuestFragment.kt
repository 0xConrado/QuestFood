package com.quest.food.ui.quest

import PopupQuestFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.adapter.QuestAdapter
import com.quest.food.viewmodel.QuestViewModel

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

        // Definir layout manager para o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Instanciar o adaptador e associar à RecyclerView
        questAdapter = QuestAdapter()
        recyclerView.adapter = questAdapter

        // Observar a lista de missões
        questViewModel.quests.observe(viewLifecycleOwner, { quests ->
            questAdapter.submitList(quests)
        })

//        // Verificar se o usuário é admin e mostrar o botão
//        questViewModel.isAdmin.observe(viewLifecycleOwner, { isAdmin ->
//            createQuestButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
//        })

        // Configurar clique para abrir o Popup de criar quest
        createQuestButton.setOnClickListener {
            val popupQuestFragment = PopupQuestFragment()
            popupQuestFragment.show(childFragmentManager, "popupQuest")
        }

        return binding
    }
}
