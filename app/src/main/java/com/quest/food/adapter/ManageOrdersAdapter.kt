package com.quest.food.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.Order

class ManageOrdersAdapter(
    private val orders: List<Order>,
    private val onStatusChange: (Order, String) -> Unit,
    private val onDeleteOrder: (Order) -> Unit
) : RecyclerView.Adapter<ManageOrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.textOrderId)
        val orderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
        val statusGroup: RadioGroup = itemView.findViewById(R.id.statusGroup)
        val deleteButton: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.orderId.text = "Pedido: ${order.id.takeLast(6)}"
        holder.orderTotal.text = "Total: R$%.2f".format(order.total)

        // Define o status atual
        when (order.status) {
            "Aprovado" -> holder.statusGroup.check(R.id.radioApproved)
            "Cancelado" -> holder.statusGroup.check(R.id.radioCancelled)
            "Em Produção" -> holder.statusGroup.check(R.id.radioInProduction)
            "Saiu para Entregar" -> holder.statusGroup.check(R.id.radioOutForDelivery)
            "Entregue" -> holder.statusGroup.check(R.id.radioDelivered)
            "Concluído" -> holder.statusGroup.check(R.id.radioCompleted)
        }

        // Atualização do status apenas quando houver uma mudança
        holder.statusGroup.setOnCheckedChangeListener { _, checkedId ->
            val newStatus = when (checkedId) {
                R.id.radioApproved -> "Aprovado"
                R.id.radioCancelled -> "Cancelado"
                R.id.radioInProduction -> "Em Produção"
                R.id.radioOutForDelivery -> "Saiu para Entregar"
                R.id.radioDelivered -> "Entregue"
                R.id.radioCompleted -> "Concluído"
                else -> order.status
            }
            if (newStatus != order.status) {
                onStatusChange(order, newStatus)
            }
        }

        // Confirmação antes de excluir o pedido
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir o pedido ${order.id.takeLast(6)}?")
                .setPositiveButton("Sim") { _, _ ->
                    onDeleteOrder(order)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = orders.size
}
