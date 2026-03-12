package com.example.snapmemo.ui.sync

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.snapmemo.R
import com.example.snapmemo.databinding.FragmentSyncStatusBinding
import com.example.snapmemo.ui.adapter.SyncEntryListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class SyncStatusFragment : Fragment() {

    private var _binding: FragmentSyncStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SyncStatusViewModel by viewModels()
    private lateinit var adapter: SyncEntryListAdapter
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SyncEntryListAdapter(
            onRetry = { id -> viewModel.retryEntry(id) },
            onCancel = { id -> viewModel.cancelEntry(id) }
        )
        binding.rvSyncEntries.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    adapter.submitList(state.entries)
                    binding.tvLastSyncTime.text = if (state.lastSyncTime != null) {
                        getString(R.string.sync_last_time, formatter.format(state.lastSyncTime))
                    } else {
                        getString(R.string.sync_never)
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
