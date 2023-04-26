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
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class FoodEntriesAdapter(private val interaction: Interaction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DATETIME_ENTRY = 0
        private const val VIEW_TYPE_FOOD_ITEM_ENTRY = 1
    }

    private val diffCallback = object : DiffUtil.ItemCallback<FoodEntryListItemsViewState>() {

        override fun areItemsTheSame(
            oldItem: FoodEntryListItemsViewState,
            newItem: FoodEntryListItemsViewState
        ): Boolean {
            if (oldItem::class != newItem::class)
                return false
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: FoodEntryListItemsViewState,
            newItem: FoodEntryListItemsViewState
        ): Boolean {
            if (oldItem::class != newItem::class)
                return false
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, diffCallback)


    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is FoodEntryListItemsViewState.DateEntry -> VIEW_TYPE_DATETIME_ENTRY
            is FoodEntryListItemsViewState.FoodItem -> VIEW_TYPE_FOOD_ITEM_ENTRY
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
                holder.bind(differ.currentList[position] as FoodEntryListItemsViewState.FoodItem)
            }
            is DateViewHolder -> {
                holder.bind(differ.currentList[position] as FoodEntryListItemsViewState.DateEntry)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is FoodItemViewHolder -> {
                holder.binding.itemFoodEntryCard.translationX = 0F
            }
        }
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<FoodEntryListItemsViewState>) {
        differ.submitList(list)
    }
}

class FoodItemViewHolder(
    val binding: ItemFoodEntryBinding,
    private val interaction: Interaction
) :
    RecyclerView.ViewHolder(binding.root) {



    fun bind(item: FoodEntryListItemsViewState.FoodItem) {
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

    fun bind(item: FoodEntryListItemsViewState.DateEntry) {
        val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val date = localizedDateFormatter.format(item.date)
        binding.itemFoodEntrySeparatorText.text = date
        binding.addEntryToDate.setOnClickListener {
            interaction.onAddItemToDateClicked(adapterPosition, item)
        }
    }
}

interface Interaction {
    fun onItemEditClicked(position: Int, item: FoodEntryListItemsViewState.FoodItem)
    fun onAddItemToDateClicked(position: Int, item: FoodEntryListItemsViewState.DateEntry)
}