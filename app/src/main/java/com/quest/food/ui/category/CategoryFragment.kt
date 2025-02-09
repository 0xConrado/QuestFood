package com.quest.food.ui.category

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quest.food.adapter.CategoriesAdapter
import com.quest.food.databinding.DialogAddCategoryBinding
import com.quest.food.databinding.FragmentCategoryBinding
import com.quest.food.model.CategoryMenuItem
import com.quest.food.viewmodel.CategoryViewModel

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var adapter: CategoriesAdapter
    private var isAdmin: Boolean = false  // Inicializando como false, será atualizado

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        checkUserRole()  // Verifica o papel do usuário e carrega as categorias
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Recarregar as categorias ao retornar para o fragmento
        categoryViewModel.loadCategories()  // Carregar novamente as categorias se necessário
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoriesAdapter()

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateCategories(categories)  // Atualizar o adapter com as novas categorias
        }
    }

    private fun setupCategoriesAdapter() {
        adapter = CategoriesAdapter(
            categories = mutableListOf(),
            isAdmin = isAdmin,  // Verifica se é admin para exibir opções de administração
            onCategoryClick = { selectedCategory -> openCategoryDetail(selectedCategory) },
            onAddCategoryClick = { if (isAdmin) showAddCategoryDialog() },
            onEditCategory = { category -> showEditCategoryDialog(category) },
            onDeleteCategory = { category -> deleteCategory(category) }
        )

        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecyclerView.adapter = adapter
    }

    private fun deleteCategory(category: CategoryMenuItem) {
        categoryViewModel.deleteCategory(category)
        Toast.makeText(requireContext(), "${category.title} excluída com sucesso!", Toast.LENGTH_SHORT).show()
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
            override fun afterTextChanged(s: Editable?) {
                Glide.with(this@CategoryFragment)
                    .load(s.toString())
                    .into(dialogBinding.categoryImagePreview)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        dialogBinding.addCategoryButton.setOnClickListener {
            val title = dialogBinding.categoryTitleEditText.text.toString()
            val subtitle = dialogBinding.categorySubtitleEditText.text.toString()
            val imageUrl = dialogBinding.categoryImageUrlEditText.text.toString()

            if (title.isNotBlank() && subtitle.isNotBlank() && imageUrl.isNotBlank()) {
                val newCategory = CategoryMenuItem(title = title, subtitle = subtitle, imageUrl = imageUrl)
                categoryViewModel.addCategory(newCategory)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditCategoryDialog(category: CategoryMenuItem) {
        val dialogBinding = DialogAddCategoryBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBinding.categoryTitleEditText.setText(category.title)
        dialogBinding.categorySubtitleEditText.setText(category.subtitle)
        dialogBinding.categoryImageUrlEditText.setText(category.imageUrl)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setTitle("Editar Categoria")
            .create()

        dialogBinding.addCategoryButton.text = "Salvar"
        dialogBinding.addCategoryButton.setOnClickListener {
            val updatedCategory = CategoryMenuItem(
                id = category.id,
                title = dialogBinding.categoryTitleEditText.text.toString(),
                subtitle = dialogBinding.categorySubtitleEditText.text.toString(),
                imageUrl = dialogBinding.categoryImageUrlEditText.text.toString()
            )
            categoryViewModel.updateCategory(updatedCategory)
            dialog.dismiss()
        }

        dialogBinding.cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun checkUserRole() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("users").child(userId).child("role")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)
                    isAdmin = role.equals("admin", ignoreCase = true)  // Atualiza o isAdmin baseado na role
                    setupCategoriesAdapter()  // Atualiza o adapter
                    categoryViewModel.loadCategories()  // Carrega as categorias para o usuário
                }

                override fun onCancelled(error: DatabaseError) {
                    isAdmin = false
                    setupCategoriesAdapter()  // Atualiza o adapter para o padrão de não admin
                    categoryViewModel.loadCategories()  // Carrega as categorias para o usuário
                    Toast.makeText(requireContext(), "Erro ao obter o papel do usuário: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            isAdmin = false
            setupCategoriesAdapter()  // Atualiza o adapter para o padrão de não admin
            categoryViewModel.loadCategories()  // Carrega as categorias para o usuário
            Toast.makeText(requireContext(), "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
