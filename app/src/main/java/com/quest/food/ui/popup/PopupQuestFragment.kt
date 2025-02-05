import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.databinding.FragmentPopupQuestBinding
import com.quest.food.model.Quest
import com.quest.food.viewmodel.CategoryViewModel
import com.quest.food.viewmodel.ProductViewModel

class PopupQuestFragment : DialogFragment() {  // Mudança para DialogFragment

    private var _binding: FragmentPopupQuestBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()

    private lateinit var selectedCategoryId: String
    private lateinit var selectedProductId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPopupQuestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carregar categorias
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryTitles = categories.map { it.title }
            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryTitles)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.rewardCategorySpinner.adapter = categoryAdapter

            binding.rewardCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCategoryId = categories[position].id
                    loadProductsForCategory(selectedCategoryId)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Quando o usuário seleciona um produto da categoria
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            val productNames = products.map { it.name }
            val productAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, productNames)
            productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.rewardProductSpinner.adapter = productAdapter

            binding.rewardProductSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedProductId = products[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Ao clicar em "Criar Missão", salvar a missão no banco de dados
        binding.createButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val quantity = binding.quantityEditText.text.toString().toIntOrNull() ?: 0
            val exp = binding.expEditText.text.toString().toIntOrNull() ?: 0
            val rewardQuantity = binding.rewardQuantityEditText.text.toString().toIntOrNull() ?: 0
            val imageUrl = binding.imageUrlEditText.text.toString()

            val quest = Quest(
                title = title,
                description = description,
                quantity = quantity,
                exp = exp,
                rewardCategoryId = selectedCategoryId,
                rewardProductId = selectedProductId,
                rewardQuantity = rewardQuantity,
                rewardImageUrl = imageUrl
            )

            // Salvar no banco de dados
            saveQuest(quest)
        }
    }

    private fun loadProductsForCategory(categoryId: String) {
        productViewModel.loadProductsForCategory(categoryId)
    }

    private fun saveQuest(quest: Quest) {
        val questRef = FirebaseDatabase.getInstance().getReference("quests").push()
        quest.id = questRef.key ?: ""
        questRef.setValue(quest).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(requireContext(), "Missão criada com sucesso!", Toast.LENGTH_SHORT).show()
                dismiss()  // Fechar o diálogo após salvar a missão
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
