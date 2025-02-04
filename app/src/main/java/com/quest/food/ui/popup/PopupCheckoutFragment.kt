package com.quest.food.ui.popup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.databinding.FragmentPopupCheckoutBinding
import com.quest.food.model.CartItem
import com.quest.food.model.Order
import com.quest.food.model.User
import com.quest.food.viewmodel.CartViewModel
import com.quest.food.viewmodel.OrderViewModel
import com.quest.food.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class PopupCheckoutFragment(private val onCartCleared: (() -> Unit)? = null) : DialogFragment() {

    private var _binding: FragmentPopupCheckoutBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPopupCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userViewModel.getUserById(userId) { user ->
            user?.let {
                val cartItems = arguments?.getParcelableArrayList<CartItem>("cartItems") ?: emptyList()

                binding.buttonConfirmOrder.setOnClickListener {
                    val paymentMethod = if (binding.radioCard.isChecked) "Cartão de Crédito/Débito" else "Dinheiro"
                    val deliveryOption = if (binding.radioDelivery.isChecked) "Delivery" else "Retirada"
                    val observation = binding.observationField.text.toString()

                    val totalPrice = cartItems.sumOf { it.price * it.quantity }
                    val deliveryFee = if (deliveryOption == "Delivery") 5.00 else 0.00
                    val finalTotal = totalPrice + deliveryFee

                    val database = FirebaseDatabase.getInstance().getReference("orders")
                    val orderId = database.push().key ?: return@setOnClickListener

                    val order = Order(
                        id = orderId,
                        userId = userId,
                        items = cartItems,
                        total = finalTotal,
                        status = "Aguardando Aprovação",
                        paymentMethod = paymentMethod,
                        deliveryOption = deliveryOption,
                        observation = observation,
                        timestamp = System.currentTimeMillis()
                    )

                    saveOrderToFirebase(order) {
                        orderViewModel.clearUserCart(userId, {
                            requireContext().safeToast("Carrinho limpo com sucesso!")
                            cartViewModel.loadCartItems()

                            // ✅ Chama o callback para informar o CartFragment
                            onCartCleared?.invoke()

                            dismiss() // Fecha o Popup
                        }, {
                            requireContext().safeToast("Erro ao limpar o carrinho!")
                        })
                    }
                }
            } ?: run {
                requireContext().safeToast("Falha ao carregar dados do usuário.")
            }
        }
    }

    private fun saveOrderToFirebase(order: Order, onSuccess: () -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("orders")
        database.child(order.id).setValue(order).addOnSuccessListener {
            requireContext().safeToast("Pedido enviado com sucesso!")
            onSuccess()
        }.addOnFailureListener {
            requireContext().safeToast("Erro ao salvar o pedido!")
        }
    }

    private fun Context.safeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
