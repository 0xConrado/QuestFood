package com.quest.food.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.model.ProductItem

class ProductAdapter(
    private var products: MutableList<ProductItem>,
    private var isAdmin: Boolean,
    private val onEditProduct: (ProductItem) -> Unit,
    private val onDeleteProduct: (ProductItem) -> Unit,
    private val onViewProductDetails: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView? = itemView.findViewById(R.id.productImage)
        private val productTitle: TextView? = itemView.findViewById(R.id.productTitle)
        private val productDescription: TextView? = itemView.findViewById(R.id.productDescription)
        private val productPrice: TextView? = itemView.findViewById(R.id.productPrice)
        private val productOriginalPrice: TextView? = itemView.findViewById(R.id.productOriginalPrice)
        private val badgePromotion: TextView? = itemView.findViewById(R.id.badgePromotion)
        private val badgeBestSeller: TextView? = itemView.findViewById(R.id.badgeBestSeller)
        private val buttonEditProduct: ImageView? = itemView.findViewById(R.id.buttonEditProduct)
        private val buttonDeleteProduct: ImageView? = itemView.findViewById(R.id.buttonDeleteProduct)
        private val adminControls: LinearLayout? = itemView.findViewById(R.id.adminControls)
        private val viewMoreButton: TextView? = itemView.findViewById(R.id.viewMoreButton)

        fun bind(product: ProductItem) {
            productTitle?.text = product.name
            productDescription?.text = product.description.take(50) + if (product.description.length > 50) "..." else ""
            productPrice?.text = "R$ %.2f".format(product.price).replace('.', ',')

            productImage?.let {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(it)
            }

            badgePromotion?.apply {
                visibility = if (product.isPromotion) View.VISIBLE else View.GONE
                text = "Promoção!"
            }

            badgeBestSeller?.apply {
                visibility = if (product.isBestSeller) View.VISIBLE else View.GONE
                text = "+ Vendido!"
            }

            productOriginalPrice?.apply {
                visibility = if (product.isPromotion) View.VISIBLE else View.GONE
                text = "R$ %.2f".format(product.originalPrice ?: 0.0).replace('.', ',')
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            // Definindo os listeners para edição e exclusão, independentemente do status de admin
            buttonEditProduct?.setOnClickListener { onEditProduct(product) }
            buttonDeleteProduct?.setOnClickListener { onDeleteProduct(product) }

            adminControls?.apply {
                visibility = if (isAdmin) View.VISIBLE else View.GONE
            }

            // Definindo a ação de clique para visualizar detalhes do produto
            itemView.setOnClickListener { onViewProductDetails(product) }

            // Configurando o botão "Ver mais" para descrições longas
            viewMoreButton?.apply {
                visibility = if (product.description.length > 50) View.VISIBLE else View.GONE
                setOnClickListener { onViewProductDetails(product) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    private val originalProducts = mutableListOf<ProductItem>()

    fun updateProducts(newProducts: List<ProductItem>) {
        originalProducts.clear()
        originalProducts.addAll(newProducts)
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun updateAdminStatus(isAdmin: Boolean) {
        this.isAdmin = isAdmin  // Atualiza o estado de admin diretamente
        notifyDataSetChanged()  // Notifica a mudança para todos os itens
    }

    fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalProducts
        } else {
            originalProducts.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        products.clear()
        products.addAll(filteredList)
        notifyDataSetChanged()
    }

    fun clearProducts() {
        products.clear()
        notifyDataSetChanged()
    }
}