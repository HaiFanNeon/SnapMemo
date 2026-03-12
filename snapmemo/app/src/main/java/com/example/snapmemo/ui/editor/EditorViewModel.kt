package com.example.snapmemo.ui.editor

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.domain.model.Attachment
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.AttachmentRepository
import com.example.snapmemo.domain.repository.MemoRepository
import com.example.snapmemo.domain.usecase.memo.CreateMemoUseCase
import com.example.snapmemo.domain.usecase.memo.UpdateMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val memo: Memo? = null,
    val attachments: List<Attachment> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val createMemoUseCase: CreateMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val memoRepository: MemoRepository,
    private val attachmentRepository: AttachmentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val memoId: String? = savedStateHandle["memoId"]
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState

    init {
        memoId?.let {
            loadMemo(it)
            loadAttachments(it)
        }
    }

    private fun loadMemo(id: String) {
        viewModelScope.launch {
            memoRepository.getMemoById(id).collectLatest { memo ->
                _uiState.update { it.copy(memo = memo) }
            }
        }
    }

    private fun loadAttachments(memoId: String) {
        viewModelScope.launch {
            attachmentRepository.getAttachmentsByMemo(memoId).collectLatest { list ->
                _uiState.update { it.copy(attachments = list) }
            }
        }
    }

    fun saveMemo(
        content: String,
        visibility: MemoVisibility = MemoVisibility.PRIVATE,
        tags: List<String> = emptyList()
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = if (memoId != null) {
                updateMemoUseCase(memoId, content, visibility, tags)
            } else {
                createMemoUseCase(content, visibility, tags)
            }
            result.fold(
                onSuccess = { _uiState.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
            )
        }
    }

    fun addAttachment(uri: Uri) {
        viewModelScope.launch {
            val targetMemoId = memoId
            attachmentRepository.uploadAttachment(targetMemoId, uri).onSuccess { attachment ->
                _uiState.update { it.copy(attachments = it.attachments + attachment) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = "附件添加失败: ${e.message}") }
            }
        }
    }

    fun removeAttachment(id: String) {
        viewModelScope.launch {
            attachmentRepository.deleteAttachment(id)
            _uiState.update { it.copy(attachments = it.attachments.filter { a -> a.id != id }) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
