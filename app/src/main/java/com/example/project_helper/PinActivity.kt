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

class PinActivity : AppCompatActivity() {

    val cryptoHelper = CryptographyHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cryptoHelper.generateKey()
        cryptoHelper.loadCredentials()

        if (cryptoHelper.encryptedPinCode == ""){
            val intent = Intent(this, FirstTimeSetupActivity::class.java)
            startActivity(intent)
            finish()
        }

        val pinEditText: EditText = findViewById(R.id.pinEditText)
        val submitButton: Button = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()

            if (enteredPin == cryptoHelper.decryptData(cryptoHelper.encryptedPinCode.toString())) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
            }
        }

    }
}