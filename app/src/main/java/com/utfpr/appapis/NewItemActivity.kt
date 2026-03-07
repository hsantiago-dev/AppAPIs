package com.utfpr.appapis

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.utfpr.appapis.databinding.ActivityNewItemBinding
import com.utfpr.appapis.model.ItemLocation
import com.utfpr.appapis.model.ItemValue
import com.utfpr.appapis.service.Result
import com.utfpr.appapis.service.RetrofitClient
import com.utfpr.appapis.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        fun newIntent(context: Context): Intent {
            return Intent(context, NewItemActivity::class.java)
        }
    }

    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var selectedMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        requestLocationPermission()
        setupGoogleMaps()
        setupWindowInsets()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationPermission() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            it?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        binding.mapContent.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    loadCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }
    private fun getDeviceLocation() {
        // verificar permissões e obter localização do dispositivo
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            loadCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.saveCta.setOnClickListener { saveItem() }
    }

    private fun setupGoogleMaps() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun saveItem() {
        if (!validateForms()) return
        val name = binding.name.text.toString()
        val itemPosition = selectedMarker?.position?.let {
            ItemLocation(
                name = name,
                latitude = it.latitude,
                longitude = it.longitude
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()

            val itemValue = ItemValue(
                id = id,
                name = name,
                surname = binding.surname.text.toString(),
                profession = binding.profession.text.toString(),
                imageUrl = binding.imageUrl.text.toString(),
                age = binding.age.text.toString().toInt(),
                location = itemPosition
            )

            val result = safeApiCall {
                RetrofitClient.apiService.addItem(itemValue)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> handleOnSuccess()
                    is Result.Error -> handleOnError()
                }
            }
        }
    }

    private fun handleOnError() {
        Toast.makeText(
            this,
            getString(R.string.error_add_item),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(
            this,
            getString(R.string.item_added_successfully),
            Toast.LENGTH_SHORT
        )
        finish()
    }

    private fun validateForms(): Boolean {
        var hasError = false
        if (binding.name.text.isNullOrBlank()) {
            binding.name.error = getString(R.string.required_field)
            hasError = true
        }

        if (binding.surname.text.isNullOrBlank()) {
            binding.surname.error = getString(R.string.required_field)
            hasError = true
        }

        if (binding.age.text.isNullOrBlank()) {
            binding.age.error = getString(R.string.required_field)
            hasError = true
        }

        if (binding.imageUrl.text.isNullOrBlank()) {
            binding.imageUrl.error = getString(R.string.required_field)
            hasError = true
        }

        if (binding.profession.text.isNullOrBlank()) {
            binding.profession.error = getString(R.string.required_field)
            hasError = true
        }

        return !hasError
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}