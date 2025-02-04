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

        val userId = UserManager.getCurrentUserId() ?: return

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