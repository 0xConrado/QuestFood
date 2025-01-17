package com.quest.food.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quest.food.Address
import com.quest.food.MenuAdapter
import com.quest.food.MenuItem
import com.quest.food.R
import com.quest.food.User
import com.quest.food.ui.popup.PopupAddressFragment

class ProfileFragment : Fragment() {

    private lateinit var profileUserName: TextView
    private lateinit var profileTitle: TextView
    private lateinit var profileLevelText: TextView
    private lateinit var profileProgressBar: ProgressBar
    private lateinit var profileImageView: ImageView
    private lateinit var headerTitleText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileUserName = view.findViewById(R.id.profile_user_name)
        profileTitle = view.findViewById(R.id.profile_title)
        profileLevelText = view.findViewById(R.id.levelText)
        profileProgressBar = view.findViewById(R.id.levelProgressBar)
        profileImageView = view.findViewById(R.id.profileImage)
        headerTitleText = view.findViewById(R.id.title_text)

        // Define o título do Header a partir do nav_graph
        val navController = findNavController()
        val currentDestination = navController.currentDestination
        headerTitleText.text = currentDestination?.label ?: "Default Title"

        val menuRecyclerView = view.findViewById<RecyclerView>(R.id.menu_options_list)

        val menuItems = listOf(
            MenuItem(R.drawable.map_pin, getString(R.string.my_address)),
            MenuItem(R.drawable.heart, getString(R.string.wishlist)),
            MenuItem(R.drawable.page, getString(R.string.order_history)),
            MenuItem(R.drawable.credit_card, getString(R.string.cards)),
            MenuItem(R.drawable.password_cursor, getString(R.string.manage_passwords)),
            MenuItem(R.drawable.secure_window, getString(R.string.security)),
            MenuItem(R.drawable.settings, getString(R.string.settings))
        )

        val adapter = MenuAdapter(menuItems) { menuItem ->
            when (menuItem.title) {
                getString(R.string.my_address) -> showAddressPopup()
                // Adicione outros casos para diferentes itens do menu, se necessário
            }
        }
        menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        menuRecyclerView.adapter = adapter

        loadUserData()
        loadUserAddress() // Carregar o endereço do usuário
    }

    private fun showAddressPopup() {
        val addressPopup = PopupAddressFragment()
        addressPopup.show(parentFragmentManager, "AddressPopup")
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    profileUserName.text = user.username
                    profileTitle.text = user.title
                    profileLevelText.text = "Lvl ${user.level}"
                    profileProgressBar.progress = user.levelProgress

                    if (user.profileImagePath.isNotEmpty()) {
                        Glide.with(requireContext()).load(user.profileImagePath).into(profileImageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                profileUserName.text = getString(R.string.default_user_name)
                profileTitle.text = getString(R.string.default_user_email)
            }
        })
    }

    private fun loadUserAddress() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseReference.child("address").get().addOnSuccessListener { snapshot ->
            val address = snapshot.getValue(Address::class.java) // Certifique-se de que Address foi importado
            if (address != null) {
                // Exemplo de uso: Exibir Toast com o endereço carregado
                Toast.makeText(requireContext(), "Endereço carregado: ${address.street}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar endereço.", Toast.LENGTH_SHORT).show()
        }
    }

}