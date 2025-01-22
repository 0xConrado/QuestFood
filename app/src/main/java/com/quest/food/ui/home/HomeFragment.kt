package com.quest.food.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.User
import com.quest.food.adapter.CategoriesAdapter
import com.quest.food.databinding.DialogAddCategoryBinding
import com.quest.food.databinding.FragmentHomeBinding
import com.quest.food.model.CategoryMenuItem
import com.quest.food.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var adapter: CategoriesAdapter
    private var isAdmin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoriesAdapter()

        homeViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                isAdmin = user.role == "admin"
                updateUserUI(user)
            } else {
                showGuestUser()
            }
            setupCategoriesAdapter()
        }

        homeViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)
        }
    }

    private fun setupCategoriesAdapter() {
        adapter = CategoriesAdapter(
            categories = mutableListOf(), // ✅ Passa uma lista vazia inicial
            isAdmin = isAdmin,
            onCategoryClick = { selectedCategory -> openCategoryDetail(selectedCategory) },
            onAddCategoryClick = { if (isAdmin) showAddCategoryDialog() }
        )
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecyclerView.adapter = adapter
    }

    private fun openCategoryDetail(category: CategoryMenuItem) {
        Toast.makeText(requireContext(), "Abrir detalhes de: ${category.title}", Toast.LENGTH_SHORT).show()
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = DialogAddCategoryBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.categoryImageUrlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                Glide.with(this@HomeFragment).load(url).into(dialogBinding.categoryImagePreview)
            }
        })

        dialogBinding.addCategoryButton.setOnClickListener {
            val title = dialogBinding.categoryTitleEditText.text.toString()
            val subtitle = dialogBinding.categorySubtitleEditText.text.toString()
            val imageUrl = dialogBinding.categoryImageUrlEditText.text.toString()

            if (title.isNotBlank() && subtitle.isNotBlank() && imageUrl.isNotBlank()) {
                val newCategory = CategoryMenuItem(title = title, subtitle = subtitle, imageUrl = imageUrl)
                homeViewModel.addCategory(newCategory)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun updateUserUI(user: User) {
        binding.homeProfileSection.apply {
            profileUserName.text = user.username
            profileTitle.text = user.title
            levelText.text = "Lvl ${user.level}"
            levelProgressBar.progress = user.levelProgress

            if (user.profileImagePath.isNotEmpty()) {
                Glide.with(this@HomeFragment)
                    .load(user.profileImagePath)
                    .placeholder(R.drawable.user_default)
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.user_default)
            }
        }
    }

    private fun showGuestUser() {
        binding.homeProfileSection.apply {
            profileUserName.text = "Visitante"
            profileTitle.text = "Modo Anônimo"
            levelText.text = "Lvl 0"
            levelProgressBar.progress = 0
            profileImage.setImageResource(R.drawable.user_default)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
