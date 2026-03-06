package com.utfpr.appapis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.utfpr.appapis.databinding.ActivityItemDetailBinding
import com.utfpr.appapis.model.Item
import com.utfpr.appapis.service.Result
import com.utfpr.appapis.service.RetrofitClient
import com.utfpr.appapis.service.safeApiCall
import com.utfpr.appapis.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var item: Item
    private lateinit var mMap: GoogleMap

    companion object {
        const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupView()
        fetchItem()
        setupGoogleMaps()
    }

    private fun setupGoogleMaps() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun fetchItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.getItem(itemId)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        item = result.data
                        handleSuccess()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity,
                            getString(R.string.error_fetching_item), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.name.text = item.value.fullName
        binding.age.text = getString(R.string.item_age, item.value.age)
        binding.profession.setText(item.value.profession)
        binding.image.loadUrl(item.value.imageUrl)
        loadItemInGoogleMaps()
    }

    private fun loadItemInGoogleMaps() {
        if (!this::mMap.isInitialized) return
        mMap.clear()
        item.value.location?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val location = LatLng(it.latitude, it.longitude)
            mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(it.name)
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.deleteItem(item.id)
            }

            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Success -> {
                        handleSuccessDelete()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            getString(R.string.error_deleting_item),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun handleSuccessDelete() {
        Toast.makeText(
            this,
            getString(R.string.item_deleted_successfully),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
        binding.editCTA.setOnClickListener {
            editItem()
        }
    }

    private fun editItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateItem(
                    item.id,
                    item.value.copy(profession = binding.profession.text.toString())
                )
            }

            withContext(Dispatchers.Main) {
                when(result) {
                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            getString(R.string.item_edited_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            getString(R.string.error_editing_item),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        if (::item.isInitialized) {
            loadItemInGoogleMaps()
        }
    }
}