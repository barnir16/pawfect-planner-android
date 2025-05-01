package com.example.pawfectplanner.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.databinding.ItemPetBinding
import org.threeten.bp.LocalDate
import org.threeten.bp.Period

class PetAdapter(
    private val onClick: (Pet) -> Unit,
    private val onLongClick: (Pet) -> Boolean
) : ListAdapter<Pet, PetAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onLongClick)
    }

    class ViewHolder(private val binding: ItemPetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet, onClick: (Pet) -> Unit, onLongClick: (Pet) -> Boolean) {
            binding.tvName.text = pet.name
            binding.tvBreed.text = pet.breed

            val age = Period.between(pet.birthDate, LocalDate.now()).years
            binding.tvAge.text =
                binding.root.context.getString(R.string.label_pet_age) + ": $age"

            binding.root.setOnClickListener { onClick(pet) }
            binding.root.setOnLongClickListener { onLongClick(pet) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(old: Pet, new: Pet) = old.id == new.id
            override fun areContentsTheSame(old: Pet, new: Pet) = old == new
        }
    }
}
