package com.quest.food.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.CartItem

class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private val onProductClick: (CartItem) -> Unit,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemCategoryProduct: TextView = itemView.findViewById(R.id.itemCategoryProduct) // ✅ Categoria + Produto
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantity)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val expandIcon: ImageView = itemView.findViewById(R.id.expandIcon)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        val ingredientsContainer: LinearLayout = itemView.findViewById(R.id.ingredientsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]

        // ✅ Mostra Categoria + Produto
        holder.itemCategoryProduct.text = "${item.categoryName} ${item.productName}"
        holder.itemQuantity.text = "Qtd: ${item.quantity}"
        holder.itemPrice.text = "R$ %.2f".format(item.price)

        // 📌 Navegar para o detalhe do produto
        holder.itemCategoryProduct.setOnClickListener { onProductClick(item) }

        // 🔽 Expandir/Fechar detalhes do item
        holder.expandIcon.setOnClickListener {
            if (holder.ingredientsContainer.visibility == View.VISIBLE) {
                holder.ingredientsContainer.visibility = View.GONE
                holder.expandIcon.setImageResource(R.drawable.nav_arrow_right)
            } else {
                holder.ingredientsContainer.visibility = View.VISIBLE
                holder.expandIcon.setImageResource(R.drawable.nav_arrow_down)
                displayIngredients(holder.ingredientsContainer, item.selectedIngredients)
            }
        }

        // 🗑️ Confirmação de remoção do item
        holder.deleteIcon.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Remover do Carrinho")
                .setMessage("Deseja remover '${item.productName}' do carrinho?")
                .setPositiveButton("Sim") { _, _ -> onDeleteClick(item) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun displayIngredients(container: LinearLayout, ingredients: List<String>) {
        container.removeAllViews()
        ingredients.forEach { ingredient ->
            val ingredientView = TextView(container.context).apply {
                text = "- $ingredient"
                setPadding(16, 8, 16, 8)
                textSize = 14f
                setTextColor(container.context.getColor(R.color.black))
            }
            container.addView(ingredientView)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newItems: List<CartItem>) {
        cartItems = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
