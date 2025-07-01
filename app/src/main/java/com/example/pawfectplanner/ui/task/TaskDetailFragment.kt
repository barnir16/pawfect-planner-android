package com.example.pawfectplanner.ui.task

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentTaskDetailBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.util.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.ZoneId

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val args: TaskDetailFragmentArgs by navArgs()
    private val taskVM: TaskViewModel by viewModels()
    private val petVM: PetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnAddToCalendar.setOnClickListener(null)

        // load the two parallel string‐arrays you added:
        val entries = resources.getStringArray(R.array.repeat_units_entries)
        val values = resources.getStringArray(R.array.repeat_units_values)

        taskVM.allTasks.observe(viewLifecycleOwner) { list ->
            list.find { it.id == args.taskId }?.let { task ->
                binding.tvTaskTitle.text = task.title
                binding.tvTaskDate.text = task.dateTime.toLocalDate().toString()
                binding.tvTaskTime.text = task.dateTime.toLocalTime().toString()
                if (task.repeatInterval != null && task.repeatUnit != null) {
                    binding.tvTaskInterval.text =
                        getString(R.string.label_task_repeat, task.repeatInterval, task.repeatUnit)
                } else {
                    binding.tvTaskInterval.text = getString(R.string.label_task_no_repeat)
                }
                binding.tvTaskDescription.text = task.description

                petVM.allPets.observe(viewLifecycleOwner) { pets ->
                    val names = pets.filter { task.petIds.contains(it.id) }.map { it.name }
                    binding.tvAssignedPets.text =
                        if (names.isEmpty()) getString(R.string.label_task_no_pets_assigned)
                        else names.joinToString()
                }

                binding.btnAddToCalendar.setOnClickListener {
                    val begin = task.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = begin + 3_600_000
                    val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                        putExtra(CalendarContract.Events.TITLE, task.title)
                        putExtra(CalendarContract.Events.DESCRIPTION, task.description)

                        if (task.repeatInterval != null && task.repeatUnit != null) {
                            // find the index of the display‐text in your entries array
                            val idx = entries.indexOf(task.repeatUnit)
                            // pick the corresponding value—e.g. "DAILY", "WEEKLY", etc.
                            val freq = values.getOrNull(if (idx >= 0) idx else 0)
                            if (!freq.isNullOrEmpty() && freq != "NONE") {
                                putExtra(
                                    CalendarContract.Events.RRULE,
                                    "FREQ=$freq;INTERVAL=${task.repeatInterval}"
                                )
                            }
                        }
                    }
                    startActivity(intent)
                }

                binding.btnDeleteTask.setOnClickListener {
                    NotificationHelper.cancel(requireContext(), task.id)
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.action_delete_task)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.action_delete_task) { _, _ ->
                            taskVM.delete(task)
                            findNavController().navigateUp()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }

                binding.btnEditTask.setOnClickListener {
                    findNavController().navigate(
                        TaskDetailFragmentDirections
                            .actionTaskDetailFragmentToTaskEditFragment(task.id)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
