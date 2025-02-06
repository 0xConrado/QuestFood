package com.quest.food.model

import java.io.Serializable

// Tornando Address serializável
data class Address(
        val street: String = "",
        val number: String = "",
        val complement: String = "",
        val neighborhood: String = "",
        val city: String = "",
        val state: String = "",
        val postalCode: String = "",
        val addressType: String = "" // Ex.: "Casa" ou "Trabalho"
) : Serializable

// Tornando User serializável

data class User(
        val id: String = "",
        val username: String = "",
        val email: String = "",
        val phone: String = "",
        val birthday: String = "",
        val title: String = "Novato",
        val level: Int = 1,
        val levelProgress: Int = 0,
        val profileImagePath: String = "",
        val role: String = "user",
        val address: Address = Address(),
        val completedQuests: List<String> = emptyList() // ✅ Registro de missões concluídas
) : Serializable