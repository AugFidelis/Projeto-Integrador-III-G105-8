package br.com.superid.utils

import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object HelperCripto {
    private const val KEY_SIZE = 256 // bits
    private const val IV_SIZE = 12 // bytes, recommended for GCM
    private const val PBKDF2_ITERATIONS = 310_000 // recommended for mobile 2024+
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val AES_ALGORITHM = "AES"
    private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128 // GCM recommended MAC length (bits)

    /**
     * Deriva uma chave AES a partir da senha-mestra + salt usando PBKDF2.
     */
    fun deriveKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val keySpec: KeySpec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_SIZE)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val secret = factory.generateSecret(keySpec)
        return SecretKeySpec(secret.encoded, AES_ALGORITHM)
    }

    /**
     * Criptografa dados usando AES/GCM.
     * Retorna IV + dados criptografados.
     */
    fun encryptData(plainBytes: ByteArray, secretKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val cipherBytes = cipher.doFinal(plainBytes)
        // IV + cipherText
        return iv + cipherBytes
    }

    /**
     * Descriptografa dados usando AES/GCM.
     * Espera receber IV + dados criptografados.
     */
    fun decryptData(encryptedBytes: ByteArray, secretKey: SecretKey): ByteArray {
        val iv = encryptedBytes.copyOfRange(0, IV_SIZE)
        val cipherText = encryptedBytes.copyOfRange(IV_SIZE, encryptedBytes.size)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(cipherText)
    }

    /**
     * Gera um salt aleatório (padrão: 16 bytes)
     */
    fun generateSalt(size: Int = 16): ByteArray {
        val salt = ByteArray(size)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Utilidades para Base64 (pode continuar usando o seu Base64Utils se preferir)
     */
    fun encodeToBase64(data: ByteArray): String =
        Base64.encodeToString(data, Base64.NO_WRAP)

    fun decodeFromBase64(str: String): ByteArray =
        Base64.decode(str, Base64.DEFAULT)
}


