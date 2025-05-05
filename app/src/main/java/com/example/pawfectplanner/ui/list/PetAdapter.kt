package com.example.pawfectplanner.ui.list

import android.net.Uri
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
        PetViewHolder(ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onLongClick)
    }

    class PetViewHolder(private val b: ItemPetBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(pet: Pet, onClick: (Pet) -> Unit, onLongClick: (Pet) -> Boolean) {
            b.tvName.text = pet.name
            b.tvBreed.text = pet.breed
            b.tvAge.text = b.root.context.getString(R.string.label_age_only, pet.age)
            if (pet.photoUri != null) {
                Glide.with(b.root)
                    .load(Uri.parse(pet.photoUri))
                    .into(b.imgPetThumbnail)
            } else {
                b.imgPetThumbnail.setImageResource(R.drawable.ic_photo_placeholder)
            }
            b.root.setOnClickListener { onClick(pet) }
            b.root.setOnLongClickListener { onLongClick(pet) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(old: Pet, new: Pet) = old.id == new.id
            override fun areContentsTheSame(old: Pet, new: Pet) = old == new
        }
    }
}
