package com.example.pawfectplanner.ui.detail

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentPetDetailBinding
import com.example.pawfectplanner.data.repository.BreedsRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.network.BreedsCatApiService
import com.example.pawfectplanner.network.BreedsDogApiService
import com.example.pawfectplanner.network.CatApiClient
import com.example.pawfectplanner.network.DogApiClient
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.pawfectplanner.util.ApiKeyManager
import androidx.core.net.toUri

class PetDetailFragment : Fragment(R.layout.fragment_pet_detail) {
    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PetDetailFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPetDetailBinding.bind(view)
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val breedsRepository = BreedsRepository(
            DogApiClient.retrofit.create(BreedsDogApiService::class.java),
            CatApiClient.retrofit.create(BreedsCatApiService::class.java)
        )
        viewModel = ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(dao), breedsRepository)
        )[PetViewModel::class.java]

        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            list.firstOrNull { it.id == args.petId }?.let { pet ->
                setupPetDetails(pet)
                binding.breedInfoCard.visibility = View.GONE
                val dogApiKey = ApiKeyManager.petsApiKey ?: ""
                val catApiKey = ApiKeyManager.petsApiKey ?: ""
                if (pet.breedType.equals("Dog", true)) {
                    viewModel.fetchDogBreed(pet.breed, dogApiKey)
                    observeDogBreed(pet.weightKg)
                } else if (pet.breedType.equals("Cat", true)) {
                    viewModel.fetchCatBreed(pet.breed, catApiKey)
                    observeCatBreed(pet.weightKg)
                }
            }
        }
    }

    private fun setupPetDetails(pet: com.example.pawfectplanner.data.model.Pet) {
        binding.tvPetName.text = pet.name
        binding.tvPetBreed.text = pet.breed
        binding.breedInfoTitle.text = getString(R.string.breed_info_title, pet.breed)

        when {
            pet.isBirthdayGiven && pet.birthDate != null -> {
                val bd = getString(R.string.label_birthday, pet.birthDate.toString())
                val ag = getString(R.string.label_age_only, pet.age)
                binding.tvPetBirth.visibility = View.VISIBLE
                binding.tvPetBirth.text = getString(R.string.label_birthday_age, bd, ag)
            }
            pet.age != null && pet.age > 0 -> {
                binding.tvPetBirth.visibility = View.VISIBLE
                binding.tvPetBirth.text = getString(R.string.label_age_only, pet.age)
            }
            else -> {
                binding.tvPetBirth.visibility = View.GONE
            }
        }

        pet.weightKg?.let {
            binding.tvPetWeightProfile.visibility = View.VISIBLE
            binding.tvPetWeightProfile.text = getString(R.string.label_pet_weight, it)
        } ?: run {
            binding.tvPetWeightProfile.visibility = View.GONE
        }

        if (!pet.photoUri.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(pet.photoUri.toUri())
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)
                    .centerCrop()
                    .into(binding.ivPetPhoto)
            } catch (e: Exception) {
                binding.ivPetPhoto.setImageResource(R.drawable.ic_photo_placeholder)
            }
        } else {
            binding.ivPetPhoto.setImageResource(R.drawable.ic_photo_placeholder)
        }

        binding.tvHealthIssues.text = if (pet.healthIssues.isEmpty()) {
            getString(R.string.no_issues)
        } else {
            pet.healthIssues.joinToString("\n") { getString(R.string.label_bullet_item, it) }
        }

        binding.tvBehaviorIssues.text = if (pet.behaviorIssues.isEmpty()) {
            getString(R.string.no_issues)
        } else {
            pet.behaviorIssues.joinToString("\n") { getString(R.string.label_bullet_item, it) }
        }

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                PetDetailFragmentDirections
                    .actionPetDetailFragmentToPetEditFragment(pet.id)
            )
        }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_pet_title)
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.action_delete_pet) { _, _ ->
                    viewModel.delete(pet)
                    findNavController().navigateUp()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun observeDogBreed(userWeight: Double?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dogBreed.collectLatest { info ->
                if (info != null) {
                    binding.breedInfoCard.visibility = View.VISIBLE
                    bindBreedInfo(
                        lifeSpan = info.lifeSpan,
                        weight = info.weight?.metric,
                        temperament = info.temperament,
                        breedGroup = info.breedGroup,
                        bredFor = info.bredFor,
                        userWeight = userWeight
                    )
                } else {
                    binding.breedInfoCard.visibility = View.GONE
                }
            }
        }
    }

    private fun observeCatBreed(userWeight: Double?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.catBreed.collectLatest { info ->
                if (info != null) {
                    binding.breedInfoCard.visibility = View.VISIBLE
                    bindBreedInfo(
                        lifeSpan = info.lifeSpan,
                        weight = info.weight?.metric,
                        temperament = info.temperament,
                        breedGroup = info.origin,
                        bredFor = null,
                        userWeight = userWeight
                    )
                } else {
                    binding.breedInfoCard.visibility = View.GONE
                }
            }
        }
    }

    private fun bindBreedInfo(
        lifeSpan: String?,
        weight: String?,
        temperament: String?,
        breedGroup: String?,
        bredFor: String?,
        userWeight: Double?
    ) {
        showOrHide(binding.labelLifeSpan, binding.breedLifeSpan, lifeSpan)
        showOrHide(binding.labelWeight, binding.breedWeight, weight?.let { "$it kg" })
        showOrHide(binding.labelTemperament, binding.breedTemperament, temperament)
        showOrHide(binding.labelBreedGroup, binding.breedGroup, breedGroup)
        if (bredFor != null) {
            showOrHide(binding.labelBredFor, binding.breedBredFor, bredFor)
        } else {
            binding.labelBredFor.visibility = View.GONE
            binding.breedBredFor.visibility = View.GONE
        }
        userWeight?.let { checkWeight(it, weight) }
    }

    private fun showOrHide(label: View, valueView: TextView, value: String?) {
        if (!value.isNullOrEmpty()) {
            label.visibility = View.VISIBLE
            valueView.visibility = View.VISIBLE
            valueView.text = value
        } else {
            label.visibility = View.GONE
            valueView.visibility = View.GONE
        }
    }

    private fun showWeightError(show: Boolean) {
        binding.tvWeightError.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun checkWeight(userWeight: Double, apiWeight: String?) {
        if (apiWeight.isNullOrEmpty()) {
            showWeightError(true)
            return
        }
        val numbers = apiWeight
            .split("-")
            .mapNotNull { it.trim().toDoubleOrNull() }
        if (numbers.isEmpty()) {
            showWeightError(true)
            return
        }
        val min = numbers.minOrNull()!!
        val max = numbers.maxOrNull()!!
        showWeightError(userWeight < min || userWeight > max)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
