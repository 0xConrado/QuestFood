package com.quest.food.ui.popup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.quest.food.R

class PopupAddressFragment : DialogFragment() {

    private lateinit var editTextStreet: EditText
    private lateinit var editTextNumber: EditText
    private lateinit var editTextComplement: EditText
    private lateinit var editTextNeighborhood: EditText
    private lateinit var editTextCity: EditText
    private lateinit var editTextState: EditText
    private lateinit var editTextPostalCode: EditText
    private lateinit var radioHome: RadioButton
    private lateinit var radioWork: RadioButton
    private lateinit var buttonSaveEditAddress: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null || currentUser.isAnonymous) {
            Toast.makeText(requireContext(), "Faça login para adicionar um endereço.", Toast.LENGTH_SHORT).show()
            dismiss()
            return null
        }

        return inflater.inflate(R.layout.popup_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTextStreet = view.findViewById(R.id.editTextStreet)
        editTextNumber = view.findViewById(R.id.editTextNumber)
        editTextComplement = view.findViewById(R.id.editTextComplement)
        editTextNeighborhood = view.findViewById(R.id.editTextNeighborhood)
        editTextCity = view.findViewById(R.id.editTextCity)
        editTextState = view.findViewById(R.id.editTextState)
        editTextPostalCode = view.findViewById(R.id.editTextPostalCode)
        radioHome = view.findViewById(R.id.radioHome)
        radioWork = view.findViewById(R.id.radioWork)
        buttonSaveEditAddress = view.findViewById(R.id.buttonSaveAddress)

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        loadOrEnableFields(currentUser.uid)

        buttonSaveEditAddress.setOnClickListener {
            if (buttonSaveEditAddress.text == "Editar") {
                enableFieldsForEditing()
            } else {
                saveAddressToDatabase(currentUser.uid)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    private fun loadOrEnableFields(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("address")

        databaseReference.get().addOnSuccessListener { snapshot ->
            val typeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
            val address = snapshot.getValue(typeIndicator) // Forma segura de extrair o mapa

            if (address != null) {
                populateFields(address)
                setFieldsEnabled(false)
                buttonSaveEditAddress.text = "Editar"
            } else {
                setFieldsEnabled(true)
                buttonSaveEditAddress.text = "Salvar"
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao carregar endereço.", Toast.LENGTH_SHORT).show()
            Log.e("PopupAddressFragment", "Erro ao buscar endereço: ${it.message}")
        }
    }


    private fun populateFields(address: Map<String, Any>) {
        editTextStreet.setText(address["street"] as? String ?: "")
        editTextNumber.setText(address["number"] as? String ?: "")
        editTextComplement.setText(address["complement"] as? String ?: "")
        editTextNeighborhood.setText(address["neighborhood"] as? String ?: "")
        editTextCity.setText(address["city"] as? String ?: "")
        editTextState.setText(address["state"] as? String ?: "")
        editTextPostalCode.setText(address["postalCode"] as? String ?: "")

        val addressType = address["type"] as? String
        if (addressType == "Casa") {
            radioHome.isChecked = true
        } else {
            radioWork.isChecked = true
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        editTextStreet.isEnabled = enabled
        editTextNumber.isEnabled = enabled
        editTextComplement.isEnabled = enabled
        editTextNeighborhood.isEnabled = enabled
        editTextCity.isEnabled = enabled
        editTextState.isEnabled = enabled
        editTextPostalCode.isEnabled = enabled
        radioHome.isEnabled = enabled
        radioWork.isEnabled = enabled
    }

    private fun enableFieldsForEditing() {
        setFieldsEnabled(true)
        buttonSaveEditAddress.text = "Salvar"
    }

    private fun saveAddressToDatabase(userId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("address")

        val address = mapOf(
            "street" to editTextStreet.text.toString(),
            "number" to editTextNumber.text.toString(),
            "complement" to editTextComplement.text.toString(),
            "neighborhood" to editTextNeighborhood.text.toString(),
            "city" to editTextCity.text.toString(),
            "state" to editTextState.text.toString(),
            "postalCode" to editTextPostalCode.text.toString(),
            "type" to if (radioHome.isChecked) "Casa" else "Trabalho"
        )

        databaseReference.setValue(address).addOnSuccessListener {
            Toast.makeText(requireContext(), "Endereço salvo com sucesso.", Toast.LENGTH_SHORT).show()
            setFieldsEnabled(false)
            buttonSaveEditAddress.text = "Editar"
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Erro ao salvar o endereço.", Toast.LENGTH_SHORT).show()
            Log.e("PopupAddressFragment", "Erro ao salvar endereço: ${it.message}")
        }
    }
}
