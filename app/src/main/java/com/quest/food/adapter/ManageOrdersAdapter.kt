package com.quest.food.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.Order

class ManageOrdersAdapter(
    private var orders: List<Order> = emptyList(),
    private val onStatusChange: (Order, String) -> Unit,
    private val onDeleteOrder: (Order) -> Unit,
    private val onExpandToggle: (Int) -> Unit
) : RecyclerView.Adapter<ManageOrdersAdapter.OrderViewHolder>() {

    private val expandedPositionSet = mutableSetOf<Int>()

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderId: TextView = itemView.findViewById(R.id.textOrderId)
        val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        val orderDetailContainer: ViewGroup = itemView.findViewById(R.id.orderDetailContainer)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        val radioApproved: RadioButton = itemView.findViewById(R.id.radioApproved)
        val radioCancelled: RadioButton = itemView.findViewById(R.id.radioCancelled)
        val radioInProduction: RadioButton = itemView.findViewById(R.id.radioInProduction)
        val radioOutForDelivery: RadioButton = itemView.findViewById(R.id.radioOutForDelivery)
        val radioDelivered: RadioButton = itemView.findViewById(R.id.radioDelivered)
        val radioCompleted: RadioButton = itemView.findViewById(R.id.radioCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = "Pedido: ${order.id.takeLast(6)}"

        val isExpanded = expandedPositionSet.contains(position)
        holder.orderDetailContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.setImageResource(if (isExpanded) R.drawable.nav_arrow_down else R.drawable.nav_arrow_right)

        holder.expandIcon.setOnClickListener {
            if (isExpanded) {
                expandedPositionSet.remove(position)
            } else {
                expandedPositionSet.add(position)
            }
            notifyItemChanged(position)
            onExpandToggle(position)
        }

        val statusRadioButtons = listOf(
            holder.radioApproved to "Aprovado",
            holder.radioCancelled to "Cancelado",
            holder.radioInProduction to "Em Produção",
            holder.radioOutForDelivery to "Saiu para Entregar",
            holder.radioDelivered to "Entregue",
            holder.radioCompleted to "Concluído"
        )

        statusRadioButtons.forEach { (radioButton, status) ->
            radioButton.setOnCheckedChangeListener(null)
            radioButton.isChecked = order.status == status
        }

        statusRadioButtons.forEach { (radioButton, status) ->
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && order.status != status) {
                    onStatusChange(order, status)
                }
            }
        }

        holder.deleteIcon.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView, order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(view: View, order: Order) {
        AlertDialog.Builder(view.context)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir o pedido ${order.id.takeLast(6)}?")
            .setPositiveButton("Sim") { _, _ ->
                onDeleteOrder(order)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
