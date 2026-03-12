package com.example.snapmemo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.snapmemo.data.local.datastore.AppMode
import com.example.snapmemo.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val appMode: StateFlow<AppMode> = authRepository.getAppMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppMode.OFFLINE)

    fun switchToOfflineMode() {
        viewModelScope.launch {
            authRepository.switchToOfflineMode()
        }
    }
}
