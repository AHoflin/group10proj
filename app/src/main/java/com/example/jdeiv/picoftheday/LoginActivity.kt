package com.example.jdeiv.picoftheday

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Checking if user is signed in (non-null)
        if (auth.currentUser != null){
            // Send user to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_register.setOnClickListener {
            val email = edittext_email_login.text.toString()
            val password = edittext_password_login.text.toString()
            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        Log.d("LoginActivity", "signInWithEmail:success")
                        val user = auth.currentUser
                        // Send user to MainActivity
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.w("LoginActivity", "signInWithEmail:failure", it.exception)
                        Toast.makeText(baseContext,"Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnCanceledListener {
                    Log.d("LoginActivity", "CANCELED")
                }
        }

        text_notregistered.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }



    }

}