package com.dicoding.asclepius.ui

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.dicoding.asclepius.database.History
import com.dicoding.asclepius.database.HistoryRoomDatabase
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.DateHelper
import com.dicoding.asclepius.repository.HistoryRepository
import com.dicoding.asclepius.ui.history.HistoryViewModel

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private val _binding get() = binding

    private lateinit var historyViewModel: HistoryViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val result = intent.getStringExtra("classification_result") ?: "Category not found"
        val confidenceScore = intent.getFloatExtra("confidence_score", 0.0f)

        binding.resultText.text = "$result : ${"%.2f".format(confidenceScore * 100)}%"

            val imgPath = intent.getStringExtra("image_path") ?: "Image not found"
            val bitmap = BitmapFactory.decodeFile(imgPath)
         if (bitmap != null) {
            showToast("Bitmap : $bitmap")
            binding.resultImage.setImageBitmap(bitmap)
        } else {
             showToast("Bitmap nya kosong")
         }


        val dao = HistoryRoomDatabase.getDatabase(this).historyDao()
            val repository = HistoryRepository(dao)
            historyViewModel = HistoryViewModel(repository)

            val history = History(
                date = DateHelper.getCurrentDate(),
                result = "$result : ",
                confidenceScore = confidenceScore,
                image = imgPath
            )

            historyViewModel.insert(history)

            binding.toolbar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("classification_result", binding.resultText.text.toString())
        outState.putFloat("confidence_score", intent.getFloatExtra("confidence_score", 0.0f))
        outState.putString("image_path", intent.getStringExtra("image_path"))
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}