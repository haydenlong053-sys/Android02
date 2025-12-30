package com.lxkj.aiim.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.base.commonlibrary.network.result.onAnyError
import com.base.commonlibrary.network.result.onError
import com.base.commonlibrary.network.result.onFailure
import com.base.commonlibrary.network.result.onSuccess
import com.base.commonlibrary.utils.eLogD
import com.lxkj.aiim.repository.MirrorRepository
import com.lxkj.aiim.uistate.MirrorPageUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MirrorViewModel(private val repository: MirrorRepository = MirrorRepository()): ViewModel() {
    private  val _uiState = MutableStateFlow(MirrorPageUiState())
    val uiState = _uiState.asStateFlow()

    var saveQrCodeUrl =""

    init {
        getUserInfo()
    }

    private  fun getUserSign(){
          viewModelScope.launch {
              _uiState.value = _uiState.value.copy(isLoading = false, error = null)
              repository.getUserSignInfo().onSuccess {
                  _uiState.value = _uiState.value.copy(isLoading = false, isSign = it!=null&&it.isSign==1)
              }.onError {
                  eLogD("MirrorViewModel", "getUserSignInfo: $it")
                  _uiState.value = _uiState.value.copy(isLoading = false, isSign = false, error = it.message)
              }.onFailure { code, message, data ->
                  eLogD("MirrorViewModel", "getUserSignInfo: $message")
                  _uiState.value = _uiState.value.copy(isLoading = false, isSign = false, error =message)
                   }
          }
    }

      fun getUserInfo(){
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getUserMirrorInfo().onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, userInfo = it)
                getUserSign()
            }.onError {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)

            }.onFailure { code, message, data ->
                _uiState.value = _uiState.value.copy(isLoading = false, error =message)

            }
        }
    }


}