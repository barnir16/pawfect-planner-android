package com.example.pawfectplanner.ui.gemini

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectplanner.databinding.FragmentGeminiAssistantBinding

class GeminiAssistantFragment : Fragment() {
    private var _binding: FragmentGeminiAssistantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeminiAssistantViewModel by viewModels {
        GeminiAssistantViewModelFactory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentGeminiAssistantBinding
        .inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = adapter

        binding.btnSend.setOnClickListener {
            binding.inputMessage.text
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { question ->
                    viewModel.sendMessage(question)
                    binding.inputMessage.text?.clear()
                }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.chatMessages.observe(viewLifecycleOwner) { pairs ->
            val messages = pairs.map { (text, isUser) ->
                ChatMessage(text = text, isUser = isUser)
            }
            adapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.scrollToPosition(messages.lastIndex)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
