package com.example.pawfectplanner.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentApiKeyBinding
import com.example.pawfectplanner.util.ApiKeyStore

class ApiKeyFragment : Fragment() {

    private var _binding: FragmentApiKeyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentApiKeyBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        ApiKeyStore.getDogApiKey(context)?.let { binding.inputDogApiKey.setText(it) }
        ApiKeyStore.getGeminiApiKey(context)?.let { binding.inputGeminiApiKey.setText(it) }

        binding.btnSaveDogKey.setOnClickListener {
            val key = binding.inputDogApiKey.text.toString().trim()
            if (key.isNotEmpty()) {
                ApiKeyStore.setDogApiKey(context, key)
                Toast.makeText(context, getString(R.string.toast_key_saved), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSaveGeminiKey.setOnClickListener {
            val key = binding.inputGeminiApiKey.text.toString().trim()
            if (key.isNotEmpty()) {
                ApiKeyStore.setGeminiApiKey(context, key)
                Toast.makeText(context, getString(R.string.toast_key_saved), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
