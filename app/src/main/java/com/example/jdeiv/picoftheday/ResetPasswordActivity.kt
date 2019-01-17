package com.example.jdeiv.picoftheday

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        text_go_to_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_send_reset_pwd.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            auth.sendPasswordResetEmail(txt_reset_email.text.toString())
                .addOnCompleteListener{
                    if (it.isSuccessful()){
                        Toast.makeText(this,"Email sent!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this,"Something went wrong, email not sent.", Toast.LENGTH_LONG).show()
                    }
                }
            if (user != null){
                auth.signOut()
            }
        }
    }
}
