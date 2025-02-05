package com.quest.food.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.quest.food.adapter.PromotionAdapter
import com.quest.food.databinding.FragmentHomeBinding
import com.quest.food.model.ProductItem
import com.quest.food.viewmodel.ProductViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var promotionAdapter: PromotionAdapter

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val slideInterval = 10000L // Intervalo de 10 segundos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Adapter configurado com listener para o botão "Shop Now"
        promotionAdapter = PromotionAdapter(mutableListOf()) { product ->
            navigateToProductDetail(product)
        }

        binding.promotionViewPager.adapter = promotionAdapter
        binding.promotionViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // ✅ Observa as promoções do ViewModel
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            val promotions = products.filter { it.isPromotion }
            promotionAdapter.updatePromotions(promotions)
            startAutoSlide(promotions.size)
        }
    }

    // ✅ Navegação para ProductDetailFragment
    private fun navigateToProductDetail(product: ProductItem) {
        val action = HomeFragmentDirections.actionHomeFragmentToProductDetailFragment(
            productId = product.id,
            categoryId = product.categoryId
        )
        findNavController().navigate(action)
    }

    // ✅ Função para iniciar o slideshow automático
    private fun startAutoSlide(itemCount: Int) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (itemCount > 0) {
                    currentPage = (currentPage + 1) % itemCount
                    binding.promotionViewPager.setCurrentItem(currentPage, true)
                    handler.postDelayed(this, slideInterval)
                }
            }
        }, slideInterval)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null) // Para o slideshow ao pausar
    }

    override fun onResume() {
        super.onResume()
        startAutoSlide(promotionAdapter.itemCount) // Reinicia o slideshow ao voltar
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
