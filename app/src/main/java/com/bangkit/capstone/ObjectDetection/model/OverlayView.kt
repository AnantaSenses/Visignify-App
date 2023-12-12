package com.bangkit.capstone.ObjectDetection.model

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.LinkedList

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint: Paint
    private val callbacks: MutableList<DrawCallback?> = LinkedList<DrawCallback?>()
    private var results: List<DetectionResult>? = null
    private val colors: List<Int>? = null
    private val resultsViewHeight: Float

    init {
        paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f, resources.displayMetrics
        )
        resultsViewHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            112f, resources.displayMetrics
        )
    }

    fun addCallback(callback: DrawCallback?) {
        callbacks.add(callback)
    }

    @Synchronized
    public override fun onDraw(canvas: Canvas) {
        for (callback in callbacks) {
            callback!!.drawCallback(canvas)
        }
        if (results != null) {
            for (i in results!!.indices) {
                if (results!![i].getConfidence() > 0.5) {
                    val box = reCalcSize(results!![i].getLocation())
                    val title: String = results!![i].getTitle() + java.lang.String.format(
                        " %2.2f",
                        results!![i].getConfidence() * 100
                    ) + "%"
                    paint.color = Color.RED
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(box, paint)
                    paint.strokeWidth = 2.0f
                    paint.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawText(title, box.left, box.top, paint)
                }
            }
        }
    }

    fun setResults(results: List<DetectionResult>?) {
        this.results = results
        postInvalidate()
    }

    interface DrawCallback {
        fun drawCallback(canvas: Canvas?)
    }

    private fun reCalcSize(rect: RectF): RectF {
        val padding = 5
        val overlayViewHeight = height - resultsViewHeight
        val sizeMultiplier = Math.min(
            width.toFloat() / INPUT_SIZE.toFloat(),
            overlayViewHeight / INPUT_SIZE.toFloat()
        )
        val offsetX =
            (width - INPUT_SIZE * sizeMultiplier) / 2
        val offsetY =
            (overlayViewHeight - INPUT_SIZE * sizeMultiplier) / 2 + resultsViewHeight
        val left =
            Math.max(padding.toFloat(), sizeMultiplier * rect.left + offsetX)
        val top =
            Math.max(offsetY + padding, sizeMultiplier * rect.top + offsetY)
        val right =
            Math.min(rect.right * sizeMultiplier, (width - padding).toFloat())
        val bottom = Math.min(
            rect.bottom * sizeMultiplier + offsetY,
            (height - padding).toFloat()
        )
        return RectF(left, top, right, bottom)
    }

    companion object {
        private const val INPUT_SIZE = 300
    }
}