package com.quest.food.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.quest.food.adapter.ManageUsersAdapter
import com.quest.food.databinding.FragmentManageUsersBinding
import com.quest.food.ui.popup.PopupAddressFragment
import com.quest.food.ui.popup.PopupRegisterFragment
import com.quest.food.viewmodel.UserViewModel

class ManageUsersFragment : Fragment() {

    private var _binding: FragmentManageUsersBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewManageUsers.layoutManager = LinearLayoutManager(requireContext())

        userViewModel.allUsers.observe(viewLifecycleOwner) { users ->
            binding.recyclerViewManageUsers.adapter = ManageUsersAdapter(
                users,
                onEditUserInfo = { userId ->
                    val userInfoPopup = PopupRegisterFragment(userId)
                    userInfoPopup.show(parentFragmentManager, "PopupRegisterFragment")
                },
                onEditAddress = { userId ->
                    val addressPopup = PopupAddressFragment(userId)
                    addressPopup.show(parentFragmentManager, "PopupAddressFragment")
                }
            )
        }

        userViewModel.loadAllUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
