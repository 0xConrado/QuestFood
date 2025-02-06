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
import com.quest.food.viewmodel.QuestViewModel
import com.quest.food.viewmodel.UserViewModel

class RewardPopupFragment : DialogFragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var questViewModel: QuestViewModel
    private lateinit var rewardImage: ImageView
    private lateinit var rewardTitle: TextView
    private lateinit var rewardQuantity: TextView
    private lateinit var rewardExp: TextView
    private lateinit var receiveButton: Button

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

        rewardImage = view.findViewById(R.id.rewardImageView)
        rewardTitle = view.findViewById(R.id.rewardTitleTextView)
        rewardQuantity = view.findViewById(R.id.rewardQuantityTextView)
        rewardExp = view.findViewById(R.id.rewardExpTextView)
        receiveButton = view.findViewById(R.id.receiveRewardButton)

        val questId = arguments?.getString(ARG_QUEST_ID) ?: ""
        val rewardProductId = arguments?.getString(ARG_REWARD_PRODUCT_ID) ?: ""
        val rewardQty = arguments?.getInt(ARG_REWARD_QUANTITY) ?: 0
        val expGain = arguments?.getInt(ARG_EXP_GAIN) ?: 0
        val rewardImageUrl = arguments?.getString(ARG_REWARD_IMAGE) ?: ""

        rewardTitle.text = "Recompensa: $rewardProductId"
        rewardQuantity.text = "Quantidade: $rewardQty"
        rewardExp.text = "XP Ganhado: $expGain"

        if (rewardImageUrl.isNotEmpty()) {
            Glide.with(requireContext()).load(rewardImageUrl).into(rewardImage)
        }

        receiveButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null || questId.isEmpty()) {
                Toast.makeText(requireContext(), "Erro ao recuperar dados do usuário ou missão.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.addExperience(userId, expGain) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "XP atualizado!", Toast.LENGTH_SHORT).show()
                }
            }

            questViewModel.markQuestAsCompleted(questId) // ✅ Agora questId é uma String válida

            dismiss()
        }
    }

    companion object {
        private const val ARG_QUEST_ID = "quest_id"
        private const val ARG_REWARD_PRODUCT_ID = "reward_product_id"
        private const val ARG_REWARD_QUANTITY = "reward_quantity"
        private const val ARG_EXP_GAIN = "exp_gain"
        private const val ARG_REWARD_IMAGE = "reward_image"

        fun newInstance(
            questId: String, // ✅ Adicionando questId como parâmetro obrigatório
            rewardProductId: String,
            rewardQuantity: Int,
            expGain: Int,
            rewardImageUrl: String = ""
        ) = RewardPopupFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_QUEST_ID, questId)
                putString(ARG_REWARD_PRODUCT_ID, rewardProductId)
                putInt(ARG_REWARD_QUANTITY, rewardQuantity)
                putInt(ARG_EXP_GAIN, expGain)
                putString(ARG_REWARD_IMAGE, rewardImageUrl)
            }
        }
    }
}
