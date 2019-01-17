package com.example.jdeiv.picoftheday

import android.Manifest
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import java.io.File


class LocationTask(private var context: Context, private var fusedLocationProviderClient: FusedLocationProviderClient, private var activity : Activity): AsyncTask<Any, Any, Any>() {
    private var lng : Double? = null
    private var lat : Double? = null
    lateinit var locationManager: LocationManager
    private var REQUEST_LOCATION_CODE = 101

    override fun onPreExecute() {
        super.onPreExecute()

        // Workaround buggfix
        val fileName = "/location.txt"
        val file = File(context.dataDir.toString() + fileName)
        file.bufferedWriter().use { out -> out.write("0"+ "\n" + "0") }

        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                //On success it can be null. Then it goes bananas
                Log.d("hejhej", "1")
                if(it != null) {
                    lng = it.longitude
                    lat = it.latitude
                    Log.d("LocationTask", "Pos found")

                }else{
                    locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener{
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderEnabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onProviderDisabled(provider: String?) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onLocationChanged(location: Location?) {

                            if(location != null){
                                lng = location!!.longitude
                                lat = location!!.latitude
                                Log.d("LocationTask", location.longitude.toString())
                                Log.d("LocationTask", location.latitude.toString())

//                                val fileName = "location.txt"
//                                val file = File(context.dataDir.toString(), fileName)
//                                file.bufferedWriter().use { out -> out.write(lng.toString() + "\n" + lat.toString()) }
                            }else{
                                Toast.makeText(context, "Could not find position", Toast.LENGTH_SHORT).show()
                                //kill application here or something
                            }
                        }
                    })
                }
            }
        }
    }

    override fun doInBackground(vararg params: Any?): Any {
        while(lng == null && lat == null){}
        return ""
    }

    override fun onPostExecute(result: Any?) {
        super.onPostExecute(result)
        // Check if long or lat is 0
        val fileName = "/location.txt"
        val file = File(context.dataDir.toString() + fileName)
        file.bufferedWriter().use { out -> out.write(lng.toString()+ "\n" + lat.toString()) }

        checkIfPositionWritten()
    }

    private fun checkIfPositionWritten(){
        val fileName = "/location.txt"
        val file = File(context.dataDir.toString() + fileName)
        if(!file.exists()){
            //Toast.makeText(this, "Location could not be found", Toast.LENGTH_SHORT).show()
            AlertDialog.Builder(context)
                .setTitle("Location not found")
                .setMessage("This app needs your Location to function properly, please enable location services")
                .setPositiveButton("OK", { dialog, which ->
                    ActivityCompat.requestPermissions(  activity , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                })
                .create()
                .show()
        }
    }
}



