package br.dev.tiagosutter.foodtracker.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import br.dev.tiagosutter.foodtracker.entities.SavedImage

@Dao
abstract class SavedImageDao() {

    @Insert
    abstract suspend fun insertSavedImage(savedImage: SavedImage)

    @Insert
    abstract suspend fun insertSavedImages(savedImages: List<SavedImage>)

    @Delete
    abstract suspend fun deleteSavedImage(savedImage: SavedImage)
}