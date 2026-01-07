package cn.wildfirechat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import cn.wildfirechat.model.ConversationInfo
import cn.wildfirechat.uistate.MessagePageUiState

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// 事件枚举（一目了然）
sealed class SocialAction {
    object SelectAll : SocialAction()
    object CancelSelectAll : SocialAction()
    object MarkAllAsRead : SocialAction()
    object MarkSingleAsRead : SocialAction()
    object DeleteSelected : SocialAction()

    object CloseSelect: SocialAction()
}

class HomeUIViewModel : ViewModel() {
    private  val _uiState = MutableStateFlow(MessagePageUiState())
    val uiState = _uiState.asStateFlow()
    val uiStateLiveData = _uiState.asLiveData()


    private  val _selectConversations= MutableStateFlow(mutableListOf<ConversationInfo>())
    val selectConversations=_selectConversations.asStateFlow()


    private val _actionEvent = MutableSharedFlow<SocialAction>(replay = 0)
    val actionEvent: SharedFlow<SocialAction> = _actionEvent
    // 便于 Java Fragment 监听，提供一次性事件的 LiveData 包装，避免生命周期重连导致重复消费
    val actionEventLiveData = _actionEvent.asLiveData()


    // 父 Fragment 调用这些函数
    fun selectAll() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.SelectAll)
    }

    fun cancelSelectAll() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.CancelSelectAll)
    }

    fun markAllAsRead() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.MarkAllAsRead)
    }

    fun markSingleAsRead() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.MarkSingleAsRead)
    }

    fun deleteSelected() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.DeleteSelected)
    }

    fun closeSelect() = viewModelScope.launch {
        _actionEvent.emit(SocialAction.CloseSelect)
    }






    fun updateBottomState(isShowAllRead: Boolean){
        _uiState.value=_uiState.value.copy(showBottomUI = isShowAllRead)
    }

    fun updateBottomBtnState(isSelectedAll: Boolean,isAllRead: Boolean,enableDelete: Boolean){
     _uiState.value=_uiState.value.copy(isAllSelect = isSelectedAll, isAllRead = isAllRead, enableDelete = enableDelete)
    }

    fun updateSelectConversations(info: MutableList<ConversationInfo>){
         _selectConversations.value=info
    }



}