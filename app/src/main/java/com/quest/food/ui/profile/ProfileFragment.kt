package com.quest.food.ui.profile

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quest.food.Address
import com.quest.food.MenuAdapter
import com.quest.food.MenuItem
import com.quest.food.R
import com.quest.food.User
import com.quest.food.ui.popup.PopupAddressFragment
import java.io.File

class ProfileFragment : Fragment() {

    private var profileUserName: TextView? = null
    private var profileTitle: TextView? = null
    private var profileLevelText: TextView? = null
    private var profileProgressBar: ProgressBar? = null
    private var profileImageView: ImageView? = null
    private var headerTitleText: TextView? = null
    private var menuRecyclerView: RecyclerView? = null

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            profileUserName = view.findViewById(R.id.profile_user_name)
            profileTitle = view.findViewById(R.id.profile_title)
            profileLevelText = view.findViewById(R.id.levelText)
            profileProgressBar = view.findViewById(R.id.levelProgressBar)
            profileImageView = view.findViewById(R.id.profileImage)
            headerTitleText = view.findViewById(R.id.title_text)
            menuRecyclerView = view.findViewById(R.id.menu_options_list)
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Erro ao inicializar Views: ${e.message}")
            return
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val navController = findNavController()
        val currentDestination = navController.currentDestination
        headerTitleText?.text = currentDestination?.label ?: "Default Title"

        setupMenu()

        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), "Erro: Usuário não está logado.", Toast.LENGTH_SHORT).show()
            return
        }

        if (auth.currentUser?.isAnonymous == true) {
            setupGuestUser()
        } else {
            loadUserData()
            loadUserAddress()
        }
    }

    private fun setupMenu() {
        menuRecyclerView?.apply {
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
                }
            }
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    private fun showAddressPopup() {
        val addressPopup = PopupAddressFragment()
        addressPopup.show(parentFragmentManager, "AddressPopup")
    }

    private fun setupGuestUser() {
        profileUserName?.text = "Guest"
        profileTitle?.text = "Modo Visitante"
        profileLevelText?.text = "Lvl 0"
        profileProgressBar?.progress = 0
        profileImageView?.setImageResource(R.drawable.user_default)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Log.e("ProfileFragment", "Usuário não está logado.")
            return
        }

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    profileUserName?.text = user.username
                    profileTitle?.text = user.title
                    profileLevelText?.text = "Lvl ${user.level}"
                    profileProgressBar?.progress = user.levelProgress

                    val imagePath = user.profileImagePath
                    if (!imagePath.isNullOrEmpty()) {
                        val imageFile = File(imagePath)
                        if (imageFile.exists()) {
                            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                            profileImageView?.setImageBitmap(bitmap)
                        } else {
                            profileImageView?.setImageResource(R.drawable.user_default)
                            Toast.makeText(requireContext(), "Imagem de perfil não encontrada localmente.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        profileImageView?.setImageResource(R.drawable.user_default)
                    }
                } else {
                    Log.e("ProfileFragment", "Usuário não encontrado no banco de dados.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar dados do usuário: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserAddress() {
        val userId = auth.currentUser?.uid ?: return
        val databaseReference = database.child(userId).child("address")

        databaseReference.get().addOnSuccessListener { snapshot ->
            val address = snapshot.getValue(Address::class.java)
            if (address != null) {
                Toast.makeText(requireContext(), "Endereço carregado: ${address.street}", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("ProfileFragment", "Endereço não encontrado no banco de dados.")
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar endereço.", Toast.LENGTH_SHORT).show()
        }
    }
}
