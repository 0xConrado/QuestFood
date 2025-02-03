package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.Order

class OrderHistoryAdapter(
    private val orders: MutableList<Order>,  // Lista mutável de pedidos
    private val onRateClick: (Order) -> Unit, // Função para o clique no botão de Avaliar
    private val onDisputeClick: (Order) -> Unit // Função para o clique no botão de Contestar
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    // ViewHolder responsável por referenciar os elementos da UI
    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNumber: TextView = itemView.findViewById(R.id.textOrderNumber)
        val orderStatus: TextView = itemView.findViewById(R.id.textOrderStatus)
        val orderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
        val buttonRate: Button = itemView.findViewById(R.id.buttonRate)
        val buttonDispute: Button = itemView.findViewById(R.id.buttonDispute)
    }

    // Cria a visualização para cada item na lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    // Vincula os dados ao ViewHolder
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        // Atualiza os dados do pedido
        holder.orderNumber.text = "Pedido: ${order.id.takeLast(6)}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.orderTotal.text = "Total: R$%.2f".format(order.total)

        // Controla a visibilidade dos botões
        if (order.status == "Entregue" || order.status == "Concluído") {
            holder.buttonRate.visibility = View.VISIBLE
            holder.buttonDispute.visibility = View.VISIBLE

            // Configura os cliques para avaliação e contestação
            holder.buttonRate.setOnClickListener { onRateClick(order) }
            holder.buttonDispute.setOnClickListener { onDisputeClick(order) }
        } else {
            holder.buttonRate.visibility = View.GONE
            holder.buttonDispute.visibility = View.GONE
        }
    }

    // Retorna o número de itens na lista
    override fun getItemCount(): Int = orders.size

    // Função para atualizar a lista de pedidos
    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
