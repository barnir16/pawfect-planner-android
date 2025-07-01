package com.example.pawfectplanner.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.databinding.FragmentTaskEditBinding
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.util.NotificationHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

@AndroidEntryPoint
class TaskEditFragment : Fragment() {
    private var _binding: FragmentTaskEditBinding? = null
    private val binding get() = _binding!!
    private val args: TaskEditFragmentArgs by navArgs()
    private val taskVM: TaskViewModel by viewModels()
    private val petVM: PetViewModel by viewModels()
    private var pickedDate: LocalDate? = null
    private var pickedTime: LocalTime? = null
    private var assignedPetIds: List<Int> = emptyList()
    private var assignedPets: List<Pet> = emptyList()

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTaskEditBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.inputTitle.doAfterTextChanged { validateFormRealTime() }
        binding.inputDescription.doAfterTextChanged { }
        binding.inputRepeatInterval.doAfterTextChanged { validateFormRealTime() }

        binding.spinnerRepeatUnit.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        binding.tilRepeatInterval.visibility = View.GONE
                        binding.inputRepeatInterval.setText("")
                    } else {
                        binding.tilRepeatInterval.visibility = View.VISIBLE
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    binding.tilRepeatInterval.visibility = View.GONE
                }
            }

        binding.btnPickDate.setOnClickListener {
            val now = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    pickedDate = LocalDate.of(y, m + 1, d)
                    binding.btnPickDate.text = pickedDate.toString()
                    validateFormRealTime()
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
                    validateFormRealTime()
                },
                now.hour, now.minute, true
            ).show()
        }

        binding.btnAssignPets.setOnClickListener {
            petVM.allPets.observe(viewLifecycleOwner) { pets ->
                val names = pets.map { it.name }.toTypedArray()
                val checked = BooleanArray(pets.size) { i -> assignedPetIds.contains(pets[i].id) }
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.label_task_assign_pets)
                    .setMultiChoiceItems(names, checked) { _, which, isChecked ->
                        checked[which] = isChecked
                    }
                    .setPositiveButton(R.string.btn_ok) { _, _ ->
                        assignedPetIds = pets.filterIndexed { i, _ -> checked[i] }.map { it.id }
                        assignedPets = pets.filter { assignedPetIds.contains(it.id) }
                        binding.tvAssignedPets.text =
                            if (assignedPetIds.isEmpty())
                                getString(R.string.label_task_no_pets_assigned)
                            else
                                assignedPets.joinToString { it.name }
                        validateFormRealTime()
                    }
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show()
            }
        }

        binding.btnVaccineSuggestions.setOnClickListener {
            if (assignedPets.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.no_pets_assigned)
                    .setMessage(R.string.assign_pets_first)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show()
            } else {
                VaccineSuggestionDialog.newInstance(assignedPets) { vaccine ->
                    binding.inputTitle.setText(
                        getString(R.string.vaccine_title_format, vaccine.name, vaccine.frequency)
                    )
                    binding.inputDescription.setText(
                        getString(
                            R.string.vaccine_info_format,
                            vaccine.name,
                            vaccine.frequency,
                            vaccine.description
                        )
                    )
                    parseAndSetFrequency(vaccine.frequency)
                    validateFormRealTime()
                }.show(childFragmentManager, "vaccine_suggestions")
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
                    val repeatUnits = resources.getStringArray(R.array.repeat_units)
                    binding.spinnerRepeatUnit.setSelection(
                        repeatUnits.indexOf(t.repeatUnit ?: repeatUnits[0])
                    )
                    if (t.repeatInterval != null) {
                        binding.inputRepeatInterval.setText(t.repeatInterval.toString())
                    } else {
                        binding.inputRepeatInterval.setText("")
                    }
                    assignedPetIds = t.petIds
                    petVM.allPets.observe(viewLifecycleOwner) { pets ->
                        assignedPets = pets.filter { assignedPetIds.contains(it.id) }
                        binding.tvAssignedPets.text =
                            if (assignedPetIds.isEmpty())
                                getString(R.string.label_task_no_pets_assigned)
                            else
                                assignedPets.joinToString { it.name }
                    }
                    binding.btnSave.text = getString(R.string.btn_update_task)
                    validateFormRealTime()
                }
            }
        }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                val title = binding.inputTitle.text.toString().trim()
                val desc = binding.inputDescription.text.toString().trim()
                val date = pickedDate ?: return@setOnClickListener
                val time = pickedTime ?: return@setOnClickListener
                val pos = binding.spinnerRepeatUnit.selectedItemPosition
                val interval = binding.inputRepeatInterval.text.toString().trim().toIntOrNull()
                val unit = if (pos == 0) null else binding.spinnerRepeatUnit.selectedItem as String
                val dt = LocalDateTime.of(date, time)
                val task = Task(
                    id = if (args.taskId != -1) args.taskId else 0,
                    title = title,
                    description = desc,
                    dateTime = dt,
                    repeatInterval = if (pos == 0) null else interval,
                    repeatUnit = unit,
                    petIds = assignedPetIds
                )
                if (args.taskId == -1) taskVM.insert(task) else taskVM.update(task)
                NotificationHelper.schedule(requireContext(), task)
                findNavController().popBackStack()
            }
        }
    }

    private fun validateForm(): Boolean {
        val missing = mutableListOf<String>()
        if (binding.inputTitle.text.toString().trim().isEmpty()) {
            binding.tilTitle.error = getString(R.string.field_required)
            missing.add(getString(R.string.label_task_title))
        } else {
            binding.tilTitle.error = null
        }
        if (pickedDate == null) missing.add(getString(R.string.label_task_date))
        if (pickedTime == null) missing.add(getString(R.string.label_task_time))
        if (assignedPetIds.isEmpty()) missing.add(getString(R.string.label_task_assign_pets))
        if (binding.spinnerRepeatUnit.selectedItemPosition > 0) {
            val iv = binding.inputRepeatInterval.text.toString().trim().toIntOrNull()
            if (iv == null) missing.add(getString(R.string.error_invalid_interval))
            else if (iv <= 0) missing.add(getString(R.string.error_interval_must_be_positive))
        }
        return if (missing.isNotEmpty()) {
            val msg = getString(R.string.toast_missing_fields, missing.joinToString(", "))
            android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_LONG).show()
            false
        } else true
    }

    private fun validateFormRealTime() {
        binding.tilTitle.error = null
        if (binding.inputTitle.text.toString().trim().isEmpty())
            binding.tilTitle.error = getString(R.string.field_required)
    }

    private fun parseAndSetFrequency(frequency: String) {
        val freqLower = frequency.lowercase()
        val units = resources.getStringArray(R.array.repeat_units)
        when {
            freqLower.contains("year") -> {
                binding.inputRepeatInterval.setText("1")
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Years"))
            }
            freqLower.contains("month") -> {
                val v = freqLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(v.toString())
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Months"))
            }
            freqLower.contains("week") -> {
                val v = freqLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(v.toString())
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Weeks"))
            }
            freqLower.contains("day") -> {
                val v = freqLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(v.toString())
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Days"))
            }
            freqLower.contains("hour") -> {
                val v = freqLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(v.toString())
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Hours"))
            }
            freqLower.contains("minute") -> {
                val v = freqLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(v.toString())
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Minutes"))
            }
            else -> {
                binding.inputRepeatInterval.setText("1")
                binding.spinnerRepeatUnit.setSelection(units.indexOf("Years"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
