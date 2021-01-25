package com.mingwei.imagecolorpickerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
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
 */
class ImageColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Width of picker's stroke
     */
    private var mPickerStroke: Int = 5
    var pickerStroke: Int
        get() = mPickerStroke
        set(value) {
            mPickerStroke = value
            mPickerStrokePaint.strokeWidth = value.toFloat()
            invalidate()
        }

    /**
     * Radius of picker
     */
    private var mPickerRadius: Int = 10
    var pickerRadius: Int
        get() = mPickerRadius
        set(value) {
            mPickerRadius = value
            invalidate()
        }

    /**
     * X-axis offset from user's touch point which will be calculated when showing picker.
     */
    private var mPickerOffsetX: Int = 0
    var pickerOffsetX: Int
        get() = mPickerOffsetX
        set(value) {
            mPickerOffsetX = value
            invalidate()
        }

    /**
     * Y-axis offset from user's touch point which will be calculated when showing picker.
     */
    private var mPickerOffsetY: Int = 0
    var pickerOffsetY: Int
        get() = mPickerOffsetY
        set(value) {
            mPickerOffsetY = value
            invalidate()
        }

    /**
     * Radius from user's touch point. Unit: number of pixel
     *
     * ImageColorPickerView will use user's touch point and this radius to extract
     * pixels from surrounding area for color pooling.
     */
    private var mPickerProbeRadius: Int = 10
    var pickerProbeRadius: Int
        get() = mPickerProbeRadius
        set(value) {
            mPickerProbeRadius = value
            invalidate()
        }

    /**
     * Enable picker when user touch the image view or not.
     */
    private var mEnablePicker: Boolean = true
    var enablePicker: Boolean
        get() = mEnablePicker
        set(value) {
            mEnablePicker = value
            invalidate()
        }

    /**
     * Pooling function which will be used for selecting color from probe area's pixels.
     */
    private var mPoolingFunc: PoolingFunction = AveragePooling
    fun setPoolingFunc(func: PoolingFunction) {
        mPoolingFunc = func
    }

    @ColorInt
    private var mPickColor: Int? = null

    private var mPickColorListener: PickColorListener? = null
    private var mProjectedBitmap: Bitmap? = null
    private var mOriginalBitmap: Bitmap? = null
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

    fun setImage(uri: Uri) {
        val originalBitmap = decodeUri(uri)
        setImage(originalBitmap)
        originalBitmap.recycle()
        invalidate()
    }

    fun setImage(bitmap: Bitmap) {
        mOriginalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        projectBitmap()
        invalidate()
    }

    fun setImage(drawable: Drawable) {
        // create an empty placeholder bitmap
        val originalBitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        // bind a canvas to placeholder bitmap
        val canvas = Canvas(originalBitmap)

        // draw on to the canvas that bind with placeholder bitmap
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        setImage(originalBitmap)
        originalBitmap.recycle()
        invalidate()
    }

    fun setImage(@DrawableRes id: Int) {
        try {
            val drawable = ResourcesCompat.getDrawable(
                context.applicationContext.resources,
                id,
                null
            )
            drawable?.let { setImage(it) }
        } catch (e: Exception) {
            Log.w(LOG_TAG, "Unable to find resource: $id $e")
        }
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
        // get rid of padding area to get actual image size
        mImageViewWidth = width - paddingLeft - paddingRight
        mImageViewHeight = height - paddingTop - paddingBottom

        // set up the image box position for drawing
        val left = paddingLeft
        val right = left + mImageViewWidth
        val top = paddingTop
        val bottom = top + mImageViewHeight
        mImageRec.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        // re-scale bitmap
        projectBitmap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = paddingLeft + paddingRight + suggestedMinimumWidth
        val minHeight = paddingBottom + paddingTop + suggestedMinimumHeight
        val width = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height = resolveSizeAndState(minHeight, heightMeasureSpec, 0)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            mProjectedBitmap?.let {
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
        if (!mEnablePicker || mProjectedBitmap == null) {
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
        if (mProjectedBitmap != null) {
            val selectedColor = poolColor(event.x.toInt(), event.y.toInt())
            mPickColorListener?.onColorPicked(selectedColor)
        }
    }

    /**
     * Check whether the touch event is within image view or not.
     * Padding area does not be considered as image view.
     */
    private fun touchInBound(event: MotionEvent): Boolean {
        return event.x >= paddingLeft && event.x <= mImageViewWidth + paddingLeft &&
                event.y >= paddingTop && event.y <= mImageViewHeight + paddingTop
    }

    /**
     * Pool the color from given coordination.
     */
    private fun poolColor(xPad: Int, yPad: Int): Int {
        mProjectedBitmap?.let { self ->
            // Since the bitmap has already been scaled to fit the actual showing image size,
            // the corresponding coordination of pixels in bitmap is just relative to the padding.
            val x = xPad - paddingLeft
            val y = yPad - paddingTop

            // Get the square of probing area
            val minX = (x - mPickerProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val minY = (y - mPickerProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val maxX = (x + mPickerProbeRadius).takeIf { it < self.width }
                ?: run { self.width - 1 }
            val maxY = (y + mPickerProbeRadius).takeIf { it < self.height }
                ?: run { self.height - 1 }

            // extract probing pixels from bitmap
            val probeWidth = maxX - minX + 1
            val probeHeight = maxY - minY + 1
            val pixelNum = probeWidth * probeHeight
            val pixels = IntArray(pixelNum)
            self.getPixels(pixels, 0, probeWidth, minX, minY, probeWidth, probeHeight)

            // using pooling function to extract color
            return mPoolingFunc.exec(pixels)
        }

        return 0xFFFFFF
    }

    private fun decodeUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(src)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    private fun projectBitmap() {
        if (mImageViewWidth > 0 && mImageViewHeight > 0) {
            mOriginalBitmap?.let {
                mProjectedBitmap =
                    Bitmap.createScaledBitmap(it, mImageViewWidth, mImageViewHeight, false)
            }
        }
    }


    interface PickColorListener {
        fun onPickStarted(@ColorInt color: Int)
        fun onColorPicked(@ColorInt color: Int)
        fun onColorUpdated(@ColorInt oldColor: Int?, @ColorInt newColor: Int)
    }

    companion object {
        const val LOG_TAG = "ImageColorPickerView"
    }

}