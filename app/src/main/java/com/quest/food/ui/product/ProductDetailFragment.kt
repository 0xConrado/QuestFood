package com.quest.food.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.model.ProductItem

class ProductDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = arguments?.getParcelable<ProductItem>("product")

        val productImage: ImageView = view.findViewById(R.id.productImageDetail)
        val productName: TextView = view.findViewById(R.id.productNameDetail)
        val productPrice: TextView = view.findViewById(R.id.productPriceDetail)
        val productDescription: TextView = view.findViewById(R.id.productDescriptionDetail)
        val buttonViewMore: Button = view.findViewById(R.id.buttonViewMore)

        product?.let {
            productName.text = it.name
            productPrice.text = "R$ ${it.price}"
            productDescription.text = it.description

            Glide.with(requireContext())
                .load(it.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(productImage)

            buttonViewMore.setOnClickListener {
                if (productDescription.maxLines == 3) {
                    productDescription.maxLines = Int.MAX_VALUE
                    buttonViewMore.text = "Ver menos"
                } else {
                    productDescription.maxLines = 3
                    buttonViewMore.text = "Ver mais"
                }
            }
        }
    }
}
