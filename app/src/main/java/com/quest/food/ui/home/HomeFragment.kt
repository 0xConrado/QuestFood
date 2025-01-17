package com.quest.food.ui.home

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.quest.food.Category
import com.quest.food.R
import com.quest.food.User
import com.quest.food.databinding.FragmentHomeBinding
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoriesAdapter

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentUserRole: String = "user" // Role padrão do usuário

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // Configurar RecyclerView
        setupCategoriesAdapter()

        // Configurar título do header
        setupHeaderTitle()

        // Configurar dados do usuário
        if (auth.currentUser?.isAnonymous == true) {
            setupGuestUser()
        } else {
            loadUserData()
        }

        // Carregar categorias do Firebase
        fetchUserRoleAndLoadCategories()
    }

    private fun setupHeaderTitle() {
        // Verifica se o cabeçalho está disponível e atualiza o título
        val navController = findNavController()
        val headerTitleTextView = binding.root.findViewById<TextView>(R.id.title_text)
        headerTitleTextView?.text = navController.currentDestination?.label ?: "Default Title"
    }

    private fun setupGuestUser() {
        binding.homeProfileSection.apply {
            profileUserName.text = "Guest"
            profileTitle.text = "Modo Visitante"
            levelText.text = "Lvl 0"
            levelProgressBar.progress = 0
            profileImage.setImageResource(R.drawable.user_default) // Define imagem padrão
        }
        currentUserRole = "guest"
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || _binding == null) return // Evita o uso do binding nulo
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.homeProfileSection.apply {
                            profileUserName.text = user.username
                            profileTitle.text = user.title
                            levelText.text = "Lvl ${user.level}"
                            levelProgressBar.progress = user.levelProgress

                            val imagePath = user.profileImagePath
                            if (!imagePath.isNullOrEmpty()) {
                                val imageFile = File(imagePath)
                                if (imageFile.exists()) {
                                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                                    profileImage.setImageBitmap(bitmap)
                                } else {
                                    profileImage.setImageResource(R.drawable.user_default)
                                    Toast.makeText(
                                        requireContext(),
                                        "Imagem de perfil não encontrada localmente.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                profileImage.setImageResource(R.drawable.user_default)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isAdded || _binding == null) return // Evita o uso do binding nulo
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar dados do usuário: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            setupGuestUser()
        }
    }

    private fun fetchUserRoleAndLoadCategories() {
        val userId = auth.currentUser?.uid ?: return

        if (auth.currentUser?.isAnonymous == true) {
            currentUserRole = "guest"
            setupCategoriesAdapter()
            loadCategories()
            return
        }

        database.child(userId).child("role").get().addOnSuccessListener { snapshot ->
            currentUserRole = snapshot.value as? String ?: "user"
            setupCategoriesAdapter()
            loadCategories()
        }.addOnFailureListener {
            currentUserRole = "user"
            setupCategoriesAdapter()
            loadCategories()
        }
    }

    private fun setupCategoriesAdapter() {
        if (!isAdded || _binding == null) return
        adapter = CategoriesAdapter(
            categories = categories,
            userRole = currentUserRole,
            onAddCategoryClick = { showAddCategoryDialog() }
        )
        binding.categoriesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.categoriesRecyclerView.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        if (currentUserRole != "admin") {
            Toast.makeText(requireContext(), "Apenas administradores podem adicionar categorias.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val titleEditText: EditText = dialogView.findViewById(R.id.categoryTitleEditText)
        val subtitleEditText: EditText = dialogView.findViewById(R.id.categorySubtitleEditText)
        val urlEditText: EditText = dialogView.findViewById(R.id.categoryImageUrlEditText)
        val previewImageView: ImageView = dialogView.findViewById(R.id.categoryImagePreview)
        val addButton: Button = dialogView.findViewById(R.id.addCategoryButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        urlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val url = s.toString()
                Glide.with(this@HomeFragment).load(url).into(previewImageView)
            }
        })

        addButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val subtitle = subtitleEditText.text.toString()
            val imageUrl = urlEditText.text.toString()

            if (title.isNotBlank() && subtitle.isNotBlank() && imageUrl.isNotBlank()) {
                saveCategory(Category(title, subtitle, imageUrl))
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun saveCategory(category: Category) {
        if (currentUserRole != "admin") {
            Toast.makeText(requireContext(), "Apenas administradores podem adicionar categorias.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("categories")
        val newCategoryRef = database.push()

        if (category.title.isNotBlank() && category.subtitle.isNotBlank()) {
            newCategoryRef.setValue(category).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Erro ao salvar categoria. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        val database = FirebaseDatabase.getInstance().getReference("categories")
        database.get().addOnSuccessListener { snapshot ->
            categories.clear()
            for (child in snapshot.children) {
                val category = child.getValue(Category::class.java)
                if (category != null) categories.add(category)
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}