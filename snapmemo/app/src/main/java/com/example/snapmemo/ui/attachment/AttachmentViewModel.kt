package com.example.snapmemo.ui.attachment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.repository.AttachmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AttachmentViewModel @Inject constructor(
    private val attachmentRepository: AttachmentRepository
) : ViewModel() {

    val attachments: StateFlow<List<Attachment>> = attachmentRepository.getAllAttachments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
