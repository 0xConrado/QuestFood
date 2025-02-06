import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import com.quest.food.databinding.FragmentPopupQuestBinding
import com.quest.food.model.CategoryMenuItem
import com.quest.food.model.FoodMenuItem
import com.quest.food.model.Quest
import com.quest.food.viewmodel.CategoryViewModel
import com.quest.food.viewmodel.ProductViewModel

class PopupQuestFragment : DialogFragment() {

    private var _binding: FragmentPopupQuestBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val selectedCategories = mutableListOf<CategoryMenuItem>()
    private var selectedRewardProduct: FoodMenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPopupQuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectCategoriesButton.setOnClickListener {
            showCategorySelectionPopup()
        }

        binding.selectProductsButton.setOnClickListener {
            showProductSelectionPopup()
        }

        binding.createButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionTextView.text.toString()
            val quantity = binding.quantityEditText.text.toString().toIntOrNull() ?: 0
            val exp = binding.expEditText.text.toString().toIntOrNull() ?: 0
            val rewardQuantity = if (binding.rewardQuantityEditText.text.toString().isNotEmpty()) {
                binding.rewardQuantityEditText.text.toString().toInt()
            } else {
                1 // Valor padrão se o campo estiver vazio
            }
            val imageUrl = binding.imageUrlEditText.text.toString()

            val finalImageUrl = if (imageUrl.isNotEmpty()) imageUrl else selectedRewardProduct?.imageUrl ?: ""

            val quest = Quest(
                title = title,
                description = description,
                quantity = quantity,
                exp = exp,
                rewardCategoryId = selectedCategories.joinToString(",") { it.id },
                rewardProductId = selectedRewardProduct?.id ?: "",
                rewardQuantity = rewardQuantity,
                rewardImageUrl = finalImageUrl
            )

            saveQuest(quest)
        }

        binding.imageUrlEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatePreviewImage()
            }
        }

        binding.rewardQuantityEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateDescription()
            }
        }
    }

    private fun showCategorySelectionPopup() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_quest_participants_dialog, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.categoriesContainer)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            container?.removeAllViews()

            categories.forEach { category ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = category.title
                    isChecked = selectedCategories.any { it.id == category.id }
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedCategories.add(category)
                        } else {
                            selectedCategories.removeAll { it.id == category.id }
                        }
                        updateDescription()
                    }
                }
                container?.addView(checkBox)
            }
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            updateSelectedCategoriesView()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showProductSelectionPopup() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_quest_participants_dialog, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.categoriesContainer)
        val addButton = dialogView.findViewById<Button>(R.id.addButton)

        val categoryIds = selectedCategories.map { it.id }

        productViewModel.getProductsByCategoryIds(categoryIds) { products ->
            container?.removeAllViews()

            products.forEach { product ->
                val checkBox = CheckBox(requireContext()).apply {
                    text = product.name
                    isChecked = selectedRewardProduct?.id == product.id
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedRewardProduct = product
                        } else if (selectedRewardProduct?.id == product.id) {
                            selectedRewardProduct = null
                        }
                        updateDescription()
                        updatePreviewImage()
                    }
                }
                container?.addView(checkBox)
            }
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            updateSelectedRewardProductView()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateSelectedCategoriesView() {
        binding.selectedCategoriesContainer.removeAllViews()
        selectedCategories.forEach { category ->
            val textView = TextView(requireContext()).apply {
                text = category.title
                textSize = 16f
            }
            binding.selectedCategoriesContainer.addView(textView)
        }
    }

    private fun updateSelectedRewardProductView() {
        binding.selectProductsButton.text = selectedRewardProduct?.name ?: "Selecionar Produto de Recompensa"
    }

    private fun updateDescription() {
        val categoryNames = selectedCategories.joinToString(", ") { it.title }
        val productName = selectedRewardProduct?.name ?: "Produto"
        val quantity = binding.quantityEditText.text.toString()
        val rewardQuantity = binding.rewardQuantityEditText.text.toString().ifEmpty { "1" } // Padrão 1 se vazio

        binding.descriptionTextView.text = "Compre $quantity de $categoryNames e ganhe $rewardQuantity $productName"
    }

    private fun updatePreviewImage() {
        val imageUrl = binding.imageUrlEditText.text.toString()

        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext()).load(imageUrl).into(binding.previewImageView)
        } else {
            selectedRewardProduct?.imageUrl?.let {
                Glide.with(requireContext()).load(it).into(binding.previewImageView)
            }
        }
    }

    private fun saveQuest(quest: Quest) {
        val questRef = FirebaseDatabase.getInstance().getReference("quests").push()
        quest.id = questRef.key ?: ""
        questRef.setValue(quest).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(requireContext(), "Missão criada com sucesso!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Erro ao criar a missão", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
