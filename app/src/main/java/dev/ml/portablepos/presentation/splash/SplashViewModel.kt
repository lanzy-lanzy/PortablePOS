package dev.ml.portablepos.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ml.portablepos.data.local.preferences.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isFirstLaunch: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.isFirstLaunch.collect { isFirst ->
                if (isFirst) {
                    appPreferences.setFirstLaunchDone()
                }
                _uiState.value = SplashUiState(
                    isFirstLaunch = isFirst,
                    isLoading = false
                )
            }
        }
    }
}
