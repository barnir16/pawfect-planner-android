package com.example.pawfectplanner.ui.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.databinding.FragmentPetListBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory

class PetListFragment : Fragment(R.layout.fragment_pet_list) {

    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PetViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPetListBinding.bind(view)

        val dao =
            (requireActivity().application as PawfectPlannerApplication).database.petDao()
        val repo = PetRepository(dao)
        viewModel =
            ViewModelProvider(this, PetViewModelFactory(repo))[PetViewModel::class.java]

        val adapter = PetAdapter(
            onClick = { pet ->
                val action =
                    PetListFragmentDirections.actionListToDetail(pet.id)
                findNavController().navigate(action)
            },
            onLongClick = { pet ->
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_pet_title))
                    .setMessage(getString(R.string.delete_pet_message))
                    .setPositiveButton(getString(R.string.delete)) { _, _ ->
                        viewModel.delete(pet)
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
                true
            }
        )

        binding.rvPets.adapter = adapter

        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
