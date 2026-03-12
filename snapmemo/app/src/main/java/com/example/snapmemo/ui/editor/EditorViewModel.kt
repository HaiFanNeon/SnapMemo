package com.example.snapmemo.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.data.local.db.entity.MemoVisibility
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.usecase.memo.CreateMemoUseCase
import com.example.snapmemo.domain.usecase.memo.GetMemosUseCase
import com.example.snapmemo.domain.usecase.memo.UpdateMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val memo: Memo? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val createMemoUseCase: CreateMemoUseCase,
    private val updateMemoUseCase: UpdateMemoUseCase,
    private val memoRepository: com.example.snapmemo.domain.repository.MemoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoId: String? = savedStateHandle["memoId"]
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState

    init {
        memoId?.let { loadMemo(it) }
    }

    private fun loadMemo(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            memoRepository.getMemoById(id).collectLatest { memo ->
                _uiState.update { it.copy(memo = memo, isLoading = false) }
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
}
