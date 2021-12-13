package dku.gyeongsotone.gulging.campusplogging.ui.main.challenge

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Challenge
import dku.gyeongsotone.gulging.campusplogging.databinding.ItemChallengeBinding

class ChallengeAdapter :
    ListAdapter<Challenge, ChallengeAdapter.ViewHolder>(ChallengeDiffCallback()) {
    private val items = mutableListOf<Challenge>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun deleteItem(position: Int) {
        items.removeAt(position)
    }

    fun deleteItem(item: Challenge) {
        items.remove(item)
    }

    fun addItem(item: Challenge) {
        items.add(item)
    }

    fun addItems(items: List<Challenge>) {
        this.items.addAll(items)
    }

    fun replaceAll(items: List<Challenge>) {
        this.items.clear()
        this.items.addAll(items)
    }

    class ViewHolder private constructor(val binding: ItemChallengeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Challenge) {
            binding.item = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemChallengeBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

private class ChallengeDiffCallback : DiffUtil.ItemCallback<Challenge>() {

    override fun areItemsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Challenge, newItem: Challenge): Boolean {
        return oldItem == newItem
    }
}