package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.User
import com.quest.food.ui.popup.PopupAddressFragment
import com.quest.food.ui.popup.PopupRegisterFragment

class ManageUsersAdapter(
    private val users: List<User>,
    private val onEditUserInfo: (String) -> Unit,
    private val onEditAddress: (String) -> Unit
) : RecyclerView.Adapter<ManageUsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.textUserName)
        val userEmail: TextView = itemView.findViewById(R.id.textUserEmail)
        val buttonEditInfo: Button = itemView.findViewById(R.id.buttonEditInfo)
        val buttonEditAddress: Button = itemView.findViewById(R.id.buttonEditAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.userName.text = user.username
        holder.userEmail.text = user.email

        holder.buttonEditInfo.setOnClickListener {
            val popupRegister = PopupRegisterFragment(user.id)
            popupRegister.show((holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "PopupRegisterFragment")
        }

        holder.buttonEditAddress.setOnClickListener {
            val popupAddress = PopupAddressFragment(user.id)
            popupAddress.show((holder.itemView.context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "PopupAddressFragment")
        }
    }

    override fun getItemCount(): Int = users.size
}
