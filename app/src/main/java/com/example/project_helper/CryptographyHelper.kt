package com.example.project_helper

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptographyHelper(activityContext: Context) {

    lateinit var encryptedInfraUsername : String
    lateinit var encryptedInfraKey: String
    lateinit var encryptedPinCode : String

    var context = activityContext

    //checks if there is already a key created for the app and creates it if not
    fun generateKey() {

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        if (!keyStore.containsAlias("project_helper_key")){

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                "project_helper_key",
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    fun encryptData(data: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey("project_helper_key", null) as SecretKey

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv // Save this IV, you will need it to decrypt
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT)

        return "$ivString:$encryptedString" // Store both IV and encrypted data
    }

    fun decryptData(encryptedData: String): String {

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKey = keyStore.getKey("project_helper_key", null) as SecretKey

        val parts = encryptedData.split(":")
        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val encryptedBytes = Base64.decode(parts[1], Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decodedData = cipher.doFinal(encryptedBytes)
        return String(decodedData, Charsets.UTF_8)
    }

    //saves the username and the uri of the decrypted file into the shared preferences
    fun saveCredentials() {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("infra_username", encryptedInfraUsername)
        editor.putString("infra_key", encryptedInfraKey)
        editor.apply()  // Save asynchronously
    }
    //saves the user's pin code
    fun savePin(){
        val sharedPref = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("pin_code", encryptedPinCode)
        editor.apply()
        }

    //loads the username and the uri of the decrypted file from the shared preferences
    fun loadCredentials() {
        val sharedPref = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        encryptedInfraKey = sharedPref.getString("infra_key", null) ?: ""
        encryptedInfraUsername = sharedPref.getString("infra_username", null) ?: ""
        encryptedPinCode = sharedPref.getString("pin_code", null) ?: ""

    }
}