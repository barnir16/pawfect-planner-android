package com.example.pawfectplanner.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {
    private const val PREF_LANGUAGE_KEY = "language"
    private const val PREF_LANGUAGE_DEFAULT = "en"

    fun setLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    fun setNewLocale(context: Context, language: String): Context {
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    private fun getLanguage(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return sharedPrefs.getString(PREF_LANGUAGE_KEY, PREF_LANGUAGE_DEFAULT) ?: PREF_LANGUAGE_DEFAULT
    }

    private fun persistLanguage(context: Context, language: String) {
        val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(PREF_LANGUAGE_KEY, language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
    }
} 