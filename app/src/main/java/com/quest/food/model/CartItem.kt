package com.quest.food.model

import android.os.Parcel
import android.os.Parcelable

data class CartItem(
    var id: String = "",
    var productId: String = "",
    var productName: String = "",
    var categoryName: String = "",
    var quantity: Double = 1.0,
    var price: Double = 0.00,
    var selectedIngredients: List<String> = emptyList(),
    var timestamp: Long = System.currentTimeMillis(),
    var categoryId: String? = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readLong(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(productId)
        parcel.writeString(productName)
        parcel.writeString(categoryName)
        parcel.writeDouble(quantity)
        parcel.writeDouble(price)
        parcel.writeStringList(selectedIngredients)
        parcel.writeLong(timestamp)
        parcel.writeString(categoryId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CartItem> {
        override fun createFromParcel(parcel: Parcel): CartItem {
            return CartItem(parcel)
        }

        override fun newArray(size: Int): Array<CartItem?> {
            return arrayOfNulls(size)
        }
    }
}
