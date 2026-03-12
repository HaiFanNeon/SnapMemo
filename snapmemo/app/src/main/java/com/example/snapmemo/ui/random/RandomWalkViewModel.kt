package com.example.snapmemo.ui.random

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RandomWalkStrategy { RANDOM, TAG_SIMILARITY, TIME_DECAY }

data class RandomWalkUiState(
    val currentMemo: Memo? = null,
    val strategy: RandomWalkStrategy = RandomWalkStrategy.RANDOM,
    val isLoading: Boolean = false
)

@HiltViewModel
class RandomWalkViewModel @Inject constructor(
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RandomWalkUiState())
    val uiState: StateFlow<RandomWalkUiState> = _uiState

    init {
        loadNextMemo()
    }

    fun setStrategy(strategy: RandomWalkStrategy) {
        _uiState.update { it.copy(strategy = strategy) }
        loadNextMemo()
    }

    fun loadNextMemo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val memo = when (_uiState.value.strategy) {
                RandomWalkStrategy.RANDOM -> memoRepository.getRandomMemo()
                RandomWalkStrategy.TAG_SIMILARITY -> {
                    val current = _uiState.value.currentMemo
                    if (current != null) memoRepository.getRelatedMemo(current.id)
                    else memoRepository.getRandomMemo()
                }
                RandomWalkStrategy.TIME_DECAY -> memoRepository.getLeastRecentlyViewedMemo()
            }
            memo?.let {
                memoRepository.updateLastViewedAt(it.id, System.currentTimeMillis())
            }
            _uiState.update { it.copy(currentMemo = memo, isLoading = false) }
        }
    }
}
