package com.udacity

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var backgroundButton: Int = Color.BLACK
    private var textButton: String? = null

    private var widthSize = 0
    private var heightSize = 0

    private var textSmallGlyphHeight = 0f

    @Volatile
    private var progress: Double = 0.0

    private var valueAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        interpolator = LinearInterpolator()
        addUpdateListener {
            progress = (it.animatedValue as Float).toDouble()
            invalidate()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSmallGlyphHeight = fontMetrics.run { ascent + descent }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->

    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backgroundButton = getColor(
                R.styleable.LoadingButton_backgroundButton,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )

            textButton = getString(
                R.styleable.LoadingButton_textButton
            )
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Loading
        valueAnimator.start()

        return true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(context.getColor(R.color.colorPrimary))
        paint.strokeWidth = 0f
        paint.color = backgroundButton
        val rect = RectF(
            (width + 500f) / 2f - 50f,
            height / 2f - 50f,
            (width + 500f) / 2f + 50f,
            height / 2f + 50f
        )
        if (buttonState == ButtonState.Loading) {
            paint.color = context.getColor(R.color.colorPrimaryDark)
            canvas?.drawRect(0f, 0f, width * (progress / 100).toFloat(), height.toFloat(), paint)
            paint.color = context.getColor(R.color.colorAccent)
            canvas?.drawArc(
                rect, 0f, 360 * (progress / 100).toFloat(), true, paint
            )
        }
        paint.color = context.getColor(R.color.white)
        textButton =
            if (buttonState == ButtonState.Loading) context.getString(R.string.button_loading) else context.getString(
                R.string.download
            )
        canvas?.drawText(
            textButton!!,
            width / 2f,
            (height - textSmallGlyphHeight) / 2f,
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    fun completedDownload() {
        valueAnimator.cancel()
        buttonState = ButtonState.Completed
        invalidate()
    }

}