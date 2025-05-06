package com.example.pawfectplanner.ui.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetEditBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    ) = FragmentPetEditBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        viewModel = ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(dao))
        )[PetViewModel::class.java]

        binding.etPetName.doAfterTextChanged { editable ->
            binding.btnSavePet.isEnabled = editable?.isNotBlank() == true
        }

        val types = listOf("Dog", "Cat", "Other")
        binding.spinnerPetType.adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        binding.spinnerPetType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                binding.tilCustomType.isVisible = types[pos] == "Other"
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fun addIssue(titleRes: Int, list: MutableList<String>, chipGroup: ChipGroup) {
            val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_TEXT }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleRes)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val issue = input.text.toString().trim()
                    if (issue.isNotEmpty()) {
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
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.btnAddHealthIssue.setOnClickListener {
            addIssue(R.string.title_add_health_issue, healthIssues, binding.chipGroupHealth)
        }
        binding.btnAddBehaviorIssue.setOnClickListener {
            addIssue(R.string.title_add_behavior_issue, behaviorIssues, binding.chipGroupBehavior)
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
                    if (which == 0) showDatePicker()
                    else showAgeDialog()
                }
                .show()
        }

        if (args.petId != -1) {
            viewModel.allPets.observe(viewLifecycleOwner) { list ->
                list.firstOrNull { it.id == args.petId }?.let { pet ->
                    binding.etPetName.setText(pet.name)
                    binding.btnSavePet.isEnabled = true

                    val pos = types.indexOf(pet.breedType).takeIf { it >= 0 } ?: 2
                    binding.spinnerPetType.setSelection(pos)
                    if (pos == 2) binding.etCustomType.setText(pet.breedType)

                    binding.etPetBreed.setText(pet.breed)
                    binding.etPetWeight.setText(pet.weightKg?.toString() ?: "")

                    selectedBirthDate = pet.birthDate
                    selectedAge = pet.age

                    binding.btnBirthdayAge.text = when {
                        selectedBirthDate != null ->
                            getString(R.string.label_birthday, selectedBirthDate.toString())
                        else ->
                            getString(R.string.label_age_only, selectedAge)
                    }

                    pet.photoUri?.let {
                        Glide.with(this).load(it).into(binding.imgPetPhoto)
                    }

                    binding.chipGroupHealth.removeAllViews()
                    pet.healthIssues.forEach { issue ->
                        val chip = Chip(requireContext()).apply {
                            text = getString(R.string.label_bullet_item, issue)
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                healthIssues -= issue
                                binding.chipGroupHealth.removeView(this)
                            }
                        }
                        binding.chipGroupHealth.addView(chip)
                    }

                    binding.chipGroupBehavior.removeAllViews()
                    pet.behaviorIssues.forEach { issue ->
                        val chip = Chip(requireContext()).apply {
                            text = getString(R.string.label_bullet_item, issue)
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                behaviorIssues -= issue
                                binding.chipGroupBehavior.removeView(this)
                            }
                        }
                        binding.chipGroupBehavior.addView(chip)
                    }
                }
            }
        }

        binding.btnSavePet.setOnClickListener {
            val name = binding.etPetName.text.toString().trim()
            val type = if (binding.tilCustomType.isVisible)
                binding.etCustomType.text.toString().trim()
            else
                binding.spinnerPetType.selectedItem as String
            val breed = binding.etPetBreed.text.toString().trim()
            val weight = binding.etPetWeight.text.toString().toDoubleOrNull()
            val birthDate = selectedBirthDate
                ?: LocalDate.now().minusYears(selectedAge?.toLong() ?: 0)
            val age = selectedAge ?: Period.between(birthDate, LocalDate.now()).years

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
        ).show()
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
                    binding.btnBirthdayAge.text = getString(R.string.label_age_only, selectedAge)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
