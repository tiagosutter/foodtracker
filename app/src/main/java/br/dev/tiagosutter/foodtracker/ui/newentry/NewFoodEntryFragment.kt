package br.dev.tiagosutter.foodtracker.ui.newentry

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NewFoodEntryFragment : Fragment(), AttachedImagesAdapter.Interaction {

    private var timeOfDay: String? = null
    private var date: LocalDate? = null
    private var _binding: FragmentNewFoodEntryBinding? = null
    private val binding get() = _binding!!
    private var takenPicture: NewFoodEntryViewModel.TakenPicture? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    val args: NewFoodEntryFragmentArgs by navArgs()

    private val dateAndTime: String
        get() {
            return if (date != null && timeOfDay != null) {
                "${date}T${timeOfDay}"
            } else {
                ""
            }
        }

    private val viewModel: NewFoodEntryViewModel by viewModels()

    private lateinit var attachedImagesAdapter: AttachedImagesAdapter

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                takenPicture?.let { viewModel.pictureTaken(it) }
            }
        }

    private val pickPictureLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val filesDir = createFileDir()
                val name = UUID.randomUUID().toString()
                val photoFileUri = createFileToStoreImage(filesDir, name)
                val contentResolver = requireActivity().contentResolver
                val copy = contentResolver.openOutputStream(photoFileUri)
                val src = contentResolver.openInputStream(uri)
                src?.source().use { s ->
                    if (s == null) return@use
                    copy?.sink()?.buffer().use { c -> c?.writeAll(s) }
                }
                takenPicture = NewFoodEntryViewModel.TakenPicture(photoFileUri, name)
                takenPicture?.let {
                    viewModel.pictureTaken(it)
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setupBackPressHandler()
        setupMenuClickListener()
        setupViewModelObservers()
        _binding = FragmentNewFoodEntryBinding.inflate(inflater, container, false)
        return binding.root

    }

    private fun setupBackPressHandler() {
        val backPressedDispatcher = requireActivity().onBackPressedDispatcher
        val callback = backPressedDispatcher.addCallback(viewLifecycleOwner) {

            if (getFoodEntryFromForm() != args.foodEntry || viewModel.hasNewPictures()) {
                showConfirmationDialog()
            } else {
                isEnabled = false
                backPressedDispatcher.onBackPressed()
            }
        }
        backPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.exit_without_saving)
            .setMessage(R.string.you_made_changes)
            .setPositiveButton(R.string.exit_without_saving) { dialog, _ ->
                findNavController().popBackStack()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.continue_editing) { dialog, _ ->
                dialog.dismiss()
            }
        builder.show()
    }

    private fun setupViewModelObservers() {
        viewModel.allImages.observe(viewLifecycleOwner) { images ->
            loadImagePreviews(images)
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleArgs()
        setupDataInput()
        setupTimeInput()
        setupIngredientsTextListener()

        attachedImagesAdapter = AttachedImagesAdapter(this)
        binding.attachedImagesRecyclerView.adapter = attachedImagesAdapter

        binding.attachImageAction.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_how_to_get_image)
                .setPositiveButton(R.string.gallery) { dialog, _ ->
                    pickPictureLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.open_camera) { dialog, _ ->
                    takePicture()
                    dialog.dismiss()
                }.show()
        }
    }

    private fun takePicture() {

        val filesDir = createFileDir()
        val name = UUID.randomUUID().toString()
        val photoFileUri = createFileToStoreImage(filesDir, name)
        takenPicture = NewFoodEntryViewModel.TakenPicture(photoFileUri, name)
        takePictureLauncher.launch(takenPicture?.uri)
    }

    private fun createFileToStoreImage(filesDir: File, name: String): Uri {
        val photoFile = File(filesDir, name)
        return FileProvider.getUriForFile(
            requireContext(),
            "br.dev.tiagosutter.foodtracker.provider",
            photoFile
        )
    }

    private fun createFileDir(): File {
        val filesDir = File(requireContext().filesDir, "images")
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
        return filesDir
    }

    private fun loadImagePreviews(images: List<SavedImage>) {
        if (images.isEmpty()) return
        binding.attachImageAction.visibility = View.INVISIBLE
        binding.attachedImagesRecyclerView.visibility = View.VISIBLE
        attachedImagesAdapter.submitList(images)
    }

    private fun handleArgs() {
        val foodEntry = args.foodEntry
        val initialDate = args.initialDate
        val localizedDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        if (foodEntry != null) {
            val dateYearMonthDay = foodEntry.getDateYearMonthDay()
            date = LocalDate.parse(dateYearMonthDay)
            timeOfDay = foodEntry.getTimeOfDay()

            binding.dateInput.text = localizedDateFormatter.format(date)
            binding.timeOfDayInput.text = timeOfDay

            binding.ingredientsEditText.setText(foodEntry.ingredients)
            binding.symptomsEditText.setText(foodEntry.symptoms)
            viewModel.loadImages(foodEntry.foodEntryId)
        } else if (initialDate.isNotEmpty()) {
            date = LocalDate.parse(initialDate)
            binding.dateInput.text = localizedDateFormatter.format(date)
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
                        val foodEntry = getFoodEntryFromForm()
                        logSaveFoodEntryEvent(foodEntry)
                        viewModel.submitForm(foodEntry)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }

    private fun logSaveFoodEntryEvent(
        foodEntry: FoodEntry
    ) {
        val sameDate = foodEntry.getDateYearMonthDay() == LocalDate.now().toString()
        analytics.logEvent("save_food_entry") {
            param("photos_quantity", viewModel.getImagesQuantity().toLong())
            param("ingredients_text_len", foodEntry.ingredients.length.toLong())
            param("symptoms_text_len", foodEntry.symptoms.length.toLong())
            param("is_same_date", sameDate.toString())
        }
    }

    private fun getFoodEntryFromForm(): FoodEntry {
        val symptoms = binding.symptomsEditText.text.toString()
        val ingredients = binding.ingredientsEditText.text.toString()
        val id = args.foodEntry?.foodEntryId ?: 0L
        return FoodEntry(ingredients, dateAndTime, symptoms, id)
    }

    private fun setupTimeInput() {
        binding.timeOfDayInput.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, hour, minute ->
                    val minutesPadStart = minute.toString().padStart(2, '0')
                    val hoursPadStart = hour.toString().padStart(2, '0')
                    timeOfDay = "$hoursPadStart:$minutesPadStart"
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


    override fun onItemClicked(position: Int, item: SavedImage) {
        TODO("Not yet implemented")
    }

    override fun onAddMoreClicked() {
        takePicture()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}