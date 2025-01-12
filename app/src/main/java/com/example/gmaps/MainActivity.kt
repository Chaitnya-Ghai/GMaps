package com.example.gmaps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gmaps.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.encoders.json.BuildConfig

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val fusedLocationClient :FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    var googleMap:GoogleMap?=null
    var userLocation = LatLng(0.0,0.0)
    var markerOptions = MarkerOptions()
    var markerCenter:Marker?=null

    private val locationPermission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        permissions ->
        if (permissions.values.all { it }){
            Toast.makeText(this, "All Permissions Granted", Toast.LENGTH_SHORT).show()
            getLastLocation()
        }
        else{
            Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
            openAppSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (!hasPermissions()){
            requestPermissionWithRationale()
        }
        else{
            getLastLocation()
        }
//        get a handle to the fragment and register the callback
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
    fun hasPermissions():Boolean{
        return locationPermission.all { permissions->
            ActivityCompat.checkSelfPermission(this,permissions) == PackageManager.PERMISSION_GRANTED
        }
    }
//    denie krne kai baad fir sai ek pop up ataa jo batata hai ki ess perission ka kaam kya hai = done by rationale
    fun requestPermissionWithRationale(){
        val shouldShowRationale = locationPermission.any { permission->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
         if (shouldShowRationale){
             Toast.makeText(this, "Permissions are Required for the App", Toast.LENGTH_SHORT).show()
         }
    else{
        requestPermission()
         }
    }
    fun requestPermission(){
        requestPermissionLauncher.launch(locationPermission)
    }
    fun openAppSettings(){
        val intent =Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = android.net.Uri.fromParts("package", BuildConfig.APPLICATION_ID,null)
        intent.data = uri
        startActivity(intent)
    }

    fun requestNewLocationData(){
        val locationRequest = LocationRequest.Builder(10000).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback
            ,Looper.myLooper()
        )
    }
   private val  locationCallback: LocationCallback = object : LocationCallback() {
       override fun onLocationResult(locationResult: LocationResult) {
           userLocation = LatLng(
               locationResult.lastLocation?.latitude ?: 0.0,
               locationResult.lastLocation?.longitude ?: 0.0
           )
           updateMarker()
       }
   }
    private fun updateMarker(){
        markerCenter?.position = userLocation
        googleMap?.animateCamera(CameraUpdateFactory.newLatLng(userLocation))
        binding.mapTv.text=MapFunction.getLocationName(this@MainActivity,userLocation)
    }
    fun isLocationEnable():Boolean{
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    fun getLastLocation() {
        if (isLocationEnable()) {
            Log.e("TAG", "getLastLocation: isLocationEnable ${isLocationEnable()}")

            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location: android.location.Location = task.result
                if (location != null) {
                    requestNewLocationData()
                } else {
                    userLocation = LatLng(location.latitude, location.longitude)
                    updateMarker()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        markerCenter=map.addMarker(
            MarkerOptions().position(userLocation)
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
        map.setOnCameraIdleListener {
            userLocation=map.cameraPosition.target
            updateMarker()
        }
    }
}

