package com.quest.food.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.R
import com.quest.food.adapter.CartAdapter
import com.quest.food.databinding.FragmentCartBinding
import com.quest.food.model.CartItem
import com.quest.food.viewmodel.CartViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartAdapter = CartAdapter(
            mutableListOf(),
            onProductClick = { cartItem -> openProductDetail(cartItem) },
            onDeleteClick = { cartItem -> cartViewModel.removeFromCart(cartItem) }
        )

        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter

        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.updateCartItems(items)
        }

        binding.buttonCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Finalizando Compra!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openProductDetail(cartItem: CartItem) {
        val bundle = Bundle().apply {
            putString("productId", cartItem.productId)
            putString("categoryId", cartItem.categoryId)
        }
        findNavController().navigate(R.id.productDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
