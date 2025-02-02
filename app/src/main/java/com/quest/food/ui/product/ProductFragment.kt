package com.quest.food.ui.product

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.adapter.ProductAdapter
import com.quest.food.adapter.CategoriesAdapter
import com.quest.food.databinding.FragmentProductListBinding
import com.quest.food.model.ProductItem
import com.quest.food.viewmodel.ProductViewModel

class ProductFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var categoryId: String = ""  // ID da categoria atual

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryId = arguments?.getString("categoryId") ?: ""
        productViewModel.loadProductsForCategory(categoryId)
        productViewModel.loadCategoryDetails(categoryId)

        setupProductAdapter()

        productViewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateProducts(products)
        }

        productViewModel.category.observe(viewLifecycleOwner) { category ->
            binding.categoryTitle.text = category.title
            binding.categorySubtitle.text = category.subtitle
            Glide.with(this)
                .load(category.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.categoryImage)
        }

        productViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            binding.addProductButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            binding.addProductButton.setOnClickListener { showAddProductDialog(categoryId) }
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupProductAdapter() {
        adapter = ProductAdapter(
            products = mutableListOf(),
            isAdmin = productViewModel.isAdmin.value ?: false,
            onEditProduct = { product -> showEditProductDialog(product) },
            onDeleteProduct = { product -> confirmDeleteProduct(product) }
        )

        binding.productRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.productRecyclerView.adapter = adapter
    }

    private fun showAddProductDialog(categoryId: String) {
        val dialog = AddEditProductDialogFragment.newInstance(categoryId)
        dialog.show(parentFragmentManager, "AddProductDialog")
    }

    private fun showEditProductDialog(product: ProductItem) {
        val dialog = AddEditProductDialogFragment.newInstance(product)
        dialog.show(parentFragmentManager, "EditProductDialog")
    }

    private fun confirmDeleteProduct(product: ProductItem) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Excluir Produto")
            .setMessage("Tem certeza que deseja excluir ${product.name}?")
            .setPositiveButton("SIM") { _, _ ->
                productViewModel.deleteProduct(categoryId, product)  // 🚀 Verifique se categoryId está definido corretamente
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
