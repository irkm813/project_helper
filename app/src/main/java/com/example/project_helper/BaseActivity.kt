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

open class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

            if (fileBytes.size < headerSize + saltSize + ivSize) {
                throw IllegalArgumentException("Invalid encrypted file format.")
            }

            // Skip the "Salted__" header
            val salt = fileBytes.sliceArray(headerSize until headerSize + saltSize)
            val iv = fileBytes.sliceArray(headerSize + saltSize until headerSize + saltSize + ivSize)
            val cipherText = fileBytes.sliceArray(headerSize + saltSize + ivSize until fileBytes.size)

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

            // Decrypt the file
            val decryptedBytes = cipher.doFinal(cipherText)

            // Write the decrypted content
            FileOutputStream(decryptedFile).use { fos ->
                fos.write(decryptedBytes)
            }
        }
    }

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
            oStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        println("Temporary file size: ${tempFile.length()} bytes")

        return tempFile
    }

    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }
}