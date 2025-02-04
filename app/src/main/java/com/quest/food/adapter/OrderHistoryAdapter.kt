package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.Order
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryAdapter(
    private val orders: MutableList<Order>,  // Lista mutável de pedidos
    private val onRateClick: (Order) -> Unit, // Função para o clique no botão de Avaliar
    private val onDisputeClick: (Order) -> Unit // Função para o clique no botão de Contestar
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNumber: TextView = itemView.findViewById(R.id.textOrderNumber)
        val orderStatus: TextView = itemView.findViewById(R.id.textOrderStatus)
        val orderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
        val buttonRate: Button = itemView.findViewById(R.id.buttonRate)
        val buttonDispute: Button = itemView.findViewById(R.id.buttonDispute)
        val buttonDetails: Button = itemView.findViewById(R.id.buttonDetails)
        val orderDetailsLayout: LinearLayout = itemView.findViewById(R.id.orderDetailsLayout)
        val textOrderInfo: TextView = itemView.findViewById(R.id.textOrderInfo)
        val textUserInfo: TextView = itemView.findViewById(R.id.textUserInfo)
        val textItems: TextView = itemView.findViewById(R.id.textItems)
        val textTotal: TextView = itemView.findViewById(R.id.textTotal)
        val textPayment: TextView = itemView.findViewById(R.id.textPayment)
        val textObservation: TextView = itemView.findViewById(R.id.textObservation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.orderNumber.text = "Pedido: ${order.id.takeLast(6)}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.orderTotal.text = "Total: R$%.2f".format(order.total)

        if (order.status == "Entregue" || order.status == "Concluído") {
            holder.buttonRate.visibility = View.VISIBLE
            holder.buttonDispute.visibility = View.VISIBLE

            holder.buttonRate.setOnClickListener { onRateClick(order) }
            holder.buttonDispute.setOnClickListener { onDisputeClick(order) }
        } else {
            holder.buttonRate.visibility = View.GONE
            holder.buttonDispute.visibility = View.GONE
        }

        holder.buttonDetails.setOnClickListener {
            val isVisible = holder.orderDetailsLayout.visibility == View.VISIBLE
            holder.orderDetailsLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val orderDate = dateFormat.format(Date(order.timestamp))

        holder.textOrderInfo.text = "Pedido realizado em: $orderDate"
        holder.textUserInfo.text = "Usuário: ${order.userName}"
        holder.textItems.text = order.items.joinToString("\n") { "${it.quantity}x ${it.productName} (R$${"%.2f".format(it.price)})" }
        holder.textTotal.text = "Total do pedido: R$%.2f".format(order.total)
        holder.textPayment.text = "Pagamento: ${order.paymentMethod}"
        holder.textObservation.text = "Observação: ${order.observation}"
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
