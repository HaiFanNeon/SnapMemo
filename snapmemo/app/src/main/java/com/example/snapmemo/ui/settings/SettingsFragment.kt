package com.example.snapmemo.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentMode = viewModel.appMode.value
        if (currentMode == AppMode.OFFLINE) {
            binding.rbOffline.isChecked = true
        } else {
            binding.rbOnline.isChecked = true
        }

        binding.rgMode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.rbOffline.id) {
                viewModel.switchToOfflineMode()
            }
        }

        binding.itemExport.setOnClickListener {
            Toast.makeText(requireContext(), "导出功能即将上线", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
