package com.quest.food

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class MenuItem(val icon: Int, val title: String)

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val onItemClick: (MenuItem) -> Unit // Callback para lidar com cliques
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.menuIcon)
        val title: TextView = itemView.findViewById(R.id.menuText)

        fun bind(menuItem: MenuItem, onItemClick: (MenuItem) -> Unit) {
            icon.setImageResource(menuItem.icon)
            title.text = menuItem.title
            itemView.setOnClickListener { onItemClick(menuItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuItems[position]
        holder.bind(item, onItemClick)
    }

    override fun getItemCount(): Int = menuItems.size
}