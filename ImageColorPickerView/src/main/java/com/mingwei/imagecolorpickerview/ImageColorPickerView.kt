package com.mingwei.imagecolorpickerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.get

class ImageColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // external variables
    private var mSelectorRadius: Int = 50
    var selectorRadius: Int
        get() = mSelectorRadius
        set(value) {
            mSelectorRadius = value
            invalidate()
        }

    private var mSelectorOffsetX: Int = 0
    var selectorOffsetX: Int
        get() = mSelectorOffsetX
        set(value) {
            mSelectorOffsetX = value
            invalidate()
        }

    private var mSelectorOffsetY: Int = 0
    var selectorOffsetY: Int
        get() = mSelectorOffsetY
        set(value) {
            mSelectorOffsetY = value
            invalidate()
        }

    private  var mSelectorProbeRadius: Int = 10
    var selectorProbeRadius: Int
        get() = mSelectorProbeRadius
        set(value) {
            mSelectorProbeRadius = value
            invalidate()
        }

    private var mEnable: Boolean = true
    var enable: Boolean
        get() = mEnable
        set(value) {
            mEnable = value
            invalidate()
        }

    // internal variables
    private lateinit var mImageBitmap: Bitmap
    private lateinit var mResizedBitmap: Bitmap
    private var mImageViewWidth: Int = 0
    private var mImageViewHeight: Int = 0
    private var mImageRec: RectF = RectF()


    private val mSelectColorListeners = ArrayList<SelectColorListener>()
    private var mSelectorPositionX: Float = -1.0f
    private var mSelectorPositionY: Float = -1.0f
    private var mShowSelector: Boolean = false
    private val mSelectorPaint = Paint().apply {
        color = Color.BLACK
    }
    private val mSelectorStrokePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }


    fun addSelectColorListener(listener: SelectColorListener) {
        mSelectColorListeners.add(listener)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        // TODO: optimize memory usage
        mImageBitmap = bitmap.copy(Bitmap.Config.RGBA_F16, true)
    }

    init {

        val density = resources.displayMetrics.density
        mSelectorRadius = (mSelectorRadius * density).toInt()
        mSelectorOffsetX = (mSelectorOffsetX * density).toInt()
        mSelectorOffsetY = (mSelectorOffsetY * density).toInt()

        attrs?.let {
            val a: TypedArray =
                context.theme.obtainStyledAttributes(attrs, R.styleable.ImageColorPickerView, 0, 0)

            with(a) {
                mSelectorRadius =
                    getDimension(R.styleable.ImageColorPickerView_selectorRadius, 10f).toInt()
                mSelectorOffsetX =
                    getDimension(R.styleable.ImageColorPickerView_selectorOffsetX, 0f).toInt()
                mSelectorOffsetY =
                    getDimension(R.styleable.ImageColorPickerView_selectorOffsetY, 0f).toInt()
                mEnable = getBoolean(R.styleable.ImageColorPickerView_enableSelector, true)
            }
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

        // set up the image box position for drawing
        val left = paddingLeft
        val right = left + mImageViewWidth
        val top = paddingTop
        val bottom = top + mImageViewHeight
        mImageRec.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        mResizedBitmap = Bitmap.createScaledBitmap(mImageBitmap, mImageViewWidth, mImageViewHeight, false)
        Log.d("${this::class.java}", "New bitmap size: (${mResizedBitmap.width}, ${mResizedBitmap.height})")
        Log.d("${this::class.java}", "Image Box size: (${mImageViewWidth}, ${mImageViewHeight})")

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            mResizedBitmap?.let {
                drawBitmap(it, null, mImageRec, null)

                if (mShowSelector) {
                    drawCircle(
                        mSelectorPositionX + mSelectorOffsetX,
                        mSelectorPositionY + mSelectorOffsetY,
                        mSelectorRadius.toFloat(),
                        mSelectorPaint
                    )
                    drawCircle(
                        mSelectorPositionX + mSelectorOffsetX,
                        mSelectorPositionY + mSelectorOffsetY,
                        mSelectorRadius.toFloat(),
                        mSelectorStrokePaint
                    )
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mEnable) {
            return false
        }

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // show selector at given position
                parent.requestDisallowInterceptTouchEvent(true)
                initSelector(event)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                // update selector color
                updateSelector(event)
                true
            }
            MotionEvent.ACTION_UP -> {
                // hide selector at reset position to 0
                parent.requestDisallowInterceptTouchEvent(false)
                hideSelector()
                emitColorUpdateEvent(event)
                true
            }
            else -> true
        }
    }

    /**
     * Initialize selector's position and redraw the view
     */
    private fun initSelector(event: MotionEvent) {
        if (touchInBound(event)) {
            mShowSelector = true
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y
            mSelectorPaint.color = getProjectionColor(event.x.toInt(), event.y.toInt())
            invalidate()
        }
    }

    /**
     * Update the position of selector,
     * if the touch event is outside the image box,
     */
    private fun updateSelector(event: MotionEvent) {
        if (touchInBound(event)) {
            Log.d("${this::class.java}", "Move to (${event.x}, ${event.y})")
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y
            mSelectorPaint.color = getProjectionColor(event.x.toInt(), event.y.toInt())
            invalidate()
        }
    }

    private fun hideSelector() {
        mShowSelector = false
        invalidate()
    }

    private fun emitColorUpdateEvent(event: MotionEvent) {
        val selectedColor = getProjectionColor(event.x.toInt(), event.y.toInt())
        for (listener in mSelectColorListeners) {
            listener.onSelectColor(Color.valueOf(selectedColor))
        }
    }

    private fun touchInBound(event: MotionEvent): Boolean {
        return event.x >= paddingLeft && event.x <= mImageViewWidth + paddingLeft &&
                event.y >= paddingTop && event.y <= mImageViewHeight + paddingTop
    }


    private fun getProjectionColor(xPad: Int, yPad: Int): Int {
        // TODO: better projection formula
        val x = xPad - paddingLeft
        val y = yPad - paddingTop
        Log.d("${this::class.java}", "Select color from ($x, $y)")

        val minX = (x - mSelectorProbeRadius).takeIf { it >= 0 } ?: run { 0 }
        val minY = (y - mSelectorProbeRadius).takeIf { it >= 0 } ?: run { 0 }
        val maxX = (x + mSelectorProbeRadius).takeIf { it < mResizedBitmap.width }
            ?: run { mResizedBitmap.width - 1 }
        val maxY = (y + mSelectorProbeRadius).takeIf { it < mResizedBitmap.height }
            ?: run { mResizedBitmap.height - 1 }

        val pixels = (maxX - minX + 1) * (maxY - minY + 1)

        var rSum = 0f
        var bSum = 0f
        var gSum = 0f
        var aSum = 0f

        for (i in minX..maxX) {
            for (j in minY..maxY) {
                val color = Color.valueOf(mResizedBitmap[i, j])

                rSum += color.red()
                bSum += color.blue()
                gSum += color.green()
                aSum += color.alpha()
            }
        }

        val rAvg = rSum / pixels
        val gAvg = gSum / pixels
        val bAvg = bSum / pixels
        val aAvg = aSum / pixels

        val colorAvg = Color.valueOf(rAvg, gAvg, bAvg, aAvg)
        return colorAvg.toArgb()
    }

    interface SelectColorListener {
        fun onSelectColor(color: Color): Unit
    }

}