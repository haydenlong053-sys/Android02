package com.cboda.intelligent.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 数据模型
data class UserBroadProfile(
    val avatarUrl: String = "",
    val nickname: String = "",
    val job: String = "Android Developer",
    val interests: List<String> = listOf("Coding", "Design", "Music")
)

class ProfileViewModel : BaseViewModel() {
    // 原始数据（用于对比是否修改，以及重置）
    private var _originalProfile = UserBroadProfile()

    // 当前编辑中的数据
    private val _uiState = MutableStateFlow(_originalProfile)
    val uiState = _uiState.asStateFlow()

    // 判断是否有修改
    val isModified: Boolean
        get() = _uiState.value != _originalProfile

    // 更新职业
    fun updateJob(newJob: String) {
        _uiState.update { it.copy(job = newJob) }
    }

    // 更新兴趣列表
    fun updateInterests(newInterests: List<String>) {
        _uiState.update { it.copy(interests = newInterests) }
    }

    // 保存更改（将当前状态确认为原始状态）
    fun saveChanges() {
        _originalProfile = _uiState.value
        // 这里可以添加网络请求逻辑
    }

    // 放弃更改（回滚）
    fun discardChanges() {
        _uiState.value = _originalProfile
    }
}