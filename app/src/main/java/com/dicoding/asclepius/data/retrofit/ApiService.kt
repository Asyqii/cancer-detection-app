package com.dicoding.asclepius.data.retrofit

import com.dicoding.asclepius.data.response.ArticleResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("top-headlines")
    fun getArticle(
        @Query("q") query: String = "cancer",
        @Query("category") category: String = "health",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String = "e1b35183d84343508202d5a837453db4"
    ): Call<ArticleResponse>
}