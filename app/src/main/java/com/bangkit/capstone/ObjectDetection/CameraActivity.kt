package com.bangkit.capstone.ObjectDetection

import android.Manifest
import android.content.pm.PackageManager
import android.media.ImageReader
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.bangkit.capstone.ObjectDetection.fragment.CameraConnectionFragment2
import com.bangkit.capstone.ObjectDetection.model.OverlayView
import com.bangkit.capstone.R

abstract class CameraActivity : AppCompatActivity(), ImageReader.OnImageAvailableListener,
    CameraConnectionFragment2.ConnectionListener {
    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null
    private val sfx: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera)
        if (hasPermission()) {
            setFragment()
        } else {
            requestPermission()
        }
    }

    @Synchronized
    public override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread("inference")
        handlerThread!!.start()
        handler = Handler(handlerThread!!.looper)
    }

    @Synchronized
    public override fun onPause() {
        if (!isFinishing) {
            finish()
        }
        handlerThread!!.quitSafely()
        try {
            handlerThread!!.join()
            handlerThread = null
            handler = null
        } catch (ex: InterruptedException) {
            Log.e(LOGGING_TAG, "Exception: " + ex.message)
        }
        super.onPause()
    }

    @Synchronized
    protected fun runInBackground(runnable: Runnable?) {
        if (handler != null) {
            handler!!.postDelayed({ handler!!.post(runnable!!) }, 3000)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setFragment()
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ) {
                Toast.makeText(
                    this@CameraActivity,
                    "Camera AND storage permission are required for this app", Toast.LENGTH_LONG
                ).show()
            }
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), PERMISSIONS_REQUEST
            )
        }
    }

    private fun setFragment() {
        val cameraConnectionFragment = CameraConnectionFragment2()
        cameraConnectionFragment.addConnectionListener(this)


        cameraConnectionFragment.addImageAvailableListener(this)
        fragmentManager
            .beginTransaction()
            .replace(R.id.container, cameraConnectionFragment)
            .commit()
    }

    fun requestRender() {
        val overlay: OverlayView = findViewById<View>(R.id.overlay) as OverlayView
        overlay.postInvalidate()
    }

    fun addCallback(callback: OverlayView.DrawCallback?) {
        val overlay: OverlayView = findViewById<View>(R.id.overlay) as OverlayView
        overlay.addCallback(callback)
    }

    abstract override fun onPreviewSizeChosen(size: Size?, cameraRotation: Int)

    companion object {
        private const val LOGGING_TAG = "objdetector"
        private const val PERMISSIONS_REQUEST = 1
    }
}