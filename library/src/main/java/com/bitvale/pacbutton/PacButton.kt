package com.bitvale.pacbutton

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withTranslation
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


/**
 * Created by Alexander Kolpakov on 11/4/2018
 */
class PacButton @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIMATION_DURATION = 350L
        private const val ANIMATION_REPEAT_COUNT = 1
        private const val PAC_STATE = "pac_state"
        private const val KEY_IS_CHECKED = "is_checked"
    }

    private val buttonRect = RectF(0f, 0f, 0f, 0f)
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    private var gradientColor1 = 0
    @ColorInt
    private var gradientColor2 = 0
    @ColorInt
    private var pacColor = 0

    private var topIconRect = RectF(0f, 0f, 0f, 0f)
    private var bottomIconRect = RectF(0f, 0f, 0f, 0f)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private var bottomIcon: Bitmap? = null
    private var topIcon: Bitmap? = null

    private var iconHeight = 0f
    private var iconWidth = 0f

    private var radius = 0f
    private var startHeight = 0f
    private var heightOffset = 2f
    private var bottomIconTop = 0f
    private var bottomIconBottom = 0f
    private var animateOffset = 0f
    private var useGradient = true
    private var reverseStep = false
    private var isTopSelected = false
        set(value) {
            if (field != value) {
                field = value
                listener?.invoke(value)
            }
        }

    private var progressAnimator: ValueAnimator? = null
    private var progress = 0f
        set(value) {
            if (field != value) {
                field = value
                buttonRect.top = startHeight - lerp(0f, startHeight, value)
                if (reverseStep) {
                    animateOffset = lerp(radius * 2f, 0f, value)
                    bottomIconRect.top = bottomIconTop + animateOffset
                    bottomIconRect.bottom = bottomIconBottom + animateOffset
                }
                if (useGradient) {
                    setGradient(buttonRect.bottom, buttonRect.top)
                }
                postInvalidateOnAnimation()
            }
        }

    private var listener: ((isTopSelected: Boolean) -> Unit)? = null
    private var animationListener: ((animatedValue: Float) -> Unit)? = null

    init {
        setOnClickListener { animatePac() }
        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }
    }

    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PacButton, defStyleAttr, R.style.PacButton)

        gradientColor1 = typedArray.getColor(R.styleable.PacButton_pacGradientColor_1, 0)
        gradientColor2 = typedArray.getColor(R.styleable.PacButton_pacGradientColor_2, 0)
        pacColor = typedArray.getColor(R.styleable.PacButton_pacColor, 0)

        if (pacColor != 0) useGradient = false

        iconHeight = typedArray.getDimension(R.styleable.PacButton_iconHeight, 0f)
        iconWidth = typedArray.getDimension(R.styleable.PacButton_iconWidth, 0f)

        var drawableResId = typedArray.getResourceId(R.styleable.PacButton_bottomIcon, 0)
        var drawable = context.getVectorDrawable(drawableResId)
        bottomIcon = BitmapUtil.getBitmapFromDrawable(drawable)

        drawableResId = typedArray.getResourceId(R.styleable.PacButton_topIcon, 0)
        drawable = context.getVectorDrawable(drawableResId)
        topIcon = BitmapUtil.getBitmapFromDrawable(drawable)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(w, (h * heightOffset).toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        radius = Math.min(width, height).toFloat() / 2f
        startHeight = (height - width).toFloat()

        buttonRect.top = startHeight
        buttonRect.right = width.toFloat()
        buttonRect.bottom = height.toFloat()

        if (iconHeight == 0f) iconHeight = radius
        if (iconWidth == 0f) iconWidth = radius

        bottomIcon?.let {
            val widthOffset = iconWidth / 2f
            val heightOffset = iconHeight / 2f

            bottomIconTop = (height - radius) - heightOffset
            bottomIconBottom = (height - radius) + heightOffset

            topIconRect.left = radius - widthOffset
            topIconRect.top = bottomIconTop
            topIconRect.right = radius + widthOffset
            topIconRect.bottom = bottomIconBottom

            bottomIconRect.set(topIconRect.left, topIconRect.top, topIconRect.right, topIconRect.bottom)
        }

        if (useGradient) {
            setGradient(buttonRect.bottom, buttonRect.top)
        } else {
            buttonPaint.color = pacColor
        }
    }

    private fun setGradient(y0: Float, y1: Float) {
        buttonPaint.shader = LinearGradient(
                0f,
                y0,
                0f,
                y1,
                gradientColor1,
                gradientColor2,
                Shader.TileMode.MIRROR
        )
    }

    override fun onDraw(canvas: Canvas?) {
        if (reverseStep) {
            bottomIcon?.let {
                canvas?.withTranslation(
                        y = -radius * 2f
                ) {
                    canvas.drawBitmap(it, null, topIconRect, iconPaint)
                }
            }
        }

        canvas?.drawRoundRect(
                buttonRect,
                radius,
                radius,
                buttonPaint
        )

        topIcon?.let {
            canvas?.withTranslation(
                    y = -radius * 2f + animateOffset
            ) {
                canvas.drawBitmap(it, null, topIconRect, iconPaint)
            }
        }

        bottomIcon?.let {
            canvas?.drawBitmap(it, null, bottomIconRect, iconPaint)
        }
    }

    /**
     * Animate button
     */
    private fun animatePac() {
        progressAnimator?.cancel()

        progressAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                progress = it.animatedValue as Float
                if (progress >= 0.98f && !reverseStep) {
                    isTopSelected = !isTopSelected
                    reverseStep = true
                }
                animationListener?.invoke(progress)
            }
            doOnEnd {
                reverseStep = false
                bottomIconRect.top = bottomIconTop
                bottomIconRect.bottom = bottomIconBottom
                swapIcons()
                animateOffset = 0f
            }
            interpolator = FastOutSlowInInterpolator()
            repeatCount = ANIMATION_REPEAT_COUNT
            repeatMode = ValueAnimator.REVERSE
            duration = ANIMATION_DURATION
            start()
        }
    }

    private fun swapIcons() {
        val tmp = bottomIcon
        bottomIcon = topIcon
        topIcon = tmp
    }

    /**
     * @return true if the top icon is selected (became bottom) otherwise false.
     */
    fun isTopSelected() = isTopSelected

    /**
     * Register a callback to be invoked when the top icon is selected.
     *
     * @param action The callback that will run
     */
    fun setSelectAction(action: (isTopSelected: Boolean) -> Unit) {
        this.listener = action
    }

    /**
     * Adds a listener that is sent update events through the life of
     * an animation. This method is called for every frame of the animation,
     * after the values for the animation have been calculated.
     *
     * @param listener the listener to be added for pac button animation.
     */
    fun setAnimationUpdateListener(listener: (animatedValue: Float) -> Unit) {
        this.animationListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putBoolean(KEY_IS_CHECKED, isTopSelected)
            putParcelable(PAC_STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(PAC_STATE))
            isTopSelected = state.getBoolean(KEY_IS_CHECKED)
            if (isTopSelected) swapIcons()
        }
    }
}