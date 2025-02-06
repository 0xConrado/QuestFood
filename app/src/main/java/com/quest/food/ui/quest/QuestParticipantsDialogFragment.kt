import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.quest.food.databinding.FragmentQuestParticipantsDialogBinding
import com.quest.food.databinding.QuestParticipantsItemBinding
import com.quest.food.viewmodel.CategoryViewModel

class QuestParticipantsDialogFragment : DialogFragment() {

    private var _binding: FragmentQuestParticipantsDialogBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()

    private val selectedCategories = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestParticipantsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            binding.categoriesContainer.removeAllViews()

            categories.forEach { category ->
                val itemBinding = QuestParticipantsItemBinding.inflate(layoutInflater)

                itemBinding.categoryName.text = category.title
                itemBinding.categoryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedCategories.add(category.id)
                    else selectedCategories.remove(category.id)
                }

                binding.categoriesContainer.addView(itemBinding.root)
            }
        }

        binding.addButton.setOnClickListener {
            // Logic to handle selected categories
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
