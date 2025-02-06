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
                        userName = user.username,
                        items = cartItems,
                        total = finalTotal,
                        status = "Aguardando Aprovação",
                        paymentMethod = paymentMethod,
                        deliveryOption = deliveryOption,
                        observation = observation,
                        timestamp = System.currentTimeMillis()
                    )

                    saveOrderToFirebase(order) {
                        orderViewModel.clearUserCart(userId, onSuccess = {
                            showToast("Carrinho limpo com sucesso!")
                            cartViewModel.loadCartItems()

                            onCartCleared?.invoke()
                            sendOrderViaWhatsApp(paymentMethod, deliveryOption, observation, cartItems, user, orderId)
                            dismiss()
                        }, onFailure = {
                            showToast("Erro ao limpar o carrinho!")
                        })
                    }
                }
            } ?: run {
                showToast("Falha ao carregar dados do usuário.")
            }
        }
    }

    private fun sendOrderViaWhatsApp(
        paymentMethod: String,
        deliveryOption: String,
        observation: String,
        cartItems: List<CartItem>,
        user: User,
        orderId: String
    ) {
        val address = user.address
        val phoneNumber = "+5537999611408"
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        val items = cartItems.joinToString("\n        ") { item ->
            "   ➡️  ${item.quantity}x ${item.categoryName} ${item.productName} - R$${"%.2f".format(item.price)}"
        }

        val totalPrice = cartItems.sumOf { it.price * it.quantity }
        val deliveryFee = if (deliveryOption == "Delivery") 5.00 else 0.0
        val finalTotal = totalPrice + deliveryFee
        val orderLink = "https://questfood.app/pedido/$orderId"

        val message = """
        🛒 *Pedido Quest Food* ($currentTime)
        📦 *Número do Pedido:* $orderId
        ⏰ *Estimativa:* 30 - 50 minutos
        🔗 *Acompanhe o pedido:* $orderLink
        🚚 *Tipo de Entrega:* $deliveryOption
        🙍 *Nome:* ${user.username}
        📱 *Telefone:* ${user.phone}
        🏠 *Endereço:* ${address.street}, ${address.number}
        📍 *Bairro:* ${address.neighborhood}
        🗺️ *Complemento:* ${address.complement}
        📮 *CEP:* ${address.postalCode}
        🍽️ *Itens do Pedido:*
        --------------------------------------------------------------------------
        $items
        --------------------------------------------------------------------------
        💰 *Subtotal:* R$${"%.2f".format(totalPrice)}
        🚚 *Taxa de Entrega:* R$${"%.2f".format(deliveryFee)}
        💵 *TOTAL:* *R$${"%.2f".format(finalTotal)}*
        💳 *Forma de Pagamento:* $paymentMethod
        📝 *Observação:* ${if (observation.isNotEmpty()) observation else "Nenhuma"}
    """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun saveOrderToFirebase(order: Order, onSuccess: () -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("orders")
        database.child(order.id).setValue(order).addOnSuccessListener {
            showToast("Pedido enviado com sucesso!")
            onSuccess()
        }.addOnFailureListener {
            showToast("Erro ao salvar o pedido!")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
