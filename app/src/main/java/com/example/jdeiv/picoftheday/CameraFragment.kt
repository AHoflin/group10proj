package com.example.jdeiv.picoftheday

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker.checkSelfPermission
import android.support.v7.view.menu.MenuAdapter
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.util.*
import android.view.KeyEvent.KEYCODE_MENU
import android.widget.CheckBox
import com.google.firebase.auth.FirebaseAuth
//import com.soundcloud.android.crop.Crop
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_camera.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*


class CameraFragment : Fragment() {

    lateinit var fetchedPosition: FetchedLocation
    var longitudeDouble: Double? = null
    var latitudeDouble: Double? = null
    val username = "TunaBoy1337" //change this one to be the logged in account
    var selectedPhotoUri: Uri? = null
    var selectedPhotoUri2: Uri? = null
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val CROP_REQUEST_CODE = 1002
    var image_uri: Uri? = null
    lateinit var checkmark: MenuItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        setHasOptionsMenu(true)

        if (savedInstanceState != null){
            Log.d("CameraFragment", "savedInstanceState != null")
            image_uri = savedInstanceState.getParcelable("imageUri")
            card_text.setText(savedInstanceState?.getString("caption"))
        }

        if (image_uri != null){
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, image_uri)
            card_imageButton.setImageBitmap(bitmap)
        }

        view.btn_select_photo.setOnClickListener(){
            Log.d("UploadActivity","ImageUpload button pressed")

            //select image to upload
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        view.btn_take_photo.setOnClickListener {
            askForPermissionThenOpenCamera()
        }

        view.card_imageButton.setOnClickListener(){
            startDialog()
        }

        return view
    }

    //creates toolbar for camera fragment
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater){
        // Inflating the toolbar menu
        inflater.inflate(R.menu.top_menu_main, menu)
        val settings = menu?.findItem(R.id.settings_button)
        settings?.isVisible = false
        return super.onCreateOptionsMenu(menu, inflater)
    }

    //listens to keypress in toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean{
        val id = item.getItemId()
        checkmark = item

        if (id == R.id.upload_check){
            val positionExist = checkPosition()
            // Send user to settings page
            if(positionExist){
                item.isEnabled = false
                uploadImageToFirebaseStorage()
            }else{
                Toast.makeText(context, "No location was found", Toast.LENGTH_SHORT).show()
            }
            /* The following return should not run, but is necessary in order to compile */
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    //when called prompts a select screen
    private fun startDialog() {
        val pictureDialog = AlertDialog.Builder(activity)
        pictureDialog.setTitle("Upload photo option")
        pictureDialog.setMessage("Take a photo or select one from your device?")

        pictureDialog.setPositiveButton("Camera"){ dialog, which ->
            askForPermissionThenOpenCamera()
        }

        pictureDialog.setNegativeButton("Gallery"){ dialog, which ->
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        pictureDialog.show()
    }

    companion object {
        fun newInstance(): CameraFragment = CameraFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CROP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d("CameraFragment", "Image cropped")
            var result = CropImage.getActivityResult(data)
            selectedPhotoUri = result.uri
            val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver!!, selectedPhotoUri)
            card_imageButton.setImageBitmap(bitmap)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE){
            Log.d("CameraFragment", "Picture taken")
            //selectedPhotoUri = image_uri
            //Crop.of(image_uri, selectedPhotoUri).asSquare().start(activity)
            val intent = CropImage.activity(image_uri).setAspectRatio(1,1)
                .getIntent(context!!)
            startActivityForResult(intent, CROP_REQUEST_CODE)
        }
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("CameraFragment", "Photo selected")
            selectedPhotoUri2 = data.data
            //Crop.of(data.data, selectedPhotoUri).asSquare().start(activity)
            val intent = CropImage.activity(data.data).setAspectRatio(1,1)
                .getIntent(context!!)
            startActivityForResult(intent, CROP_REQUEST_CODE)
        }
    }

    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null){
            Toast.makeText(context, "No image selected!", Toast.LENGTH_SHORT).show()
            checkmark.isEnabled = true
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
        val input = card_text.text.toString()

        fetchedPosition = updatePosition()

        // If user is signed in.
        val currentUser = FirebaseAuth.getInstance().currentUser
        val usermail = currentUser!!.email.toString()
        val image = ImageStats(filename, 0, input, usermail, date, fetchedPosition)
        Log.d("User at upload", "usermail: $usermail")

        ref.setValue(image).addOnSuccessListener {
            Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
            card_imageButton.setImageBitmap(null)
            card_text.text = null
            selectedPhotoUri = null
            checkmark.isEnabled = true
        }

    }

    private fun checkPosition() : Boolean{

        val fileName = "/location.txt"
        val file = File(this.context?.dataDir.toString() + fileName)
        if(!file.exists()){
            Toast.makeText(context, "Position not found", Toast.LENGTH_SHORT).show()
            return false
        }
        return true

    }

    fun updatePosition(): FetchedLocation {
        val fileName = "/location.txt"
        val file = File(this.context?.dataDir.toString() + fileName)
        val coor = file.bufferedReader().readLines()
        val location = FetchedLocation(coor[0].toDouble(), coor[1].toDouble())

        return location
    }

    private fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        // camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        cameraIntent.putExtra("aspectX", 1)
        cameraIntent.putExtra("aspectY", 1)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    private fun askForPermissionThenOpenCamera(){
        // If Marshmallow or later, runtime permission is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(activity?.baseContext!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(activity?.baseContext!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                // Permission is not enabled
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // Show popup to request permission.
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                // Permission granted
                openCamera()
            }
        } else {
            // System is less than Marshmallow
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Called when the user presses allow or deny from Permission Request
        when (requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission was granted
                    openCamera()
                } else {
                    // Permission was denied
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /* if ( card_text.text.toString() != null ) { outState.putString("caption", card_text.text.toString()) } */
        if (image_uri != null ) { outState.putParcelable("imageUri", image_uri!!) }
        Log.d("CameraFragment", "Saving instance state")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CameraFragment", "Fragment destroyed (destroyview)")
    }

    /* You should not override this */
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("CameraFragment", "Fragment destroyed (destroyview)")
    }
}