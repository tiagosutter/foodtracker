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
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentFoodEntriesBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

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
        registerViewModelObservers()
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
                if (foodEntryListItem is FoodEntryListItemsViewState.FoodItem) {
                    viewModel.deleteEntry(foodEntryListItem.foodEntry)
                    val snackbar = Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.undo_deletion) {
                        viewModel.undoLatestDeletion()
                    }
                    snackbar.show()

                }
            }
        }
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(binding.foodEntriesRecyclerView)
        binding.foodEntriesRecyclerView.adapter = foodEntriesAdapter
    }

    private fun registerViewModelObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            foodEntriesAdapter.submitList(viewState.entriesByDate)
        }
        viewModel.getAllEntries()
    }

    override fun onItemEditClicked(position: Int, item: FoodEntryListItemsViewState.FoodItem) {
        val action = FoodEntriesFragmentDirections
            .actionFoodEntriesListFragmentToNewFoodEntryFragment(item.foodEntry, "")
        findNavController().navigate(action)
    }

    override fun onAddItemToDateClicked(
        position: Int,
        item: FoodEntryListItemsViewState.DateEntry
    ) {
        val action = FoodEntriesFragmentDirections
            .actionFoodEntriesListFragmentToNewFoodEntryFragment(null, item.date.toString())
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}