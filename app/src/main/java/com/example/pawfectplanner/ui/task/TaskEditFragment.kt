package com.example.pawfectplanner.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.databinding.FragmentTaskEditBinding
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class TaskEditFragment : Fragment() {
    private var _binding: FragmentTaskEditBinding? = null
    private val binding get() = _binding!!
    private val args: TaskEditFragmentArgs by navArgs()
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            TaskRepository(
                (requireActivity().application as PawfectPlannerApplication).database.taskDao()
            )
        )
    }
    private var chosenDate: LocalDate? = null
    private var chosenTime: LocalTime? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentTaskEditBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val units = resources.getStringArray(R.array.repeat_units)
        binding.spinnerRepeatUnit.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, units)

        if (args.taskId != -1) {
            viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
                tasks.find { it.id == args.taskId }?.let { task ->
                    binding.inputTitle.setText(task.title)
                    binding.inputDescription.setText(task.description)
                    chosenDate = task.dateTime.toLocalDate()
                    chosenTime = task.dateTime.toLocalTime()
                    binding.btnPickDate.text = chosenDate.toString()
                    binding.btnPickTime.text = chosenTime.toString()
                    binding.inputRepeatInterval.setText(task.repeatInterval?.toString() ?: "")
                    binding.spinnerRepeatUnit.setSelection(units.indexOf(task.repeatUnit ?: "None"))
                    binding.btnSave.text = getString(R.string.btn_update_task)
                    binding.inputDescription.isEnabled = true
                    binding.btnPickDate.isEnabled = true
                    binding.btnPickTime.isEnabled = true
                    binding.inputRepeatInterval.isEnabled = true
                    binding.spinnerRepeatUnit.isEnabled = true
                }
            }
        }

        binding.inputTitle.doAfterTextChanged { text ->
            val ok = !text.isNullOrBlank()
            binding.inputDescription.isEnabled = ok
            binding.btnPickDate.isEnabled = ok
            binding.btnPickTime.isEnabled = ok
            binding.inputRepeatInterval.isEnabled = ok
            binding.spinnerRepeatUnit.isEnabled = ok
        }

        binding.btnPickDate.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val date = LocalDate.of(y, m + 1, d)
                    if (date.isBefore(LocalDate.now())) {
                        Toast.makeText(requireContext(), R.string.error_invalid_date, Toast.LENGTH_SHORT).show()
                        return@DatePickerDialog
                    }
                    chosenDate = date
                    binding.btnPickDate.text = date.toString()
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
        }

        binding.btnPickTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    chosenTime = LocalTime.of(h, m)
                    binding.btnPickTime.text = chosenTime.toString()
                },
                now.hour, now.minute, true
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val desc = binding.inputDescription.text.toString().trim()
            val date = chosenDate
            val time = chosenTime
            if (title.isEmpty() || date == null || time == null) {
                Toast.makeText(requireContext(), R.string.error_missing_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val interval = binding.inputRepeatInterval.text.toString().toIntOrNull()
            val unit = binding.spinnerRepeatUnit.selectedItem as String
            val dateTime = LocalDateTime.of(date, time)
            val task = Task(
                id = if (args.taskId != -1) args.taskId else 0,
                title = title,
                description = desc,
                dateTime = dateTime,
                repeatInterval = interval,
                repeatUnit = if (unit == "None") null else unit
            )
            if (args.taskId == -1) viewModel.insert(task) else viewModel.update(task)
            Toast.makeText(requireContext(), R.string.message_task_saved, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
