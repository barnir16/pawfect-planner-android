package com.example.pawfectplanner.ui.edit

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.BreedsRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetEditBinding
import com.example.pawfectplanner.network.BreedsCatApiService
import com.example.pawfectplanner.network.BreedsDogApiService
import com.example.pawfectplanner.network.CatApiClient
import com.example.pawfectplanner.network.DogApiClient
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.example.pawfectplanner.util.ApiKeyManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.util.Calendar

class PetEditFragment : Fragment() {
    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    private val args: PetEditFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    private var selectedBirthDate: LocalDate? = null
    private var selectedAge: Int? = null
    private var selectedImageUriString: String? = null
    private val healthIssues = mutableListOf<String>()
    private val behaviorIssues = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPetEditBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val breedsRepo = BreedsRepository(
            DogApiClient.retrofit.create(BreedsDogApiService::class.java),
            CatApiClient.retrofit.create(BreedsCatApiService::class.java)
        )
        viewModel = ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(dao), breedsRepo)
        )[PetViewModel::class.java]

        val entries = resources.getStringArray(R.array.pet_type_entries).toList()
        val values = resources.getStringArray(R.array.pet_type_values).toList()
        binding.spinnerPetType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            entries
        )
        binding.spinnerPetType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val selectedType = values[pos]
                binding.tilCustomType.isVisible = selectedType == "Other"
                val dogKey = ApiKeyManager.petsApiKey.orEmpty()
                val catKey = ApiKeyManager.petsApiKey.orEmpty()
                viewModel.fetchBreeds(selectedType, dogKey, catKey)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        lifecycleScope.launch {
            viewModel.breedList.collect { breeds ->
                val withOther = breeds + getString(R.string.option_other_breed)
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    withOther
                )
                binding.etPetBreed.setAdapter(adapter)
                binding.etPetBreed.setOnItemClickListener { _, _, pos, _ ->
                    binding.tilCustomBreed.isVisible =
                        withOther[pos] == getString(R.string.option_other_breed)
                }
            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUriString = it.toString()
                Glide.with(this).load(it).into(binding.imgPetPhoto)
            }
        }
        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.imgPetPhoto.setOnClickListener { pickImage.launch("image/*") }

        binding.btnBirthdayAge.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setItems(
                    arrayOf(
                        getString(R.string.option_select_birthday),
                        getString(R.string.option_enter_age)
                    )
                ) { _, which ->
                    if (which == 0) showDatePicker() else showAgeDialog()
                }
                .show()
        }

        setupIssuesButtons()
        if (args.petId != -1) loadExistingPetData(values)
        binding.btnSavePet.setOnClickListener { savePet(values) }
    }

    private fun loadExistingPetData(typeValues: List<String>) {
        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            list.firstOrNull { it.id == args.petId }?.let { pet ->
                binding.etPetName.setText(pet.name)

                val ti = typeValues.indexOf(pet.breedType).takeIf { it >= 0 } ?: 2
                binding.spinnerPetType.setSelection(ti)
                if (ti == 2) binding.etCustomType.setText(pet.breedType)

                if (pet.breedListContains(pet.breed, requireContext())) {
                    binding.etPetBreed.setText(pet.breed)
                } else {
                    binding.tilCustomBreed.isVisible = true
                    binding.etCustomBreed.setText(pet.breed)
                }

                binding.etPetWeight.setText(pet.weightKg?.toString().orEmpty())
                selectedBirthDate = pet.birthDate
                selectedAge = pet.age
                binding.btnBirthdayAge.text = when {
                    selectedBirthDate != null ->
                        getString(R.string.label_birthday, selectedBirthDate.toString())
                    selectedAge != null ->
                        getString(R.string.label_age_only, selectedAge)
                    else ->
                        getString(R.string.btn_birthday_age)
                }

                pet.photoUri?.let {
                    selectedImageUriString = it
                    Glide.with(this).load(it).into(binding.imgPetPhoto)
                }

                healthIssues.clear()
                behaviorIssues.clear()
                binding.chipGroupHealth.removeAllViews()
                binding.chipGroupBehavior.removeAllViews()
                pet.healthIssues.forEach { addChip(it, healthIssues, binding.chipGroupHealth) }
                pet.behaviorIssues.forEach { addChip(it, behaviorIssues, binding.chipGroupBehavior) }
            }
        }
    }

    private fun savePet(typeValues: List<String>) {
        val name = binding.etPetName.text.toString().trim()
        val type = if (binding.tilCustomType.isVisible)
            binding.etCustomType.text.toString().trim()
        else
            typeValues[binding.spinnerPetType.selectedItemPosition]
        val breed = if (binding.tilCustomBreed.isVisible)
            binding.etCustomBreed.text.toString().trim()
        else
            binding.etPetBreed.text.toString().trim()
        val weight = binding.etPetWeight.text.toString().toDoubleOrNull()
        val birthDate = selectedBirthDate
            ?: LocalDate.now().minusYears(selectedAge?.toLong() ?: 0)
        val age = selectedAge ?: Period.between(birthDate, LocalDate.now()).years

        val missing = mutableListOf<String>()
        if (name.isEmpty()) {
            binding.etPetName.error = getString(R.string.field_required)
            missing += getString(R.string.hint_pet_name)
        } else {
            binding.etPetName.error = null
        }

        if (breed.isEmpty()) {
            if (binding.tilCustomBreed.isVisible) {
                binding.etCustomBreed.error = getString(R.string.field_required)
            } else {
                binding.etPetBreed.error = getString(R.string.field_required)
            }
            missing += getString(R.string.hint_pet_breed)
        } else {
            binding.etPetBreed.error = null
            binding.etCustomBreed.error = null
        }

        if (missing.isNotEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_missing_fields, missing.joinToString(", ")),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val pet = Pet(
            id = if (args.petId != -1) args.petId else 0,
            name = name,
            breedType = type,
            breed = breed,
            birthDate = if (selectedBirthDate != null) birthDate else null,
            age = age,
            isBirthdayGiven = selectedBirthDate != null,
            weightKg = weight,
            photoUri = selectedImageUriString,
            healthIssues = healthIssues,
            behaviorIssues = behaviorIssues
        )

        if (args.petId == -1) viewModel.insert(pet) else viewModel.update(pet)
        findNavController().navigateUp()
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                selectedBirthDate = LocalDate.of(y, m + 1, d)
                selectedAge = null
                binding.btnBirthdayAge.text =
                    getString(R.string.label_birthday, selectedBirthDate.toString())
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.maxDate = System.currentTimeMillis() }
            .show()
    }

    private fun showAgeDialog() {
        val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_NUMBER }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.option_enter_age)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                input.text.toString().toIntOrNull()?.let {
                    selectedAge = it
                    selectedBirthDate = null
                    binding.btnBirthdayAge.text =
                        getString(R.string.label_age_only, it)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setupIssuesButtons() {
        binding.btnAddHealthIssue.setOnClickListener {
            showIssueDialog(R.string.title_add_health_issue, healthIssues, binding.chipGroupHealth)
        }
        binding.btnAddBehaviorIssue.setOnClickListener {
            showIssueDialog(R.string.title_add_behavior_issue, behaviorIssues, binding.chipGroupBehavior)
        }
    }

    private fun showIssueDialog(
        titleRes: Int,
        list: MutableList<String>,
        chipGroup: com.google.android.material.chip.ChipGroup
    ) {
        val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_TEXT }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                input.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
                    addChip(it, list, chipGroup)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addChip(
        issue: String,
        list: MutableList<String>,
        chipGroup: com.google.android.material.chip.ChipGroup
    ) {
        list += issue
        val chip = Chip(requireContext()).apply {
            text = getString(R.string.label_bullet_item, issue)
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                list -= issue
                chipGroup.removeView(this)
            }
        }
        chipGroup.addView(chip)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun Pet.breedListContains(breed: String, context: Context): Boolean {
    val list = context.resources.getStringArray(R.array.pet_type_entries)
    return breed in list
}
