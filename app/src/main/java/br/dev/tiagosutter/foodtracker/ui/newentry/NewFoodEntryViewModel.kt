package br.dev.tiagosutter.foodtracker.ui.newentry

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.dev.tiagosutter.foodtracker.database.FoodEntryDao
import br.dev.tiagosutter.foodtracker.entities.FoodEntry
import br.dev.tiagosutter.foodtracker.entities.FoodEntryWithImages
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class NewFoodEntryViewModel @Inject constructor(private val dao: FoodEntryDao) : ViewModel() {

    data class TakenPicture(
        val uri: Uri,
        val name: String
    )

    sealed class SaveResult {
        object IngredientsEmptyError : SaveResult()
        object DateTimeEmptyError : SaveResult()
        object Success : SaveResult()
    }

    private val _saveResult = MutableLiveData<SaveResult>()
    val saveResult: LiveData<SaveResult> = _saveResult

    /**
     * Images to show in the UI, these are not supposed be persisted back to the database.
     */
    private val _allImages = MutableLiveData<List<SavedImage>>()
    val allImages: LiveData<List<SavedImage>> = _allImages

    private val takenPictures: MutableList<TakenPicture> = mutableListOf()

    fun submitForm(foodEntry: FoodEntry) {
        if (!validateForm(foodEntry)) {
            return
        }
        viewModelScope.launch {
            val imagesToAdd = takenPictures.map {
                SavedImage(it.name, foodEntry.foodEntryId)
            }
            val foodEntryWithImages = FoodEntryWithImages(
                foodEntry,
                imagesToAdd
            )
            dao.insertFoodEntryWithImages(foodEntryWithImages)
            _saveResult.value = SaveResult.Success
        }
    }

    fun pictureTaken(takenPicture: TakenPicture) {
        takenPictures.add(takenPicture)
        val savedImage = SavedImage(takenPicture.name, 0, 0)
        val currentImages = _allImages.value ?: listOf()
        _allImages.value = currentImages + savedImage
    }

    fun loadImages(foodEntryId: Long) {
        viewModelScope.launch {
            dao.getFoodEntryWithImagesById(foodEntryId).collect { foodEntryWithImages ->
                _allImages.value = foodEntryWithImages.images
            }
        }
    }

    private fun validateForm(foodEntry: FoodEntry): Boolean {
        var valid = true
        if (foodEntry.ingredients.isBlank()) {
            valid = false
            _saveResult.value = SaveResult.IngredientsEmptyError
        }
        try {
            LocalDateTime.parse(foodEntry.dateAndTime)
        } catch (e: DateTimeParseException) {
            valid = false
            _saveResult.value = SaveResult.DateTimeEmptyError
        }
        return valid
    }

    fun hasNewPictures(): Boolean = takenPictures.isNotEmpty()
}