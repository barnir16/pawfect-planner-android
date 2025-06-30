package com.example.pawfectplanner.ui.list

import androidx.core.net.toUri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.databinding.ItemPetBinding

class PetAdapter(
    private val onClick: (Pet) -> Unit,
    private val onLongClick: (Pet) -> Boolean
) : ListAdapter<Pet, PetAdapter.PetViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetViewHolder(
            ItemPetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(
        private val binding: ItemPetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?.let { onClick(getItem(it)) }
            }
            binding.root.setOnLongClickListener {
                adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                    ?.let { return@setOnLongClickListener onLongClick(getItem(it)) }
                false
            }
        }

        fun bind(pet: Pet) {
            binding.petName.text = pet.name
            binding.petBreed.text = pet.breed
            binding.petAge.text =
                itemView.context.getString(R.string.label_age_only, pet.age)

            if (!pet.photoUri.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(pet.photoUri.toUri())
                    .centerCrop()
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)
                    .into(binding.petImage)
            } else {
                binding.petImage.setImageResource(R.drawable.ic_photo_placeholder)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(oldItem: Pet, newItem: Pet) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Pet, newItem: Pet) =
                oldItem == newItem
        }
    }
}
