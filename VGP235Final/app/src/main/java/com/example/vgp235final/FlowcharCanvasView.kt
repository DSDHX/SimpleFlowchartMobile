package com.example.vgp235final

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.atan2

class FlowcharCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
    private val shapeFillPaint = Paint().apply { color = Color.parseColor("#E1F5FE"); style = Paint.Style.FILL }
    private val shapeBorderPaint = Paint().apply { color = Color.parseColor("#4285F4"); strokeWidth = 3f; style = Paint.Style.STROKE }
    private val linePaint = Paint().apply { color = Color.parseColor("#F44336"); strokeWidth = 4f; style = Paint.Style.STROKE }

    private val gridSize = 60f

    private val shapes = mutableListOf<Shape>()
    private val connections = mutableListOf<ConnectionLine>()

    private var draggedShape: Shape? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var scaledShape: Shape? = null
    private val scaleGestureDetector: ScaleGestureDetector

    private val gestureDetector: GestureDetector
    private var isLineDrawingMode = false
    private var lineStartShape: Shape? = null
    private var tempLineEndPoint: PointF? = null

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val shape = findShapeAt(e.x, e.y)
                if (shape != null) {
                    isLineDrawingMode = true
                    lineStartShape = shape
                    draggedShape = null
                    tempLineEndPoint = PointF(e.x, e.y)
                    invalidate()
                }
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        drawGrid(canvas)
        drawConnections(canvas)
        drawShapes(canvas)
        drawTemporaryLine(canvas)
    }

    private fun drawShapes(canvas: Canvas) {
        for (shape in shapes) {
            when (shape.type) {
                ShapeType.RECTANGLE -> {
                    canvas.drawRect(shape.rectF, shapeFillPaint)
                    canvas.drawRect(shape.rectF, shapeBorderPaint)
                }
                ShapeType.CIRCLE -> {
                    canvas.drawOval(shape.rectF, shapeFillPaint)
                    canvas.drawOval(shape.rectF, shapeBorderPaint)
                }
            }
        }
    }

    private fun drawGrid(canvas: Canvas) {
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint); x += gridSize
        }
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint); y += gridSize
        }
    }

    private fun drawConnections(canvas: Canvas) {
        for (connection in connections) {
            val startShape = findShapeById(connection.startShapeId)
            val endShape = findShapeById(connection.endShapeId)

            if (startShape != null && endShape != null) {
                val startX = startShape.rectF.centerX()
                val startY = startShape.rectF.centerY()
                val endX = endShape.rectF.centerX()
                val endY = endShape.rectF.centerY()
                
                val midX = (startX + endX) / 2
                
                canvas.drawLine(startX, startY, midX, startY, linePaint)
                canvas.drawLine(midX, startY, midX, endY, linePaint)
                canvas.drawLine(midX, endY, endX, endY, linePaint)
            }
        }
    }

    private fun drawTemporaryLine(canvas: Canvas){
        if(isLineDrawingMode && lineStartShape != null && tempLineEndPoint != null){
            val startX = lineStartShape!!.rectF.centerX()
            val startY = lineStartShape!!.rectF.centerY()
            canvas.drawLine(startX, startY, tempLineEndPoint!!.x, tempLineEndPoint!!.y, linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val scaleHandled = scaleGestureDetector.onTouchEvent(event)
        val gestureHandled = gestureDetector.onTouchEvent(event)

        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scaleGestureDetector.isInProgress) {
                    draggedShape = findShapeAt(touchX, touchY)
                    draggedShape?.let {
                        lastTouchX = touchX; lastTouchY = touchY
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLineDrawingMode) {
                    tempLineEndPoint = PointF(touchX, touchY)
                    invalidate()
                } else if (draggedShape != null && !scaleGestureDetector.isInProgress) {
                    val dx = touchX - lastTouchX; val dy = touchY - lastTouchY
                    draggedShape!!.rectF.offset(dx, dy)
                    lastTouchX = touchX; lastTouchY = touchY
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if(isLineDrawingMode){
                    val endShape = findShapeAt(touchX, touchY)
                    if(endShape != null && endShape != lineStartShape){
                        val newConnection = ConnectionLine(lineStartShape!!.id, endShape.id)
                        connections.add(newConnection)
                    }
                    isLineDrawingMode = false
                    lineStartShape = null
                    tempLineEndPoint = null
                    invalidate()
                }
                draggedShape = null
                scaledShape = null
            }
        }
        return true
    }

    private fun findShapeAt(x: Float, y: Float): Shape? = shapes.lastOrNull { it.isHit(x, y) }
    private fun findShapeById(id: Int): Shape? = shapes.find { it.id == id }

    fun addShape(type: ShapeType) {
        val centerX = width / 2f
        val centerY = height / 2f
        val shapeSize = 250f

        val newRect = RectF(
            centerX - shapeSize / 2,
            centerY - shapeSize / 2,
            centerX + shapeSize / 2,
            centerY + shapeSize / 2
        )

        val newShape = Shape(shapes.size + 1, type, newRect)
        shapes.add(newShape)
        invalidate()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener()
    {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            val touchX = detector.focusX
            val touchY = detector.focusY
            scaledShape = shapes.lastOrNull { it.isHit(touchX, touchY) }
            return scaledShape != null
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaledShape?.let { shape ->
                val scaleFactor = detector.scaleFactor

                val currentWidth = shape.rectF.width()
                val currentHeight = shape.rectF.height()

                val newWidth = currentWidth * scaleFactor
                val newHeight = currentHeight * scaleFactor

                val focusX = detector.focusX
                val focusY = detector.focusY

                val dx = (newWidth - currentWidth) / 2
                val dy = (newHeight - currentHeight) / 2

                shape.rectF.left -= dx
                shape.rectF.right += dx
                shape.rectF.top -= dy
                shape.rectF.bottom += dy

                invalidate()
                return true
            }
            return false
        }
    }

//    private enum class Mode { NORMAL, EDIT }
//    private var currentMode = Mode.NORMAL
//    private var editedShape: Shape? = null
//
//    private val gestureDetector: GestureDetector
//    private val scaleGestureDetector: ScaleGestureDetector
//
//    private val gridPaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f; style = Paint.Style.STROKE }
//    private val shapeFillPaint = Paint().apply { color = Color.parseColor("#E1F5FE"); style = Paint.Style.FILL }
//    private val shapeBorderPaint = Paint().apply { color = Color.parseColor("#4285F4"); strokeWidth = 3f; style = Paint.Style.STROKE }
//    private val shapeEditBorderPaint = Paint().apply { color = Color.parseColor("#FFAB00"); strokeWidth = 6f; style = Paint.Style.STROKE }
//    private val dimPaint = Paint().apply { color = Color.parseColor("#80000000"); }
//
//    private val shapes = mutableListOf<Shape>()
//    private var draggedShape: Shape? = null
//    private var lastTouchX = 0f
//    private var lastTouchY = 0f
//    private var lastRotationAngle = 0f
//
//    init {
//        val testShape = Shape(1, ShapeType.RECTANGLE, RectF(300f, 500f, 600f, 750f))
//        shapes.add(testShape)
//
//        gestureDetector = GestureDetector(context, GestureListener())
//        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        canvas.drawColor(Color.WHITE)
//        drawGrid(canvas)
//
//        for (shape in shapes) {
//            if (shape != editedShape) {
//                drawShape(canvas, shape, shapeBorderPaint)
//            }
//        }
//
//        if (currentMode == Mode.EDIT && editedShape != null)
//        {
//            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)
//            drawShape(canvas, editedShape!!, shapeEditBorderPaint)
//        }
//    }
//
//    private fun drawShape(canvas: Canvas, shape: Shape, borderPaint: Paint) {
//        canvas.save()
//        canvas.rotate(shape.rotationDegrees, shape.rectF.centerX(), shape.rectF.centerY())
//        canvas.drawRect(shape.rectF, shapeFillPaint)
//        canvas.drawRect(shape.rectF, borderPaint)
//        canvas.restore()
//    }
//
//    private fun drawGrid(canvas: Canvas) {
//        var x = 0f
//        while (x < width) { canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint); x += 60f }
//        var y = 0f
//        while (y < height) { canvas.drawLine(0f, y, width.toFloat(), y, gridPaint); y += 60f }
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val scaleHandled = scaleGestureDetector.onTouchEvent(event)
//        val gestureHandled = gestureDetector.onTouchEvent(event)
//        if (scaleHandled || gestureHandled) {
//            invalidate()
//            return true
//        }
//
//        val touchX = event.x
//        val touchY = event.y
//
//        if (currentMode == Mode.NORMAL) {
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    draggedShape = shapes.lastOrNull { it.isHit(touchX, touchY) }
//                    draggedShape?.let { lastTouchX = touchX; lastTouchY = touchY }
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    draggedShape?.let {
//                        val dx = touchX - lastTouchX
//                        val dy = touchY - lastTouchY
//                        it.rectF.offset(dx, dy)
//                        lastTouchX = touchX
//                        lastTouchY = touchY
//                    }
//                }
//                MotionEvent.ACTION_UP -> { draggedShape = null }
//            }
//        } else if (currentMode == Mode.EDIT) {
//            when (event.actionMasked) {
//                MotionEvent.ACTION_POINTER_DOWN -> {
//                    if (event.pointerCount >= 2) {
//                        lastRotationAngle = getAngle(event)
//                    }
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    if (event.pointerCount >= 2 && editedShape != null) {
//                        val currentAngle = getAngle(event)
//                        val deltaAngle = currentAngle - lastRotationAngle
//                        editedShape!!.rotationDegrees += deltaAngle
//                        lastRotationAngle = currentAngle
//                    }
//                }
//                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { editedShape = null; currentMode = Mode.NORMAL }
//            }
//        }
//
//        invalidate()
//        return true
//    }
//
//    private fun getAngle(event: MotionEvent): Float {
//        val dx = (event.getX(0) - event.getX(1))
//        val dy = (event.getY(0) - event.getY(1))
//        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
//    }
//
//    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
//        override fun onLongPress(e: MotionEvent) {
//            if (currentMode == Mode.NORMAL) {
//                val shape = shapes.lastOrNull { it.isHit(e.x, e.y) }
//                if (shape != null) {
//                    currentMode = Mode.EDIT
//                    editedShape = shape
//                    draggedShape = null
//                    invalidate()
//                }
//            }
//        }
//
//        override fun onSingleTapUp(e: MotionEvent): Boolean {
//            if (currentMode == Mode.EDIT) {
//                val hit = editedShape?.isHit(e.x, e.y) ?: false
//                if (!hit) {
//                    currentMode = Mode.NORMAL
//                    editedShape = null
//                    invalidate()
//                    return true
//                }
//            }
//            return false
//        }
//    }
//
//    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
//            return currentMode == Mode.EDIT
//        }
//
//        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            if (currentMode == Mode.EDIT) {
//                editedShape?.let { shape ->
//                    val scaleFactor = detector.scaleFactor
//                    shape.rectF.right = shape.rectF.left + shape.rectF.width() * scaleFactor
//                    shape.rectF.bottom = shape.rectF.top + shape.rectF.height() * scaleFactor
//                    invalidate()
//                    return true
//                }
//            }
//            return false
//        }
//    }

//    private val gridPaint = Paint().apply {
//        color = Color.LTGRAY
//        strokeWidth = 1f
//        style = Paint.Style.STROKE
//    }
//
//    private val shapeFillPaint = Paint().apply {
//        color = Color.parseColor("#E1F5FE")
//        style = Paint.Style.FILL
//    }
//
//    private val shapeBorderPaint = Paint().apply {
//        color = Color.parseColor("#4285F4")
//        strokeWidth = 3f
//        style = Paint.Style.STROKE
//    }
//
//    private val gridSize = 60f
//
//    private val shapes = mutableListOf<Shape>()
//    private var draggingShape: Shape? = null
//    private var lastTouchX = 0f
//    private var lastTouchY = 0f
//
//    private var scaledShape: Shape? = null
//    private val scaleGestureDetector: ScaleGestureDetector
//
//    init {
//        val initalRect = RectF(200f, 400f, 400f, 600f)
//        val testShape = Shape(1, ShapeType.RECTANGLE, initalRect)
//        shapes.add(testShape)
//
//        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        canvas.drawColor(Color.WHITE)
//        drawGrid(canvas)
//
//        for (shape in shapes) {
//            when (shape.type) {
//                ShapeType.RECTANGLE -> {
//                    canvas.drawRect(shape.rectF, shapeFillPaint)
//                    canvas.drawRect(shape.rectF, shapeBorderPaint)
//                }
//                ShapeType.CIRCLE -> {
//                    canvas.drawOval(shape.rectF, shapeFillPaint)
//                    canvas.drawOval(shape.rectF, shapeBorderPaint)
//                }
//            }
//        }
//    }
//
//    private fun drawGrid(canvas: Canvas) {
//        var x = 0f
//        while (x < width)
//        {
//            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
//            x += gridSize
//        }
//
//        var y = 0f
//        while (y < height)
//        {
//            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
//            y += gridSize
//        }
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val scaleHandled = scaleGestureDetector.onTouchEvent(event)
//
//        val touchX = event.x
//        val touchY = event.y
//
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                if (!scaleGestureDetector.isInProgress)
//                {
//                    draggingShape = shapes.lastOrNull { it.isHit(touchX, touchY)  }
//                    draggingShape?.let {
//                        lastTouchX = touchX
//                        lastTouchY = touchY
//                    }
//                }
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                if (scaleGestureDetector.isInProgress)
//                {
//                    draggingShape = null
//                }
//                else
//                {
//                    draggingShape?.let { shape ->
//                        val dx = touchX - lastTouchX
//                        val dy = touchY - lastTouchY
//
//                        shape.rectF.offset(dx, dy)
//
//                        lastTouchX = touchX
//                        lastTouchY = touchY
//
//                        invalidate()
//                    }
//                }
//            }
//
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                draggingShape = null
//                scaledShape = null
//            }
//        }
//
//        return scaleHandled || draggingShape != null || super.onTouchEvent(event)
//    }
//
//    fun addShape(type: ShapeType) {
//        val centerX = width / 2f
//        val centerY = height / 2f
//        val shapeSize = 250f
//
//        val newRect = RectF(
//            centerX - shapeSize / 2,
//            centerY - shapeSize / 2,
//            centerX + shapeSize / 2,
//            centerY + shapeSize / 2
//        )
//
//        val newShape = Shape(shapes.size + 1, type, newRect)
//
//        shapes.add(newShape)
//
//        invalidate()
//    }
//
//    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener()
//    {
//        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
//            val touchX = detector.focusX
//            val touchY = detector.focusY
//            scaledShape = shapes.lastOrNull { it.isHit(touchX, touchY) }
//            return scaledShape != null
//        }
//
//        override fun onScale(detector: ScaleGestureDetector): Boolean {
//            scaledShape?.let { shape ->
//                val scaleFactor = detector.scaleFactor
//
//                val currentWidth = shape.rectF.width()
//                val currentHeight = shape.rectF.height()
//
//                val newWidth = currentWidth * scaleFactor
//                val newHeight = currentHeight * scaleFactor
//
//                val focusX = detector.focusX
//                val focusY = detector.focusY
//
//                val dx = (newWidth - currentWidth) / 2
//                val dy = (newHeight - currentHeight) / 2
//
//                shape.rectF.left -= dx
//                shape.rectF.right += dx
//                shape.rectF.top -= dy
//                shape.rectF.bottom += dy
//
//                invalidate()
//                return true
//            }
//            return false
//        }
//    }
}