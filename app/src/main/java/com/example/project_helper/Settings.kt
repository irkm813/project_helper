package com.example.project_helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File


class Settings : BaseActivity() {

    private lateinit var filePickerButton: Button
    private lateinit var saveButton: Button
    private lateinit var fileLocationText: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var encryptedFile: File
    private lateinit var decryptedFile: File
    private lateinit var fileData: String

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            // This part is responsible for handling the file the user selected.
            if (uri != null) {
                infraKey = uri;
                fileLocationText.text="file selected:\n"+infraKey
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
        decryptedFile = File(cacheDir, "decryptedFile.temp")


        usernameEditText.setText(infraUsername)
        fileLocationText.text="file selected:\n"+infraKey

        filePickerButton.setOnClickListener{showFileChooser()}
        saveButton.setOnClickListener{

            infraUsername = usernameEditText.text.toString()
            saveCredentials()
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

    private fun fileChooserButtonLogic(uri: Uri){

        encryptedFile = fileFromContentUri(this,uri)
        decryptFileWithPBKDF2(encryptedFile, decryptedFile, "password")
        fileData = decryptedFile.readText()

        fileLocationText.text="encrypted:\n"+encryptedFile.readText()+"\ndecrypted:\n"+fileData
        println("thins is: "+ fileData)
    }
}