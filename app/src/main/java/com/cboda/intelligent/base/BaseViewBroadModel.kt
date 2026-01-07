package com.cboda.intelligent.base


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.utils.getResumeActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


open class BaseViewBroadModel : ViewModel(), IBaseViewBroadModel {

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    //生命周期管理
    override fun onAny(owner: LifecycleOwner?, event: Lifecycle.Event?) {

    }

    override fun onCreate() {

    }

    override fun onDestroy() {

    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }


    /**
     * 跳转页面
     *
     */
    inline fun <reified T : Activity> startAct(bundle: Bundle? = null, canFinish: Boolean = false) {
        getResumeActivity()?.let {
            val newIntent = Intent(it, T::class.java)
            bundle?.let { newIntent.putExtras(bundle) }
            it.startActivity(newIntent)
            if (canFinish) {
                it.finish()
            }
        }
    }

}