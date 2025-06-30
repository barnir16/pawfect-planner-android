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

class PetDetailFragment : Fragment() {
    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PetDetailFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?) =
        FragmentPetDetailBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
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
            val pet = list.firstOrNull { it.id == args.petId }

            if (pet != null) {
                setupPetDetails(pet)
                
                binding.breedInfoCard.visibility = View.GONE

                val dogApiKey = ApiKeyManager.petsApiKey ?: ""
                val catApiKey = ApiKeyManager.petsApiKey ?: ""

                if (pet.breedType.equals("Dog", ignoreCase = true)) {
                    viewModel.fetchDogBreed(pet.breed, dogApiKey)
                    observeDogBreed(pet.weightKg)
                } else if (pet.breedType.equals("Cat", ignoreCase = true)) {
                    viewModel.fetchCatBreed(pet.breed, catApiKey)
                    observeCatBreed(pet.weightKg)
                }
            }
        }
    }

    private fun observeDogBreed(userWeight: Double?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dogBreed.collectLatest { info ->
                if (info != null) {
                    binding.breedInfoCard.visibility = View.VISIBLE
                    bindBreedInfo(
                        lifeSpan = info.life_span,
                        weight = info.weight?.metric,
                        temperament = info.temperament,
                        breedGroup = info.breed_group,
                        bredFor = info.bred_for,
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
                        lifeSpan = info.life_span,
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

    private fun setupPetDetails(pet: com.example.pawfectplanner.data.model.Pet) {
        binding.tvPetName.text = pet.name
        binding.tvPetBreed.text = pet.breed
        binding.breedInfoTitle.text = getString(R.string.breed_info_title, pet.breed)

        binding.tvPetBirth.text = if (pet.isBirthdayGiven && pet.birthDate != null) {
            val bd = getString(R.string.label_birthday, pet.birthDate.toString())
            val ag = getString(R.string.label_age_only, pet.age)
            getString(R.string.label_birthday_age, bd, ag)
        } else {
            getString(R.string.label_age_only, pet.age)
        }

        if (!pet.photoUri.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(Uri.parse(pet.photoUri))
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
            findNavController().navigate(PetDetailFragmentDirections.actionPetDetailFragmentToPetEditFragment(pet.id))
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

        userWeight?.let {
            checkWeight(it, weight)
        }
    }

    private fun showOrHide(label: View, valueView: TextView, value: String?) {
        if (!value.isNullOrEmpty()) {
            valueView.text = value
            label.visibility = View.VISIBLE
            valueView.visibility = View.VISIBLE
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

        val numbers = apiWeight.split("-").map { it.trim().toDoubleOrNull() }.filterNotNull()

        if (numbers.isEmpty()) {
            showWeightError(true)
            return
        }

        if (numbers.size == 1) {
            val exact = numbers[0]
            val margin = exact * 0.2
            showWeightError(userWeight !in (exact - margin)..(exact + margin))
        } else if (numbers.size == 2) {
            val min = numbers.minOrNull()!!
            val max = numbers.maxOrNull()!!
            showWeightError(userWeight !in min..max)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
