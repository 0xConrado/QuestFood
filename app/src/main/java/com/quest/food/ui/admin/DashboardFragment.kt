package com.quest.food.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.quest.food.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botões para gerenciar diferentes áreas
        view.findViewById<Button>(R.id.manageOrdersButton).setOnClickListener {
            findNavController().navigate(R.id.manageOrdersFragment)
        }

//        view.findViewById<Button>(R.id.manageUsersButton).setOnClickListener {
//            findNavController().navigate(R.id.manageUsersFragment)
//        }

//        view.findViewById<Button>(R.id.managePromosButton).setOnClickListener {
//            findNavController().navigate(R.id.managePromosFragment)
//        }

//        view.findViewById<Button>(R.id.manageQuestsButton).setOnClickListener {
//            findNavController().navigate(R.id.manageQuestsFragment)
//        }
    }
}
