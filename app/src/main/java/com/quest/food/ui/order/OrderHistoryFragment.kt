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

        // Configura o RecyclerView com o Adapter
        adapter = OrderHistoryAdapter(mutableListOf(),
            onRateClick = { showRatingDialog(it.id) },
            onDisputeClick = { showDisputeDialog(it.id) }
        )

        binding.recyclerViewOrderHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOrderHistory.adapter = adapter

        // Observa os pedidos do usuário
        observeUserOrders()

        // Carrega os pedidos
        orderViewModel.loadUserOrders()
    }

    private fun observeUserOrders() {
        orderViewModel.userOrders.observe(viewLifecycleOwner) { orders ->
            Log.d("OrderHistoryFragment", "Pedidos observados: $orders")
            if (orders.isEmpty()) {
                Log.d("OrderHistoryFragment", "Nenhum pedido encontrado.")
            }
            adapter.updateOrders(orders)  // Atualiza o RecyclerView com os pedidos
        }
    }


    private fun showRatingDialog(orderId: String) {
        // Lógica para mostrar o diálogo de avaliação
        // Exemplo de como você pode exibir um RatingDialog
    }

    private fun showDisputeDialog(orderId: String) {
        // Lógica para mostrar o diálogo de contestação
        // Exemplo de como você pode exibir um DisputeDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
