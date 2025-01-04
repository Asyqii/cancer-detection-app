package com.dicoding.asclepius.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(history: History)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("SELECT * from history ORDER BY id DESC")
    suspend fun getAllHistory(): List<History>

}