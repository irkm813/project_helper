package com.example.project_helper

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge

class ChangePinActivity : BaseActivity() {

    lateinit var saveNewPinButton : Button
    lateinit var enterOldPinTextbox : EditText
    lateinit var enterNewPinTextbox : EditText
    lateinit var reEnterNewPinTextbox : EditText
    val cryptoHelper = CryptographyHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        layoutInflater.inflate(R.layout.activity_change_pin, findViewById(R.id.content_frame))

        cryptoHelper.loadCredentials()

        saveNewPinButton = findViewById(R.id.save_pin_button)
        enterOldPinTextbox = findViewById(R.id.enter_old_pin_textbox)
        enterNewPinTextbox = findViewById(R.id.enter_new_pin_textbox)
        reEnterNewPinTextbox = findViewById(R.id.re_enter_new_pin_textbox)

        saveNewPinButton.setOnClickListener{
            val newPinAgain = reEnterNewPinTextbox.text.toString()
            val newPin = enterNewPinTextbox.text.toString()
            val oldPin = enterOldPinTextbox.text.toString()

            val isOldPinCorrect =  oldPin == cryptoHelper.decryptData(cryptoHelper.encryptedPinCode)
            val areNewPinsSame = newPin == newPinAgain
            val isNewPinLongEnough = newPin.length > 5


            when {
                !isOldPinCorrect -> {
                    Toast.makeText(this, "Incorrect Old PIN", Toast.LENGTH_SHORT).show()
                }
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
                }
            }

        }

    }
}