package com.example.project_helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts


class Settings : BaseActivity() {

    private lateinit var filePickerButton: Button
    private lateinit var saveButton: Button
    private lateinit var fileLocationText: TextView
    private lateinit var usernameEditText: EditText
    val cryptoHelper = CryptographyHelper(this)

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data

            if (uri != null) {
                //reads the file's content and encrypts it, then saves the data into the encryptedInfraKey variable
                cryptoHelper.encryptedInfraKey = cryptoHelper.encryptData(fileFromContentUri(this,uri).readText(charset = Charsets.UTF_8))
                fileLocationText.setText("File selected")
                Toast.makeText(this, "File selected: $uri", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File selection canceled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_settings, findViewById(R.id.content_frame))

        filePickerButton= findViewById(R.id.file_picker_button)
        fileLocationText = findViewById(R.id.key_file_path)
        usernameEditText = findViewById(R.id.username_text)
        saveButton= findViewById(R.id.save_button)

        cryptoHelper.generateKey()
        cryptoHelper.loadCredentials()

        usernameEditText.setText(cryptoHelper.encryptedInfraUsername)

        filePickerButton.setOnClickListener{showFileChooser()}
        saveButton.setOnClickListener{

            cryptoHelper.encryptedInfraUsername = cryptoHelper.encryptData(usernameEditText.text.toString())
            cryptoHelper.saveCredentials()
        }
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type="*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            // Launch the intent using the ActivityResultLauncher
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"))
        } catch (exception: Exception) {
            Toast.makeText(this, "Please install a file manager.", Toast.LENGTH_SHORT).show()
        }
    }
}