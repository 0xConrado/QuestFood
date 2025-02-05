package com.quest.food.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
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

        // Configuração do Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Web Client ID do Firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Botão de login com email e senha
        binding.buttonLogin.setOnClickListener {
            val input = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                loginViewModel.loginWithEmail(input, password)
            } else {
                loginViewModel.loginWithUsername(input, password)
            }
        }

        // Popup para cadastro
        binding.textViewRegister.setOnClickListener {
            val registerPopup = PopupRegisterFragment()
            registerPopup.show(parentFragmentManager, "RegisterPopup")
        }

        // Botão de login com Google
        binding.buttonLoginGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 9001)  // Solicitar a autenticação do Google
        }

        // Observando o estado de login do usuário
        loginViewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                verificarRoleDoUsuario(user.uid)
            }
        }

        loginViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Resultado do login com o Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 9001) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (task.isSuccessful) {
                val account = task.result
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } else {
                Toast.makeText(requireContext(), "Falha no login com o Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de autenticação do Firebase com Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    val database = FirebaseDatabase.getInstance().getReference("users").child(user!!.uid)

                    database.get().addOnSuccessListener { snapshot ->
                        if (!snapshot.exists()) {
                            val newUser = mapOf(
                                "username" to (user.displayName ?: "Usuário"),
                                "email" to (user.email ?: ""),
                                "phone" to (user.phoneNumber ?: ""),
                                "birthday" to "",
                                "title" to "Novato",
                                "level" to 1,
                                "levelProgress" to 0,
                                "profileImagePath" to (user.photoUrl?.toString() ?: ""),
                                "role" to "user"
                            )
                            database.setValue(newUser)
                        }
                        verificarRoleDoUsuario(user.uid)
                    }
                } else {
                    Toast.makeText(requireContext(), "Falha na autenticação com o Google", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Verificar a role do usuário após login
    private fun verificarRoleDoUsuario(userId: String) {
        val database = FirebaseDatabase.getInstance().getReference("users").child(userId)

        database.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").getValue(String::class.java) ?: "user"

            val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottomNavigationView)
            bottomNavigationView?.visibility = View.VISIBLE

            Toast.makeText(requireContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao verificar a role do usuário.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
