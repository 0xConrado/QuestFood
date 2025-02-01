package com.quest.food.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.quest.food.R
import com.quest.food.databinding.FragmentLoginBinding
import com.quest.food.ui.popup.PopupRegisterFragment
import com.quest.food.viewmodel.LoginViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.GONE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        loginViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) navigateToHome()
        }

        loginViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let { showToast(it) }
        }

        loginViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loginProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            loginViewModel.loginWithEmail(email, password)
        }

        binding.buttonLoginGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.buttonLoginAnonymous.setOnClickListener {
            loginViewModel.loginAnonymously()
        }

        binding.textViewRegister.setOnClickListener {
            val registerPopup = PopupRegisterFragment()
            registerPopup.show(parentFragmentManager, "RegisterPopup")
        }
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { loginViewModel.loginWithGoogle(it) }
            } catch (e: ApiException) {
                showToast("Falha no login: ${e.message}")
            }
        }
    }

    private fun signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun navigateToHome() {
        requireActivity().findViewById<View>(R.id.bottomNavigationView)?.visibility = View.VISIBLE
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
