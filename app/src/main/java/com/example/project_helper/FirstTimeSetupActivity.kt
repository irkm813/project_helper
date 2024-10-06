package com.example.project_helper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FirstTimeSetupActivity : AppCompatActivity() {

    lateinit var saveNewPinButton : Button
    lateinit var enterNewPinTextbox : EditText
    lateinit var reEnterNewPinTextbox : EditText
    val cryptoHelper = CryptographyHelper(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_first_time_setup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cryptoHelper.loadCredentials()

        saveNewPinButton = findViewById(R.id.create_pin_button)
        enterNewPinTextbox = findViewById(R.id.create_new_pin_textbox)
        reEnterNewPinTextbox = findViewById(R.id.create_new_pin_again_textbox)

        saveNewPinButton.setOnClickListener{
            val newPinAgain = reEnterNewPinTextbox.text.toString()
            val newPin = enterNewPinTextbox.text.toString()

            val areNewPinsSame = newPin == newPinAgain
            val isNewPinLongEnough = newPin.length > 5


            when {
                !isNewPinLongEnough -> {
                    Toast.makeText(this, "Pin code must be 6 characters long", Toast.LENGTH_SHORT).show()
                }
                !areNewPinsSame -> {
                    Toast.makeText(this, "New pins don't match", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    cryptoHelper.encryptedPinCode = cryptoHelper.encryptData(enterNewPinTextbox.text.toString())
                    Toast.makeText(this, "Pin code successfully changed", Toast.LENGTH_SHORT).show()
                    cryptoHelper.savePin()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }


    }
}