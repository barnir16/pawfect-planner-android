package com.example.pawfectplanner.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.repository.TaskRepository
import com.example.pawfectplanner.util.LocaleHelper
import com.example.pawfectplanner.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.pawfectplanner.ui.MainActivity
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var taskRepository: TaskRepository

    private lateinit var notificationPref: SwitchPreferenceCompat

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                enableAllExistingNotifications()
            } else {
                notificationPref.isChecked = false
            }
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationPref = findPreference<SwitchPreferenceCompat>("notifications")!!
        notificationPref.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnPreferenceChangeListener false
                }
                enableAllExistingNotifications()
            } else {
                disableAllExistingNotifications()
            }
            true
        }

        findPreference<Preference>("dark_mode")!!
            .setOnPreferenceClickListener {
                val current = LocaleHelper.getDarkModeSetting(requireContext())
                val next = if (current == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else
                    AppCompatDelegate.MODE_NIGHT_YES
                LocaleHelper.setDarkModeSetting(requireContext(), next)
                true
            }

        findPreference<Preference>("language")!!
            .setOnPreferenceClickListener {
                showLanguageDialog()
                true
            }
    }

    private fun enableAllExistingNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            taskRepository.allTasks.collect { tasks ->
                tasks.forEach { task ->
                    NotificationHelper.schedule(requireContext(), task)
                }
            }
        }
    }

    private fun disableAllExistingNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            taskRepository.allTasks.collect { tasks ->
                tasks.forEach { task ->
                    NotificationHelper.cancel(requireContext(), task.id)
                }
            }
        }
    }

    private fun showLanguageDialog() {
        val languages = resources.getStringArray(R.array.language_entries)
        val values = resources.getStringArray(R.array.language_values)
        val current = LocaleHelper.getCurrentLanguage(requireContext())
        val idx = values.indexOf(current)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.pref_language)
            .setSingleChoiceItems(languages, idx) { dialog, which ->
                val selected = values[which]
                if (selected != current) {
                    LocaleHelper.setNewLocale(requireContext(), selected)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                dialog.dismiss()
            }
            .show()
    }
}
