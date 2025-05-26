package br.com.superid.main.utils

import android.util.Base64

object Base64Utils {
    fun encodeToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun decodeFromBase64(str: String): ByteArray {
        return Base64.decode(str, Base64.DEFAULT)
    }
}