package com.mingwei.imagecolorpickerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.mingwei.imagecolorpickerview.pooling.AveragePooling
import com.mingwei.imagecolorpickerview.pooling.PoolingFunction

/**
 * A ImageColorPickerView which allow user to pick color from provided image,
 * feature includes:
 * <ul>
 *     <li> Allow clients custom radius of picker and stroke of picker.
 *     <li> Allow clients custom position of picker relative to touch point.
 *     <li> Allow clients decide how to choose color from a touch point.
 *     <li> Provide suitable callback for pick color event.
 * </ul>
 *
 * @property enablePicker Enable picker or not.
 * @property pickerOffsetX X-axis offset from user's touch point which will be calculated when showing picker.
 * @property pickerOffsetY Y-axis offset from user's touch point which will be calculated when showing picker.
 * @property pickerStroke Stroke's width of picker.
 * @property pickerRadius Radius of picker.
 * @property pickerProbeRadius Radius from user's touch point. Given a touch point as a center, ImageColorView will
 *                             use this radius to pool the color from nearby pixels.
 */
class ImageColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // external variables
    private var mPickerStroke: Int = 5
    var pickerStroke: Int
        get() = mPickerStroke
        set(value) {
            mPickerStroke = value
            mPickerStrokePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    private var mPickerRadius: Int = 10
    var pickerRadius: Int
        get() = mPickerRadius
        set(value) {
            mPickerRadius = value
            invalidate()
        }

    private var mPickerOffsetX: Int = 0
    var pickerOffsetX: Int
        get() = mPickerOffsetX
        set(value) {
            mPickerOffsetX = value
            invalidate()
        }

    private var mPickerOffsetY: Int = 0
    var pickerOffsetY: Int
        get() = mPickerOffsetY
        set(value) {
            mPickerOffsetY = value
            invalidate()
        }

    private var mPickerProbeRadius: Int = 10
    var pickerProbeRadius: Int
        get() = mPickerProbeRadius
        set(value) {
            mPickerProbeRadius = value
            invalidate()
        }

    private var mEnablePicker: Boolean = true
    var enablePicker: Boolean
        get() = mEnablePicker
        set(value) {
            mEnablePicker = value
            invalidate()
        }

    private var mPoolingFunc: PoolingFunction = AveragePooling
    fun setPoolingFunc(func: PoolingFunction) {
        mPoolingFunc = func
    }


    // internal variables
    @ColorInt
    private var mPickColor: Int? = null

    private var mPickColorListener: PickColorListener? = null
    private var mImageBitmap: Bitmap? = null
    private var mResizedBitmap: Bitmap? = null
    private var mImageViewWidth: Int = 0
    private var mImageViewHeight: Int = 0
    private var mImageRec: RectF = RectF()

    private var mSelectorPositionX: Float = -1.0f
    private var mSelectorPositionY: Float = -1.0f
    private var mShowPicker: Boolean = false

    private val mPickerPaint = Paint().apply {
        color = Color.BLACK
    }
    private val mPickerStrokePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = mPickerStroke.toFloat()
        style = Paint.Style.STROKE
    }

    fun setPickColorListener(listener: PickColorListener) {
        mPickColorListener = listener
    }


    fun setImageBitmap(bitmap: Bitmap) {
        mImageBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mImageBitmap?.let {
            mResizedBitmap = Bitmap.createScaledBitmap(it, mImageViewWidth, mImageViewHeight, false)
        }
        invalidate()
    }

    init {
        val density = resources.displayMetrics.density
        pickerRadius = (pickerRadius * density).toInt()
        pickerOffsetX = (pickerOffsetX * density).toInt()
        pickerOffsetY = (pickerOffsetY * density).toInt()
        pickerStroke = (pickerStroke * density).toInt()

        attrs?.let {
            val a: TypedArray =
                context.theme.obtainStyledAttributes(attrs, R.styleable.ImageColorPickerView, 0, 0)

            with(a) {
                pickerRadius =
                    getDimension(R.styleable.ImageColorPickerView_pickerRadius, 10f).toInt()
                pickerOffsetX =
                    getDimension(R.styleable.ImageColorPickerView_pickerOffsetX, 0f).toInt()
                pickerOffsetY =
                    getDimension(R.styleable.ImageColorPickerView_pickerOffsetY, 0f).toInt()
                pickerStroke =
                    getDimension(R.styleable.ImageColorPickerView_pickerStroke, 5f).toInt()
                pickerProbeRadius =
                    getInt(R.styleable.ImageColorPickerView_pickerProbeRadius, 10)
                enablePicker = getBoolean(R.styleable.ImageColorPickerView_enablePicker, true)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // set up the image box position for drawing
        val left = paddingLeft
        val right = left + mImageViewWidth
        val top = paddingTop
        val bottom = top + mImageViewHeight
        mImageRec.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        mImageBitmap?.let {
            mResizedBitmap = Bitmap.createScaledBitmap(it, mImageViewWidth, mImageViewHeight, false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        val minHeight = paddingBottom + paddingTop + suggestedMinimumHeight
        val width = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height = resolveSizeAndState(minHeight, heightMeasureSpec, 0)

        // get rid of padding area to get actual image size
        mImageViewWidth = width - paddingLeft - paddingRight
        mImageViewHeight = height - paddingTop - paddingBottom

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            mResizedBitmap?.let {
                drawBitmap(it, null, mImageRec, null)

                if (mShowPicker) {
                    drawCircle(
                        mSelectorPositionX + mPickerOffsetX,
                        mSelectorPositionY + mPickerOffsetY,
                        mPickerRadius.toFloat(),
                        mPickerPaint
                    )
                    drawCircle(
                        mSelectorPositionX + mPickerOffsetX,
                        mSelectorPositionY + mPickerOffsetY,
                        mPickerRadius.toFloat(),
                        mPickerStrokePaint
                    )
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mEnablePicker || mResizedBitmap == null) {
            return false
        }

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // show selector at given position
                parent.requestDisallowInterceptTouchEvent(true)
                initPicker(event)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                // update selector color
                updatePicker(event)
                true
            }
            MotionEvent.ACTION_UP -> {
                // hide selector at reset position to 0
                parent.requestDisallowInterceptTouchEvent(false)
                hidePicker()
                emitColorPickedEvent(event)
                true
            }
            else -> true
        }
    }

    /**
     * Initialize picker's position and emit onPickStarted event.
     */
    private fun initPicker(event: MotionEvent) {
        if (touchInBound(event)) {
            // make selector visible
            mShowPicker = true
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y

            // trigger callback to notify client that user has started to select color
            val newColor = poolColor(event.x.toInt(), event.y.toInt())
            mPickColorListener?.onPickStarted(newColor)

            // cache new color and apply it to paint object
            mPickColor = newColor
            mPickerPaint.color = mPickColor ?: Color.WHITE
            invalidate()
        }
    }

    /**
     * Update the position of picker, and emit onColorUpdated event.
     */
    private fun updatePicker(event: MotionEvent) {
        if (touchInBound(event)) {
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y

            // trigger callback to notify client that user moved the selector to different color
            val newColor = poolColor(event.x.toInt(), event.y.toInt())
            mPickColorListener?.onColorUpdated(mPickColor, newColor)

            //
            mPickColor = newColor
            mPickerPaint.color = mPickColor ?: Color.WHITE
            invalidate()
        }
    }

    private fun hidePicker() {
        mShowPicker = false
        invalidate()
    }

    private fun emitColorPickedEvent(event: MotionEvent) {
        val selectedColor = poolColor(event.x.toInt(), event.y.toInt())
        mPickColorListener?.onColorPicked(selectedColor)
    }

    private fun touchInBound(event: MotionEvent): Boolean {
        return event.x >= paddingLeft && event.x <= mImageViewWidth + paddingLeft &&
                event.y >= paddingTop && event.y <= mImageViewHeight + paddingTop
    }


    private fun poolColor(xPad: Int, yPad: Int): Int {
        mResizedBitmap?.let { self ->
            val x = xPad - paddingLeft
            val y = yPad - paddingTop

            val minX = (x - mPickerProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val minY = (y - mPickerProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val maxX = (x + mPickerProbeRadius).takeIf { it < self.width }
                ?: run { self.width - 1 }
            val maxY = (y + mPickerProbeRadius).takeIf { it < self.height }
                ?: run { self.height - 1 }

            val probeWidth = maxX - minX + 1
            val probeHeight = maxY - minY + 1
            val pixelNum = probeWidth * probeHeight
            val pixels = IntArray(pixelNum)
            self.getPixels(pixels, 0, probeWidth, minX, minY, probeWidth, probeHeight)

            return mPoolingFunc.exec(pixels)
        }

        return 0xFFFFFF
    }

    interface PickColorListener {
        fun onPickStarted(@ColorInt color: Int)
        fun onColorPicked(@ColorInt color: Int)
        fun onColorUpdated(@ColorInt oldColor: Int?, @ColorInt newColor: Int)
    }

}