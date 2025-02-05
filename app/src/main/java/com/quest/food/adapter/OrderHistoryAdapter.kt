package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import com.quest.food.model.Order
import com.quest.food.model.User

class OrderHistoryAdapter(private var orders: MutableList<Order>) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderNumber: TextView = itemView.findViewById(R.id.textOrderNumber)
        val orderStatus: TextView = itemView.findViewById(R.id.textOrderStatus)
        val orderTotal: TextView = itemView.findViewById(R.id.textOrderTotal)
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderNumber.text = "Pedido: ${order.id}"
        holder.orderStatus.text = "Status: ${order.status}"
        holder.orderTotal.text = "Total: R$${"%.2f".format(order.total)}"

        holder.buttonDetails.setOnClickListener {
            val isVisible = holder.orderDetailsLayout.visibility == View.VISIBLE
            if (isVisible) {
                holder.orderDetailsLayout.visibility = View.GONE
                holder.buttonDetails.text = "Ver Mais"
            } else {
                holder.orderDetailsLayout.visibility = View.VISIBLE
                holder.buttonDetails.text = "Ver Menos"

                holder.textOrderInfo.text = "Pedido: ${order.id}"
                holder.textPayment.text = "Pagamento: ${order.paymentMethod}"
                holder.textObservation.text = "Observação: ${order.observation.ifEmpty { "Nenhuma" }}"

                holder.textItems.text = order.items.joinToString("\n") {
                    "${it.quantity}x ${it.productName} - R$${"%.2f".format(it.price)}"
                }

                holder.textTotal.text = """
                    Subtotal: R$${"%.2f".format(order.total)}
                    Taxa de Entrega: R$${"%.2f".format(order.deliveryFee)}
                    TOTAL: R$${"%.2f".format(order.total + order.deliveryFee)}
                """.trimIndent()

                // Buscar informações do usuário pelo userId
                fetchUserDetails(order.userId, holder)
            }
        }
    }

    private fun fetchUserDetails(userId: String, holder: OrderViewHolder) {
        val userDatabase = FirebaseDatabase.getInstance().getReference("users")
        userDatabase.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                holder.textUserInfo.text = """
                    Nome: ${user.username}
                    Telefone: ${user.phone}
                    Endereço: ${user.address.street}, ${user.address.number} - ${user.address.neighborhood}
                    Cidade: ${user.address.city}, ${user.address.state}
                    CEP: ${user.address.postalCode}
                    Complemento: ${user.address.complement}
                """.trimIndent()
            } else {
                holder.textUserInfo.text = "Informações do usuário indisponíveis."
            }
        }.addOnFailureListener {
            holder.textUserInfo.text = "Erro ao carregar informações do usuário."
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }
}
