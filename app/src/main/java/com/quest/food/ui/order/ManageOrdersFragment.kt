package com.quest.food.ui.order

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var adapter: ManageOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewManageOrders.layoutManager = LinearLayoutManager(requireContext())

        adapter = ManageOrdersAdapter(
            emptyList(),
            onStatusChange = { order, newStatus ->
                orderViewModel.updateOrderStatus(order.id, newStatus)
                showToast("Status atualizado para: $newStatus")
                orderViewModel.loadAllOrders()
            },
            onDeleteOrder = { order ->
                orderViewModel.deleteOrder(order.id)
                showToast("Pedido excluído com sucesso!")
                orderViewModel.loadAllOrders()
            },
            onExpandToggle = { position ->
                adapter.notifyItemChanged(position)
            }
        )

        binding.recyclerViewManageOrders.adapter = adapter

        orderViewModel.allOrders.observe(viewLifecycleOwner) { orders ->
            if (orders.isNotEmpty()) {
                adapter.updateOrders(orders)
            } else {
                showToast("Nenhum pedido disponível.")
            }
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                orderViewModel.filterOrders(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

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
