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
    private val taskVM: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(app.database.taskDao()))
    }
    private val petVM: PetViewModel by lazy {
        ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(app.database.petDao()))
        )[PetViewModel::class.java]
    }
    private var pickedDate: LocalDate? = null
    private var pickedTime: LocalTime? = null
    private var assignedPetIds: List<Int> = emptyList()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTaskEditBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(v: android.view.View, s: Bundle?) {
        binding.inputTitle.doAfterTextChanged { updateSaveEnabled() }
        binding.inputDescription.doAfterTextChanged { updateSaveEnabled() }
        binding.inputRepeatInterval.doAfterTextChanged { }

        binding.btnPickDate.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    pickedDate = LocalDate.of(y, m + 1, d)
                    binding.btnPickDate.text = pickedDate.toString()
                    updateSaveEnabled()
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).apply { datePicker.minDate = System.currentTimeMillis() }
                .show()
        }

        binding.btnPickTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, h, m ->
                    pickedTime = LocalTime.of(h, m)
                    binding.btnPickTime.text = pickedTime.toString()
                    updateSaveEnabled()
                },
                now.hour, now.minute, true
            ).show()
        }

        binding.btnAssignPets.setOnClickListener {
            petVM.allPets.observe(viewLifecycleOwner) { pets ->
                val names = pets.map { it.name }.toTypedArray()
                val checked = BooleanArray(pets.size) { i -> assignedPetIds.contains(pets[i].id) }
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.label_task_assign_pets)
                    .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                        checked[which] = isChecked
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        assignedPetIds = pets.filterIndexed { i, _ -> checked[i] }.map { it.id }
                        binding.tvAssignedPets.text =
                            if (assignedPetIds.isEmpty())
                                getString(R.string.label_task_no_pets_assigned)
                            else
                                pets.filter { assignedPetIds.contains(it.id) }
                                    .joinToString { it.name }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }

        if (args.taskId != -1) {
            taskVM.allTasks.observe(viewLifecycleOwner) { list ->
                list.find { it.id == args.taskId }?.let { t ->
                    binding.inputTitle.setText(t.title)
                    binding.inputDescription.setText(t.description)
                    pickedDate = t.dateTime.toLocalDate()
                    pickedTime = t.dateTime.toLocalTime()
                    binding.btnPickDate.text = pickedDate.toString()
                    binding.btnPickTime.text = pickedTime.toString()
                    binding.inputRepeatInterval.setText(t.repeatInterval?.toString() ?: "")
                    binding.spinnerRepeatUnit.setSelection(
                        resources.getStringArray(R.array.repeat_units)
                            .indexOf(t.repeatUnit ?: R.array.repeat_units.toString())
                    )
                    assignedPetIds = t.petIds
                    petVM.allPets.observe(viewLifecycleOwner) { pets ->
                        binding.tvAssignedPets.text =
                            pets.filter { assignedPetIds.contains(it.id) }
                                .joinToString { it.name }
                    }
                    binding.btnSave.text = getString(R.string.btn_update_task)
                    updateSaveEnabled()
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text.toString().trim()
            val desc = binding.inputDescription.text.toString().trim()
            val date = pickedDate ?: return@setOnClickListener
            val time = pickedTime ?: return@setOnClickListener
            val interval = binding.inputRepeatInterval.text.toString().toIntOrNull()
            val unit = binding.spinnerRepeatUnit.selectedItem as String
            val dt = LocalDateTime.of(date, time)

            val task = Task(
                id = if (args.taskId != -1) args.taskId else 0,
                title = title,
                description = desc,
                dateTime = dt,
                repeatInterval = interval,
                repeatUnit = unit,
                petIds = assignedPetIds
            )

            if (args.taskId == -1) taskVM.insert(task) else taskVM.update(task)
            NotificationHelper.schedule(requireContext(), task)
            findNavController().popBackStack()
        }
    }

    private fun updateSaveEnabled() {
        binding.btnSave.isEnabled =
            binding.inputTitle.text.toString().isNotBlank() &&
                    pickedDate != null &&
                    pickedTime != null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
