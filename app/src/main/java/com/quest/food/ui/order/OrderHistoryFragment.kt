package com.quest.food.ui.order

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.R
import com.quest.food.adapter.OrderHistoryAdapter
import com.quest.food.databinding.FragmentOrderHistoryBinding
import com.quest.food.viewmodel.OrderViewModel
import com.quest.food.model.Order

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderHistoryAdapter(mutableListOf(),
            onRateClick = { showRatingDialog(it.id) },
            onDisputeClick = { showDisputeDialog(it.id) }
        )

        binding.recyclerViewOrderHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOrderHistory.adapter = adapter

        val orderId = arguments?.getString("orderId")

        if (orderId.isNullOrEmpty()) {
            refreshUserOrders()
        } else {
            loadSpecificOrder(orderId)
        }
    }

    private fun refreshUserOrders() {
        orderViewModel.loadUserOrders()
        observeUserOrders()
    }

    private fun loadSpecificOrder(orderId: String) {
        orderViewModel.loadSpecificOrder(orderId)
        observeUserOrders()
    }

    private fun observeUserOrders() {
        orderViewModel.userOrders.observe(viewLifecycleOwner) { orders ->
            Log.d("OrderHistoryFragment", "Pedidos observados: $orders")
            if (orders.isEmpty()) {
                Log.d("OrderHistoryFragment", "Nenhum pedido encontrado.")
            }
            adapter.updateOrders(orders)
        }
    }

    private fun showRatingDialog(orderId: String) {
        // Lógica para mostrar o diálogo de avaliação
    }

    private fun showDisputeDialog(orderId: String) {
        // Lógica para mostrar o diálogo de contestação
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
