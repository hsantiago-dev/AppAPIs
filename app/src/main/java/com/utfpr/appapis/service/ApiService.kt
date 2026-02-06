package com.utfpr.appapis.service

import com.utfpr.appapis.model.Item
import retrofit2.http.GET

interface ApiService {
    @GET("/items")
    suspend fun getItems(): List<Item>
}