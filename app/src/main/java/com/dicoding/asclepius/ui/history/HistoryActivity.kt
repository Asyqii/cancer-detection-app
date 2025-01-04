package com.dicoding.asclepius.ui.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.database.HistoryRoomDatabase
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.repository.HistoryRepository

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = binding.rvHistory
        recyclerView.layoutManager = LinearLayoutManager(this)

            val dao = HistoryRoomDatabase.getDatabase(this).historyDao()
            val repository = HistoryRepository(dao)
            historyViewModel = HistoryViewModel(repository)

            historyViewModel.getAllHistory { historyList ->

                if (historyList.isEmpty()) {
                    binding.historyNotFound.visibility = View.VISIBLE
                } else {
                    adapter = HistoryAdapter(historyList)
                    recyclerView.adapter = adapter
                }

            }
 
        binding.toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

    }
}