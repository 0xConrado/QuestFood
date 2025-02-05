package com.quest.food

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quest.food.databinding.ActivityMainBinding
import com.quest.food.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Configurar NavController corretamente
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val titleTextView = findViewById<TextView>(R.id.title_text)
            titleTextView.text = destination.label ?: "Quest Food"

            // Ocultar o header em telas específicas, se necessário
            val headerSection = findViewById<View>(R.id.header_section)
            if (destination.id == R.id.splashFragment || destination.id == R.id.loginFragment) {
                headerSection.visibility = View.GONE
            } else {
                headerSection.visibility = View.VISIBLE
            }
        }

        // Observar mudanças no usuário e atualizar o perfil
        observeUserProfile()
    }

    private fun observeUserProfile() {
        viewModel.user.observe(this, Observer { user ->
            user?.let { updateUserProfile(it) }
        })
    }

    private fun updateUserProfile(user: FirebaseUser) {
        val database = FirebaseDatabase.getInstance().getReference("users")
        val uid = user.uid

        database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: "Usuário"

                    Log.d("MainActivity", "Nome do usuário: $name")

                    // Atualiza o nome do usuário no layout ativo
                    val currentFragment = supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment)
                        ?.childFragmentManager
                        ?.fragments
                        ?.firstOrNull()

                    currentFragment?.view?.findViewById<TextView>(R.id.profileUserName)?.let { profileNameTextView ->
                        profileNameTextView.text = name
                    } ?: Log.e("MainActivity", "Erro: profile_user_name não encontrado no fragmento ativo")
                } else {
                    Log.e("MainActivity", "Erro: Snapshot do usuário não encontrado no Firebase")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Erro ao buscar usuário no Firebase: ${error.message}")
            }
        })
    }
}
