package com.quest.food.ui.foodmenu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.quest.food.adapter.FoodMenuAdapter
import com.quest.food.databinding.ActivityFoodMenuBinding
import com.quest.food.model.FoodMenuItem

class FoodMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodMenuBinding
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FoodMenuAdapter
    private var foodItems = mutableListOf<FoodMenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("categoryId") ?: return

        // Inicializar Firebase
        database = FirebaseDatabase.getInstance().getReference("categories").child(categoryId).child("items")

        // Configurar RecyclerView
        adapter = FoodMenuAdapter(foodItems)
        binding.foodMenuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.foodMenuRecyclerView.adapter = adapter

        loadCategoryHeader(categoryId)
        loadFoodItems()

        // Filtro de busca
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filterFoodItems(query)
            }
        })
    }

    private fun loadCategoryHeader(categoryId: String) {
        val categoryRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryId)

        categoryRef.get().addOnSuccessListener { snapshot ->
            val title = snapshot.child("title").value.toString()
            val subtitle = snapshot.child("subtitle").value.toString()
            val imageUrl = snapshot.child("imageUrl").value.toString()

            binding.categoryTitle.text = title
            binding.categorySubtitle.text = subtitle

            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(binding.categoryImage)
        }
    }

    private fun loadFoodItems() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodItems.clear()
                for (child in snapshot.children) {
                    val item = child.getValue(FoodMenuItem::class.java)
                    if (item != null) foodItems.add(item)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun filterFoodItems(query: String) {
        val filteredList = foodItems.filter { it.name.contains(query, ignoreCase = true) }
        adapter = FoodMenuAdapter(filteredList)
        binding.foodMenuRecyclerView.adapter = adapter
    }
}
