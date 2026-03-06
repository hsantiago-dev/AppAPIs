package com.utfpr.appapis.service

import com.utfpr.appapis.model.Item
import com.utfpr.appapis.model.ItemValue
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ApiService {
    @GET("/items")
    suspend fun getItems(): List<Item>

    @GET("/items/{id}")
    suspend fun getItem(@Path("id") id: String): Item

    @DELETE("/items/{id}")
    suspend fun deleteItem(@Path("id") id: String): Item

    @PATCH("/items/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: ItemValue): Item
}