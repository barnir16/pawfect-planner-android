package com.example.pawfectplanner.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetDetailBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory

class PetDetailFragment : Fragment() {
    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PetDetailFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val repo = PetRepository(dao)
        viewModel = ViewModelProvider(this, PetViewModelFactory(repo))[PetViewModel::class.java]

        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            val pet = list.firstOrNull { it.id == args.petId } ?: return@observe
            binding.tvPetDetails.text =
                "${pet.name}\n${pet.breed}\n${pet.birthDate}\nAge: ${pet.age}"
            binding.btnEdit.setOnClickListener {
                val action = PetDetailFragmentDirections
                    .actionPetDetailFragmentToPetEditFragment(pet.id)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}