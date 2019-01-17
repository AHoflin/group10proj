package com.example.jdeiv.picoftheday

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(my_toolbar)

        btn_logout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Put this in a delete account button
        btn_delete_account.setOnClickListener {
            val deleteAccountDialog = AlertDialog.Builder(this)
            deleteAccountDialog.setTitle("Delete account")
            deleteAccountDialog.setMessage("Are you sure you want to delete your account?")

            deleteAccountDialog.setPositiveButton("Yes"){ dialog, which ->
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()
                    ?.addOnCompleteListener{
                        if(it.isSuccessful){
                            Log.d("SettingsActivity", "User account deleted")
                            Toast.makeText(this,"Your account has been deleted", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
            }

            deleteAccountDialog.setNegativeButton("No"){ dialog, which ->
                // Do someting maybe not...
            }

            deleteAccountDialog.show()
        }

        btn_reset_pwd_2.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflating the toolbar menu
        menuInflater.inflate(R.menu.top_menu_settings, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.back_arrow_settings){
            // Send user back to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            /* The following return should not run, but is necessary in order to compile */
            return false
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}