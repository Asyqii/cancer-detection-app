package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import androidx.camera.core.ImageProxy
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import kotlin.math.log


class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResult: Int = 3,
    var modelName: String = "cancer_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {

    private var imageClassifier: ImageClassifier? = null

    interface ClassifierListener {
        fun onError(err: String)
        fun onResults(
            results: List<Classifications>?,
            inferenceTime: Long
        )
    }

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResult)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
       if (imageClassifier == null) {
           setupImageClassifier()
       }


        val bitmap = toBitmap(imageUri)
        if (bitmap != null) {

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(500, 500, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .add(CastOp(DataType.FLOAT32))
                .build()

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

            // Mulai klasifikasi dan hitung waktu inferensi
            val inferenceTime = SystemClock.uptimeMillis()
            val results = imageClassifier?.classify(tensorImage)
            val elapsedInferenceTime = SystemClock.uptimeMillis() - inferenceTime

            // Kirim hasil ke listener
            classifierListener?.onResults(results, elapsedInferenceTime)
        } else {
            Log.e("Image Classifier Helper", "Gagal konversi ke bitmap")
        }

    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }

    private fun toBitmap(imageUri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        const val TAG = "ImageClassifierHelper"
    }

}