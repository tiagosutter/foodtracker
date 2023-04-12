package br.dev.tiagosutter.foodtracker.ui

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentNewFoodEntryBinding
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewFoodEntryFragment : Fragment() {

    private var _binding: FragmentNewFoodEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewFoodEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // no-op
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        // TODO: Validate and save entry if valid
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        _binding = FragmentNewFoodEntryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}