package com.cboda.baselibrary.utils

import android.annotation.SuppressLint
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptUtil {
    private const val TAG = "EncryptUtilClass"

    @SuppressLint("GetInstance")
    fun aesEncrypt(content: String?, transformation: String = "AES/ECB/PKCS5Padding", aesKey: String? = null, flags: Int? = null): String {
        return try {
            val key = aesKey ?: HttpsBroadConfig.appAesPwd
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"))
            val encrypted = cipher.doFinal(content.toString().toByteArray())
            Base64.encodeToString(encrypted, flags ?: Base64.DEFAULT)
        } catch (e: Exception) {
            e.toLog(TAG)
            content.toString()
        }
    }


    @SuppressLint("GetInstance")
    fun aesDecrypt(content: String?, transformation: String = "AES/ECB/PKCS5Padding", aesKey: String? = null, flags: Int? = null): String {
        return try {
            val key = aesKey ?: HttpsBroadConfig.appAesPwd
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"))
            String(cipher.doFinal(Base64.decode(content.toString(), flags ?: Base64.DEFAULT)))
        } catch (e: Exception) {
            e.toLog(TAG)
            content.toString()
        }
    }


}