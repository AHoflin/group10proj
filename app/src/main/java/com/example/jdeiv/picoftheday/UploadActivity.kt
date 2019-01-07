package com.example.jdeiv.picoftheday

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.PersistableBundle
import android.provider.FontRequest
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.jar.Manifest
import kotlinx.android.synthetic.main.fragment_camera.*

class UploadActivity: AppCompatActivity() {

    lateinit var fetchedPosition: FetchedLocation
    var longitudeDouble: Double? = null
    var latitudeDouble: Double? = null
    val username = "TunaBoy1337" //change this one to be the logged in account
    var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_camera)

        imageButton_upload.setOnClickListener(){
            Log.d("UploadActivity","ImageUpload button pressed")

            //select image to upload
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        upload_to_db_button.setOnClickListener{

            uploadImageToFirebaseStorage()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("UploadImage", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            imageButton_upload.setImageBitmap(bitmap)
        }

    }

    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null){
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
            return
        }

        //create random filename
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")

        //upload into database
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("UploadActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    saveImageToFirebaseDb(it.toString(), filename)
                }
            }.addOnFailureListener(){
                Log.d("UploadActivity", "Upload failed")
            }
    }

    //save image and all necessary information into database
    private fun saveImageToFirebaseDb(filename: String, imgFilename: String){

        val date = Calendar.getInstance().time

        val ref = FirebaseDatabase.getInstance().getReference("/POTD/$imgFilename")
        val input = input_text.text.toString()

        fetchedPosition = updatePosition()

        // If user is signed in.
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser!=null){
            currentUser.let {
                val image = ImageStats(filename, 0, input, it.email.toString(), date, fetchedPosition)
            }
        }
        val image = ImageStats(filename, 0, input, username, date, fetchedPosition)

        ref.setValue(image).addOnSuccessListener {
            Toast.makeText(this, "Image uploaded!", Toast.LENGTH_SHORT).show()
        }

    }

    fun updatePosition(): FetchedLocation {
        val fileName = "/location.txt"
        val file = File(this.dataDir.toString() + fileName)
        val coor = file.bufferedReader().readLines()

        val location = FetchedLocation(coor[0].toDouble(), coor[1].toDouble())

        return location
    }

}