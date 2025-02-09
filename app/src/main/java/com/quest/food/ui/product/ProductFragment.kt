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
import com.quest.food.databinding.FragmentProductListBinding
import com.quest.food.model.ProductItem
import com.quest.food.viewmodel.ProductViewModel
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.setFragmentResultListener
import com.quest.food.ui.product.ProductFragmentDirections

class ProductFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter
    private var categoryId: String = ""

    override fun onResume() {
        productViewModel.checkAdminStatus()
        super.onResume()
    }

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

        setupProductAdapter()

        // Listener para recarregar produtos após fechar o diálogo
        setFragmentResultListener("refreshProducts") { _, _ ->
            productViewModel.loadProductsForCategory(categoryId)
        }

        loadCategoryData()

        // Configuração do filtro de busca de produtos
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                productViewModel.filterProducts(s.toString()) // Aplica o filtro nos produtos com base no texto digitado
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCategoryData() {
        // Carregar dados da categoria e produtos
        productViewModel.loadCategoryDetails(categoryId)
        productViewModel.loadProductsForCategory(categoryId)

        productViewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.updateProducts(products.filter { it.categoryId == categoryId })
        }

        productViewModel.category.observe(viewLifecycleOwner) { category ->
            binding.categoryTitle.text = category.title
            binding.categorySubtitle.text = category.subtitle
            Glide.with(this)
                .load(category.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.categoryImage)
        }

        // Observador para verificar se o usuário é administrador
        productViewModel.isAdmin.observe(viewLifecycleOwner) { isAdmin ->
            // Atualiza a visibilidade dos botões de editar e excluir nos itens do adapter
            adapter.updateAdminStatus(isAdmin)
        }
    }

    private fun setupProductAdapter() {
        adapter = ProductAdapter(
            products = mutableListOf(),
            isAdmin = productViewModel.isAdmin.value ?: false,  // Inicializa com o valor atual
            onEditProduct = { product -> showEditProductDialog(product) },
            onDeleteProduct = { product -> confirmDeleteProduct(product) },
            onViewProductDetails = { product -> showProductDetailFragment(product) }
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

    private fun showProductDetailFragment(product: ProductItem) {
        val action = ProductFragmentDirections.actionProductListFragmentToProductDetailFragment(
            productId = product.id,
            categoryId = categoryId
        )
        findNavController().navigate(action)
    }

    private fun confirmDeleteProduct(product: ProductItem) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Excluir Produto")
            .setMessage("Tem certeza que deseja excluir ${product.name}?")
            .setPositiveButton("SIM") { _, _ ->
                productViewModel.deleteProduct(categoryId, product)
                productViewModel.loadProductsForCategory(categoryId) // Atualiza a lista após exclusão
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
