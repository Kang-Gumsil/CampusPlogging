package dku.gyeongsotone.gulging.campusplogging.ui.main.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dku.gyeongsotone.gulging.campusplogging.data.local.model.Plogging
import dku.gyeongsotone.gulging.campusplogging.databinding.ItemPloggingHistoryBinding
import dku.gyeongsotone.gulging.campusplogging.utils.Constant.EXTRA_PLOGGING_ID

class PloggingHistoryAdapter :
    ListAdapter<Plogging, PloggingHistoryAdapter.ViewHolder>(PloggingDiffCallback()) {
    private val items = mutableListOf<Plogging>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    /**
     * 아이템 클릭 시, 플로깅 디테일 화면 띄우는 클릭 리스너 설정
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            val context = holder.binding.root.context
            val intent = Intent(context, PloggingDetailActivity::class.java)
            intent.putExtra(EXTRA_PLOGGING_ID, items[position].id)
            context.startActivity(intent)
        }
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun deleteItem(position: Int) {
        items.removeAt(position)
    }

    fun deleteItem(item: Plogging) {
        items.remove(item)
    }

    fun addItem(item: Plogging) {
        items.add(item)
    }

    fun addItems(items: List<Plogging>) {
        this.items.addAll(items)
    }

    fun replaceAll(items: List<Plogging>) {
        this.items.clear()
        this.items.addAll(items)
    }

    class ViewHolder private constructor(val binding: ItemPloggingHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Plogging) {
            binding.item = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemPloggingHistoryBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

private class PloggingDiffCallback : DiffUtil.ItemCallback<Plogging>() {

    override fun areItemsTheSame(oldItem: Plogging, newItem: Plogging): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Plogging, newItem: Plogging): Boolean {
        return oldItem == newItem
    }
}