package com.cboda.baselibrary.utils


import android.util.Log
import com.cboda.baselibrary.BuildConfig
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

private const val eLogTag = "ELogClass"

//private var mIsDebug = false
//fun getIsDebug(): Boolean = BuildConfig.DEBUG
fun getIsDebug(): Boolean = true
//fun setIsDebug(isDebug: Boolean) {
//    mIsDebug = isDebug
//}

fun eLogV(vararg msg: Any?) {
    eLogV(eLogTag, msg)
}


fun eLogV(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 1)
            }
        }
    }
}


fun eLogD(vararg msg: Any?) {
    eLogD(eLogTag, msg)
}

fun eLogD(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 2)
            }
        }
    }
}

//唯有debug模式才写入文件
fun eLogDebugFile(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 7)
            }
        }
    }
}

//任何模式都写入文件
fun eLogFile(tag: String?, vararg msgS: Any?) {
    msgS?.also {
        for (msg in msgS) {
            log2(tag, getMsgFormat(tag, msg), 6)
        }
    }
}

fun eLogI(vararg msg: Any?) {
    eLogI(eLogTag, msg)
}

fun eLogI(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 3)
            }
        }
    }
}


fun okGoLog(tag: String?, msg: String) {
    if (getIsDebug()) {
        log2(tag, msg, 4)
    }
}

fun eLogW(vararg msg: Any?) {
    eLogW(eLogTag, msg)
}

fun eLogW(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 4)
            }
        }
    }
}

fun eLogWtf(vararg msg: Any?) {
    eLogWtf(eLogTag, msg)
}

fun eLogWtf(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 5)
            }
        }
    }
}

fun eLogE(vararg msg: Any?) {
    eLogE(eLogTag, msg)
}


fun eLogE(tag: String?, vararg msgS: Any?) {
    if (getIsDebug()) {
        msgS?.also {
            for (msg in msgS) {
                log2(tag, getMsgFormat(tag, msg), 0)
            }
        }
    }
}

private fun log3(tag: String?, msg: String, type: Int) {
    if (getIsDebug()) {
        when (type) {
            0 -> {
                Log.e(tag ?: eLogTag, msg)
            }

            1 -> {
                Log.v(tag ?: eLogTag, msg)
            }

            3 -> {
                Log.i(tag ?: eLogTag, msg)
            }

            4 -> {
                Log.w(tag ?: eLogTag, msg)
            }

            5 -> {
                Log.wtf(tag ?: eLogTag, msg)
            }

            2, 6, 7 -> {
                Log.d(tag ?: eLogTag, msg)
                if (type == 7) {
//                    buglyServer?.onBgReport("$tag:$msg")
                } else if (type == 6) {
//                    buglyServer?.onBgReport("$tag:$msg")
                }
            }
        }
    } else if (type == 6) {
//        buglyServer?.onBgReport("$tag:$msg")
    }
}

private fun log2(tag: String?, msg: String, type: Int) {
    if (getIsDebug()) {
        val segmentSize = 1024 * 2
        val segmentSize2 = 1024 * 2 * 80
        val length = msg.length
        when {
            length <= segmentSize -> {
                log3(tag, msg, type)
            }

            length <= segmentSize2 -> {
                val message = msg.substring(0, segmentSize)
                log3(tag, message, type)
                val message2 = msg.substring(segmentSize)
                log2(tag, "\t\t$message2", type)
            }

            else -> {
                log3(tag, msg, type)
            }
        }
    } else {
        log3(tag, msg, type)
    }
}

private fun getMsgFormatInt(msg: Int): String {
    return msg.toString() + " ;" + getFunctionName()
}

private fun getMsgFormatString(msg: String? = null): String {
    return msg + " ;" + getFunctionName()
}

private fun getFunctionName(): String? {
    val sts = Thread.currentThread().stackTrace
    if (null != sts) {
        for (st in sts) {
            if (st.isNativeMethod) {
                continue
            }
            val className = st.className
            if (className == Thread::class.java.name) {
                continue
            }
            if (className == "com.cboda.baselibrary.utils.ELogUtilKt" || className == "com.cboda.baselibrary.utils.ELogUtilKt" || className == "com.cboda.baselibrary.utils.p") {
                continue
            }
            return ("[ Thread:" + Thread.currentThread().name + ", at " + st.className + "." + st.methodName
                    + "(" + st.fileName + ":" + st.lineNumber + ")" + " ]")
        }
    }
    return null
}

fun Exception.toLog() = this.toLog(null)
fun Exception.toLog(tag: String?) = this.toLog(tag, true)
fun Exception.toLog(tag: String?, canPush: Boolean = true): String {
    return if (getIsDebug()) {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw, true))
        val errorStr = sw.toString()
        sw.close()
        tag?.also { eLogD(tag, errorStr) }
        if (canPush && !getIsDebug()) {
//            buglyServer?.onBgReport("$tag:$errorStr")
        }
        errorStr
    } else {
        ""
    }
}

fun Throwable.toLog() = this.toLog(null)
fun Throwable.toLog(tag: String?) = this.toLog(tag, true)
fun Throwable.toLog(tag: String?, canPush: Boolean = true): String {
    return if (getIsDebug()) {
        val sw = StringWriter()
        this.printStackTrace(PrintWriter(sw, true))
        val errorStr = sw.toString()
        sw.close()
        tag?.also { eLogD(tag, errorStr) }
        if (canPush && !getIsDebug()) {
//            buglyServer?.onBgReport("$tag:$errorStr")
        }
        errorStr
    } else {
        ""
    }
}


val Throwable.stackTraceStr: String
    get() {
        val stackTrace = stackTraceToString()
        val lMsg = this.localizedMessage ?: "noErrorMsg"
        return when {
            stackTrace.isNotEmpty() -> stackTrace
            else -> lMsg
        }
    }

fun Throwable.asIOException(): IOException {
    val newException = IOException(this.message)
    newException.initCause(this)
    return newException
}

private fun getMsgFormat(tag: String?, msg: Any?): String {
    return if (null != msg) {
        when (msg) {
            is String -> getMsgFormatString(msg)
            is Int -> getMsgFormatInt(msg)
            is Exception -> msg.toLog(tag)
            is Throwable -> msg.toLog(tag)
            else -> getMsgFormatString(toJson2(msg))
        }
    } else {
        getMsgFormatString()
    }
}
