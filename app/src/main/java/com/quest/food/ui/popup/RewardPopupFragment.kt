package com.quest.food.ui.popup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.quest.food.R
import com.quest.food.model.CartItem
import com.quest.food.viewmodel.CartViewModel
import com.quest.food.viewmodel.QuestViewModel
import com.quest.food.viewmodel.UserViewModel
import com.quest.food.model.Quest

class RewardPopupFragment : DialogFragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var questViewModel: QuestViewModel
    private lateinit var cartViewModel: CartViewModel

    private lateinit var rewardImage: ImageView
    private lateinit var rewardTitle: TextView
    private lateinit var rewardQuantity: TextView
    private lateinit var rewardExp: TextView
    private lateinit var receiveButton: Button

    private lateinit var rewardCategoryNames: String // Variável para armazenar o nome da categoria

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reward_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        questViewModel = ViewModelProvider(requireActivity()).get(QuestViewModel::class.java)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        rewardImage = view.findViewById(R.id.rewardImageView)
        rewardTitle = view.findViewById(R.id.rewardTitleTextView)
        rewardQuantity = view.findViewById(R.id.rewardQuantityTextView)
        rewardExp = view.findViewById(R.id.rewardExpTextView)
        receiveButton = view.findViewById(R.id.receiveButton)

        // Recuperando os argumentos passados
        val questId = arguments?.getString(ARG_QUEST_ID) ?: ""
        val rewardProductId = arguments?.getString(ARG_REWARD_PRODUCT_ID) ?: ""
        val rewardProductName = arguments?.getString(ARG_REWARD_PRODUCT_NAME) ?: "Recompensa"
        val rewardQty = arguments?.getInt(ARG_REWARD_QUANTITY) ?: 0
        val expGain = arguments?.getDouble(ARG_EXP_GAIN) ?: 0.0
        val rewardImageUrl = arguments?.getString(ARG_REWARD_IMAGE) ?: ""
        rewardCategoryNames = arguments?.getString(ARG_REWARD_CATEGORY_NAMES) ?: ""  // Corrigido aqui

        // Verificar se o nome da recompensa foi passado corretamente
        Toast.makeText(requireContext(), "Produto recebido: $rewardProductName", Toast.LENGTH_SHORT).show()

        setupRewardUI(questId, rewardProductId, rewardProductName, rewardQty, expGain, rewardImageUrl)
    }

    private fun setupRewardUI(
        questId: String,
        rewardProductId: String,
        rewardProductName: String,
        rewardQty: Int,
        expGain: Double,
        rewardImageUrl: String
    ) {
        // Usando diretamente rewardCategoryNames, sem buscar no ViewModel
        rewardTitle.text = "${rewardCategoryNames}: $rewardProductName"

        rewardQuantity.text = "Quantidade: $rewardQty"
        rewardExp.text = "XP Ganhado: $expGain"

        if (rewardImageUrl.isNotEmpty()) {
            Glide.with(requireContext()).load(rewardImageUrl).into(rewardImage)
        }

        questViewModel.getQuestById(questId)?.let { quest ->
            if (quest.rewardClaimed) {
                receiveButton.text = "Recompensa Recebida"
                receiveButton.isEnabled = false
                return
            }
        }

        receiveButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null || questId.isEmpty()) {
                Toast.makeText(requireContext(), "Erro ao recuperar dados do usuário ou missão.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            questViewModel.getQuestById(questId)?.let { quest ->
                if (quest.rewardClaimed) {
                    Toast.makeText(requireContext(), "Você já resgatou esta recompensa.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Criação do item de recompensa no carrinho
                val rewardItem = CartItem(
                    id = "",
                    productId = rewardProductId,
                    productName = rewardProductName,
                    categoryName = rewardCategoryNames,  // Usando o nome da categoria aqui
                    quantity = rewardQty.toDouble(),
                    price = 0.0,
                    selectedIngredients = emptyList(),
                    timestamp = System.currentTimeMillis(),
                    categoryId = "reward"
                )
                cartViewModel.addToCart(rewardItem)

                // Adicionando XP ao usuário
                userViewModel.addExperience(userId, expGain.toInt()) {
                    if (isAdded && context != null) {
                        Toast.makeText(requireContext(), "$rewardQty x $rewardProductName adicionado ao carrinho e XP atualizado!", Toast.LENGTH_SHORT).show()
                    }
                }

                // Atualizando a missão para refletir que a recompensa foi resgatada
                val updatedQuest = quest.copy(
                    rewardProductName = rewardProductName,
                    rewardClaimed = true // Marcando a recompensa como recebida
                )
                questViewModel.markRewardAsReceived(questId)  // Atualizando a missão corretamente

                // Desabilitar o botão e atualizar o texto
                receiveButton.text = "Recompensa Recebida"
                receiveButton.isEnabled = false

                dismissAllowingStateLoss()
            }
        }
    }

    companion object {
        private const val ARG_QUEST_ID = "quest_id"
        private const val ARG_REWARD_PRODUCT_ID = "reward_product_id"
        private const val ARG_REWARD_PRODUCT_NAME = "reward_product_name"
        private const val ARG_REWARD_QUANTITY = "reward_quantity"
        private const val ARG_EXP_GAIN = "exp_gain"
        private const val ARG_REWARD_IMAGE = "reward_image"
        private const val ARG_REWARD_CATEGORY_NAMES = "reward_category_names"  // Corrigido aqui

        fun newInstance(
            questId: String,
            rewardProductId: String,
            rewardProductName: String,
            rewardQuantity: Int,
            expGain: Double,
            rewardImageUrl: String = "",
            rewardCategoryNames: String // Corrigido aqui
        ) = RewardPopupFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_QUEST_ID, questId)
                putString(ARG_REWARD_PRODUCT_ID, rewardProductId)
                putString(ARG_REWARD_PRODUCT_NAME, rewardProductName)
                putInt(ARG_REWARD_QUANTITY, rewardQuantity)
                putDouble(ARG_EXP_GAIN, expGain)
                putString(ARG_REWARD_IMAGE, rewardImageUrl)
                putString(ARG_REWARD_CATEGORY_NAMES, rewardCategoryNames)  // Corrigido aqui
            }
        }
    }
}
