package com.example.pawfectplanner.ui.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pawfectplanner.R
import com.example.pawfectplanner.databinding.FragmentFaqBinding

class FAQFragment : Fragment() {

    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFaqBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        val items = listOf(
            FAQItem(context.getString(R.string.faq_uses), context.getString(R.string.faq_uses_text)),
            FAQItem(context.getString(R.string.faq_missing_breed), context.getString(R.string.faq_missing_breed_text)),
            FAQItem(context.getString(R.string.faq_api_key), context.getString(R.string.faq_api_key_text)),
            FAQItem(context.getString(R.string.faq_api_get), context.getString(R.string.faq_api_get_text)),
            FAQItem(context.getString(R.string.faq_english_only), context.getString(R.string.faq_english_only_text)),
            FAQItem(context.getString(R.string.faq_offline), context.getString(R.string.faq_offline_text)),
            FAQItem(context.getString(R.string.faq_backup), context.getString(R.string.faq_backup_text)),
            FAQItem(context.getString(R.string.faq_privacy), context.getString(R.string.faq_privacy_text)),
        )

        binding.rvFaq.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFaq.adapter = FaqAdapter(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
