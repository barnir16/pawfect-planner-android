package com.example.pawfectplanner.ui.task

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.model.Vaccine
import com.example.pawfectplanner.data.repository.VaccineLocalizedRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class VaccineSuggestionDialog : DialogFragment() {
    
    private var onVaccineSelected: ((Vaccine) -> Unit)? = null
    private var assignedPets: List<Pet> = emptyList()
    private var vaccineRepository: VaccineLocalizedRepository? = null
    
    companion object {
        fun newInstance(pets: List<Pet>, onVaccineSelected: (Vaccine) -> Unit): VaccineSuggestionDialog {
            return VaccineSuggestionDialog().apply {
                this.assignedPets = pets
                this.onVaccineSelected = onVaccineSelected
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            vaccineRepository = VaccineLocalizedRepository(requireContext())
        } catch (e: Exception) {
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val vaccines = getRelevantVaccines()
        
        val vaccineAdapter = VaccineAdapter(vaccines) { vaccine ->
            onVaccineSelected?.invoke(vaccine)
            dismiss()
        }
        
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vaccineAdapter
        }
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.vaccine_suggestions_title))
            .setView(recyclerView)
            .setNegativeButton(getString(R.string.btn_cancel)) { _, _ -> dismiss() }
            .create()
    }
    
    private fun getRelevantVaccines(): List<VaccineWithSpecies> {
        val repository = vaccineRepository ?: return emptyList()
        val catVaccines = repository.getCatVaccines()
        val dogVaccines = repository.getDogVaccines()
        val merged = mutableMapOf<String, VaccineWithSpecies>()

        fun addVaccine(v: Vaccine, species: Species) {
            val key = v.name.trim().lowercase()
            val existing = merged[key]
            if (existing == null) {
                merged[key] = VaccineWithSpecies(v, speciesSet = mutableSetOf(species))
            } else {
                existing.speciesSet.add(species)
            }
        }

        if (assignedPets.isEmpty()) {
            catVaccines.forEach { addVaccine(it, Species.CAT) }
            dogVaccines.forEach { addVaccine(it, Species.DOG) }
        } else {
            val showCat = assignedPets.any { it.breedType.equals("cat", true) || it.breedType.equals("kitten", true) }
            val showDog = assignedPets.any { it.breedType.equals("dog", true) || it.breedType.equals("puppy", true) }
            if (showCat) catVaccines.forEach { addVaccine(it, Species.CAT) }
            if (showDog) dogVaccines.forEach { addVaccine(it, Species.DOG) }
        }
        return merged.values.sortedBy { it.vaccine.name }
    }

    private enum class Species { CAT, DOG }
    private data class VaccineWithSpecies(val vaccine: Vaccine, val speciesSet: MutableSet<Species>)
    
    private inner class VaccineAdapter(
        private val vaccines: List<VaccineWithSpecies>,
        private val onVaccineClick: (Vaccine) -> Unit
    ) : RecyclerView.Adapter<VaccineAdapter.VaccineViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccineViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vaccine_suggestion, parent, false)
            return VaccineViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: VaccineViewHolder, position: Int) {
            holder.bind(vaccines[position])
        }
        
        override fun getItemCount() = vaccines.size
        
        inner class VaccineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val cardView: MaterialCardView = itemView.findViewById(R.id.cardVaccine)
            private val tvName: TextView = itemView.findViewById(R.id.tvVaccineName)
            private val tvFrequency: TextView = itemView.findViewById(R.id.tvFrequency)
            private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
            private val ivCat: View = itemView.findViewById(R.id.ivCatIcon)
            private val ivDog: View = itemView.findViewById(R.id.ivDogIcon)
            
            fun bind(vaccineWithSpecies: VaccineWithSpecies) {
                val vaccine = vaccineWithSpecies.vaccine
                tvName.text = vaccine.name
                tvFrequency.text = vaccine.frequency
                tvDescription.text = vaccine.description
                val species = vaccineWithSpecies.speciesSet
                ivCat.visibility = if (species.contains(Species.CAT)) View.VISIBLE else View.GONE
                ivDog.visibility = if (species.contains(Species.DOG)) View.VISIBLE else View.GONE
                cardView.setOnClickListener {
                    onVaccineClick(vaccine)
                }
            }
        }
    }
} 