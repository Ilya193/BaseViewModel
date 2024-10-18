package ru.ikom.baseviewmodel

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.Serializable
import ru.ikom.baseviewmodel.databinding.ItemBinding

class ItemsAdapter(
    private val onClick: (Int) -> Unit
) : ListAdapter<ItemUi, ItemsAdapter.ViewHolder>(DiffItems())  {

    inner class ViewHolder(private val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) onClick(adapterPosition)
            }
        }

        fun bind(item: ItemUi) {
            binding.root.text = item.text
            bindBackground(item)
        }

        fun bindBackground(item: ItemUi) {
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(
                    if (item.isSelected) 0xFF6E9827.toInt()
                    else 0xFFFFFFFF.toInt()
                )
            }
            binding.root.background = background
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else holder.bindBackground(getItem(position))
    }
}

@Serializable
data class ItemUi(
    val id: Int,
    val text: String,
    val isSelected: Boolean = false
)

class DiffItems : DiffUtil.ItemCallback<ItemUi>() {
    override fun areItemsTheSame(oldItem: ItemUi, newItem: ItemUi): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ItemUi, newItem: ItemUi): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: ItemUi, newItem: ItemUi): Any? {
        return oldItem.isSelected != newItem.isSelected
    }

}

fun generateItems(): List<ItemUi> {
    return List(50) {
        ItemUi(
            id = it,
            text = "Item $it"
        )
    }
}