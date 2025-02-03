package com.quest.food.ui.popup

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
import com.quest.food.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

class PopupCheckoutFragment : DialogFragment() {

    private var _binding: FragmentPopupCheckoutBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o estilo para tela cheia
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

        userViewModel.loadUserData()
        val cartItems = arguments?.getParcelableArrayList<CartItem>("cartItems") ?: emptyList()

        binding.buttonConfirmOrder.setOnClickListener {
            val paymentMethod = if (binding.radioCard.isChecked) "Cartão de Crédito/Débito" else "Dinheiro"
            val deliveryOption = if (binding.radioDelivery.isChecked) "Delivery" else "Retirada"
            val observation = binding.observationField.text.toString()

            val user = userViewModel.user.value
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "" // ✅ Correção do userId

            val totalPrice = cartItems.sumOf { it.price * it.quantity }
            val deliveryFee = if (deliveryOption == "Delivery") 5.00 else 0.00
            val finalTotal = totalPrice + deliveryFee

            val order = Order(
                id = "",
                userId = userId, // ✅ Correção aqui
                items = cartItems,
                total = finalTotal,
                status = "Aguardando Aprovação",
                paymentMethod = paymentMethod,
                deliveryOption = deliveryOption,
                observation = observation,
                timestamp = System.currentTimeMillis()
            )

            sendOrderViaWhatsApp(paymentMethod, deliveryOption, observation, cartItems)
            saveOrderToFirebase(order)
        }
    }

    private fun sendOrderViaWhatsApp(paymentMethod: String, deliveryOption: String, observation: String, cartItems: List<CartItem>) {
        val user = userViewModel.user.value
        val address = user?.address // ✅ Certifique-se que Address existe no User

        val phoneNumber = "+5537999611408"
        //val phoneNumber = "+5537999696735" // thiago
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        val orderNumber = UUID.randomUUID().toString().take(8)

        val items = cartItems.joinToString("\n------------------------------\n") { item ->
            "➡ ${item.quantity}x ${item.productName} (R$${"%.2f".format(item.price)})"
        }

        val totalPrice = cartItems.sumOf { it.price * it.quantity }
        val deliveryFee = if (deliveryOption == "Delivery") 5.00 else 0.0
        val finalTotal = totalPrice + deliveryFee

        val orderLink = "https://questfood.app/pedido/$orderNumber"

        val message = """
            Pedido Quest Food ($currentTime): $orderNumber
            Estimativa: 30 - 50 minutos

            Acompanhe o pedido👇: $orderLink

            Tipo: $deliveryOption
            NOME: ${user?.username}
            Fone: ${user?.phone}
            Endereço: ${address?.street}, ${address?.number}
            CEP: ${address?.postalCode}
            Bairro: ${address?.neighborhood}
            Complemento: ${address?.complement}
            ------------------------------
            $items
            ------------------------------
            Itens: R$${"%.2f".format(totalPrice)}
            Entrega: R$${"%.2f".format(deliveryFee)}
            TOTAL: R$${"%.2f".format(finalTotal)}
            ------------------------------
            Pagamento: $paymentMethod

            Observação: $observation
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")
        }
        startActivity(intent)
    }

    private fun saveOrderToFirebase(order: Order) {
        val database = FirebaseDatabase.getInstance().getReference("orders")
        val orderId = database.push().key ?: return
        order.id = orderId

        database.child(orderId).setValue(order).addOnSuccessListener {
            Toast.makeText(context, "Pedido enviado com sucesso!", Toast.LENGTH_SHORT).show()
            dismiss()
        }.addOnFailureListener {
            Toast.makeText(context, "Erro ao salvar o pedido!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
