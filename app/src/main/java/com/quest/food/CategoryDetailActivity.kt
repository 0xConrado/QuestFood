package com.quest.food

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.adapter.MenuAdapter
import com.quest.food.databinding.ActivityCategoryDetailBinding
import com.quest.food.model.FoodMenuItem

class CategoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryDetailBinding
    private lateinit var adapter: MenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera o objeto Category da Intent
        val category = intent.getParcelableExtra<Category>("category")
        if (category != null) {
            binding.categoryTitle.text = category.title

            adapter = MenuAdapter { menuItem: FoodMenuItem ->
                // Aqui você pode abrir uma tela de detalhes do item se necessário
            }
            binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.menuRecyclerView.adapter = adapter

            // Converte List<MenuItem> para List<FoodMenuItem>
            val foodItems = category.items.filterIsInstance<FoodMenuItem>()

            // Atualiza o adapter com a lista convertida
            adapter.submitList(foodItems)
        }
    }
}
