package br.dev.tiagosutter.foodtracker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.databinding.ItemFoodEntryBinding
import br.dev.tiagosutter.foodtracker.databinding.ItemFoodEntryDateSeparatorBinding
import br.dev.tiagosutter.foodtracker.entities.FoodEntry

sealed class FoodEntryListItem {

    val id: String
        get() {
            return when (this) {
                is DateEntry -> this.date
                is FoodItem -> this.foodEntry.foodEntryId.toString()
            }
        }

    data class DateEntry(val date: String) : FoodEntryListItem()
    data class FoodItem(val foodEntry: FoodEntry) : FoodEntryListItem()
}

class FoodEntriesAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DATETIME_ENTRY = 0
        private const val VIEW_TYPE_FOOD_ITEM_ENTRY = 1
    }

    private val diffCallback = object : DiffUtil.ItemCallback<FoodEntryListItem>() {

        override fun areItemsTheSame(
            oldItem: FoodEntryListItem,
            newItem: FoodEntryListItem
        ): Boolean {
            if (oldItem::class != newItem::class)
                return false
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FoodEntryListItem,
            newItem: FoodEntryListItem
        ): Boolean {
            if (oldItem::class != newItem::class)
                return false
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, diffCallback)


    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is FoodEntryListItem.DateEntry -> VIEW_TYPE_DATETIME_ENTRY
            is FoodEntryListItem.FoodItem -> VIEW_TYPE_FOOD_ITEM_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATETIME_ENTRY -> createDateViewHolder(parent)
            VIEW_TYPE_FOOD_ITEM_ENTRY -> createFoodItemViewHolder(parent)
            else -> createFoodItemViewHolder(parent)
        }
    }

    private fun createDateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val layoutInflater = parent.getLayoutInflater()
        val binding = ItemFoodEntryDateSeparatorBinding.inflate(layoutInflater, parent, false)

        return DateViewHolder(binding, interaction)
    }

    private fun createFoodItemViewHolder(parent: ViewGroup): FoodItemViewHolder {
        val layoutInflater = parent.getLayoutInflater()
        val binding = ItemFoodEntryBinding.inflate(layoutInflater, parent, false)

        return FoodItemViewHolder(
            binding,
            interaction
        )
    }

    private fun ViewGroup.getLayoutInflater(): LayoutInflater = LayoutInflater.from(this.context)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FoodItemViewHolder -> {
                holder.bind(differ.currentList[position] as FoodEntryListItem.FoodItem)
            }
            is DateViewHolder -> {
                holder.bind(differ.currentList[position] as FoodEntryListItem.DateEntry)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<FoodEntryListItem>) {
        differ.submitList(list)
    }
}

class FoodItemViewHolder(private val binding: ItemFoodEntryBinding, private val interaction: Interaction?) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FoodEntryListItem.FoodItem) {
        binding.itemFoodEntryIngredients.text = item.foodEntry.ingredients
    }
}

class DateViewHolder(
    private val binding: ItemFoodEntryDateSeparatorBinding,
    private val interaction: Interaction?
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FoodEntryListItem.DateEntry) {
        binding.itemFoodEntrySeparatorText.text = item.date
    }
}

interface Interaction {
    fun onItemEditClicked(position: Int, item: FoodEntryListItem)
    fun onAddItemToDateClicked(position: Int, item: FoodEntryListItem)
}