package com.quest.food.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.quest.food.R

class ExploreFragment : Fragment() {

    private lateinit var headerTitleText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o título do cabeçalho
        headerTitleText = view.findViewById(R.id.title_text)

        // Define o título do Header a partir do nav_graph
        val navController = findNavController()
        val currentDestination = navController.currentDestination
        headerTitleText.text = currentDestination?.label ?: "Default Title"
    }
}