package com.dicoding.dicodingstoryapplication.view.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.dicodingstoryapplication.R
import com.dicoding.dicodingstoryapplication.api.response.ListStoryItem
import com.dicoding.dicodingstoryapplication.databinding.ActivityMapsBinding
import com.dicoding.dicodingstoryapplication.model.UserPreference
import com.dicoding.dicodingstoryapplication.view.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
    }

    // Override onMapReady, add each stories location
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle()
        getMyLocation()
    }

    // Override back menu button
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    // Basic view setup
    private fun setupView() {
        val actionBar = supportActionBar
        actionBar?.title = "All Stories Location"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // ViewModel setup
    private fun setupViewModel() {
        mapViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore), this)
        )[MapViewModel::class.java]

        mapViewModel.getUser().observe(this) { user ->
            mapViewModel.getAllStories("Bearer " + user.token)
        }

        mapViewModel.location.observe(this) { stories ->
            setLocation(stories)
        }
    }

    // Set map style
    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style: ", exception)
        }
    }

    // Set stories location
    private fun setLocation(stories: List<ListStoryItem>) {
        stories.forEach { story ->
            if (story.lat != null && story.lon != null) {
                val latLng = LatLng(story.lat, story.lon)
                val addressName = getAddressName(story.lat, story.lon)
                val markerOptions =
                    MarkerOptions()
                    .position(latLng)
                    .title("Uploaded by " + story.name)
                    .snippet(addressName)
                mMap.addMarker(
                    markerOptions
                )
            }
        }

        // Set Indonesia as main camera view
        val indonesia = LatLng(-0.7893, 118.9213)
        val zoomLevel = 3.5f
        val cameraPosition = CameraPosition.Builder().target(indonesia).zoom(zoomLevel).build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    // Get Address Name
    private fun getAddressName(lat: Double, lon: Double): String? {
        var addressName: String? = null
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (list != null && list.size != 0) {
                addressName = list[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressName
    }

    // Get current location
    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            Toast.makeText(this, "Presice location is not enabled.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}