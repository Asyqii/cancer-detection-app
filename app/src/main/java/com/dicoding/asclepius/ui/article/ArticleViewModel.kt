package com.dicoding.asclepius.ui.article

import androidx.lifecycle.ViewModel
import com.dicoding.asclepius.data.response.ArticlesItem

class ArticleViewModel : ViewModel() {
    var articles: List<ArticlesItem?> = emptyList()
    var currentIndex: Int = 0
}