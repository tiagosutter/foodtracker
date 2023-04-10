package br.dev.tiagosutter.foodtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentFoodEntriesBinding
import br.dev.tiagosutter.foodtracker.entities.FoodEntry

class FoodEntriesFragment : Fragment() {

    private var _binding: FragmentFoodEntriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFoodEntriesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testData = listOf(
            FoodEntryListItem.DateEntry("Hoje"),
            FoodEntryListItem.FoodItem(
                FoodEntry(
                    "ing", "2023-04-09", "", 1
                ),
            ),
            FoodEntryListItem.FoodItem(
                FoodEntry(
                    "ing", "2023-04-09", "teste", 2
                ),
            )
        )
        binding.foodEntriesRecyclerView.adapter = FoodEntriesAdapter().apply {
            submitList(testData)
        }

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}