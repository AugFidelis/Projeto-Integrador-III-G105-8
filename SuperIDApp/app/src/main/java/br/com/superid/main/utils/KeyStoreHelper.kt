package br.com.superid.main.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreHelper {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MyAESKeyAlias"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12 // GCM recomenda IV de 12 bytes
    private const val TAG_LENGTH_BIT = 128 // Tamanho do MAC para GCM

    /**
     * Cria uma chave AES no Android Keystore, caso ainda não exista.
     */
    fun createKeyIfNotExists() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        // Verifica se já existe uma chave com esse alias
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).apply {
                setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                // .setUserAuthenticationRequired(true) // se quiser exigir biometria/PIN
            }.build()

            keyGenerator.init(parameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Recupera a chave secreta do Keystore.
     */
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Criptografa dados usando AES/GCM.
     * Retorna IV + bytes criptografados.
     */
    fun encryptData(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv

        val cipherBytes = cipher.doFinal(plaintext)
        // Vamos concatenar IV + dados criptografados para depois poder descriptografar
        return iv + cipherBytes
    }

    /**
     * Descriptografa dados usando AES/GCM.
     * Espera receber IV + bytes criptografados.
     */
    fun decryptData(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = encryptedData.copyOfRange(0, IV_SIZE)
        val cipherText = encryptedData.copyOfRange(IV_SIZE, encryptedData.size)

        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(cipherText)
    }
}