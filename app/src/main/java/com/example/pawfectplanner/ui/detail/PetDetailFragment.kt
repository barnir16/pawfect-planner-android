package com.example.pawfectplanner.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentPetDetailBinding
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory

class PetDetailFragment : Fragment() {
    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val args: PetDetailFragmentArgs by navArgs()
    private lateinit var viewModel: PetViewModel

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPetDetailBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        val dao = (requireActivity().application as PawfectPlannerApplication).database.petDao()
        viewModel = ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(dao))
        ).get(PetViewModel::class.java)

        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            list.firstOrNull { it.id == args.petId }?.let { pet ->
                binding.tvPetName.text = pet.name
                binding.tvPetBreed.text = pet.breed

                if (pet.birthDate != null) {
                    val birthdayText = getString(R.string.label_birthday, pet.birthDate.toString())
                    val ageText = getString(R.string.label_age_only, pet.age)
                    binding.tvPetBirth.text =
                        getString(R.string.label_birthday_age, birthdayText, ageText)
                } else {
                    binding.tvPetBirth.text =
                        getString(R.string.label_age_only, pet.age)
                }

                binding.tvPetNotes.text =
                    (pet.healthIssues + pet.behaviorIssues)
                        .joinToString("\n") { getString(R.string.label_bullet_item, it) }

                binding.btnEdit.setOnClickListener {
                    findNavController().navigate(
                        PetDetailFragmentDirections
                            .actionPetDetailFragmentToPetEditFragment(pet.id)
                    )
                }
                binding.btnDelete.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_pet_title)
                        .setMessage(R.string.delete_pet_message)
                        .setPositiveButton(R.string.action_delete_pet) { _, _ ->
                            viewModel.delete(pet)
                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
