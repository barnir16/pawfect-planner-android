package com.example.pawfectplanner.ui.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
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
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import java.util.*

class PetEditFragment : Fragment() {
    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    private val args: PetEditFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel
    private val healthIssues = mutableListOf<String>()
    private val behaviorIssues = mutableListOf<String>()
    private var selectedBirthDate: LocalDate? = null
    private var selectedAge: Int? = null
    private var selectedImageUriString: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentPetEditBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        viewModel = ViewModelProvider(this, PetViewModelFactory(PetRepository(dao)))[PetViewModel::class.java]

        binding.etPetName.doAfterTextChanged {
            binding.btnSavePet.isEnabled = it?.isNotBlank() == true
        }

        val types = listOf("Dog", "Cat", "Other")
        binding.spinnerPetType.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        binding.spinnerPetType.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long
                ) {
                    binding.tilCustomType.visibility =
                        if (types[pos] == "Other") View.VISIBLE else View.GONE
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
            }

        binding.btnAddHealthIssue.setOnClickListener {
            val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_TEXT }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_add_health_issue)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    input.text.toString().takeIf { it.isNotBlank() }?.let {
                        healthIssues += it
                        binding.containerHealthIssues.addView(
                            TextView(requireContext()).apply {
                                text = getString(R.string.label_bullet_item, it)
                            }
                        )
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        binding.btnAddBehaviorIssue.setOnClickListener {
            val input = EditText(requireContext()).apply { inputType = InputType.TYPE_CLASS_TEXT }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_add_behavior_issue)
                .setView(input)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    input.text.toString().takeIf { it.isNotBlank() }?.let {
                        behaviorIssues += it
                        binding.containerBehaviorIssues.addView(
                            TextView(requireContext()).apply {
                                text = getString(R.string.label_bullet_item, it)
                            }
                        )
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
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
            val options = arrayOf(
                getString(R.string.option_select_birthday),
                getString(R.string.option_enter_age)
            )
            AlertDialog.Builder(requireContext())
                .setItems(options) { _, which ->
                    if (which == 0) showDatePicker() else showAgeDialog()
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
                    binding.etPetWeight.setText(pet.weightKg?.toString())
                    selectedBirthDate = pet.birthDate
                    selectedAge = pet.age
                    val label = selectedBirthDate
                        ?.let { getString(R.string.label_birthday, it.toString()) }
                        ?: getString(R.string.label_age_only, selectedAge)
                    binding.btnBirthdayAge.text = label
                    pet.photoUri?.let { Glide.with(this).load(it).into(binding.imgPetPhoto) }
                }
            }
        }

        binding.btnSavePet.setOnClickListener {
            val name = binding.etPetName.text.toString()
            val type = if (binding.tilCustomType.isVisible)
                binding.etCustomType.text.toString()
            else binding.spinnerPetType.selectedItem as String
            val breed = binding.etPetBreed.text.toString()
            val weight = binding.etPetWeight.text.toString().toDoubleOrNull()
            val birthDate = selectedBirthDate
                ?: LocalDate.now().minusYears(selectedAge?.toLong() ?: 0)
            val age = selectedAge
                ?: Period.between(birthDate, LocalDate.now()).years
            val pet = Pet(
                id = if (args.petId != -1) args.petId else 0,
                name = name,
                breedType = type,
                breed = breed,
                birthDate = birthDate,
                age = age,
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
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.option_enter_age)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                selectedAge = input.text.toString().toIntOrNull()
                selectedBirthDate = null
                binding.btnBirthdayAge.text =
                    getString(R.string.label_age_only, selectedAge)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
