package com.example.pawfectplanner.ui.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetEditBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import org.threeten.bp.LocalDate

class PetEditFragment : Fragment() {

    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    private val args: PetEditFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao =
            (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val repo = PetRepository(dao)
        viewModel =
            ViewModelProvider(this, PetViewModelFactory(repo))[PetViewModel::class.java]

        if (args.petId != -1) {
            viewModel.allPets.observe(viewLifecycleOwner) { list ->
                val pet = list.firstOrNull { it.id == args.petId } ?: return@observe
                binding.etPetName.setText(pet.name)
                binding.etPetBreed.setText(pet.breed)
            }
        }

        binding.btnSavePet.setOnClickListener {
            val name = binding.etPetName.text.toString()
            val breed = binding.etPetBreed.text.toString()
            val pet = Pet(
                id = if (args.petId == -1) 0 else args.petId,
                name = name,
                breed = breed,
                age = 0,
                birthDate = LocalDate.now()
            )
            if (args.petId == -1) {
                viewModel.insert(pet)
            } else {
                viewModel.update(pet)
            }
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
