package com.example.snapmemo.ui.ai

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.AiSummary
import com.example.snapmemo.domain.repository.AiRepository
import com.example.snapmemo.domain.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiSummaryUiState(
    val memoContent: String = "",
    val summaryText: String = "",
    val keywords: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AiSummaryViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val memoRepository: MemoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val memoId: String? = savedStateHandle["memoId"]
    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState

    init {
        memoId?.let { loadMemoContent(it) }
    }

    private fun loadMemoContent(id: String) {
        viewModelScope.launch {
            memoRepository.getMemoById(id).collectLatest { memo ->
                if (memo != null) {
                    _uiState.update { it.copy(memoContent = memo.content) }
                }
            }
        }
    }

    fun generateSummary() {
        val content = _uiState.value.memoContent
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            aiRepository.generateSummary(memoId ?: "", content)
                .onSuccess { summary ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summaryText = summary.summary,
                            keywords = summary.keywords
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun streamSummary() {
        val content = _uiState.value.memoContent
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStreaming = true, summaryText = "", errorMessage = null) }
            val sb = StringBuilder()
            aiRepository.streamSummary(memoId ?: "", content)
                .onCompletion {
                    _uiState.update { it.copy(isStreaming = false) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isStreaming = false, errorMessage = e.message) }
                }
                .collect { chunk ->
                    sb.append(chunk)
                    _uiState.update { it.copy(summaryText = sb.toString()) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
