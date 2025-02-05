package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.model.ProductItem

class PromotionAdapter(
    private var promotions: MutableList<ProductItem>,
    private val onShopNowClick: (ProductItem) -> Unit // ✅ Callback para o botão "Shop Now"
) : RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {

    inner class PromotionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bannerTitle: TextView = itemView.findViewById(R.id.bannerTitle)
        val bannerSubtitle: TextView = itemView.findViewById(R.id.bannerSubtitle)
        val bannerImage: ImageView = itemView.findViewById(R.id.bannerImage)
        val shopNowButton: Button = itemView.findViewById(R.id.shopNowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promotion, parent, false)
        return PromotionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        val product = promotions[position]
        holder.bannerTitle.text = product.name
        holder.bannerSubtitle.text = product.description

        // ✅ Carregamento da imagem usando Glide
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.bannerImage)

        // ✅ Callback do botão "Shop Now"
        holder.shopNowButton.setOnClickListener {
            onShopNowClick(product)
        }
    }

    override fun getItemCount(): Int = promotions.size

    // ✅ Atualiza a lista de promoções
    fun updatePromotions(newPromotions: List<ProductItem>) {
        promotions.clear()
        promotions.addAll(newPromotions)
        notifyDataSetChanged()
    }
}
