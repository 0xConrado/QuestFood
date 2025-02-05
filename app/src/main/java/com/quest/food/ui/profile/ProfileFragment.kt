package com.quest.food.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import com.quest.food.adapter.ProfileMenuAdapter
import com.quest.food.databinding.FragmentProfileBinding
import com.quest.food.model.ProfileMenuItem
import com.quest.food.ui.popup.PopupAddressFragment
import com.quest.food.ui.popup.PopupRegisterFragment
import com.quest.food.user.UserManager
import com.bumptech.glide.Glide
import com.quest.food.model.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        val menuItems = listOf(
            ProfileMenuItem("Editar Informações", R.drawable.ic_id),
            ProfileMenuItem("Editar Endereço", R.drawable.ic_street),
            ProfileMenuItem("Histórico de Pedidos", R.drawable.page),
            ProfileMenuItem("Logout", R.drawable.shutdown)
        )

        val adapter = ProfileMenuAdapter(menuItems) { item ->
            handleMenuItemClick(item.title, userId)
        }

        binding.profileMenuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profileMenuRecyclerView.adapter = adapter

        // Carregar as informações do usuário
        loadUserProfile(userId)

        // Lógica para o botão de ganhar XP
        binding.buttonGainXp.setOnClickListener {
            gainExperience(userId)
        }
    }

    // Função para carregar os dados do usuário no layout
    private fun loadUserProfile(userId: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            user?.let {
                // Verifica se displayName e photoUrl estão disponíveis
                val userName = it.username.takeIf { it.isNotEmpty() } ?: "Usuário"
                val userTitle = it.title.takeIf { it.isNotEmpty() } ?: "Sem Título"

                // Atualizar os dados no layout_profile_section.xml
                binding.homeProfileSection.profileUserName.text = userName
                binding.homeProfileSection.profileTitle.text = userTitle

                // Se a foto do perfil estiver disponível, configure-a
                if (it.profileImagePath.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(it.profileImagePath)
                        .into(binding.homeProfileSection.profileImage)
                }

                // Atualizar o nível e progresso
                binding.homeProfileSection.levelText.text = "Lvl ${it.level}"
                binding.homeProfileSection.levelProgressBar.progress = it.levelProgress

                // Atualizar o limite da ProgressBar com o XP necessário para o próximo nível
                val xpRequired = it.level * 100
                binding.homeProfileSection.levelProgressBar.max = xpRequired
            } ?: run {
                Toast.makeText(requireContext(), "Erro ao carregar os dados do usuário", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar os dados do usuário", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para ganhar XP e atualizar o ProgressBar
    private fun gainExperience(userId: String) {
        val xpGained = 50  // XP ganho por clique no botão

        // Chama o UserManager para adicionar XP e atualizar o título
        UserManager.addExperience(userId, xpGained) { level, progress, newTitle ->
            // Atualiza a interface após a experiência ser adicionada
            binding.homeProfileSection.levelText.text = "Lvl $level"
            binding.homeProfileSection.levelProgressBar.progress = progress

            // Calcula o XP necessário para o próximo nível
            val xpRequired = level * 100

            // Ajusta o máximo da ProgressBar conforme o XP necessário para o próximo nível
            binding.homeProfileSection.levelProgressBar.max = xpRequired

            // Atualiza o título do usuário
            binding.homeProfileSection.profileTitle.text = newTitle

            // Mostra uma mensagem de sucesso
            Toast.makeText(requireContext(), "+$xpGained XP! Agora você é Lvl $level", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleMenuItemClick(title: String, userId: String) {
        when (title) {
            "Editar Informações" -> {
                val popupRegister = PopupRegisterFragment(userId)
                popupRegister.show(parentFragmentManager, "PopupRegisterFragment")
            }
            "Editar Endereço" -> {
                val popupAddress = PopupAddressFragment(userId)
                popupAddress.show(parentFragmentManager, "PopupAddressFragment")
            }
            "Histórico de Pedidos" -> {
                findNavController().navigate(R.id.orderHistoryFragment)
            }
            "Logout" -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.loginFragment)
            }
            else -> {
                Toast.makeText(requireContext(), "Clicou em: $title", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
