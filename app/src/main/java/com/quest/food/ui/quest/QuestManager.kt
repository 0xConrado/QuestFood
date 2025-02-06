package com.quest.food.user

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.quest.food.model.Quest
import com.quest.food.model.ProductItem
import com.quest.food.model.User
import com.quest.food.model.Category

object QuestManager {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("quests")

    // Função para verificar o progresso da missão
    fun updateQuestProgress(userId: String, questId: String, purchasedProduct: ProductItem) {
        // Pega os dados da missão
        database.child(questId).get().addOnSuccessListener { snapshot ->
            val quest = snapshot.getValue(Quest::class.java)

            // Verifica se a missão existe
            if (quest != null && quest.rewardProductId == purchasedProduct.id) {
                val currentProgress = quest.quantity // Progresso atual da missão
                val newProgress = currentProgress + 1

                // Atualiza o progresso da missão no banco de dados
                val updatedQuest = quest.copy(quantity = newProgress)
                database.child(questId).setValue(updatedQuest).addOnSuccessListener {
                    // Atualiza o progresso visualmente
                    checkQuestCompletion(userId, questId, newProgress, quest.quantity)
                }
            }
        }
    }

    // Função para checar se a missão foi concluída
    private fun checkQuestCompletion(userId: String, questId: String, newProgress: Int, totalQuantity: Int) {
        if (newProgress >= totalQuantity) {
            // Missão concluída, agora podemos marcar como concluída e adicionar a recompensa
            markQuestAsCompleted(userId, questId)
        }
    }

    // Função para marcar a missão como concluída e adicionar a recompensa ao carrinho
    private fun markQuestAsCompleted(userId: String, questId: String) {
        // Atualiza o status da missão para concluída
        database.child(questId).child("isCompleted").setValue(true)

        // Busca o produto de recompensa
        val rewardProductId = getRewardProductId(questId)
        val rewardProduct = getProductById(rewardProductId)

        // Adiciona o produto ao carrinho com 100% de desconto
        addProductToCart(userId, rewardProduct)
    }

    // Função para pegar o produto de recompensa pela ID
    private fun getRewardProductId(questId: String): String {
        // Exemplo de como pegar a ID do produto, pode ser do banco ou estático
        return database.child(questId).child("rewardProductId").toString()
    }

    // Função para pegar um produto pelo ID
    private fun getProductById(productId: String): ProductItem {
        // Pega os dados do produto com base no ID
        // Você pode buscar pelo banco ou usar uma lista pré-definida
        // Exemplo:
        return ProductItem(id = productId, name = "Produto de Recompensa", price = 0.0)
    }

    // Função para adicionar um produto ao carrinho
    private fun addProductToCart(userId: String, product: ProductItem) {
        val cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId)
        val productMap = mapOf(
            "productId" to product.id,
            "productName" to product.name,
            "price" to 0.0 // 100% de desconto
        )
        cartRef.push().setValue(productMap).addOnSuccessListener {
            // Produto foi adicionado ao carrinho
        }
    }
}
