package com.example.pawfectplanner.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.databinding.FragmentTaskEditBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory
import com.example.pawfectplanner.util.NotificationHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class TaskEditFragment : Fragment() {
    private var _binding: FragmentTaskEditBinding? = null
    private val binding get() = _binding!!
    private val args: TaskEditFragmentArgs by navArgs()
    private val app by lazy { requireActivity().application as PawfectPlannerApplication }
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(app.database.taskDao()))
    }
    private val petViewModel: PetViewModel by lazy {
        ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(app.database.petDao()))
        )[PetViewModel::class.java]
    }
    private var chosenDate: LocalDate? = null
    private var chosenTime: LocalTime? = null
    private var petIds: List<Int> = emptyList()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTaskEditBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        binding.inputTitle.doAfterTextChanged { updateSave() }
        binding.inputDescription.doAfterTextChanged { updateSave() }

        binding.btnPickDate.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    chosenDate = LocalDate.of(y, m + 1, d)
                    binding.btnPickDate.text = chosenDate.toString()
                    updateSave()
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).apply { datePicker.minDate = System.currentTimeMillis() }.show()
        }

        binding.btnPickTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    chosenTime = LocalTime.of(h, m)
                    binding.btnPickTime.text = chosenTime.toString()
                    updateSave()
                },
                now.hour, now.minute, true
            ).show()
        }

        binding.btnAssignPets.setOnClickListener {
            petViewModel.allPets.observe(viewLifecycleOwner) { pets ->
                val names = pets.map { it.name }.toTypedArray()
                val selected = BooleanArray(pets.size) { i -> petIds.contains(pets[i].id) }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.label_task_assign_pets)
                    .setMultiChoiceItems(names, selected) { _, which, isChecked ->
                        selected[which] = isChecked
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        petIds = pets.filterIndexed { i, _ -> selected[i] }.map { it.id }
                        binding.tvAssignedPets.text =
                            if (petIds.isEmpty())
                                getString(R.string.label_task_no_pets_assigned)
                            else
                                pets.filter { petIds.contains(it.id) }.joinToString { it.name }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        if (args.taskId != -1) {
            viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
                tasks.find { it.id == args.taskId }?.let { t ->
                    binding.inputTitle.setText(t.title)
                    binding.inputDescription.setText(t.description)
                    chosenDate = t.dateTime.toLocalDate()
                    chosenTime = t.dateTime.toLocalTime()
                    binding.btnPickDate.text = chosenDate.toString()
                    binding.btnPickTime.text = chosenTime.toString()
                    binding.inputRepeatInterval.setText(t.repeatInterval?.toString() ?: "")
                    binding.spinnerRepeatUnit.setSelection(
                        resources.getStringArray(R.array.repeat_units).indexOf(t.repeatUnit ?: "Never")
                    )
                    petIds = t.petIds
                    petViewModel.allPets.observe(viewLifecycleOwner) { pets ->
                        binding.tvAssignedPets.text =
                            pets.filter { petIds.contains(it.id) }.joinToString { it.name }
                    }
                    binding.btnSave.text = getString(R.string.btn_update_task)
                    updateSave()
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val desc = binding.inputDescription.text.toString().trim()
            val date = chosenDate ?: return@setOnClickListener
            val time = chosenTime ?: return@setOnClickListener
            val interval = binding.inputRepeatInterval.text.toString().toIntOrNull()
            val unit = binding.spinnerRepeatUnit.selectedItem as String
            val dt = LocalDateTime.of(date, time)
            val task = Task(
                id = if (args.taskId != -1) args.taskId else 0,
                title = title,
                description = desc,
                dateTime = dt,
                repeatInterval = interval,
                repeatUnit = if (unit == "Never") null else unit,
                petIds = petIds
            )
            if (args.taskId == -1) viewModel.insert(task) else viewModel.update(task)
            try {
                NotificationHelper.schedule(requireContext(), task)
            } catch (_: SecurityException) {
            }
            findNavController().popBackStack()
        }
    }

    private fun updateSave() {
        binding.btnSave.isEnabled =
            binding.inputTitle.text.toString().isNotBlank() &&
                    chosenDate != null &&
                    chosenTime != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
