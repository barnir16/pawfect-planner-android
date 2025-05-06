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
import com.example.pawfectplanner.databinding.FragmentTaskDetailBinding
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.ui.viewmodel.TaskViewModel
import com.example.pawfectplanner.ui.viewmodel.TaskViewModelFactory
import com.example.pawfectplanner.ui.viewmodel.PetViewModel
import com.example.pawfectplanner.ui.viewmodel.PetViewModelFactory
import com.example.pawfectplanner.util.NotificationHelper
import org.threeten.bp.ZoneId
import android.content.DialogInterface

class TaskDetailFragment : Fragment() {
    private var _b: FragmentTaskDetailBinding? = null
    private val b get() = _b!!
    private val args: TaskDetailFragmentArgs by navArgs()
    private val app by lazy { requireActivity().application as PawfectPlannerApplication }
    private val tm: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(app.database.taskDao()))
    }
    private val pm by lazy {
        ViewModelProvider(
            this,
            PetViewModelFactory(PetRepository(app.database.petDao()))
        )[PetViewModel::class.java]
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTaskDetailBinding.inflate(inflater, container, false)
        .also { _b = it }
        .root

    override fun onViewCreated(v: android.view.View, s: Bundle?) {
        b.btnAddToCalendar.setOnClickListener(null)

        tm.allTasks.observe(viewLifecycleOwner) { list ->
            list.find { it.id == args.taskId }?.let { t ->
                b.tvTaskTitle.text = t.title
                b.tvTaskDate.text = t.dateTime.toLocalDate().toString()
                b.tvTaskTime.text = t.dateTime.toLocalTime().toString()
                b.tvTaskInterval.text = t.repeatInterval
                    ?.let { interval ->
                        getString(R.string.label_task_repeat, interval, t.repeatUnit!!)
                    }
                    ?: getString(R.string.label_task_no_repeat)

                pm.allPets.observe(viewLifecycleOwner) { pets ->
                    val names = pets.filter { t.petIds.contains(it.id) }.map { it.name }
                    b.tvAssignedPets.text = if (names.isEmpty())
                        getString(R.string.label_task_no_pets_assigned)
                    else
                        names.joinToString(separator = ", ") { petName -> petName }
                }

                b.btnAddToCalendar.setOnClickListener {
                    val begin = t.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val end = begin + 3_600_000
                    Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
                        putExtra(CalendarContract.Events.TITLE, t.title)
                        putExtra(CalendarContract.Events.DESCRIPTION, t.description)
                        if (t.repeatInterval != null && t.repeatUnit != null) {
                            val freq = when (t.repeatUnit) {
                                "Minutes" -> "MINUTELY"
                                "Hours"   -> "HOURLY"
                                "Days"    -> "DAILY"
                                "Weeks"   -> "WEEKLY"
                                "Months"  -> "MONTHLY"
                                "Years"   -> "YEARLY"
                                else      -> ""
                            }
                            putExtra(
                                CalendarContract.Events.RRULE,
                                "FREQ=$freq;INTERVAL=${t.repeatInterval}"
                            )
                        }
                    }.also(::startActivity)
                }

                b.btnDeleteTask.setOnClickListener {
                    NotificationHelper.cancel(requireContext(), t.id)
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.action_delete_task)
                        .setMessage(R.string.delete_message)
                        .setPositiveButton(R.string.action_delete_task) { dialog: DialogInterface, which: Int ->
                            tm.delete(t)
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
        _b = null
    }
}
