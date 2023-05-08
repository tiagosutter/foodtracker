package br.dev.tiagosutter.foodtracker.ui.entrieslist

import android.Manifest
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentFoodEntriesBinding
import br.dev.tiagosutter.foodtracker.notifications.NotificationScheduler
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class FoodEntriesFragment : Fragment(), Interaction {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var _binding: FragmentFoodEntriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var foodEntriesAdapter: FoodEntriesAdapter

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val viewModel: FoodEntriesViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodEntriesBinding.inflate(inflater, container, false)
        foodEntriesAdapter = FoodEntriesAdapter(this)
        registerViewModelObservers()
        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                viewModel.handleNotificationPermission(isGranted)
            }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_FoodEntriesListFragment_to_settingsFragment)
        }
        binding.tvNoEntriesFound.setVisible(true)
    }

    private fun setupRecyclerView() {
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder is DateViewHolder) return 0
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (viewHolder is FoodItemViewHolder) {
                        val foodItemViewHolder: FoodItemViewHolder = viewHolder
                        foodItemViewHolder.binding.itemFoodEntryCard.translationX = dX
                        if (foodItemViewHolder.binding.deletingItemBackgroundStub.parent != null) {
                            foodItemViewHolder.binding.deletingItemBackgroundStub.inflate()
                        }
                        // TODO: Create some interesting animation for the delete icon
                    }
                    return
                } else {
                    super.onChildDraw(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                    )
                }

            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val foodEntryListItem = foodEntriesAdapter.differ.currentList[position]
                if (foodEntryListItem is FoodEntryListItemsViewState.FoodItem) {
                    viewModel.deleteEntry(foodEntryListItem.foodEntry)
                    analytics.logEvent("swipe_delete_item") {}
                    val snackbar =
                        Snackbar.make(binding.root, R.string.deleted, Snackbar.LENGTH_LONG)
                    snackbar.setAction(R.string.undo_deletion) {
                        analytics.logEvent("undo_delete_entry") {}
                        it.isEnabled = false
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

    fun View.setVisible(visible: Boolean) {
        this.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun registerViewModelObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            val empty = viewState.entriesByDate.isEmpty()
            binding.tvNoEntriesFound.setVisible(empty)
            foodEntriesAdapter.submitList(viewState.entriesByDate)
        }
        viewModel.scheduleNotificationResult.observe(viewLifecycleOwner) { result ->
            if (result == NotificationScheduler.SchedulingResult.NO_PERMISSION) {
                requestNotificationPermission()
            }
        }
        viewModel.getAllEntries()
    }

    private fun ifTiramisu(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) action()
    }

    private fun requestNotificationPermission() = ifTiramisu {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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