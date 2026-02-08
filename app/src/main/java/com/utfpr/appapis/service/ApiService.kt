package com.utfpr.appapis.service

import com.utfpr.appapis.model.Item
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/items")
    suspend fun getItems(): List<Item>

    @GET("/items/{id}")
    suspend fun getItem(@Path("id") id: String): Item
}