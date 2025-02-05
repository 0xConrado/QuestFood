package com.quest.food.ui.promotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.databinding.FragmentPromotionBinding
import com.quest.food.viewmodel.ProductViewModel
import com.quest.food.adapter.PromotionAdapter
import com.quest.food.model.ProductItem

class PromotionFragment : Fragment() {

    private var _binding: FragmentPromotionBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var adapter: PromotionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPromotionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Configuração do Adapter com o callback para o botão "Shop Now"
        adapter = PromotionAdapter(mutableListOf()) { product ->
            navigateToProductDetail(product) // 🔗 Navega para o ProductDetailFragment
        }

        binding.recyclerViewPromotions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPromotions.adapter = adapter

        // ✅ Observa o LiveData de produtos e filtra as promoções
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            val promotions = products.filter { it.isPromotion }
            adapter.updatePromotions(promotions)
        }
    }

    // ✅ Função para navegar até o ProductDetailFragment
    private fun navigateToProductDetail(product: ProductItem) {
        val action = PromotionFragmentDirections
            .actionPromotionFragmentToProductDetailFragment(
                productId = product.id,
                categoryId = product.categoryId
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
