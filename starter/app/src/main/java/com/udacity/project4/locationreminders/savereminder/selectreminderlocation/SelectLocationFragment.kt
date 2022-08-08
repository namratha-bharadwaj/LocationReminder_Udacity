package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import java.util.*
import org.koin.android.ext.android.bind
import org.koin.android.ext.android.inject

private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val TAG = "LocationFragment"

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var lastKnownLocation: Location? = null
    private val GEOFENCE_RADIUS_METERS = 200f
    private var latitudeLongitude: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveLocationBtn.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        latitudeLongitude?.let { latLng ->
            binding.viewModel?.latitude?.value = latLng.latitude
            binding.viewModel?.longitude?.value = latLng.longitude
            requireActivity().onBackPressed()
        } ?: run {
            Toast.makeText(context, "Please select a location to save", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val zoomLevel = 15f

        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            @SuppressLint("MissingPermission")
            override fun onLocationChanged(location: Location) {
                map.isMyLocationEnabled = true

                val userLocation = LatLng(location.latitude, location.longitude)
                latitudeLongitude = userLocation
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        userLocation, zoomLevel
                    )
                )

            }

            override fun onStatusChanged(provider: String?, status: Int, bundle: Bundle?) {
                // Do nothing
            }

            override fun onProviderEnabled(provider: String?) {
                // Do nothing
            }

            override fun onProviderDisabled(provider: String?) {
                // Do nothing
            }

        }

        if (foregroundLocationPermissionApproved()) {
            moveCameraToLastKnownLocation(locationListener)
        } else {
            requestLocationPermissions()
        }
        setMapLongClick()
        setPoiClick()
        setMapStyle()
    }

    private fun setMapLongClick() {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val longClickMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
            )
            longClickMarker.showInfoWindow()
            addCircle(latLng, GEOFENCE_RADIUS_METERS)
            latitudeLongitude = latLng
            binding.viewModel?.reminderSelectedLocationStr?.value = getString(R.string.dropped_pin)

        }
    }

    private fun setPoiClick() {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
            addCircle(poi.latLng, GEOFENCE_RADIUS_METERS)
            latitudeLongitude = poi.latLng
            binding.viewModel?.reminderSelectedLocationStr?.value = poi.name
        }
    }

    private fun addCircle(latLng: LatLng, radius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
        circleOptions.strokeWidth(4f)
        map.addCircle(circleOptions)
    }

    private fun setMapStyle() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Cant find style. Error: ", e)
        }
    }

    private fun foregroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = PackageManager.PERMISSION_GRANTED ==
                context?.let {
                    ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
                }
        return foregroundLocationApproved
    }

    private fun requestLocationPermissions() {
        if (foregroundLocationPermissionApproved()) {
            return
        }
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        Log.d(TAG, "Request foreground only location perm")
        requestPermissions(permissionsArray, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                moveCameraToLastKnownLocation(locationListener)
        } else {
                Snackbar.make(
                    binding.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        startActivityForResult(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", requireContext().packageName, null)
                        }, REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE)
                    }.show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> {
                // act accordingly
                Log.i(this.javaClass.simpleName, "after requesting foregroung permissions")
                moveCameraToLastKnownLocation(locationListener)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToLastKnownLocation(locationListener: LocationListener) {
        map.isMyLocationEnabled = true
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
        )
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastKnownLocation?.let { lastLocation ->
                map.isMyLocationEnabled = true
                val userLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
            }
    }
}
