package com.example.pawfectplanner.ui.task

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.databinding.FragmentTaskDetailBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory
import org.threeten.bp.ZoneId

class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val args: TaskDetailFragmentArgs by navArgs()
    private val app by lazy { requireActivity().application as PawfectPlannerApplication }
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(app.database.taskDao()))
    }
    private val petViewModel by lazy {
        ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(app.database.petDao()))
        ).get(PetViewModel::class.java)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTaskDetailBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            tasks.find { it.id == args.taskId }?.let { t ->
                binding.tvTaskTitle.text = t.title
                binding.tvTaskDate.text = t.dateTime.toLocalDate().toString()
                binding.tvTaskTime.text = t.dateTime.toLocalTime().toString()
                binding.tvTaskRepeat.text =
                    t.repeatInterval?.let { getString(R.string.label_task_repeat, it, t.repeatUnit!!) }
                        ?: getString(R.string.label_task_no_repeat)
                petViewModel.allPets.observe(viewLifecycleOwner) { pets ->
                    val names = pets.filter { t.petIds.contains(it.id) }.map { it.name }
                    binding.tvTaskPets.text =
                        if (names.isEmpty()) getString(R.string.label_task_no_pets_assigned)
                        else names.joinToString()
                }
                binding.btnEditTask.setOnClickListener {
                    findNavController().navigate(
                        TaskDetailFragmentDirections
                            .actionTaskDetailFragmentToTaskEditFragment(t.id)
                    )
                }
                binding.btnAddToCalendar.setOnClickListener {
                    val begin = t.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = begin + 3_600_000
                    startActivity(
                        Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                            .putExtra(CalendarContract.Events.TITLE, t.title)
                            .putExtra(CalendarContract.Events.DESCRIPTION, t.description)
                    )
                }
                binding.btnDeleteTask.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.action_delete_task)
                        .setMessage(R.string.error_missing_fields)
                        .setPositiveButton(R.string.action_delete_task) { _, _ ->
                            viewModel.delete(t)
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
