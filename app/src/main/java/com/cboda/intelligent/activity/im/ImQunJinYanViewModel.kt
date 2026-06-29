package com.cboda.intelligent.activity.im

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupVo
import com.freddy.im.listener.OnHttpImListener

class ImQunJinYanViewModel : ViewModel() {

    private val _allMute = MutableLiveData(false)
    val allMute: LiveData<Boolean> = _allMute

    fun load(groupId: String) {
        if (groupId.isEmpty()) return
        ImGroupHttpExample.requestGetGroupDetail(groupId, object : OnHttpImListener<GroupVo> {
            override fun onHttpImSuccess(result: GroupVo) {
                _allMute.postValue(result.isAllMute)
            }
        }, {})
    }

    fun setAllMute(groupId: String, mute: Boolean, onDone: (Boolean) -> Unit) {
        ImGroupHttpExample.requestSetAllMute(groupId, mute, object : OnHttpImListener<Boolean> {
            override fun onHttpImSuccess(result: Boolean) {
                _allMute.postValue(mute)
                onDone(true)
            }
        }, { onDone(false) })
    }
}
