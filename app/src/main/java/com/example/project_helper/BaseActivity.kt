package com.example.project_helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

//This class is created for the sake of simplicity.
//It consists of the main drawer and a few functions which the other activities inherit
open class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle
    var infraUsername : String? = null
    var infraKey : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadCredentials()

        drawerLayout = findViewById(R.id.main)
        navView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val homeIntent = Intent(this, MainActivity::class.java)
                    startActivity(homeIntent)
                    finish()
                }
                R.id.nav_settings -> {
                    val settingsIntent = Intent(this, Settings::class.java)
                    startActivity(settingsIntent)
                    finish()
                }
            }
            menuItem.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    //reads an encrypted file and after decrypting it, saves the result into the decryptedFile variable
    fun decryptFileWithPBKDF2(
        encryptedFile: File,
        decryptedFile: File,
        password: String,
        iterations: Int = 10000,
        keyLength: Int = 256
    ) {
        val saltSize = 8 // OpenSSL salt is 8 bytes
        val ivSize = 16 // AES block size is 16 bytes for CBC mode
        val headerSize = 8 // Salted__ is the first 8 bytes

        FileInputStream(encryptedFile).use { fis ->
            val fileBytes = fis.readBytes()

            // Check if the file is at least large enough to contain the header, salt, and IV
            if (fileBytes.size < headerSize + saltSize + ivSize) {
                throw IllegalArgumentException("Invalid encrypted file format.")
            }

            // Extract the salt (bytes 8 to 15)
            val salt = fileBytes.sliceArray(headerSize until headerSize + saltSize)

            // Ciphertext starts after "Salted__", salt (total 16 bytes)
            val cipherText = fileBytes.sliceArray(headerSize + saltSize until fileBytes.size)

            // Derive the key and IV using PBKDF2 with the same parameters as OpenSSL
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength + ivSize*8)
            val tmp = factory.generateSecret(spec)

            val key = tmp.encoded.sliceArray(0 until keyLength/8)
            val iv = tmp.encoded.sliceArray(keyLength/8 until keyLength/8 + ivSize)
            val secretKey = SecretKeySpec(key, "AES")

            // Initialize AES cipher for decryption (AES/CBC/PKCS5Padding)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

            // Decrypt the file
            val decryptedBytes = cipher.doFinal(cipherText)

            // Write the decrypted content to the output file
            FileOutputStream(decryptedFile).use { fos ->
                fos.write(decryptedBytes)
            }
        }
    }

    //It reads the uri, and returns the file it refers to.
    fun fileFromContentUri(context: Context, contentUri: Uri): File {

        val fileName = "temporary_file"

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    //a helper function for fileFromContentUri. It basically copies a file.
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }
    //saves the username and the uri of the decrypted file into the shared preferences
    fun saveCredentials() {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("infra_username_key", infraUsername)
        editor.putString("infra_key", infraKey.toString())
        editor.apply()  // Save asynchronously
    }
    //loads the username and the uri of the decrypted file from the shared preferences
    fun loadCredentials() {
        val sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Retrieve the username (String)
        infraUsername = sharedPref.getString("infra_username_key", null)

        // Retrieve the private key (Uri) as a String, then parse it back into a Uri
        val privateKeyString = sharedPref.getString("infra_key", null)
        infraKey = privateKeyString?.let { Uri.parse(it) }

    }
}