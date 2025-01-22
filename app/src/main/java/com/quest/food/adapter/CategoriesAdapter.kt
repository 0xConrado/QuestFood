package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.databinding.AddItemCategoryBinding
import com.quest.food.databinding.MenuItemCategoryBinding
import com.quest.food.model.CategoryMenuItem

class CategoriesAdapter(
    private var categories: MutableList<CategoryMenuItem>,
    private val isAdmin: Boolean,
    private val onCategoryClick: (CategoryMenuItem) -> Unit,
    private val onAddCategoryClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_CATEGORY = 1
    private val TYPE_ADD_BUTTON = 2

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin && position == categories.size) TYPE_ADD_BUTTON else TYPE_CATEGORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CATEGORY) {
            val binding = MenuItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CategoryViewHolder(binding)
        } else {
            val binding = AddItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AddCategoryViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryViewHolder) {
            holder.bind(categories[position])
        } else if (holder is AddCategoryViewHolder) {
            holder.bind()
        }
    }

    override fun getItemCount(): Int {
        return if (isAdmin) categories.size + 1 else categories.size
    }

    fun updateCategories(newCategories: List<CategoryMenuItem>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: MenuItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryMenuItem) {
            binding.categoryTitle.text = category.title
            binding.categorySubtitle.text = category.subtitle

            Glide.with(binding.root.context)
                .load(category.imageUrl)
                .centerCrop()
                .into(binding.categoryImage)

            binding.categoryImage.clipToOutline = true

            binding.root.setOnClickListener { onCategoryClick(category) }
        }
    }

    inner class AddCategoryViewHolder(private val binding: AddItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.addCategoryButton.setOnClickListener { onAddCategoryClick() }
        }
    }
}
