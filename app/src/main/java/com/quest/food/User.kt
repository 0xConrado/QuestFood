package com.quest.food

data class Address(
        val street: String = "",
        val number: String = "",
        val complement: String = "",
        val neighborhood: String = "",
        val city: String = "",
        val state: String = "",
        val postalCode: String = "",
        val addressType: String = "" // Ex.: "Casa" ou "Trabalho"
)

// User data class to map Firebase Realtime Database entries
data class User(
        val username: String = "",
        val email: String = "",
        val phone: String = "",
        val birthday: String = "",
        val title: String = "Novato",
        val level: Int = 1,
        val levelProgress: Int = 0,
        val profileImagePath: String = "", // Updated to use profileImagePath
        val role: String = "user" // Default role: "user" or "admin"
)