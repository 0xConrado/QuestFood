package com.quest.food.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.quest.food.R
import com.quest.food.model.Quest

class QuestAdapter(
    private var quests: List<Quest>,
    private val onQuestProgressUpdate: (String, Int) -> Unit, // Atualiza progresso da missão
    private val onQuestCompleted: (Quest) -> Unit // Chamada quando a missão é concluída
) : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    inner class QuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questTitle: TextView = itemView.findViewById(R.id.questName)
        val questProgressBar: ProgressBar = itemView.findViewById(R.id.questProgress)
        val questProgressText: TextView = itemView.findViewById(R.id.questTask)
        val questStatusIcon: ImageView = itemView.findViewById(R.id.questStatusIcon)
        val questImage: ImageView = itemView.findViewById(R.id.questIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        val quest = quests[position]

        // Logs para depuração
        Log.d("QuestAdapter", "Quest ID: ${quest.id}, currentProgress: ${quest.currentProgress}, quantity: ${quest.quantity}, isRewardClaimed: ${quest.rewardClaimed}")

        // Configurar os dados da quest
        holder.questTitle.text = quest.title
        holder.questProgressBar.max = quest.quantity
        holder.questProgressBar.progress = quest.currentProgress
        holder.questProgressText.text = "${quest.currentProgress}/${quest.quantity} Tarefas concluídas"

        // Carregar a imagem da recompensa usando Glide
        if (quest.rewardImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(quest.rewardImageUrl)
                .into(holder.questImage)
        }

        // Atualizar o status do ícone
        updateQuestStatus(holder, quest)

        // Atualizar o progresso da missão (ignorar chamadas desnecessárias)
        onQuestProgressUpdate(quest.id, quest.currentProgress)
    }

    private fun updateQuestStatus(holder: QuestViewHolder, quest: Quest) {
        when {
            quest.rewardClaimed -> {
                holder.questStatusIcon.setImageResource(R.drawable.gift_box_received) // Recompensa recebida
                holder.questStatusIcon.setOnClickListener(null) // Desativa clique
            }
            quest.currentProgress >= quest.quantity -> {
                holder.questStatusIcon.setImageResource(R.drawable.gift_box_open) // Missão concluída
                holder.questStatusIcon.setOnClickListener {
                    Log.d("QuestAdapter", "Quest status icon clicked. Opening RewardPopupFragment for quest ID: ${quest.id}")
                    onQuestCompleted(quest) // Notifica o fragmento para abrir o RewardPopupFragment
                }
            }
            else -> {
                holder.questStatusIcon.setImageResource(R.drawable.gift_box) // Missão em andamento
                holder.questStatusIcon.setOnClickListener(null) // Desativa clique
            }
        }
    }

    override fun getItemCount(): Int = quests.size

    fun updateQuests(newQuests: List<Quest>) {
        quests = newQuests
        notifyDataSetChanged() // Atualiza a lista de quests no RecyclerView
    }
}