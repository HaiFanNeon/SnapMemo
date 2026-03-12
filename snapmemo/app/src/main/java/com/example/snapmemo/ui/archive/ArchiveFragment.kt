package com.example.snapmemo.ui.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.snapmemo.databinding.FragmentArchiveBinding
import com.example.snapmemo.ui.adapter.MemoListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArchiveViewModel by viewModels()
    private lateinit var adapter: MemoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MemoListAdapter(
            onMemoClick = { memo ->
                val action = ArchiveFragmentDirections.actionArchiveToEditor(memoId = memo.id)
                findNavController().navigate(action)
            }
        )
        binding.rvArchived.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { binding.swipeRefresh.isRefreshing = false }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.archivedMemos.collectLatest { memos ->
                    adapter.submitList(memos)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
