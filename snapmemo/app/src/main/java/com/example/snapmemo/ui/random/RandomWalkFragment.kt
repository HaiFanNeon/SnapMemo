package com.example.snapmemo.ui.random

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.snapmemo.R
import com.example.snapmemo.databinding.FragmentRandomWalkBinding
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class RandomWalkFragment : Fragment() {

    private var _binding: FragmentRandomWalkBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RandomWalkViewModel by viewModels()

    private val strategies = listOf(
        RandomWalkStrategy.RANDOM to "完全随机",
        RandomWalkStrategy.TAG_SIMILARITY to "标签相似度",
        RandomWalkStrategy.TIME_DECAY to "时间衰减"
    )
    private val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStrategySpinner()
        setupButtons()
        observeState()
    }

    private fun setupStrategySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            strategies.map { it.second }
        )
        binding.spinnerStrategy.setAdapter(adapter)
        binding.spinnerStrategy.setOnItemClickListener { _, _, position, _ ->
            viewModel.setStrategy(strategies[position].first)
        }
    }

    private fun setupButtons() {
        binding.btnNextMemo.setOnClickListener { viewModel.loadNextMemo() }
        binding.btnEditMemo.setOnClickListener {
            val memo = viewModel.uiState.value.currentMemo ?: return@setOnClickListener
            val action = RandomWalkFragmentDirections.actionRandomToEditor(memoId = memo.id)
            findNavController().navigate(action)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    val memo = state.currentMemo
                    if (memo != null) {
                        binding.cardMemo.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        binding.tvMemoContent.text = memo.content
                        binding.tvMemoTime.text = timeFormatter.format(memo.displayTime)
                        binding.btnEditMemo.isEnabled = true

                        binding.chipGroupTags.removeAllViews()
                        memo.tags.forEach { tag ->
                            binding.chipGroupTags.addView(Chip(requireContext()).apply {
                                text = "#$tag"
                                isClickable = false
                            })
                        }
                    } else {
                        binding.cardMemo.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.btnEditMemo.isEnabled = false
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
