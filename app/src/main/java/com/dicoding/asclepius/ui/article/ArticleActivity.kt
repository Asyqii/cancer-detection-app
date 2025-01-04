 package com.dicoding.asclepius.ui.article

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.asclepius.data.response.ArticleResponse
import com.dicoding.asclepius.data.retrofit.ApiConfig
import com.dicoding.asclepius.databinding.ActivityArticleBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences
import androidx.activity.viewModels


 class ArticleActivity : AppCompatActivity() {

     private lateinit var binding: ActivityArticleBinding
     private lateinit var sharedPreferences: SharedPreferences
     private val articleViewModel: ArticleViewModel by viewModels()

     companion object {
         private const val TAG = "MainActivity"
         private const val ARTICLE_INDEX_KEY = "ARTICLE_INDEX_KEY"
     }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         binding = ActivityArticleBinding.inflate(layoutInflater)
         setContentView(binding.root)

         sharedPreferences = getSharedPreferences("ArticlePreferences", Context.MODE_PRIVATE)

         binding.toolbar.setNavigationOnClickListener {
             onBackPressedDispatcher.onBackPressed()
         }

         if (articleViewModel.articles.isEmpty()) {
             findArticle()
         } else {
             showArticle(articleViewModel.currentIndex)
         }
     }

     private fun findArticle() {
         hideView()
         val client = ApiConfig.getApiService().getArticle()
         client.enqueue(object : Callback<ArticleResponse> {
             override fun onResponse(call: Call<ArticleResponse>, response: Response<ArticleResponse>) {
                 if (response.isSuccessful) {
                     showView()
                     val responseBody = response.body()
                     if (responseBody != null) {
                         articleViewModel.articles = responseBody.articles ?: emptyList()

                         if (articleViewModel.articles.isNotEmpty()) {
                             val lastIndex = sharedPreferences.getInt(ARTICLE_INDEX_KEY, 0)
                             articleViewModel.currentIndex = lastIndex % articleViewModel.articles.size

                             showArticle(articleViewModel.currentIndex)

                             sharedPreferences.edit()
                                 .putInt(ARTICLE_INDEX_KEY, articleViewModel.currentIndex + 1)
                                 .apply()
                         } else {
                             Log.e(TAG, "Daftar artikel kosong")
                         }
                     }
                 } else {
                     Log.e(TAG, "onFailure: ${response.message()}")
                 }
             }

             override fun onFailure(call: Call<ArticleResponse>, t: Throwable) {
                 showView()
                 Log.e(TAG, "onFailure: ${t.message}")
             }
         })
     }

     private fun showArticle(index: Int) {
         val article = articleViewModel.articles[index]

         if (article?.author == null && article?.title?.isEmpty() == true) {
             binding.dataNotFound.visibility = View.VISIBLE
             hideView()
         } else {
             binding.title.text = article?.title
             binding.edtAuthor.text = article?.author.toString()
             binding.description.text = article?.description
             Glide.with(this@ArticleActivity)
                 .load(article?.urlToImage)
                 .into(binding.imgResult)
         }
     }

     private fun hideView() {
         binding.imgResult.visibility = View.GONE
         binding.author.visibility = View.GONE
         binding.edtAuthor.visibility = View.GONE
         binding.title.visibility = View.GONE
         binding.description.visibility = View.GONE
         binding.loading.visibility = View.VISIBLE
     }

     private fun showView() {
         binding.imgResult.visibility = View.VISIBLE
         binding.author.visibility = View.VISIBLE
         binding.edtAuthor.visibility = View.VISIBLE
         binding.title.visibility = View.VISIBLE
         binding.description.visibility = View.VISIBLE
         binding.loading.visibility = View.GONE
     }
 }