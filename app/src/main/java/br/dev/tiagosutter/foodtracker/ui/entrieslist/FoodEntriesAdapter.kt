package br.dev.tiagosutter.foodtracker.ui.entrieslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.ItemFoodEntryBinding
import br.dev.tiagosutter.foodtracker.databinding.ItemFoodEntryDateSeparatorBinding
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class FoodEntryListItem {

    val id: String
        get() {
            return when (this) {
                is DateEntry -> this.date.toString()
                is FoodItem -> this.foodEntry.foodEntryId.toString()
            }
        }

    data class DateEntry(val date: LocalDate) : FoodEntryListItem()
    data class FoodItem(val foodEntry: FoodEntry) : FoodEntryListItem()
}

class FoodEntriesAdapter(private val interaction: Interaction) :
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
    val differ = AsyncListDiffer(this, diffCallback)


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

class FoodItemViewHolder(
    private val binding: ItemFoodEntryBinding,
    private val interaction: Interaction
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FoodEntryListItem.FoodItem) {
        binding.itemFoodEntryIngredients.text = item.foodEntry.ingredients
        binding.editEntryImageView.setOnClickListener {
            interaction.onItemEditClicked(adapterPosition, item)
        }
        binding.timeOfDayTextView.text = item.foodEntry.getTimeOfDay()
        val context = binding.root.context
        if (item.foodEntry.symptoms.isBlank()) {
            val green100 = ContextCompat.getColorStateList(context, R.color.main_green_color_100)
            val mainGreen = ContextCompat.getColorStateList(context, R.color.main_green_color)
            binding.itemFoodEntryCard.backgroundTintList = green100
            binding.itemFoodEntryCardTop.backgroundTintList = mainGreen
            binding.statusImage.setImageResource(R.drawable.smile)
            binding.statusText.setText(R.string.all_fine)
        } else {
            val red100 = ContextCompat.getColorStateList(context, R.color.danger_red_color_100)
            val red600 = ContextCompat.getColorStateList(context, R.color.danger_red_color_600)
            binding.itemFoodEntryCard.backgroundTintList = red100
            binding.itemFoodEntryCardTop.backgroundTintList = red600
            binding.statusImage.setImageResource(R.drawable.sick)
            binding.statusText.setText(R.string.symptomatic)
        }
    }
}

class DateViewHolder(
    private val binding: ItemFoodEntryDateSeparatorBinding,
    private val interaction: Interaction
) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FoodEntryListItem.DateEntry) {
        val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val date = localizedDateFormatter.format(item.date)
        binding.itemFoodEntrySeparatorText.text = date
        binding.addEntryToDate.setOnClickListener {
            interaction.onAddItemToDateClicked(adapterPosition, item)
        }
    }
}

interface Interaction {
    fun onItemEditClicked(position: Int, item: FoodEntryListItem.FoodItem)
    fun onAddItemToDateClicked(position: Int, item: FoodEntryListItem.DateEntry)
}