package com.quest.food.ui.product

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import com.quest.food.model.CartItem
import com.quest.food.viewmodel.CartViewModel
import com.quest.food.viewmodel.ProductViewModel

class ProductDetailFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

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
        val cartBadge: TextView = view.findViewById(R.id.cartBadge)

        cartBadge.text = "0"
        productOldPrice.paintFlags = productOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        productViewModel.getProductById(categoryId, productId)

        productViewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            product?.let {
                productName.text = it.name
                productPrice.text = "R$ %.2f".format(it.price)

                if (it.originalPrice != null && it.originalPrice > it.price) {
                    productOldPrice.text = "R$ %.2f".format(it.originalPrice)
                    productOldPrice.visibility = View.VISIBLE
                } else {
                    productOldPrice.visibility = View.GONE
                }

                Glide.with(requireContext())
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(productImage)

                ingredientsContainer.removeAllViews()
                it.ingredients.forEach { ingredient ->
                    val ingredientView = layoutInflater.inflate(R.layout.ingredient_item, ingredientsContainer, false)
                    val ingredientName: TextView = ingredientView.findViewById(R.id.ingredientName)
                    val ingredientSwitch: Switch = ingredientView.findViewById(R.id.ingredientSwitch)

                    ingredientName.text = ingredient
                    ingredientSwitch.isChecked = true

                    ingredientsContainer.addView(ingredientView)
                }

                buttonAddToCart.setOnClickListener {
                    val selectedIngredients = mutableListOf<String>()
                    for (i in 0 until ingredientsContainer.childCount) {
                        val view = ingredientsContainer.getChildAt(i)
                        val switch = view.findViewById<Switch>(R.id.ingredientSwitch)
                        val name = view.findViewById<TextView>(R.id.ingredientName)

                        if (switch.isChecked) {
                            selectedIngredients.add(name.text.toString())
                        }
                    }

                    getCategoryName(categoryId) { categoryName ->
                        val cartItem = CartItem(
                            id = "",
                            productId = product.id,
                            productName = product.name,
                            categoryName = categoryName,
                            price = product.price,
                            quantity = 1,
                            selectedIngredients = selectedIngredients,
                            timestamp = System.currentTimeMillis(),
                            categoryId = categoryId
                        )

                        cartViewModel.addToCart(cartItem)

                        val currentCount = cartBadge.text.toString().toInt()
                        cartBadge.text = (currentCount + 1).toString()
                        cartBadge.visibility = View.VISIBLE

                        Toast.makeText(requireContext(), "Item adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getCategoryName(categoryId: String, callback: (String) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("categories")
        database.child(categoryId).get().addOnSuccessListener { snapshot ->
            val categoryName = snapshot.child("title").getValue(String::class.java) ?: "Categoria"
            callback(categoryName)
        }
    }
}
