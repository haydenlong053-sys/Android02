package com.cboda.intelligent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.eLogD
import com.cboda.intelligent.repository.MirrorBroadRepository
import com.cboda.intelligent.uistate.MirrorPageUiBroadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MirrorViewBroadModel(private val repository: MirrorBroadRepository = MirrorBroadRepository()): ViewModel() {
    private  val _uiState = MutableStateFlow(MirrorPageUiBroadState())
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
                  eLogD("MirrorViewBroadModel", "getUserSignInfo: $it")
                  _uiState.value = _uiState.value.copy(isLoading = false, isSign = false, error = it.message)
              }.onFailure { code, message, data ->
                  eLogD("MirrorViewBroadModel", "getUserSignInfo: $message")
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