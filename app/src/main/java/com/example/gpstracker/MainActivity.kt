package com.example.gpstracker

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task


class MainActivity : AppCompatActivity(),OnMapReadyCallback {

    // reference for dialog
    var dialog: AlertDialog? = null

    lateinit var fusedLocationClient: FusedLocationProviderClient
     var googleMap: GoogleMap? = null



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        initMap()

    }

    lateinit var mapFragment: SupportMapFragment

    fun initMap(){

        mapFragment =supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // this is the function that controls the map.

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(30.1273975,31.3724326),
            17F
        ))
    }

  /*  fun updateUserMarker(newLocation: Location){

        var userMarker = MarkerOptions()
            userMarker.position = LatLng(newLocation.latitude,newLocation.longitude)
        googleMap?.addMarker()

    }

   */


    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        isGranted: Boolean ->

        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
            checkSettingsQualified()
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
            showPermissionRational()

        }
    }


    fun requestPermission(){

        when {
            ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.

                checkSettingsQualified()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION

            ) -> {

                showPermissionRational()
            }
            else -> {

                requestPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION

                )
            }
        }
    }


    fun showDialog(message:String,
                   positiveActionCallback: (() -> Unit)? = null,
                   negativeActionCallback: (() -> Unit)? = null,
                   positiveActionName:String ? =null,
                   negativeActionName:String?=null,
                   isCancellable:Boolean = true){

        val alertDialogBuilder : AlertDialog.Builder = AlertDialog.Builder(this)

        alertDialogBuilder.setMessage(message)

        alertDialogBuilder.setPositiveButton(positiveActionName,object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                positiveActionCallback?.invoke()
                dialog?.dismiss()
            }
        })
        alertDialogBuilder.setNegativeButton(negativeActionName,object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                negativeActionCallback?.invoke()
                dialog?.dismiss()
            }
        })

        alertDialogBuilder.setCancelable(isCancellable)

       dialog = alertDialogBuilder.show()
    }

    fun showPermissionRational(){
        showDialog("We need to Access Location to be able to continue",
            positiveActionCallback = {requestPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION

            )}, positiveActionName = "Show Again" )
    }

  @SuppressLint("MissingPermission")
  fun  getUserLocation(){
      Toast.makeText(this,"can Access User Location",Toast.LENGTH_LONG).show()

      fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

      fusedLocationClient.requestLocationUpdates(locationRequest, LocationListener {

          Log.e("new Location","${it.longitude} / ${it.longitude}")

      }, Looper.getMainLooper())
  }

    lateinit var locationRequest: LocationRequest

    fun checkSettingsQualified() {
         locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .build()

        locationRequest.minUpdateIntervalMillis

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnCompleteListener {

            if (task.isSuccessful) {
                getUserLocation()
            } else {
                val exception = task.exception
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            this@MainActivity,
                            LOCATION_REQUEST_CHECK_CODE
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }

                }
            }
        }

    }

    val LOCATION_REQUEST_CHECK_CODE = 200


}

