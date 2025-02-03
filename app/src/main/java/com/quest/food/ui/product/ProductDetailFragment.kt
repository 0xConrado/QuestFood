package com.quest.food.ui.product

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.widget.Switch
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.viewmodel.ProductViewModel

class ProductDetailFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId") ?: return
        val categoryId = arguments?.getString("categoryId") ?: return

        val productImage: ImageView = view.findViewById(R.id.productImageDetail)
        val productName: TextView = view.findViewById(R.id.productNameDetail)
        val productOldPrice: TextView = view.findViewById(R.id.productOldPriceDetail)
        val productPrice: TextView = view.findViewById(R.id.productPriceDetail)
        val buttonAddToCart: Button = view.findViewById(R.id.addToCartButton)
        val ingredientsContainer: LinearLayout = view.findViewById(R.id.ingredientsContainer)

        // Aplicar o efeito de texto riscado ao preço antigo
        productOldPrice.paintFlags = productOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Buscar o produto pelo ID e categoria
        productViewModel.getProductById(categoryId, productId)

        // Observar o produto retornado
        productViewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            product?.let {
                productName.text = it.name
                if (it.originalPrice != null && it.originalPrice > it.price) {
                    productOldPrice.text = "R$ %.2f".format(it.originalPrice)
                    productOldPrice.visibility = View.VISIBLE
                } else {
                    productOldPrice.visibility = View.GONE
                }
                productPrice.text = "R$ %.2f".format(it.price)

                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)

                // Exibir lista de ingredientes reais
                ingredientsContainer.removeAllViews()
                it.ingredients.forEach { ingredient ->
                    val ingredientView = layoutInflater.inflate(R.layout.ingredient_item, ingredientsContainer, false)
                    val ingredientName: TextView = ingredientView.findViewById(R.id.ingredientName)
                    val ingredientSwitch: Switch = ingredientView.findViewById(R.id.ingredientSwitch)

                    ingredientName.text = ingredient
                    ingredientSwitch.isChecked = true  // Ingrediente selecionado por padrão

                    ingredientsContainer.addView(ingredientView)
                }

                buttonAddToCart.setOnClickListener {
                    // Lógica para adicionar o item ao pedido
                }
            }
        }
    }
}