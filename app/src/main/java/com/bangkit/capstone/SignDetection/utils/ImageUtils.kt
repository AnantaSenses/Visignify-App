package com.bangkit.capstone.SignDetection.utils

import android.graphics.Matrix
import android.media.Image
import android.media.Image.Plane

class ImageUtils {

    companion object {
        fun getTransformationMatrix(
            srcWidth: Int, srcHeight: Int,
            dstWidth: Int, dstHeight: Int,
            applyRotation: Int, maintainAspectRatio: Boolean
        ): Matrix {
            val matrix = Matrix()
            if (applyRotation != 0) {
                // Translate so center of image is at origin.
                matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

                // Rotate around origin.
                matrix.postRotate(applyRotation.toFloat())
            }

            // Account for the already applied rotation, if any, and then determine how
            // much scaling is needed for each axis.
            val transpose = (Math.abs(applyRotation) + 90) % 180 == 0
            val inWidth = if (transpose) srcHeight else srcWidth
            val inHeight = if (transpose) srcWidth else srcHeight

            // Apply scaling if necessary.
            if (inWidth != dstWidth || inHeight != dstHeight) {
                val scaleFactorX = dstWidth / inWidth.toFloat()
                val scaleFactorY = dstHeight / inHeight.toFloat()
                if (maintainAspectRatio) {
                    // Scale by minimum factor so that dst is filled completely while
                    // maintaining the aspect ratio. Some image may fall off the edge.
                    val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
                    matrix.postScale(scaleFactor, scaleFactor)
                } else {
                    // Scale exactly to fill dst from src.
                    matrix.postScale(scaleFactorX, scaleFactorY)
                }
            }
            if (applyRotation != 0) {
                // Translate back from origin centered reference to destination frame.
                matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
            }
            return matrix
        }

        fun convertYUVToARGB(image: Image, previewWidth: Int, previewHeight: Int): IntArray {
            val planes = image.planes
            val yuvBytes = fillBytes(planes)
            return convertYUV420ToARGB8888(
                yuvBytes[0], yuvBytes[1], yuvBytes[2], previewWidth,
                previewHeight, planes[0].rowStride, planes[1].rowStride, planes[1].pixelStride
            )
        }

        private fun fillBytes(planes: Array<Plane>): Array<ByteArray?> {
            val yuvBytes = arrayOfNulls<ByteArray>(3)
            for (i in planes.indices) {
                val buffer = planes[i].buffer
                if (yuvBytes[i] == null) {
                    yuvBytes[i] = ByteArray(buffer.capacity())
                }
                buffer[yuvBytes[i]]
            }
            return yuvBytes
        }

        private fun convertYUV420ToARGB8888(
            yData: ByteArray?, uData: ByteArray?, vData: ByteArray?, width: Int, height: Int,
            yRowStride: Int, uvRowStride: Int, uvPixelStride: Int
        ): IntArray {
            val out = IntArray(width * height)
            var i = 0
            for (y in 0 until height) {
                val pY = yRowStride * y
                val uv_row_start = uvRowStride * (y shr 1)
                for (x in 0 until width) {
                    val uv_offset = (x shr 1) * uvPixelStride
                    out[i++] = YUV2RGB(
                        convertByteToInt(yData, pY + x),
                        convertByteToInt(uData, uv_row_start + uv_offset),
                        convertByteToInt(vData, uv_row_start + uv_offset)
                    )
                }
            }
            return out
        }

        private fun convertByteToInt(arr: ByteArray?, pos: Int): Int {
            return arr!![pos].toInt() and 0xFF
        }

        private fun YUV2RGB(nY: Int, nU: Int, nV: Int): Int {
            var nY = nY
            var nU = nU
            var nV = nV
            val kMaxChannelValue = 262143
            nY -= 16
            nU -= 128
            nV -= 128
            if (nY < 0) nY = 0
            var nR = 1192 * nY + 1634 * nV
            var nG = 1192 * nY - 833 * nV - 400 * nU
            var nB = 1192 * nY + 2066 * nU
            nR = Math.min(kMaxChannelValue, Math.max(0, nR))
            nG = Math.min(kMaxChannelValue, Math.max(0, nG))
            nB = Math.min(kMaxChannelValue, Math.max(0, nB))
            nR = nR shr 10 and 0xff
            nG = nG shr 10 and 0xff
            nB = nB shr 10 and 0xff
            return -0x1000000 or (nR shl 16) or (nG shl 8) or nB
        }
    }
}