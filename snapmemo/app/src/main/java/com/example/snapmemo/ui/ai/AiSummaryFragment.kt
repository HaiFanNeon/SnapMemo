package com.example.snapmemo.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.snapmemo.databinding.FragmentAiSummaryBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiSummaryFragment : Fragment() {

    private var _binding: FragmentAiSummaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiSummaryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarAi.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnGenerate.setOnClickListener { viewModel.generateSummary() }
        binding.btnStream.setOnClickListener { viewModel.streamSummary() }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    binding.progressBar.visibility =
                        if (state.isLoading || state.isStreaming) View.VISIBLE else View.GONE
                    binding.btnGenerate.isEnabled = !state.isLoading && !state.isStreaming
                    binding.btnStream.isEnabled = !state.isLoading && !state.isStreaming
                    binding.tvSummaryContent.text = state.summaryText.ifEmpty { "点击生成摘要…" }

                    binding.chipGroupKeywords.removeAllViews()
                    state.keywords.forEach { kw ->
                        val chip = Chip(requireContext()).apply { text = kw }
                        binding.chipGroupKeywords.addView(chip)
                    }

                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
