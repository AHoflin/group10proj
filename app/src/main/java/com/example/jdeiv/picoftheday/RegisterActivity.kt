package com.example.jdeiv.picoftheday

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity(){

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        text_gotologin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()

        btn_register.setOnClickListener {
            Toast.makeText(this, "Button pressed", Toast.LENGTH_SHORT).show()
            val email = edittext_email_register.text.toString()
            val password = edittext_password_register.text.toString()
            val password2 = edittext_password2_register.text.toString()

            if (email.isEmpty() || password.isEmpty() || password2.isEmpty()){
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!(password == password2)){
                Toast.makeText(this,"Please confirm your password correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            Log.d("RegisterActivity", "createUserWithEmail:success")
                            val user = auth.currentUser
                            // Send user to mainactivity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Log.w("RegisterActivity", "createUserWithEmail:failure", it.exception)
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}