package com.dicoding.asclepius.repository

import androidx.lifecycle.LiveData
import com.dicoding.asclepius.database.History
import com.dicoding.asclepius.database.HistoryDao


class HistoryRepository(private val dao: HistoryDao) {
    suspend fun insertHistory(history: History) {
        dao.insert(history)
    }

    suspend fun getAllHistory(): List<History> {
        return dao.getAllHistory()
    }

    suspend fun deleteAllHistory() {
        dao.deleteAllHistory()
    }
}