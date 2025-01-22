package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.databinding.MenuItemFoodBinding
import com.quest.food.model.FoodMenuItem

class FoodMenuAdapter(private val foodItems: List<FoodMenuItem>) :
    RecyclerView.Adapter<FoodMenuAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(private val binding: MenuItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(foodItem: FoodMenuItem) {
            binding.menuTitle.text = foodItem.name
            binding.menuDescription.text = foodItem.description
            binding.menuPrice.text = "R$ ${foodItem.price}"

            // Exibir preço antigo (se existir)
            if (foodItem.oldPrice > 0) {
                binding.menuOldPrice.text = "R$ ${foodItem.oldPrice}"
                binding.menuOldPrice.visibility = View.VISIBLE
            } else {
                binding.menuOldPrice.visibility = View.GONE
            }

            // Exibir tags de promoção e mais vendido
            binding.tagPromo.visibility = if (foodItem.isPromo) View.VISIBLE else View.GONE
            binding.tagSold.visibility = if (foodItem.isBestSeller) View.VISIBLE else View.GONE

            // Carregar imagem com Glide
            Glide.with(binding.root.context)
                .load(foodItem.imageUrl)
                .centerCrop()
                .into(binding.menuImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = MenuItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodItems[position])
    }

    override fun getItemCount(): Int = foodItems.size
}
