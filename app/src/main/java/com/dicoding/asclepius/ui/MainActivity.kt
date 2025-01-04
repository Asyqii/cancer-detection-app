package com.dicoding.asclepius.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.ui.article.ArticleActivity
import com.dicoding.asclepius.ui.history.HistoryActivity
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private var currentImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentImageUri = savedInstanceState?.getParcelable("current_image_uri")
        currentImageUri?.let { showImage() }

        binding.galleryButton.setOnClickListener{
            startGallery()
        }

        binding.analyzeButton.setOnClickListener{
            analyzeImage()
        }

        binding.toolbar.setOnMenuItemClickListener{ menuItem ->
            when (menuItem.itemId) {
                R.id.action_history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_info -> {
                    val intent = Intent(this, ArticleActivity::class.java)
                    startActivity(intent)
                    true
                }
               else -> false
            }

        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable("current_image_uri", currentImageUri)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(null)
            binding.previewImageView.setImageURI(it)

            binding.previewImageView.layoutParams.width = 1000
            binding.previewImageView.layoutParams.height = 3000
            binding.previewImageView.requestLayout()
        }
    }

    private fun analyzeImage() {
        if (currentImageUri == null) {
            showToast("Pilih gambar terlebih dahulu!")
            return
        }

        val classifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(err: String) {
                    showToast("Gagal menampilkan gambar : $err")
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    moveToResult(results)
                }
            }
        )

        classifierHelper.classifyStaticImage(currentImageUri!!)
    }

    private fun moveToResult(results: List<Classifications>?) {
        val intent = Intent(this, ResultActivity::class.java)

        results?.let {
            val classificationResult = it.firstOrNull()?.categories?.firstOrNull()
            val label = classificationResult?.label ?: "Tidak diketahui"
            val confidenceScore = classificationResult?.score ?: 0.0f

            intent.putExtra("classification_result", label)
            intent.putExtra("confidence_score", confidenceScore)
        }

        currentImageUri?.let {
            val bitmap = currentImageUri?.let { uri -> toBitmap(uri) }
            if (bitmap != null) {
                val imagePath = saveBitmapToCache(bitmap)
                showToast("Mengirimkan data image ke result $imagePath")
                intent.putExtra("image_path", imagePath)
            } else {
                showToast("Gagal menampilkan gambar ke bitmap")
            }
        }


        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            startUCrop(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startUCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(16f, 10f)
            .withMaxResultSize(1000, 1000)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            currentImageUri = resultUri
            showImage()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.printStackTrace()
        } else if (resultCode == RESULT_CANCELED && requestCode == UCrop.REQUEST_CROP) {
            currentImageUri = null
            binding.previewImageView.setImageURI(null)
            
        }
    }

    private fun toBitmap(imageUri: Uri): Bitmap? {
        return try {
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): String {
        val cachePath = File(cacheDir, "images")
        cachePath.mkdirs()
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }

        val fileName = "result_image_${System.currentTimeMillis()}.png"
        val file = File(cachePath, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("saveBitmapToCache", "Gagal menyimpan bitmap: ${e.message}")
        }


        return file.absolutePath
    }
}