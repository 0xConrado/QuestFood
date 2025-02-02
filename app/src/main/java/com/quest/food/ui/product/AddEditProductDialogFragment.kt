package com.quest.food.ui.product

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.model.ProductItem
import com.quest.food.viewmodel.ProductViewModel

class AddEditProductDialogFragment : DialogFragment() {

    private val productViewModel: ProductViewModel by viewModels()
    private var categoryId: String? = null
    private var product: ProductItem? = null

    private lateinit var editTextProductName: EditText
    private lateinit var editTextProductDescription: EditText
    private lateinit var editTextProductPrice: EditText
    private lateinit var editTextProductImageUrl: EditText
    private lateinit var editTextProductDiscountPrice: EditText
    private lateinit var checkboxPromotion: CheckBox
    private lateinit var checkboxBestSeller: CheckBox
    private lateinit var ingredientsContainer: LinearLayout
    private lateinit var buttonSaveProduct: Button
    private lateinit var imagePreview: ImageView

    private lateinit var editTextIngredient: EditText
    private lateinit var buttonAddIngredient: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString("categoryId")
        product = arguments?.getParcelable("product")

        if (product != null && categoryId == null) {
            categoryId = product?.categoryId  // Garantir o categoryId ao editar
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_product, null)

        editTextProductName = view.findViewById(R.id.editTextProductName)
        editTextProductDescription = view.findViewById(R.id.editTextProductDescription)
        editTextProductPrice = view.findViewById(R.id.editTextProductPrice)
        editTextProductImageUrl = view.findViewById(R.id.editTextProductImageUrl)
        editTextProductDiscountPrice = view.findViewById(R.id.editTextProductDiscountPrice)
        checkboxPromotion = view.findViewById(R.id.checkboxPromotion)
        checkboxBestSeller = view.findViewById(R.id.checkboxBestSeller)
        ingredientsContainer = view.findViewById(R.id.ingredientsContainer)
        buttonSaveProduct = view.findViewById(R.id.buttonSaveProduct)
        imagePreview = view.findViewById(R.id.imagePreview)

        editTextIngredient = view.findViewById(R.id.editTextIngredient)
        buttonAddIngredient = view.findViewById(R.id.buttonAddIngredient)

        buttonAddIngredient.setOnClickListener {
            val ingredient = editTextIngredient.text.toString().trim()
            if (ingredient.isNotEmpty()) {
                addIngredientToList(ingredient)
                editTextIngredient.text.clear()
            }
        }

        buttonSaveProduct.setOnClickListener { saveProduct() }

        editTextProductImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Glide.with(requireContext())
                    .load(s.toString())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imagePreview)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        product?.let {
            editTextProductName.setText(it.name)
            editTextProductDescription.setText(it.description)
            editTextProductPrice.setText(it.price.toString())
            editTextProductImageUrl.setText(it.imageUrl)
            editTextProductDiscountPrice.setText(it.originalPrice?.toString() ?: "")
            checkboxPromotion.isChecked = it.isPromotion
            checkboxBestSeller.isChecked = it.isBestSeller
            it.ingredients.forEach { ingredient -> addIngredientToList(ingredient) }
        }

        builder.setView(view)
        return builder.create()
    }

    private fun addIngredientToList(ingredient: String) {
        val ingredientView = TextView(requireContext()).apply {
            text = "- $ingredient"
            textSize = 16f
            setPadding(8, 4, 8, 4)
        }
        ingredientsContainer.addView(ingredientView)
    }

    private fun saveProduct() {
        val name = editTextProductName.text.toString()
        val description = editTextProductDescription.text.toString()
        val price = editTextProductPrice.text.toString().toDoubleOrNull() ?: 0.0
        val discountPrice = editTextProductDiscountPrice.text.toString().toDoubleOrNull()
        val imageUrl = editTextProductImageUrl.text.toString()
        val isPromotion = checkboxPromotion.isChecked
        val isBestSeller = checkboxBestSeller.isChecked

        val ingredients = (0 until ingredientsContainer.childCount).map {
            (ingredientsContainer.getChildAt(it) as TextView).text.toString().removePrefix("- ")
        }

        val updatedProduct = product?.copy(
            name = name,
            description = description,
            imageUrl = imageUrl,
            price = price,
            originalPrice = discountPrice,
            ingredients = ingredients,
            isPromotion = isPromotion,
            isBestSeller = isBestSeller,
            categoryId = categoryId ?: "",
            id = product?.id ?: "" // ✅ Mantém o ID do produto durante a edição
        ) ?: ProductItem(
            id = "",  // Novo produto
            name = name,
            description = description,
            imageUrl = imageUrl,
            price = price,
            originalPrice = discountPrice,
            ingredients = ingredients,
            isPromotion = isPromotion,
            isBestSeller = isBestSeller,
            categoryId = categoryId ?: ""
        )

        categoryId?.let { catId ->
            productViewModel.addOrUpdateProduct(catId, updatedProduct)
            Toast.makeText(requireContext(), "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show()
        }

        dismiss()
    }


    companion object {
        fun newInstance(categoryId: String): AddEditProductDialogFragment {
            val fragment = AddEditProductDialogFragment()
            val args = Bundle().apply {
                putString("categoryId", categoryId)
            }
            fragment.arguments = args
            return fragment
        }

        fun newInstance(product: ProductItem): AddEditProductDialogFragment {
            val fragment = AddEditProductDialogFragment()
            val args = Bundle().apply {
                putParcelable("product", product)
                putString("categoryId", product.categoryId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
