package com.quest.food.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuração do RecyclerView
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Configuração do botão de finalizar compra
        binding.buttonCheckout.setOnClickListener {
            // Ação do botão
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
