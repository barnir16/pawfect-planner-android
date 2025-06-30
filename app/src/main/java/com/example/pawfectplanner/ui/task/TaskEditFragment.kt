package com.example.pawfectplanner.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.model.Task
import com.example.pawfectplanner.data.repository.BreedsRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.databinding.FragmentTaskEditBinding
import com.example.pawfectplanner.network.BreedsCatApiService
import com.example.pawfectplanner.network.BreedsDogApiService
import com.example.pawfectplanner.network.CatApiClient
import com.example.pawfectplanner.network.DogApiClient
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory
import com.example.pawfectplanner.util.NotificationHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TaskEditFragment : Fragment() {
    private var _binding: FragmentTaskEditBinding? = null
    private val binding get() = _binding!!
    private val args: TaskEditFragmentArgs by navArgs()
    private val app by lazy { requireActivity().application as PawfectPlannerApplication }
    private val taskVM: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(app.database.taskDao(), requireContext()))
    }
    private val petVM: PetViewModel by lazy {
        val breedsRepository = BreedsRepository(
            DogApiClient.retrofit.create(BreedsDogApiService::class.java),
            CatApiClient.retrofit.create(BreedsCatApiService::class.java)
        )
        ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(app.database.petDao()), breedsRepository)
        )[PetViewModel::class.java]
    }
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

    override fun onViewCreated(v: android.view.View, s: Bundle?) {
        binding.inputTitle.doAfterTextChanged { 
            validateFormRealTime()
        }
        binding.inputDescription.doAfterTextChanged { }
        binding.inputRepeatInterval.doAfterTextChanged { 
            validateFormRealTime()
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
            showVaccineSuggestions()
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
                            .indexOf(t.repeatUnit ?: resources.getStringArray(R.array.repeat_units)[0])
                    )
                    assignedPetIds = t.petIds
                    petVM.allPets.observe(viewLifecycleOwner) { pets ->
                        assignedPets = pets.filter { assignedPetIds.contains(it.id) }
                        binding.tvAssignedPets.text =
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
    }

    private fun validateForm(): Boolean {
        val title = binding.inputTitle.text.toString().trim()
        val repeatInterval = binding.inputRepeatInterval.text.toString().trim()
        
        clearFieldErrors()
        
        val missingFields = mutableListOf<String>()
        
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.field_required)
            missingFields.add(getString(R.string.label_task_title))
        } else {
            binding.tilTitle.error = null
        }
        
        if (pickedDate == null) {
            missingFields.add(getString(R.string.label_task_date))
        }
        
        if (pickedTime == null) {
            missingFields.add(getString(R.string.label_task_time))
        }
        
        if (assignedPetIds.isEmpty()) {
            missingFields.add(getString(R.string.label_task_assign_pets))
        }
        
        if (repeatInterval.isNotEmpty() && repeatInterval.toIntOrNull() == null) {
            missingFields.add(getString(R.string.error_invalid_interval))
        }
        
        if (repeatInterval.isNotEmpty() && repeatInterval.toIntOrNull() != null && repeatInterval.toInt() <= 0) {
            missingFields.add(getString(R.string.error_interval_must_be_positive))
        }
        
        if (missingFields.isNotEmpty()) {
            val message = getString(R.string.toast_missing_fields, missingFields.joinToString(", "))
            android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
            return false
        }
        
        return true
    }

    private fun validateFormRealTime() {
        val title = binding.inputTitle.text.toString().trim()
        val repeatInterval = binding.inputRepeatInterval.text.toString().trim()
        
        binding.tilTitle.error = null
        
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.field_required)
        }
    }

    private fun clearFieldErrors() {
        binding.tilTitle.error = null
    }

    private fun showVaccineSuggestions() {
        if (assignedPets.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.no_pets_assigned)
                .setMessage(R.string.assign_pets_first)
                .setPositiveButton(R.string.btn_ok, null)
                .show()
            return
        }

        val dialog = VaccineSuggestionDialog.newInstance(assignedPets) { vaccine ->
            val vaccineInfo = "${vaccine.name} - ${vaccine.frequency}\n${vaccine.description}"
            binding.inputDescription.setText(vaccineInfo)
            binding.inputTitle.setText("${vaccine.name} - ${vaccine.frequency}")
            
            parseAndSetFrequency(vaccine.frequency)
            
            validateFormRealTime()
        }
        dialog.show(childFragmentManager, "vaccine_suggestions")
    }

    private fun parseAndSetFrequency(frequency: String) {
        val frequencyLower = frequency.lowercase()
        val repeatUnits = resources.getStringArray(R.array.repeat_units)
        
        when {
            frequencyLower.contains("yearly") || frequencyLower.contains("year") -> {
                binding.inputRepeatInterval.setText("1")
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Years"))
            }
            frequencyLower.contains("month") -> {
                val months = frequencyLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(months.toString())
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Months"))
            }
            frequencyLower.contains("week") -> {
                val weeks = frequencyLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(weeks.toString())
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Weeks"))
            }
            frequencyLower.contains("day") -> {
                val days = frequencyLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(days.toString())
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Days"))
            }
            frequencyLower.contains("hour") -> {
                val hours = frequencyLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(hours.toString())
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Hours"))
            }
            frequencyLower.contains("minute") -> {
                val minutes = frequencyLower.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 1
                binding.inputRepeatInterval.setText(minutes.toString())
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Minutes"))
            }
            else -> {
                // Default to yearly if frequency is not recognized
                binding.inputRepeatInterval.setText("1")
                binding.spinnerRepeatUnit.setSelection(repeatUnits.indexOf("Years"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
