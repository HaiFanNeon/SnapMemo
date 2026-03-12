package com.example.snapmemo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.TagRepository
import com.example.snapmemo.domain.usecase.memo.DeleteMemoUseCase
import com.example.snapmemo.domain.usecase.memo.GetMemosUseCase
import com.example.snapmemo.domain.usecase.memo.SearchMemosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val memos: List<Memo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMemosUseCase: GetMemosUseCase,
    private val searchMemosUseCase: SearchMemosUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val tagRepository: TagRepository,
    private val memoRepository: com.example.snapmemo.domain.repository.MemoRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    val totalCount: StateFlow<Int> = memoRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val tagCount: StateFlow<Int> = tagRepository.getTagCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val activeDayCount: StateFlow<Int> = memoRepository.getActiveDayCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    /** dayEpoch (UTC 00:00 ms) -> memo count, 用于 HeatmapCalendarView */
    val dailyStats: StateFlow<Map<Long, Int>> = memoRepository.getDailyStats(90)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        loadMemos()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadMemos() {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                if (query.isBlank()) getMemosUseCase()
                else searchMemosUseCase(query)
            }.collect { memos ->
                _uiState.update { it.copy(memos = memos, isLoading = false) }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteMemo(id: String) {
        viewModelScope.launch {
            deleteMemoUseCase(id)
        }
    }
}
