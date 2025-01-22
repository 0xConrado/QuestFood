package com.quest.food.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.quest.food.LoginActivity
import com.quest.food.R
import com.quest.food.databinding.FragmentProfileBinding
import com.quest.food.User
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

        // Carregar dados do usuário
        loadUserData()

        // Observar mudanças no usuário para redirecionar após logout
        mainViewModel.user.observe(viewLifecycleOwner, Observer { user ->
            if (user == null) navigateToLogin()
        })

        // Configurar botão de logout
        binding.buttonLogout.setOnClickListener {
            mainViewModel.logout()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return

        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return // Evita crashes

                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    updateProfileUI(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Se der erro, mostrar dados padrão
                showGuestUser()
            }
        })
    }

    private fun updateProfileUI(user: User) {
        binding.homeProfileSection.apply {
            profileUserName.text = user.username
            profileTitle.text = user.title
            levelText.text = "Lvl ${user.level}"
            levelProgressBar.progress = user.levelProgress

            // Carregar imagem do perfil corretamente
            Glide.with(this@ProfileFragment)
                .load(user.profileImagePath)
                .placeholder(R.drawable.user_default)
                .into(profileImage)
        }
    }

    private fun showGuestUser() {
        binding.homeProfileSection.apply {
            profileUserName.text = "Visitante"
            profileTitle.text = "Modo Anônimo"
            levelText.text = "Lvl 0"
            levelProgressBar.progress = 0
            profileImage.setImageResource(R.drawable.user_default) // Define imagem padrão
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
