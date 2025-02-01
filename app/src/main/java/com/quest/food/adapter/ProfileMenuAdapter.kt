package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.databinding.ItemProfileMenuBinding

class ProfileMenuAdapter(
    private val menuItems: List<ProfileMenuItem>,
    private val onItemClick: (ProfileMenuItem) -> Unit
) : RecyclerView.Adapter<ProfileMenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemProfileMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = menuItems.size

    class MenuViewHolder(private val binding: ItemProfileMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfileMenuItem) {
            binding.menuIcon.setImageResource(item.iconResId)
            binding.menuTitle.text = item.title
        }
    }
}

data class ProfileMenuItem(val title: String, val iconResId: Int)
