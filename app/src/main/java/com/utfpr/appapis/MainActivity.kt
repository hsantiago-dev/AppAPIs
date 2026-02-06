package com.utfpr.appapis

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.utfpr.appapis.adapter.ItemAdapter
import com.utfpr.appapis.databinding.ActivityMainBinding
import com.utfpr.appapis.model.Item
import com.utfpr.appapis.service.RetrofitClient
import com.utfpr.appapis.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.utfpr.appapis.service.Result

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItens()
    }

    private fun setupView() {
        binding.main.setOnRefreshListener {

        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.addCta.setOnClickListener {

        }
    }

    private fun fetchItens() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.getItems()
            }

            withContext(Dispatchers.Main) {
                binding.main.isRefreshing = false
                when (result) {
                    is Result.Success -> handleOnSuccess(result.data)
                    is Result.Error -> {
                        // Handle error
                    }
                }
            }
        }
    }

    private fun handleOnSuccess(items: List<Item>) {
        binding.recyclerView.adapter = ItemAdapter(items)

    }
}