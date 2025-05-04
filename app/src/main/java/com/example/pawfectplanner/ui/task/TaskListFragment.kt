package com.example.pawfectplanner.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.databinding.FragmentTaskListBinding
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            TaskRepository(
                (requireActivity().application as PawfectPlannerApplication).database.taskDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = TaskAdapter { taskId ->
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskEditFragment(taskId)
            view.findNavController().navigate(action)
        }

        binding.fabAdd.setOnClickListener {
            val action = TaskListFragmentDirections
                .actionTaskListFragmentToTaskEditFragment(-1)
            view.findNavController().navigate(action)
        }

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            (binding.recyclerView.adapter as TaskAdapter).submitList(tasks)
            binding.labelNoTasks.visibility =
                if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
