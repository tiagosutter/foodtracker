package br.dev.tiagosutter.foodtracker.ui.newentry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.dev.tiagosutter.foodtracker.R
import br.dev.tiagosutter.foodtracker.databinding.ItemAttachedImageBinding
import br.dev.tiagosutter.foodtracker.entities.SavedImage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File

class AttachedImagesAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SavedImage>() {

        override fun areItemsTheSame(oldItem: SavedImage, newItem: SavedImage): Boolean {
            TODO("not implemented")
        }

        override fun areContentsTheSame(oldItem: SavedImage, newItem: SavedImage): Boolean {
            TODO("not implemented")
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAttachedImageBinding.inflate(inflater, parent, false)
        return AttachedImagesViewHolder(
            binding,
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AttachedImagesViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<SavedImage>) {
        differ.submitList(list)
    }

    class AttachedImagesViewHolder
    constructor(
        private val binding: ItemAttachedImageBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SavedImage) {
            val context = itemView.context
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
            val filesDir = File(context.filesDir, "images")
            val image = File(filesDir, item.name)
            val radius = context.resources.getDimensionPixelSize(R.dimen.attached_image_corner_radius)

            Glide.with(context)
                .load(image.path)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(radius)))
                .into(binding.attachedImage);
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: SavedImage)
    }
}