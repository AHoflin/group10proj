package com.example.jdeiv.picoftheday

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        btn_logout.setOnClickListener {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
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
            /* The following return should not run, but is necessary in order to compile */
            return false
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}