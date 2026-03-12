package com.example.snapmemo.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val memoRepository: MemoRepository
) : ViewModel() {

    val deletedMemos: StateFlow<List<Memo>> = memoRepository.getDeletedMemos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restore(id: String) {
        viewModelScope.launch { memoRepository.restoreMemo(id) }
    }

    fun hardDelete(id: String) {
        viewModelScope.launch { memoRepository.hardDeleteMemo(id) }
    }
}
