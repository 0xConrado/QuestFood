package com.quest.food.ui.cart

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.adapter.CartAdapter
import com.quest.food.databinding.FragmentCartBinding
import com.quest.food.model.CartItem
import com.quest.food.ui.popup.PopupCheckoutFragment
import com.quest.food.viewmodel.CartViewModel
import com.quest.food.viewmodel.UserViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val cartViewModel: CartViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
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
            val cartItems = cartViewModel.cartItems.value
            if (!cartItems.isNullOrEmpty()) {
                userViewModel.loadUserData() // Ensure user data is loaded
                val popupCheckout = PopupCheckoutFragment()
                val bundle = Bundle().apply {
                    putParcelableArrayList("cartItems", ArrayList(cartItems))
                }
                popupCheckout.arguments = bundle
                popupCheckout.show(parentFragmentManager, "PopupCheckoutFragment")
            } else {
                Toast.makeText(requireContext(), "Seu carrinho está vazio!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openProductDetail(cartItem: CartItem) {
        Toast.makeText(requireContext(), "Detalhes do produto: ${cartItem.productName}", Toast.LENGTH_SHORT).show()
    }
}