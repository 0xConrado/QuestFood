package com.quest.food.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.adapter.ManageOrdersAdapter
import com.quest.food.databinding.FragmentManageOrdersBinding
import com.quest.food.viewmodel.OrderViewModel

class ManageOrdersFragment : Fragment() {

    private var _binding: FragmentManageOrdersBinding? = null
    private val binding get() = _binding!!
    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuração do RecyclerView
        binding.recyclerViewManageOrders.layoutManager = LinearLayoutManager(requireContext())

        // Observa alterações na lista de pedidos
        orderViewModel.allOrders.observe(viewLifecycleOwner) { orders ->
            if (orders.isNotEmpty()) {
                binding.recyclerViewManageOrders.adapter = ManageOrdersAdapter(
                    orders,
                    onStatusChange = { order, newStatus ->
                        orderViewModel.updateOrderStatus(order.id, newStatus)
                        showToast("Status atualizado para: $newStatus")
                        orderViewModel.loadAllOrders() // Atualiza a lista após mudança de status
                    },
                    onDeleteOrder = { order ->
                        orderViewModel.deleteOrder(order.id)
                        showToast("Pedido excluído com sucesso!")
                        orderViewModel.loadAllOrders() // Atualiza a lista após exclusão
                    }
                )
            } else {
                showToast("Nenhum pedido disponível.")
            }
        }

        // Carrega todos os pedidos
        orderViewModel.loadAllOrders()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
