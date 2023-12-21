package com.bangkit.capstone.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.Toast
import com.bangkit.capstone.ObjectDetection.CameraActivity
import com.bangkit.capstone.ObjectDetection.model.DetectionResult
import com.bangkit.capstone.ObjectDetection.model.MobileNetObjDetector
import com.bangkit.capstone.ObjectDetection.model.OverlayView
import com.bangkit.capstone.ObjectDetection.utils.ImageUtils
import com.bangkit.capstone.R
import java.io.IOException
import java.util.Locale


class ObjectDetectionActivity : CameraActivity(), OnImageAvailableListener {
    var textToSpeech: TextToSpeech? = null
    private var sensorOrientation: Int? = null
    private var previewWidth = 0
    private var previewHeight = 0
    private var frameLayout: FrameLayout? = null
    private var objectDetector: MobileNetObjDetector? = null
    private var imageBitmapForModel: Bitmap? = null
    private var rgbBitmapForCameraImage: Bitmap? = null
    private var computing = false
    private var imageTransformMatrix: Matrix? = null
    private var overlayView: OverlayView? = null
    override fun onPreviewSizeChosen(previewSize: Size?, rotation: Int) {
        val textSizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TEXT_SIZE_DIP, resources.displayMetrics
        )
        try {
            objectDetector = MobileNetObjDetector.create(assets)
            Log.i(LOGGING_TAG, "Model Initiated successfully.")
            Toast.makeText(
                applicationContext,
                "MobileNetObjDetector created",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                applicationContext,
                "MobileNetObjDetector could not be created",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        overlayView = findViewById<OverlayView>(R.id.overlay)
        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech!!.language = Locale.US
                textToSpeech!!.speak("welcome to object detection", TextToSpeech.QUEUE_FLUSH, null)
            }
        }
        frameLayout = findViewById(R.id.container)
        frameLayout!!.setOnLongClickListener {
            finish()
            val i = Intent(applicationContext, HomeBlindActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            false
        }
        val screenOrientation: Int = getWindowManager().getDefaultDisplay().rotation
        //Sensor orientation: 90, Screen orientation: 0
        sensorOrientation = rotation + screenOrientation
        Log.i(
            LOGGING_TAG, String.format(
                "Camera rotation: %d, Screen orientation: %d, Sensor orientation: %d",
                rotation, screenOrientation, sensorOrientation
            )
        )
        if (previewSize != null) {
            previewWidth = previewSize.width
        }
        if (previewSize != null) {
            previewHeight = previewSize.height
        }
        Log.i(LOGGING_TAG, "preview width: $previewWidth")
        Log.i(LOGGING_TAG, "preview height: $previewHeight")
        // create empty bitmap
        imageBitmapForModel = Bitmap.createBitmap(
            MODEL_IMAGE_INPUT_SIZE,
            MODEL_IMAGE_INPUT_SIZE,
            Bitmap.Config.ARGB_8888
        )
        rgbBitmapForCameraImage =
            Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        imageTransformMatrix = ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            MODEL_IMAGE_INPUT_SIZE, MODEL_IMAGE_INPUT_SIZE, sensorOrientation!!, true
        )
        imageTransformMatrix!!.invert(Matrix())
    }

    override fun onImageAvailable(reader: ImageReader) {
        var imageFromCamera: Image? = null
        try {
            imageFromCamera = reader.acquireLatestImage()
            if (imageFromCamera == null) {
                return
            }
            if (computing) {
                imageFromCamera.close()
                return
            }
            computing = true
            preprocessImageForModel(imageFromCamera)
            imageFromCamera.close()
        } catch (ex: Exception) {
            imageFromCamera?.close()
            Log.e(LOGGING_TAG, ex.message!!)
        }
        runInBackground {
            val results: List<DetectionResult>? =
                imageBitmapForModel?.let { objectDetector?.detectObjects(it)}
            overlayView?.setResults(results)
            if (results != null) {
                if (results.isNotEmpty()) {
                    val title: String? = results[0]?.getTitle()
                    textToSpeech!!.speak(title.toString(), TextToSpeech.QUEUE_FLUSH, null)
                    Toast.makeText(this@ObjectDetectionActivity, results[0].toString(), Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "move forward object is not present.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            requestRender()
            computing = false
        }
    }


    private fun preprocessImageForModel(imageFromCamera: Image) {
        rgbBitmapForCameraImage!!.setPixels(
            ImageUtils.convertYUVToARGB(imageFromCamera, previewWidth, previewHeight),
            0, previewWidth, 0, 0, previewWidth, previewHeight
        )
        Canvas(imageBitmapForModel!!).drawBitmap(
            rgbBitmapForCameraImage!!,
            imageTransformMatrix!!, null
        )
    }

    protected override fun onDestroy() {
        super.onDestroy()
        objectDetector?.close()
    }

    override fun onPause() {
        super.onPause()
        if (textToSpeech != null) {
            textToSpeech!!.stop()
        }
    }



    companion object {
        private const val MODEL_IMAGE_INPUT_SIZE = 300
        private val LOGGING_TAG = ObjectDetectionActivity::class.java.name
        private const val TEXT_SIZE_DIP = 10f
    }
}