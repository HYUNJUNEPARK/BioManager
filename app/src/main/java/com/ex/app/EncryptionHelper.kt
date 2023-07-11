package com.ex.app

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object EncryptionHelper {
    private const val KEY_ALIAS = "MyKeyAlias"

    fun encryptString(plainText: String?): String? {
        try {
            if (plainText == null) return null

            // Get an instance of the KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Generate a new symmetric key if it doesn't exist
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                Timber.d("Generate a new symmetric key")
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
                )
                val builder = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                       .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            } else {
                Timber.d("Use Existing Key")
            }

            // Get the key
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            // Initialize the cipher with the key
            val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // Generate IV
            val iv = cipher.iv

            // Encrypt the string
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(UTF_8))
            Timber.d("EncryptedBytes : $encryptedBytes")

            // Combine IV and encrypted data
            val combinedData = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combinedData, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combinedData, iv.size, encryptedBytes.size)

            Timber.d("combinedData : $combinedData // size : ${combinedData.size}")

            // Return the combined data as a Base64-encoded string
            return Base64.encodeToString(combinedData, Base64.DEFAULT)
        } catch (e: Exception) {
            Timber.e("FAIL : Encrypt String")
            e.printStackTrace()
            // Handle encryption error
        }
        return null
    }

    fun decryptString(encryptedString: String?): String? {
        try {
            if (encryptedString == null) return null

            // Get an instance of the KeyStore
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Get the key
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            // Initialize the cipher with the key and IV
            val cipher = Cipher.getInstance(
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            )

            // Decode the Base64 string
            val encryptedData = Base64.decode(encryptedString, Base64.DEFAULT)
            Timber.d("Decode the Base64 string : $encryptedData")

            // Extract IV and encrypted data
            val iv = encryptedData.copyOfRange(0, cipher.blockSize)
            val encryptedBytes = encryptedData.copyOfRange(cipher.blockSize, encryptedData.size)
            Timber.d("Extract IV($iv) and encrypted data($encryptedBytes)")

            // Set the IV parameter
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

            // Decrypt the encrypted string
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            // Return the decrypted string
            return String(decryptedBytes, UTF_8)
        } catch (e: Exception) {
            Timber.e("FAIL : Decrypt String")
            e.printStackTrace()
            // Handle decryption error
        }
        return null
    }

    fun deleteKey(): Boolean {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                Timber.d("Fail : Empty KeyStore")
                return false
            }

            keyStore.deleteEntry(KEY_ALIAS)
            return true
        } catch (e: Exception) {
            Timber.e("Fail Delete KeyStore Key : ${e.message}")
            e.printStackTrace()
        }
        return false
    }
}