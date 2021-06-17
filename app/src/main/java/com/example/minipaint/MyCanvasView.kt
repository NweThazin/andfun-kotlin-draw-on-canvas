package com.example.minipaint

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

private const val STROKE_WIDTH = 12f // has to be float

class MyCanvasView(context: Context) : View(context) {

    // class level member variables
    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    // class level background color
    private val backgroundColor = ResourcesCompat.getColor(resources, R.color.colorBackground, null)
    private val drawColor = ResourcesCompat.getColor(resources, R.color.colorPaint, null)

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true  // Smooths out edges of what is drawn without affecting shape.
        isDither =
            true // Dithering affects how colors with higher-precision than the device are down-sampled.
        style = Paint.Style.STROKE // default : FILL
        strokeJoin = Paint.Join.ROUND // default : MITER
        strokeCap = Paint.Cap.ROUND // default : BUTT
        strokeWidth = STROKE_WIDTH  // default: Hairline-width (really thin)
    }

    // initialize the Path
    private val path = Path()

    // to add a frame
    private lateinit var frame: Rect

    /**
     * This callback method is called by the Android system with the changed screen dimensions,
     * that is, with a new width and height (to change to) and the old width and height (to change from).
     * **/
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        /**
         * to prevent memory leak because a new bitmap and canvas are created every times called onSizeChanged
         * **/
        if (::extraBitmap.isInitialized) extraBitmap.recycle()


        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)

        extraCanvas.drawColor(backgroundColor)

        /**
         * to create the Rect that will be used for the frame, using new dimentions and inset
         * */
        val inset = 40
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        /**
         * The 2D coordinate system used for drawing on a Canvas is in pixels,
         * and the origin (0,0) is at the top left corner of the Canvas
         * **/
        canvas?.drawBitmap(extraBitmap, 0f, 0f, null)

        /**
         * after drawing a bitmap, draw a rectangle
         * */
        canvas?.drawRect(frame, paint)
    }

    // variables for caching the x and y coordinates of the current touch event
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    // to respond to motion on the display
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            motionTouchEventX = event.x
            motionTouchEventY = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchStart() // touching down on the screen
                MotionEvent.ACTION_MOVE -> touchMove() // moving on the screen
                MotionEvent.ACTION_UP -> touchUp() // releasing touch on the screen
            }
            return true
        }

        return super.onTouchEvent(event)

    }

    // to cache the latest x and y values
    private var currentX = 0f
    private var currentY = 0f
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    /**
     * Calculate the distance that has been moved (dx, dy).
     * If the movement was further than the touch tolerance, add a segment to the path.
     * Set the starting point for the next segment to the endpoint of this segment.
     * Using quadTo() instead of lineTo() create a smoothly drawn line without corners. See Bezier Curves.
     * Call invalidate() to (eventually call onDraw() and) redraw the view.
     * **/
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private fun touchMove() {
        // calculate the distance that you have been moved
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)

        if (dx >= touchTolerance || dy >= touchTolerance) {
            // QuadTo() adds a quadratic bezier from the last point,
            // approaching control point (x1,y1), and ending at (x2,y2).
            path.quadTo(
                currentX,
                currentY,
                (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            // Draw the path in the extra bitmap to cache it.
            extraCanvas.drawPath(path, paint)
        }
        invalidate()
    }

    private fun touchUp() {
        // reset the path so it doesn't get drawn again
        path.reset()
    }
}