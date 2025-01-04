package com.dicoding.asclepius.ui.history


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.database.History
import com.dicoding.asclepius.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    fun insert(history: History) {
       viewModelScope.launch {
           repository.insertHistory(history)
       }
    }
    fun getAllHistory(callback: (List<History>) -> Unit) {
        viewModelScope.launch {
            val historyList = repository.getAllHistory()
            callback(historyList)
        }
    }

    fun delete(history: History) {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }
}