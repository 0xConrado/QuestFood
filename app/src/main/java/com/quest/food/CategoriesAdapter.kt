package com.quest.food.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.Category
import com.quest.food.R

class CategoriesAdapter(
    private val categories: MutableList<Category>,
    private val userRole: String, // Adicionado para verificar o role do usuário
    private val onAddCategoryClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADD_BUTTON = 0
        private const val VIEW_TYPE_CATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        // Exibe o botão "Adicionar Categoria" apenas para admins
        return if (userRole == "admin" && position == categories.size) {
            VIEW_TYPE_ADD_BUTTON
        } else {
            VIEW_TYPE_CATEGORY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_BUTTON) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_category, parent, false)
            AddCategoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            CategoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddCategoryViewHolder) {
            holder.bind(onAddCategoryClick)
        } else if (holder is CategoryViewHolder) {
            val category = categories[position]
            holder.bind(category)
        }
    }

    override fun getItemCount(): Int {
        // O botão "Adicionar Categoria" só será exibido para admins
        return if (userRole == "admin") {
            categories.size + 1 // +1 para incluir o botão "Adicionar Categoria"
        } else {
            categories.size // Apenas as categorias
        }
    }

    // ViewHolder para o botão "Adicionar Categoria"
    inner class AddCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addButton: TextView = itemView.findViewById(R.id.addCategoryButton)

        fun bind(onClick: () -> Unit) {
            addButton.setOnClickListener { onClick() }
        }
    }

    // ViewHolder para as categorias
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTitle: TextView = itemView.findViewById(R.id.categoryTitle)
        private val categorySubtitle: TextView = itemView.findViewById(R.id.categorySubtitle)
        private val categoryImage: ImageView = itemView.findViewById(R.id.categoryImage)

        fun bind(category: Category) {
            categoryTitle.text = category.title
            categorySubtitle.text = category.subtitle
            Glide.with(itemView.context).load(category.imageUrl).into(categoryImage)
        }
    }

    // Adiciona uma nova categoria e notifica a mudança no RecyclerView
    fun addCategory(newCategory: Category) {
        categories.add(newCategory)
        notifyItemInserted(categories.size - 1) // Atualiza o RecyclerView
    }
}