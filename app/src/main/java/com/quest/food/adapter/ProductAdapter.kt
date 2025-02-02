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
    private val onDeleteProduct: (ProductItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImage: ImageView? = itemView.findViewById(R.id.productImage)
        private val productTitle: TextView? = itemView.findViewById(R.id.productTitle)
        private val productDescription: TextView? = itemView.findViewById(R.id.productDescription)
        private val productPrice: TextView? = itemView.findViewById(R.id.productPrice)
        private val productOriginalPrice: TextView? = itemView.findViewById(R.id.productOriginalPrice)
        private val badgePromotion: ImageView? = itemView.findViewById(R.id.badgePromotion)
        private val badgeBestSeller: ImageView? = itemView.findViewById(R.id.badgeBestSeller)
        private val buttonEditProduct: ImageView? = itemView.findViewById(R.id.buttonEditProduct)
        private val buttonDeleteProduct: ImageView? = itemView.findViewById(R.id.buttonDeleteProduct)
        private val adminControls: LinearLayout? = itemView.findViewById(R.id.adminControls)

        fun bind(product: ProductItem) {
            productTitle?.text = product.name
            productDescription?.text = product.description
            productPrice?.text = "R$ %.2f".format(product.price).replace('.', ',')

            productImage?.let {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(it)
            }

            badgePromotion?.visibility = if (product.isPromotion) View.VISIBLE else View.GONE
            badgeBestSeller?.visibility = if (product.isBestSeller) View.VISIBLE else View.GONE

            productOriginalPrice?.apply {
                visibility = if (product.isPromotion) View.VISIBLE else View.GONE
                text = "R$ %.2f".format(product.originalPrice ?: 0.0).replace('.', ',')
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            if (isAdmin) {
                adminControls?.visibility = View.VISIBLE
                buttonEditProduct?.setOnClickListener { onEditProduct(product) }
                buttonDeleteProduct?.setOnClickListener { onDeleteProduct(product) }
            } else {
                adminControls?.visibility = View.GONE
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
        this.isAdmin = isAdmin
        notifyDataSetChanged()
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
}