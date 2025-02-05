package com.quest.food.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quest.food.R
import com.quest.food.model.Quest
import com.bumptech.glide.Glide

class QuestAdapter : RecyclerView.Adapter<QuestAdapter.QuestViewHolder>() {

    private var quests = listOf<Quest>()

    inner class QuestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questName: TextView = itemView.findViewById(R.id.questName)
        private val questProgress: ProgressBar = itemView.findViewById(R.id.questProgress)
        private val questTask: TextView = itemView.findViewById(R.id.questTask)
        private val questIcon: ImageView = itemView.findViewById(R.id.questIcon)
        private val questStatusIcon: ImageView = itemView.findViewById(R.id.questStatusIcon)

        fun bind(quest: Quest) {
            questName.text = quest.title
            questProgress.max = quest.quantity
            questProgress.progress = quest.quantity // Atualize o progresso conforme necessário
            questTask.text = "${quest.quantity}/${quest.quantity}"

            // Carregar a imagem de recompensa
            Glide.with(itemView.context)
                .load(quest.rewardImageUrl)
                .into(questIcon)

            // Mostrar o status da missão
            if (quest.isCompleted) {
                questStatusIcon.setImageResource(R.drawable.check_circle)
            } else {
                questStatusIcon.setImageResource(R.drawable.circle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quest, parent, false)
        return QuestViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestViewHolder, position: Int) {
        holder.bind(quests[position])
    }

    override fun getItemCount(): Int = quests.size

    fun submitList(newQuests: List<Quest>) {
        quests = newQuests
        notifyDataSetChanged()
    }
}
