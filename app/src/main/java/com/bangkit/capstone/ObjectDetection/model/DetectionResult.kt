package com.bangkit.capstone.ObjectDetection.model

import android.graphics.RectF

class DetectionResult(
    val id: Int, private val title: String,
    private val confidence: Float, private var location: RectF
) : Comparable<DetectionResult?> {

    fun getLocation(): RectF {
        return RectF(location)
    }

    fun setLocation(location: RectF) {
        this.location = location
    }

    override fun compareTo(other: DetectionResult?): Int {
        if (other != null) {
            return other.confidence.compareTo(confidence)
        }
        // If 'other' is null, return a default value (you can choose what makes sense)
        return 0
    }

    override fun toString(): String {
        return "DetectionResult{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", confidence=" + confidence +
                ", location=" + location +
                '}'
    }

    fun getConfidence(): Float {
        return confidence
    }

    fun getTitle(): String {
        return title
    }
}