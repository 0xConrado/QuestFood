package com.quest.food.ui.popup

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class PopupRegisterFragment : DialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var profileImageView: ImageView

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Define o estilo para tela cheia
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_popup_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Referências aos elementos de UI
        val closeButton = view.findViewById<ImageView>(R.id.closeButton) // Botão Fechar
        val usernameEditText = view.findViewById<EditText>(R.id.register_username)
        val emailEditText = view.findViewById<EditText>(R.id.register_email)
        val phoneEditText = view.findViewById<EditText>(R.id.register_phone)
        val birthdayEditText = view.findViewById<EditText>(R.id.register_date_of_birth)
        val passwordEditText = view.findViewById<EditText>(R.id.register_password)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.register_confirm_password)
        val registerButton = view.findViewById<Button>(R.id.register_submit_button)
        profileImageView = view.findViewById(R.id.register_profile_image)
        val uploadButton = view.findViewById<ImageView>(R.id.upload_profile_image_button)

        // Configura o botão fechar
        closeButton.setOnClickListener {
            dismiss() // Fecha o popup
        }

        // Adiciona máscara ao campo de telefone
        phoneEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "(##) #####-####"
            private fun applyMask(s: String): String {
                var unmasked = s.replace(Regex("[^0-9]"), "")
                val result = StringBuilder()
                var i = 0
                for (char in mask) {
                    if (char == '#' && i < unmasked.length) {
                        result.append(unmasked[i])
                        i++
                    } else if (i < unmasked.length) {
                        result.append(char)
                    }
                }
                return result.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                isUpdating = true
                phoneEditText.setText(applyMask(s.toString()))
                phoneEditText.setSelection(phoneEditText.text.length)
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Botão de upload
        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Configuração do Date Picker
        birthdayEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker, // Aplica o tema personalizado, // Aplica tema claro ao DatePicker
                { _, year, month, dayOfMonth ->
                    val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    birthdayEditText.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Botão de registro
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val birthday = birthdayEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validateInputs(username, email, phone, birthday, password, confirmPassword)) {
                val profileImagePath = saveProfileImageLocally()
                registerUser(username, email, phone, birthday, password, profileImagePath)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                profileImageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileImageLocally(): String? {
        return try {
            val drawable = profileImageView.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val file = File(requireContext().filesDir, "profile_image.jpg")
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                file.absolutePath
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e("PopupRegisterFragment", "Error saving image: ${e.message}")
            null
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        phone: String,
        birthday: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || birthday.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(
        username: String,
        email: String,
        phone: String,
        birthday: String,
        password: String,
        profileImagePath: String?
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                saveUserDataToDatabase(userId, username, email, phone, birthday, profileImagePath)
            } else {
                Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserDataToDatabase(
        userId: String,
        username: String,
        email: String,
        phone: String,
        birthday: String,
        profileImagePath: String?
    ) {
        val user = mapOf(
            "username" to username,
            "email" to email,
            "phone" to phone,
            "birthday" to birthday,
            "profileImagePath" to profileImagePath.orEmpty(),
            "title" to "Novato",
            "level" to 1,
            "levelProgress" to 0,
            "role" to "user"
        )
        database.getReference("users").child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Failed to save user: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
