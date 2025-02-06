package com.quest.food.adapter

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
    private val onQuestProgressUpdate: (String, Int) -> Unit, // ✅ Adicionado parâmetro
    private val onQuestCompleted: (Quest) -> Unit // ✅ Parâmetro para missão concluída
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

        holder.questTitle.text = quest.title
        holder.questProgressBar.max = quest.quantity
        holder.questProgressBar.progress = quest.currentProgress
        holder.questProgressText.text = "${quest.currentProgress}/${quest.quantity} Tarefas concluídas"

        if (quest.rewardImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(quest.rewardImageUrl)
                .into(holder.questImage)
        }

        // Verifica se a missão foi concluída
        if (quest.currentProgress >= quest.quantity) {
            holder.questStatusIcon.setImageResource(R.drawable.giftbox_open) // Ícone de missão concluída
            holder.questStatusIcon.setOnClickListener {
                onQuestCompleted(quest)
            }
        } else {
            holder.questStatusIcon.setImageResource(R.drawable.giftbox) // Ícone de bloqueado
        }
    }

    override fun getItemCount(): Int = quests.size

    fun updateQuests(newQuests: List<Quest>) {
        quests = newQuests
        notifyDataSetChanged()
    }
}
