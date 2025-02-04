package com.quest.food.ui.popup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.R

class PopupAddressFragment(private val userId: String? = null) : DialogFragment() {

    private lateinit var streetEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var complementEditText: EditText
    private lateinit var neighborhoodEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var postalCodeEditText: EditText
    private lateinit var radioHome: RadioButton
    private lateinit var radioWork: RadioButton
    private lateinit var buttonEditSave: Button

    private val currentUserId: String? by lazy {
        userId ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_popup_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streetEditText = view.findViewById(R.id.editTextStreet)
        numberEditText = view.findViewById(R.id.editTextNumber)
        complementEditText = view.findViewById(R.id.editTextComplement)
        neighborhoodEditText = view.findViewById(R.id.editTextNeighborhood)
        cityEditText = view.findViewById(R.id.editTextCity)
        stateEditText = view.findViewById(R.id.editTextState)
        postalCodeEditText = view.findViewById(R.id.editTextPostalCode)
        radioHome = view.findViewById(R.id.radioHome)
        radioWork = view.findViewById(R.id.radioWork)
        buttonEditSave = view.findViewById(R.id.buttonSaveAddress)

        loadAddressData()

        buttonEditSave.setOnClickListener {
            if (buttonEditSave.text == "Editar") {
                enableFieldsForEditing()
            } else {
                saveAddressData()
            }
        }
    }

    private fun loadAddressData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(currentUserId ?: return).child("address")

        databaseReference.get().addOnSuccessListener { snapshot ->
            streetEditText.setText(snapshot.child("street").getValue(String::class.java) ?: "")
            numberEditText.setText(snapshot.child("number").getValue(String::class.java) ?: "")
            complementEditText.setText(snapshot.child("complement").getValue(String::class.java) ?: "")
            neighborhoodEditText.setText(snapshot.child("neighborhood").getValue(String::class.java) ?: "")
            cityEditText.setText(snapshot.child("city").getValue(String::class.java) ?: "")
            stateEditText.setText(snapshot.child("state").getValue(String::class.java) ?: "")
            postalCodeEditText.setText(snapshot.child("postalCode").getValue(String::class.java) ?: "")

            val addressType = snapshot.child("type").getValue(String::class.java)
            if (addressType == "Casa") {
                radioHome.isChecked = true
            } else if (addressType == "Trabalho") {
                radioWork.isChecked = true
            }

            setFieldsEnabled(false)
            buttonEditSave.text = "Editar"
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar endereço.", Toast.LENGTH_SHORT).show()
            Log.e("PopupAddressFragment", "Erro ao buscar endereço: ${it.message}")
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        streetEditText.isEnabled = enabled
        numberEditText.isEnabled = enabled
        complementEditText.isEnabled = enabled
        neighborhoodEditText.isEnabled = enabled
        cityEditText.isEnabled = enabled
        stateEditText.isEnabled = enabled
        postalCodeEditText.isEnabled = enabled
        radioHome.isEnabled = enabled
        radioWork.isEnabled = enabled
    }

    private fun enableFieldsForEditing() {
        setFieldsEnabled(true)
        buttonEditSave.text = "Salvar"
    }

    private fun saveAddressData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            .child(currentUserId ?: return).child("address")

        val updatedAddress = mapOf(
            "street" to streetEditText.text.toString(),
            "number" to numberEditText.text.toString(),
            "complement" to complementEditText.text.toString(),
            "neighborhood" to neighborhoodEditText.text.toString(),
            "city" to cityEditText.text.toString(),
            "state" to stateEditText.text.toString(),
            "postalCode" to postalCodeEditText.text.toString(),
            "type" to if (radioHome.isChecked) "Casa" else "Trabalho"
        )

        databaseReference.setValue(updatedAddress).addOnSuccessListener {
            Toast.makeText(requireContext(), "Endereço atualizado com sucesso.", Toast.LENGTH_SHORT).show()
            setFieldsEnabled(false)
            buttonEditSave.text = "Editar"
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao atualizar o endereço.", Toast.LENGTH_SHORT).show()
            Log.e("PopupAddressFragment", "Erro ao salvar endereço: ${it.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
