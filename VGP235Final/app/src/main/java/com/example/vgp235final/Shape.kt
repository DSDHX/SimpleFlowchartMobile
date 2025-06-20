package com.example.vgp235final

import android.graphics.RectF

data class Shape (
    var id: Int,
    var type: ShapeType,
    var rectF: RectF,
//    var rotationDegrees: Float = 0f
){
    fun isHit(x: Float, y: Float): Boolean {
        return rectF.contains(x, y)
    }
}

enum class ShapeType
{
    RECTANGLE,
    CIRCLE
}