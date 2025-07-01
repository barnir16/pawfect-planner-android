package com.example.pawfectplanner.ui.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentPetListBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PetListFragment : Fragment(R.layout.fragment_pet_list) {
    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PetViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPetListBinding.bind(view)

        val adapter = PetAdapter(
            onClick = { pet ->
                findNavController().navigate(
                    PetListFragmentDirections
                        .actionPetListFragmentToPetDetailFragment(pet.id)
                )
            },
            onLongClick = { pet ->
                viewModel.delete(pet)
                true
            }
        )

        binding.rvPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPets.adapter = adapter

        viewModel.allPets.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.cardNoPets.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.fabAddPet.setOnClickListener {
            findNavController().navigate(
                PetListFragmentDirections
                    .actionPetListFragmentToPetEditFragment(-1)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
