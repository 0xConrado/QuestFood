package com.quest.food.ui.popup

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R
import com.quest.food.model.Address
import com.quest.food.model.User
import java.util.Calendar

class PopupRegisterFragment(private val userId: String? = null) : DialogFragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var birthdayEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var buttonEditSave: Button

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        usernameEditText = view.findViewById(R.id.register_username)
        emailEditText = view.findViewById(R.id.register_email)
        phoneEditText = view.findViewById(R.id.register_phone)
        birthdayEditText = view.findViewById(R.id.register_date_of_birth)
        passwordEditText = view.findViewById(R.id.register_password)
        buttonEditSave = view.findViewById(R.id.register_submit_button)

        phoneEditText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "(##) #####-####"
            private fun applyMask(s: String): String {
                val unmasked = s.replace(Regex("[^0-9]"), "")
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

        birthdayEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                R.style.CustomDatePicker,
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

        if (userId != null) {
            loadUserData()
        }

        buttonEditSave.setOnClickListener {
            if (buttonEditSave.text == "Editar") {
                enableFieldsForEditing()
            } else {
                if (validateInput()) {
                    if (auth.currentUser == null) {
                        createUser()
                    } else {
                        saveUserData()
                    }
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        return if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "E-mail inválido.", Toast.LENGTH_SHORT).show()
            false
        } else if (TextUtils.isEmpty(password) || password.length < 6) {
            Toast.makeText(requireContext(), "Senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun loadUserData() {
        val currentUserId = userId ?: auth.currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)

        databaseReference.get().addOnSuccessListener { snapshot ->
            usernameEditText.setText(snapshot.child("username").getValue(String::class.java) ?: "")
            emailEditText.setText(snapshot.child("email").getValue(String::class.java) ?: "")
            phoneEditText.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
            birthdayEditText.setText(snapshot.child("birthday").getValue(String::class.java) ?: "")

            setFieldsEnabled(false)
            buttonEditSave.text = "Editar"
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show()
            Log.e("PopupRegisterFragment", "Erro ao buscar dados: ${it.message}")
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        usernameEditText.isEnabled = enabled
        emailEditText.isEnabled = enabled
        phoneEditText.isEnabled = enabled
        birthdayEditText.isEnabled = enabled
        passwordEditText.isEnabled = enabled
    }

    private fun enableFieldsForEditing() {
        setFieldsEnabled(true)
        buttonEditSave.text = "Salvar"
    }

    private fun createUser() {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserData()
                } else {
                    Toast.makeText(requireContext(), "Erro ao criar a conta: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("PopupRegisterFragment", "Erro ao criar conta: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserData() {
        val currentUserId = userId ?: auth.currentUser?.uid ?: return
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)

        val updatedUser = User(
            id = currentUserId,
            username = usernameEditText.text.toString(),
            email = emailEditText.text.toString(),
            phone = phoneEditText.text.toString(),
            birthday = birthdayEditText.text.toString(),
            title = "Novato",
            level = 1,
            levelProgress = 0,
            profileImagePath = "",
            role = "user",
            address = Address()
        )

        databaseReference.setValue(updatedUser).addOnSuccessListener {
            Toast.makeText(requireContext(), "Dados atualizados com sucesso.", Toast.LENGTH_SHORT).show()
            dismiss()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao salvar os dados.", Toast.LENGTH_SHORT).show()
            Log.e("PopupRegisterFragment", "Erro ao salvar dados: ${it.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
