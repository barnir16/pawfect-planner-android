package com.example.pawfectplanner.ui.edit

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.R
import com.example.pawfectplanner.PawfectPlannerApplication
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

    private var selectedBirthDate: LocalDate? = null
    private var selectedAge: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val repo = PetRepository(dao)
        viewModel = ViewModelProvider(this, PetViewModelFactory(repo))[PetViewModel::class.java]

        if (args.petId != -1) {
            viewModel.allPets.observe(viewLifecycleOwner) { list ->
                val pet = list.firstOrNull { it.id == args.petId } ?: return@observe
                binding.etPetName.setText(pet.name)
                binding.etPetBreed.setText(pet.breed)
                selectedBirthDate = pet.birthDate
                selectedAge = pet.age
                updateBirthdayAgeButtonText()
            }
        }

        binding.btnBirthdayAge.setOnClickListener {
            showBirthdayAgeDialog()
        }

        binding.btnSavePet.setOnClickListener {
            val name = binding.etPetName.text.toString()
            val breed = binding.etPetBreed.text.toString()
            val (birthDate, age) = if (selectedBirthDate != null) {
                val bd = selectedBirthDate!!
                bd to Period.between(bd, LocalDate.now()).years
            } else {
                val a = selectedAge ?: 0
                LocalDate.now().minusYears(a.toLong()) to a
            }
            val pet = Pet(
                id = if (args.petId == -1) 0 else args.petId,
                name = name,
                breed = breed,
                age = age,
                birthDate = birthDate
            )
            if (args.petId == -1) viewModel.insert(pet) else viewModel.update(pet)
            findNavController().navigateUp()
        }
    }

    private fun showBirthdayAgeDialog() {
        val options = arrayOf(
            getString(R.string.option_select_birthday),
            getString(R.string.option_enter_age)
        )
        AlertDialog.Builder(requireContext())
            .setItems(options) { _: DialogInterface, which: Int ->
                if (which == 0) showDatePicker() else showAgeInputDialog()
            }
            .show()
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                selectedBirthDate = LocalDate.of(y, m + 1, d)
                selectedAge = null
                updateBirthdayAgeButtonText()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showAgeInputDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.option_enter_age)
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                selectedAge = input.text.toString().toIntOrNull()
                selectedBirthDate = null
                updateBirthdayAgeButtonText()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateBirthdayAgeButtonText() {
        binding.btnBirthdayAge.text = when {
            selectedBirthDate != null -> selectedBirthDate.toString()
            selectedAge != null -> getString(R.string.label_age_only, selectedAge)
            else -> getString(R.string.btn_birthday_age)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
