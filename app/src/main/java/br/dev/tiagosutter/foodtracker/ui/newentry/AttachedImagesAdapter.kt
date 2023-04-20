package br.dev.tiagosutter.foodtracker.ui.newentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.ItemAddMoreImagesBinding
import br.dev.tiagosutter.foodtracker.databinding.ItemAttachedImageBinding
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File

private val diffCallback = object : DiffUtil.ItemCallback<SavedImage>() {
    override fun areItemsTheSame(oldItem: SavedImage, newItem: SavedImage): Boolean =
        oldItem.savedImageId == newItem.savedImageId

    override fun areContentsTheSame(oldItem: SavedImage, newItem: SavedImage): Boolean =
        oldItem == newItem
}

class AttachedImagesAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Interaction {
        fun onItemClicked(position: Int, item: SavedImage)
        fun onAddMoreClicked()
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    object ViewTypes {
        const val IMAGE = 0
        const val ADD_MORE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == ViewTypes.IMAGE) {
            val binding = ItemAttachedImageBinding.inflate(inflater, parent, false)
            AttachedImagesViewHolder(binding, interaction)
        } else {
            val binding = ItemAddMoreImagesBinding.inflate(inflater, parent, false)
            AddMoreImagesViewHolder(binding, interaction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == differ.currentList.size) {
            ViewTypes.ADD_MORE
        } else {
            ViewTypes.IMAGE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AttachedImagesViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size + 1
    }

    fun submitList(list: List<SavedImage>) {
        differ.submitList(list)
    }


}

class AttachedImagesViewHolder constructor(
    private val binding: ItemAttachedImageBinding,
    private val interaction: AttachedImagesAdapter.Interaction?
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SavedImage) {
        val context = itemView.context
        val filesDir = File(context.filesDir, "images")
        val image = File(filesDir, item.name)
        val radius =
            context.resources.getDimensionPixelSize(R.dimen.attached_image_corner_radius)

        Glide.with(context)
            .load(image.path)
            .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(radius)))
            .into(binding.attachedImage)
    }
}

class AddMoreImagesViewHolder constructor(
    private val binding: ItemAddMoreImagesBinding,
    private val interaction: AttachedImagesAdapter.Interaction?
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.attachedAnotherImage.setOnClickListener {
            interaction?.onAddMoreClicked()
        }
    }
}