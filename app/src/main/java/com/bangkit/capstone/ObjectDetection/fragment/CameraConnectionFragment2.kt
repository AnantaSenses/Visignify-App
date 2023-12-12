package com.bangkit.capstone.ObjectDetection.fragment

import android.Manifest
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bangkit.capstone.ObjectDetection.model.AutoFitTextureView
import com.bangkit.capstone.ObjectDetection.utils.ErrorDialog
import com.bangkit.capstone.R
import java.util.Arrays
import java.util.Collections
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class CameraConnectionFragment2() : Fragment() {
    private val cameraOpenCloseLock = Semaphore(1)
    private var imageListener: OnImageAvailableListener? = null
    private var cameraConnectionListener: ConnectionListener? = null
    private var cameraId: String? = null
    private var textureView: AutoFitTextureView? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var sensorOrientation: Int? = null
    private var previewSize: Size? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var previewReader: ImageReader? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var previewRequest: CaptureRequest? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textureView = view.findViewById<View>(R.id.texture) as AutoFitTextureView
        super.onViewCreated(view, savedInstanceState)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera(textureView!!.width, textureView!!.height)
        } else {
            textureView!!.surfaceTextureListener = object : SurfaceTextureListener {
                @RequiresApi(api = Build.VERSION_CODES.M)
                override fun onSurfaceTextureAvailable(
                    texture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    openCamera(width, height)
                }

                override fun onSurfaceTextureSizeChanged(
                    texture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    configureTransform(width, height)
                }

                override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
                    return true
                }

                override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
            }
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    fun addConnectionListener(cameraConnectionListener: ConnectionListener?) {
        this.cameraConnectionListener = cameraConnectionListener
    }

    fun addImageAvailableListener(imageListener: OnImageAvailableListener?) {
        this.imageListener = imageListener
    }

    interface ConnectionListener {
        fun onPreviewSizeChosen(size: Size?, cameraRotation: Int)
    }

    private fun showToast(text: String) {
        val activity = activity
        activity?.runOnUiThread(Runnable {
            Toast.makeText(
                activity,
                text,
                Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun setUpCameraOutputs() {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId: String in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                Log.i(
                    LOGGING_TAG,
                    "Sensor Orientation: $sensorOrientation"
                )

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(
                        SurfaceTexture::class.java
                    )
                )


                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = resources.configuration.orientation
                Log.i(
                    LOGGING_TAG,
                    "Resource Orientation: $orientation"
                )
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView!!.setAspectRatio(previewSize!!.width, previewSize!!.height)
                } else {
                    textureView!!.setAspectRatio(previewSize!!.height, previewSize!!.width)
                }
                this.cameraId = cameraId
            }
        } catch (ex: CameraAccessException) {
            Log.e(LOGGING_TAG, "Exception: " + ex.message)
        } catch (ex: NullPointerException) {
            ErrorDialog.newInstance(getString(R.string.camera_error))
                .show(childFragmentManager, FRAGMENT_DIALOG)
            throw RuntimeException(getString(R.string.camera_error))
        }
        cameraConnectionListener!!.onPreviewSizeChosen(previewSize, sensorOrientation!!)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs()
        configureTransform(width, height)
        val activity = activity
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                    override fun onOpened(cameraDevice: CameraDevice) {
                        // This method is called when the camera is opened.  We start camera preview here.
                        cameraOpenCloseLock.release()
                        this@CameraConnectionFragment2.cameraDevice = cameraDevice
                        createCameraPreviewSession()
                    }

                    override fun onDisconnected(cameraDevice: CameraDevice) {
                        cameraOpenCloseLock.release()
                        cameraDevice.close()
                        this@CameraConnectionFragment2.cameraDevice = null
                    }

                    override fun onError(cameraDevice: CameraDevice, error: Int) {
                        cameraOpenCloseLock.release()
                        cameraDevice.close()
                        this@CameraConnectionFragment2.cameraDevice = null
                        val activity = getActivity()
                        activity?.finish()
                    }
                }, backgroundHandler)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
        } catch (ex: CameraAccessException) {
            Log.e(LOGGING_TAG, "Exception: " + ex.message)
        } catch (ex: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", ex)
        }
    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            if (null != captureSession) {
                captureSession!!.close()
                captureSession = null
            }
            if (null != cameraDevice) {
                cameraDevice!!.close()
                cameraDevice = null
            }
            if (null != previewReader) {
                previewReader!!.close()
                previewReader = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("ImageListener")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (ex: InterruptedException) {
            Log.e(LOGGING_TAG, "Exception: " + ex.message)
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView!!.surfaceTexture
            assert(texture != null)

            // We configure the size of default buffer to be the size of camera preview we want.
            texture!!.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder!!.addTarget(surface)
            Log.i(
                LOGGING_TAG, String.format(
                    "Opening camera preview: "
                            + previewSize!!.width + "x" + previewSize!!.height
                )
            )

            // Create the reader for the preview frames.
            previewReader = ImageReader.newInstance(
                previewSize!!.width, previewSize!!.height,
                ImageFormat.YUV_420_888, 2
            )
            previewReader!!.setOnImageAvailableListener(imageListener, backgroundHandler)
            previewRequestBuilder!!.addTarget(previewReader!!.surface)

            //fixDeviceCameraOrientation(previewRequestBuilder);

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface, previewReader!!.surface),
                captureSessionStateCallback, null
            )
        } catch (ex: CameraAccessException) {
            Log.e(LOGGING_TAG, "Exception: " + ex.message)
        }
    }

    private val captureSessionStateCallback: CameraCaptureSession.StateCallback
        private get() = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                // The camera is already closed
                if (null == cameraDevice) {
                    return
                }

                // When the session is ready, we start displaying the preview.
                captureSession = cameraCaptureSession
                try {
                    // Auto focus should be continuous for camera preview.
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    // Flash is automatically enabled when necessary.
                    previewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                    )

                    //fixDeviceCameraOrientation(previewRequestBuilder);

                    // Finally, we start displaying the camera preview.
                    previewRequest = previewRequestBuilder!!.build()
                    captureSession!!.setRepeatingRequest(previewRequest!!, null, backgroundHandler)
                } catch (ex: CameraAccessException) {
                    Log.e(LOGGING_TAG, "Exception: " + ex.message)
                }
            }

            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                showToast("Failed")
            }
        }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity = activity
        if ((null == textureView) || (null == previewSize) || (null == activity)) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect =
            RectF(0f, 0f, previewSize!!.height.toFloat(), previewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / previewSize!!.height,
                viewWidth.toFloat() / previewSize!!.width
            )
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView!!.setTransform(matrix)
    }

    private fun fixDeviceCameraOrientation(previewRequestBuilder: CaptureRequest.Builder) {
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        val jpegOrientation = (ORIENTATIONS[deviceRotation] + (sensorOrientation)!! + 270) % 360
        previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation)
    }

    internal class CompareSizesByArea() : Comparator<Size?> {
        override fun compare(p0: Size?, p1: Size?): Int {
            // We cast here to ensure the multiplications won't overflow
            if (p1 != null) {
                if (p0 != null) {
                    return java.lang.Long.signum(
                        (p0.width.toLong() * p0.height
                                - p1.width.toLong() * p1.height)
                    )
                }
            }
            return 0
        }
    }

    companion object {
        private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        private val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        private val ORIENTATIONS = SparseIntArray(4)

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val LOGGING_TAG = CameraConnectionFragment2::class.java.name
        private val DESIRED_PREVIEW_SIZE = Size(screenWidth, screenHeight)
        private val MINIMUM_PREVIEW_SIZE = 500
        private val FRAGMENT_DIALOG = "dialog"
        private fun chooseOptimalSize(choices: Array<Size>): Size? {
            val minSize = Math.max(
                Math.min(
                    DESIRED_PREVIEW_SIZE.width,
                    DESIRED_PREVIEW_SIZE.height
                ), MINIMUM_PREVIEW_SIZE
            )
            Log.i(LOGGING_TAG, "Min size: $minSize")

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough: MutableList<Size?> = ArrayList()
            val tooSmall: ArrayList<Size> = ArrayList<Size>()
            for (option: Size in choices) {
                if ((option == DESIRED_PREVIEW_SIZE)) {
                    return DESIRED_PREVIEW_SIZE
                }
                if (option.height >= minSize && option.width >= minSize) {
                    bigEnough.add(option)
                } else {
                    tooSmall.add(option)
                }
            }
            // Pick the smallest of those, assuming we found any
            val chosenSize = if ((bigEnough.size > 0)) Collections.min(
                bigEnough,
                CompareSizesByArea()
            ) else choices[0]
            Log.i(
                LOGGING_TAG,
                "Desired size: " + DESIRED_PREVIEW_SIZE + ", min size: " + minSize + "x" + minSize
            )
            Log.i(LOGGING_TAG, "Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]")
            Log.i(LOGGING_TAG, "Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]")
            Log.i(
                LOGGING_TAG,
                "Chosen preview size: $chosenSize"
            )

            //return new Size(DESIRED_PREVIEW_SIZE.getHeight(), DESIRED_PREVIEW_SIZE.getWidth());
            return chosenSize
        }
    }
}