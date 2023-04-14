package br.dev.tiagosutter.foodtracker.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentNewFoodEntryBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.util.*

@AndroidEntryPoint
class NewFoodEntryFragment : Fragment() {

    private var timeOfDay: String? = null
    private var date: LocalDate? = null
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
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        binding.dateInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month+1, dayOfMonth)
                    binding.dateInput.text = "$year ${month+1} $dayOfMonth"
                },
                year, month, dayOfMonth
            ).show()
        }

        binding.timeOfDayInput.setOnClickListener {
            TimePickerDialog(
                requireContext(), { view, hour, minute ->
                    timeOfDay = "$hour:$minute"
                },
                0, 0, true
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}