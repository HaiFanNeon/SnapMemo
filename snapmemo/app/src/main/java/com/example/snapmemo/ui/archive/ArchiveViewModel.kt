package com.example.snapmemo.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.domain.model.Memo
import com.example.snapmemo.domain.repository.MemoRepository
import com.example.snapmemo.domain.usecase.memo.ArchiveMemoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val archiveMemoUseCase: ArchiveMemoUseCase
) : ViewModel() {

    val archivedMemos: StateFlow<List<Memo>> = memoRepository.getArchivedMemos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchive(id: String) {
        viewModelScope.launch {
            archiveMemoUseCase.unarchive(id)
        }
    }
}
