package com.quest.food.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.R
import com.quest.food.adapter.ProfileMenuAdapter
import com.quest.food.adapter.ProfileMenuItem
import com.quest.food.databinding.FragmentProfileBinding
import com.quest.food.ui.popup.PopupAddressFragment
import com.quest.food.user.UserManager
import com.quest.food.viewmodel.MainViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        setupProfileMenu()

        // Carregar dados do usuário ao abrir o perfil
        loadUserData()

        // Simular ganho de XP ao clicar no botão
        binding.buttonGainXp.setOnClickListener {
            val userId = UserManager.getCurrentUserId() ?: return@setOnClickListener
            UserManager.addExperience(userId, 50) { level, progress, title ->
                updateLevelUI(level, progress, title)
            }
        }
    }

    private fun setupProfileMenu() {
        val menuItems = listOf(
            ProfileMenuItem("Meu Endereço", R.drawable.map_pin),
            ProfileMenuItem("Lista de Desejos", R.drawable.heart),
            ProfileMenuItem("Histórico de Pedidos", R.drawable.page),
            ProfileMenuItem("Notificações", R.drawable.bell_notification),
            ProfileMenuItem("Cartões de Pagamento", R.drawable.credit_card),
            ProfileMenuItem("Gerenciar Senhas", R.drawable.password_cursor),
            ProfileMenuItem("Segurança", R.drawable.window_lock),
            ProfileMenuItem("Configurações", R.drawable.settings),
            ProfileMenuItem("Logout", R.drawable.shutdown)
        )

        val adapter = ProfileMenuAdapter(menuItems) { item ->
            handleMenuItemClick(item.title)
        }

        binding.profileMenuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profileMenuRecyclerView.adapter = adapter
    }

    /**
     * Lida com os cliques nos itens do menu do perfil.
     */
    private fun handleMenuItemClick(title: String) {
        when (title) {
            "Meu Endereço" -> {
                val addressPopup = PopupAddressFragment()
                addressPopup.show(parentFragmentManager, "PopupAddressFragment")
            }
            "Logout" -> {
                logoutUser()
            }
            else -> {
                Toast.makeText(requireContext(), "Clicou em: $title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val userId = UserManager.getCurrentUserId() ?: return

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                val userName = snapshot.child("username").getValue(String::class.java) ?: "Usuário"
                val userTitle = snapshot.child("title").getValue(String::class.java) ?: "Novato"
                val profileImagePath = snapshot.child("profileImagePath").getValue(String::class.java) ?: ""
                val level = snapshot.child("level").getValue(Int::class.java) ?: 1
                val levelProgress = snapshot.child("levelProgress").getValue(Int::class.java) ?: 0

                updateLevelUI(level, levelProgress, userTitle)

                binding.homeProfileSection.apply {
                    profileUserName.text = userName

                    if (profileImagePath.isNotEmpty()) {
                        Glide.with(this@ProfileFragment)
                            .load(profileImagePath)
                            .placeholder(R.drawable.user_default)
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.user_default)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showGuestUser()
            }
        })
    }

    private fun updateLevelUI(level: Int, progress: Int, title: String) {
        binding.homeProfileSection.apply {
            levelText.text = "Lvl $level"
            levelProgressBar.max = level * 100
            levelProgressBar.progress = progress
            profileTitle.text = title
        }
    }

    private fun showGuestUser() {
        binding.homeProfileSection.apply {
            profileUserName.text = "Visitante"
            profileTitle.text = "Modo Anônimo"
            levelText.text = "Lvl 0"
            levelProgressBar.progress = 0
            profileImage.setImageResource(R.drawable.user_default)
        }
    }

    private fun logoutUser() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE
        FirebaseAuth.getInstance().signOut()
        mainViewModel.logout()

        if (isAdded && findNavController().currentDestination?.id == R.id.profileFragment) {
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
