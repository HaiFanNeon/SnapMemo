package com.example.snapmemo.ui.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapmemo.databinding.FragmentEditorBinding
import com.example.snapmemo.ui.adapter.AttachmentListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditorViewModel by viewModels()
    private val args: EditorFragmentArgs by navArgs()
    private lateinit var attachmentAdapter: AttachmentListAdapter

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addAttachment(it) } }

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.addAttachment(it) } }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupAttachmentRecyclerView()
        setupButtons()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbarEditor.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAttachmentRecyclerView() {
        attachmentAdapter = AttachmentListAdapter(
            onAttachmentClick = {},
            onLongClick = { attachment -> viewModel.removeAttachment(attachment.id) }
        )
        binding.rvAttachments.apply {
            adapter = attachmentAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val content = binding.etContent.text?.toString() ?: ""
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "内容不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveMemo(content)
        }

        binding.btnAttachImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAttachFile.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    // 首次加载时填充内容
                    if (state.memo != null && binding.etContent.text.isNullOrEmpty()) {
                        binding.etContent.setText(state.memo.content)
                    }

                    // 附件列表
                    val hasAttachments = state.attachments.isNotEmpty()
                    binding.rvAttachments.visibility =
                        if (hasAttachments) View.VISIBLE else View.GONE
                    attachmentAdapter.submitList(state.attachments)

                    state.errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }

                    if (state.isSaved) findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
