package br.dev.tiagosutter.foodtracker.ui.entrieslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.databinding.FragmentFoodEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class FoodEntriesFragment : Fragment(), Interaction {

    private var _binding: FragmentFoodEntriesBinding? = null
    private val binding get() = _binding!!
    private val foodEntriesAdapter: FoodEntriesAdapter = FoodEntriesAdapter(this)

    private val viewModel: FoodEntriesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFoodEntriesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val foodEntryListItem = foodEntriesAdapter.differ.currentList[position]
                if (foodEntryListItem is FoodEntryListItem.FoodItem) {
                    viewModel.deleteEntry(foodEntryListItem.foodEntry)
                }
            }
        }
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(binding.foodEntriesRecyclerView)
        binding.foodEntriesRecyclerView.adapter = foodEntriesAdapter
    }

    override fun onStart() {
        viewModel.entries.observe(viewLifecycleOwner) { foodEntriesByDate ->
            // TODO: ViewModel should directly handle mapping to ui models, and ui should not deal with mapping
            val list: MutableList<FoodEntryListItem> = mutableListOf()
            for (entry in foodEntriesByDate) {
                val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                val date = localizedDateFormatter.format(entry.date)
                val dateTimeSeparator = FoodEntryListItem.DateEntry(date)
                val foodEntries = entry.entries.map {
                    FoodEntryListItem.FoodItem(it)
                }
                list.add(dateTimeSeparator)
                list.addAll(foodEntries)
            }
            foodEntriesAdapter.submitList(list)
        }
        viewModel.getAllEntries()
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemEditClicked(position: Int, item: FoodEntryListItem.FoodItem) {
        val action = FoodEntriesFragmentDirections
            .actionFoodEntriesListFragmentToNewFoodEntryFragment(item.foodEntry)
        findNavController().navigate(action)
    }

    override fun onAddItemToDateClicked(position: Int, item: FoodEntryListItem.DateEntry) {
        TODO("Not yet implemented")
    }
}