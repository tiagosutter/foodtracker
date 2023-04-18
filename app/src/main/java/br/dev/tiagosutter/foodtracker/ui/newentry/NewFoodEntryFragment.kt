package br.dev.tiagosutter.foodtracker.ui.newentry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.dev.tiagosutter.foodtracker.BuildConfig
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.FragmentNewFoodEntryBinding
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@AndroidEntryPoint
class NewFoodEntryFragment : Fragment() {

    private var timeOfDay: String? = null
    private var date: LocalDate? = null
    private var _binding: FragmentNewFoodEntryBinding? = null
    private val binding get() = _binding!!

    val args: NewFoodEntryFragmentArgs by navArgs()

    val dateAndTime: String
        get() {
            return if (date != null && timeOfDay != null) {
                "${date}T${timeOfDay}"
            } else {
                ""
            }
        }

    private val viewModel: NewFoodEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupMenuClickListener()

        _binding = FragmentNewFoodEntryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArgs()
        setupDataInput()
        setupTimeInput()
        setupIngredientsTextListener()
    }

    override fun onStart() {
        super.onStart()
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                NewFoodEntryViewModel.SaveResult.DateTimeEmptyError -> {
                    binding.dateTimeRequiredErroTextView.visibility = View.VISIBLE
                }
                NewFoodEntryViewModel.SaveResult.IngredientsEmptyError -> {
                    binding.ingredientsTextInputLayout.error =
                        getString(R.string.ingredients_required)
                }
                NewFoodEntryViewModel.SaveResult.Success -> {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(requireContext(), "Successful save", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // TODO: Success dialog before going back just to have some dialogs usage
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun handleArgs() {
        val foodEntry = args.foodEntry

        if (foodEntry != null) {
            val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            val dateYearMonthDay = foodEntry.getDateYearMonthDay()
            date = LocalDate.parse(dateYearMonthDay)
            timeOfDay = foodEntry.getTimeOfDay()

            binding.dateInput.text = localizedDateFormatter.format(date)
            binding.timeOfDayInput.text = timeOfDay

            binding.ingredientsEditText.setText(foodEntry.ingredients)
            binding.symptomsEditText.setText(foodEntry.symptoms)
        }
    }

    private fun setupIngredientsTextListener() {
        binding.ingredientsEditText.addTextChangedListener {
            binding.ingredientsTextInputLayout.error = null
        }
    }

    private fun setupMenuClickListener() {
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // no-op
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        val symptoms = binding.symptomsEditText.text.toString()
                        val ingredients = binding.ingredientsEditText.text.toString()
                        val id = args.foodEntry?.foodEntryId ?: 0
                        viewModel.submitForm(FoodEntry(ingredients, dateAndTime, symptoms, id))
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun setupTimeInput() {
        binding.timeOfDayInput.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, hour, minute ->
                    timeOfDay = "$hour:$minute"
                    binding.timeOfDayInput.text = timeOfDay
                },
                0, 0, true
            ).show()
        }
    }

    private fun setupDataInput() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)

        binding.dateInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month + 1, dayOfMonth)

                    val localizedDateFormatter =
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    binding.dateInput.text = localizedDateFormatter.format(date)
                },
                year, month, dayOfMonth
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}