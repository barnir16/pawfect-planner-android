package com.example.pawfectplanner.ui.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetEditBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

class PetEditFragment : Fragment() {
    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PetViewModel
    private var selectedDate: LocalDate? = null
    private var selectedImageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.also {
            selectedImageUri = it
            binding.imgPetPhoto.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val repo = PetRepository(dao)
        viewModel = ViewModelProvider(this, PetViewModelFactory(repo))[PetViewModel::class.java]

        binding.btnPickDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().build()
            picker.show(childFragmentManager, "date")
            picker.addOnPositiveButtonClickListener { ts ->
                selectedDate = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
                binding.btnPickDate.text = selectedDate.toString()
            }
        }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSavePet.setOnClickListener {
            val name = binding.etPetName.text.toString()
            val breed = binding.etPetBreed.text.toString()
            val birth = selectedDate ?: LocalDate.now()
            val age = birth.until(LocalDate.now()).years
            val photo = selectedImageUri?.toString()
            viewModel.insert(Pet(name = name, breed = breed, age = age, birthDate = birth, photoUri = photo))
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
