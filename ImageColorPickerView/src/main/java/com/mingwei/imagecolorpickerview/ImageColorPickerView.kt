package com.mingwei.imagecolorpickerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.get

class ImageColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // external variables
    private var mSelectorStroke: Int = 5
    var selectorStroke: Int
        get() = mSelectorStroke
        set(value) {
            mSelectorStroke = value
            invalidate()
        }

    private var mSelectorRadius: Int = 10
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

    private var mSelectorProbeRadius: Int = 10
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
    @ColorInt
    private var mSelectedColor: Int? = null

    private var mSelectColorListeners: SelectColorListener? = null
    private var mImageBitmap: Bitmap? = null
    private var mResizedBitmap: Bitmap? = null
    private var mImageViewWidth: Int = 0
    private var mImageViewHeight: Int = 0
    private var mImageRec: RectF = RectF()

    private var mSelectorPositionX: Float = -1.0f
    private var mSelectorPositionY: Float = -1.0f
    private var mShowSelector: Boolean = false

    private val mSelectorPaint = Paint().apply {
        color = Color.BLACK
    }
    private val mSelectorStrokePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = mSelectorStroke.toFloat()
        style = Paint.Style.STROKE
    }

    fun setSelectColorListener(listener: SelectColorListener) {
        mSelectColorListeners = listener
    }


    fun setImageBitmap(bitmap: Bitmap) {
        // TODO: optimize memory usage
        mImageBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        mImageBitmap?.let {
            mResizedBitmap = Bitmap.createScaledBitmap(it, mImageViewWidth, mImageViewHeight, false)
        }
        invalidate()
    }

    init {
        val density = resources.displayMetrics.density
        mSelectorRadius = (mSelectorRadius * density).toInt()
        mSelectorOffsetX = (mSelectorOffsetX * density).toInt()
        mSelectorOffsetY = (mSelectorOffsetY * density).toInt()
        mSelectorStroke = (mSelectorStroke * density).toInt()

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
                mSelectorStroke =
                    getDimension(R.styleable.ImageColorPickerView_selectorStroke, 5f).toInt()
                mSelectorProbeRadius =
                    getInt(R.styleable.ImageColorPickerView_selectorProbeRadius, 10)
                mEnable = getBoolean(R.styleable.ImageColorPickerView_enableSelector, true)
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
        if (!mEnable || mResizedBitmap == null) {
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
            // make selector visible
            mShowSelector = true
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y

            // trigger callback to notify client that user has started to select color
            val newColor = getProjectionColor(event.x.toInt(), event.y.toInt())
            mSelectColorListeners?.onSelectionStarted(newColor)

            // cache new color and apply it to paint object
            mSelectedColor = newColor
            mSelectorPaint.color = mSelectedColor ?: Color.WHITE
            invalidate()
        }
    }

    /**
     * Update the position of selector,
     * if the touch event is outside the image box,
     */
    private fun updateSelector(event: MotionEvent) {
        if (touchInBound(event)) {
            mSelectorPositionX = event.x
            mSelectorPositionY = event.y

            // trigger callback to notify client that user moved the selector to different color
            val newColor = getProjectionColor(event.x.toInt(), event.y.toInt())
            mSelectColorListeners?.onColorUpdated(mSelectedColor, newColor)

            //
            mSelectedColor = newColor
            mSelectorPaint.color = mSelectedColor ?: Color.WHITE
            invalidate()
        }
    }

    private fun hideSelector() {
        mShowSelector = false
        invalidate()
    }

    private fun emitColorUpdateEvent(event: MotionEvent) {
        val selectedColor = getProjectionColor(event.x.toInt(), event.y.toInt())
        mSelectColorListeners?.onColorSelected(selectedColor)
    }

    private fun touchInBound(event: MotionEvent): Boolean {
        return event.x >= paddingLeft && event.x <= mImageViewWidth + paddingLeft &&
                event.y >= paddingTop && event.y <= mImageViewHeight + paddingTop
    }


    private fun getProjectionColor(xPad: Int, yPad: Int): Int {
        // TODO: better projection formula

        mResizedBitmap?.let { self ->
            val x = xPad - paddingLeft
            val y = yPad - paddingTop

            val minX = (x - mSelectorProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val minY = (y - mSelectorProbeRadius).takeIf { it >= 0 } ?: run { 0 }
            val maxX = (x + mSelectorProbeRadius).takeIf { it < self.width }
                ?: run { self.width - 1 }
            val maxY = (y + mSelectorProbeRadius).takeIf { it < self.height }
                ?: run { self.height - 1 }
            val pixels = (maxX - minX + 1) * (maxY - minY + 1)

            var rSum = 0f
            var bSum = 0f
            var gSum = 0f
            var aSum = 0f

            for (i in minX..maxX) {
                for (j in minY..maxY) {
                    @ColorInt val color = self[i, j]

                    rSum += Color.red(color)
                    bSum += Color.blue(color)
                    gSum += Color.green(color)
                    aSum += Color.alpha(color)
                }
            }

            val rAvg = rSum / pixels
            val gAvg = gSum / pixels
            val bAvg = bSum / pixels
            val aAvg = aSum / pixels

            return Color.argb(aAvg.toInt(), rAvg.toInt(), gAvg.toInt(), bAvg.toInt())
        }

        return 0xFFFFFF
    }

    interface SelectColorListener {
        fun onSelectionStarted(@ColorInt color: Int)
        fun onColorSelected(@ColorInt color: Int)
        fun onColorUpdated(@ColorInt oldColor: Int?, @ColorInt newColor: Int)
    }

}