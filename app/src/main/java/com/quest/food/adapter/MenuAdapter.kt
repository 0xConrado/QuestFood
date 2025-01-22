package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.databinding.MenuItemFoodBinding
import com.quest.food.model.FoodMenuItem
import android.graphics.Paint

class MenuAdapter(
    private val onMenuItemClick: (FoodMenuItem) -> Unit
) : ListAdapter<FoodMenuItem, MenuAdapter.MenuViewHolder>(MenuDiffCallback()) {

    inner class MenuViewHolder(private val binding: MenuItemFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: FoodMenuItem) {
            binding.menuTitle.text = menuItem.name
            binding.menuDescription.text = menuItem.description
            binding.menuPrice.text = String.format("R$ %.2f", menuItem.price)

            if (menuItem.oldPrice > 0) {
                binding.menuOldPrice.visibility = View.VISIBLE
                binding.menuOldPrice.text = String.format("R$ %.2f", menuItem.oldPrice)
                binding.menuOldPrice.paintFlags = binding.menuOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.menuOldPrice.visibility = View.GONE
            }

            // Carregar imagem com Glide
            Glide.with(binding.root.context)
                .load(menuItem.imageUrl)
                .into(binding.menuImage)

            binding.root.setOnClickListener { onMenuItemClick(menuItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MenuDiffCallback : DiffUtil.ItemCallback<FoodMenuItem>() {
    override fun areItemsTheSame(oldItem: FoodMenuItem, newItem: FoodMenuItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: FoodMenuItem, newItem: FoodMenuItem) = oldItem == newItem
}
