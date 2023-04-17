package br.dev.tiagosutter.foodtracker.ui.entrieslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import br.dev.tiagosutter.foodtracker.databinding.FragmentFoodEntriesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class FoodEntriesFragment() : Fragment() {

    private var _binding: FragmentFoodEntriesBinding? = null
    private val binding get() = _binding!!
    private val foodEntriesAdapter: FoodEntriesAdapter = FoodEntriesAdapter()

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
}