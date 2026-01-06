package com.cboda.agora.service.service.arbiter

import com.cboda.agora.service.service.callback.AUIBroadException
import com.cboda.agora.service.service.rtm.AUIRtmBroadManager
import com.cboda.agora.service.service.rtm.AUIRtmLockRespObserver

class AUIBroadArbiter(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager,
    private val currentUserId: String
) {

    private var lockOwnerId = ""
    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            if (lockOwnerId.isNotEmpty() && lockOwner == currentUserId) {
                rtmManager.fetchMetaDataSnapshot(channelName) {
                    //TODO: error handler, retry?
                    lockOwnerId = lockOwner
                }
            } else {
                lockOwnerId = lockOwner
            }
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {
            if (channelName == this@AUIBroadArbiter.channelName) {
                acquire()
            }
        }
    }

    init {
        rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
    }

    fun deInit(){
        rtmManager.unsubscribeLock(rtmLockRespObserver)
    }

    fun create(){
        rtmManager.setLock(channelName){}
    }

    fun destroy(){
        rtmManager.removeLock(channelName){}
    }

    fun acquire(callback: ((AUIBroadException?)->Unit)? = null) {
        rtmManager.acquireLock(channelName){
            callback?.invoke(if(it == null) null else AUIBroadException(
                AUIBroadException.ERROR_CODE_RTM,
                "$it"
            )
            )
        }
    }

    fun release() {
        rtmManager.releaseLock(channelName){}
    }

    fun isArbiter() = lockOwnerId == currentUserId

    fun lockOwnerId() = lockOwnerId
}