package com.example.leanmasscalculator

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leanmasscalculator.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val items: MutableList<HistoryItem>,
    private val onDelete: (HistoryItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    data class HistoryItem(
        val id: String = "",
        val date: String = "",
        val gender: String = "",
        val weight: Double = 0.0,
        val height: Double = 0.0,
        val lbm: Double = 0.0,
        val status: String = ""
    )

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvDate.text = item.date
        holder.binding.tvDetails.text = "${item.gender} | ${item.weight}kg | ${item.height}cm"
        holder.binding.tvLbm.text = String.format("LBM = %.2f kg — %s", item.lbm, item.status)

        holder.binding.btnDelete.setOnClickListener {
            onDelete(item, position)
        }
    }

    override fun getItemCount() = items.size

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}