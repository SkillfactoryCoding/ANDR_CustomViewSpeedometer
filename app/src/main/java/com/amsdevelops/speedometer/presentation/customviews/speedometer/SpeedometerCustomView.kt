package com.amsdevelops.speedometer.presentation.customviews.speedometer

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.amsdevelops.speedometer.R
import com.amsdevelops.speedometer.constants.SpeedometerConstants
import kotlin.math.min

class SpeedometerCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs),
    SpeedChangeListener {

    private var maxSpeed = 0f
    private var currentSpeed = 0f

    private lateinit var onMarkPaint: Paint
    private lateinit var offMarkPaint: Paint
    private lateinit var scalePaint: Paint
    private lateinit var readingPaint: Paint
    private lateinit var onPath: Path
    private lateinit var offPath: Path
    private var oval = RectF()

    //Drawing colors
    private var ON_COLOR = Color.argb(255, 0xff, 0xA5, 0x00)
    private var OFF_COLOR = Color.argb(100, 0x3e, 0x3e, 0x3e)
    private var SCALE_COLOR = Color.argb(255, 255, 255, 255)
    private var SCALE_SIZE = 34f
    private var READING_SIZE = 60f

    //Scale configuration
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    init {
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.SpeedometerCustomView, 0, 0)

        try {
            maxSpeed = attributes.getFloat(
                R.styleable.SpeedometerCustomView_maxSpeed,
                SpeedometerConstants.DEFAULT_MAX_SPEED
            )
            currentSpeed = attributes.getFloat(R.styleable.SpeedometerCustomView_currentSpeed, 0f)
            ON_COLOR = attributes.getColor(R.styleable.SpeedometerCustomView_onColor, ON_COLOR)
            OFF_COLOR = attributes.getColor(R.styleable.SpeedometerCustomView_offColor, OFF_COLOR)
            SCALE_COLOR = attributes.getColor(R.styleable.SpeedometerCustomView_scaleColor, SCALE_COLOR)
            SCALE_SIZE = attributes.getDimension(R.styleable.SpeedometerCustomView_scaleTextSize, SCALE_SIZE)
            READING_SIZE =
                attributes.getDimension(R.styleable.SpeedometerCustomView_readingTextSize, READING_SIZE)
        } finally {
            attributes.recycle()
        }

        initDrawingTools()
    }

    private fun initDrawingTools() {
        onMarkPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = ON_COLOR
            strokeWidth = 155f//dash length
            setShadowLayer(0f, 0f, 0f, ON_COLOR)
            isAntiAlias = true
        }
        offMarkPaint = Paint().apply {
            color = OFF_COLOR
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 55f
        }
        scalePaint = Paint(offMarkPaint).apply {
            strokeWidth = 2f
            textSize = SCALE_SIZE
            setShadowLayer(5f, 0f, 0f, Color.RED)
            color = SCALE_COLOR
        }
        readingPaint = Paint(scalePaint).apply {
            style = Paint.Style.FILL_AND_STROKE
            setShadowLayer(3f, 0f, 0f, Color.WHITE)
            textSize = 65f
            typeface = Typeface.SANS_SERIF
            color = Color.WHITE
        }

        onPath = Path()
        offPath = Path()
    }

    fun getCurrentSpeed() = currentSpeed

    private fun checkLimits(speed: Float)= when {
            speed > maxSpeed -> {
                maxSpeed
            }
            speed < 0 -> {
                0f
            }
            else -> {
                speed
            }
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        //Setting up the oval area in which the arc will be drawn
        radius = if (width > height) {
            height.div(2.5f)
        } else {
            width.div(2.5f)
        }

        oval.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val chosenDimension = min(chosenWidth, chosenHeight)
        centerX = chosenDimension.div(2f)
        centerY = chosenDimension.div(2f)

        setMeasuredDimension(chosenDimension, chosenDimension)
    }

    private fun chooseDimension(mode: Int, size: Int) =
        when (mode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
            else -> 300
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGaugeBackground(canvas)
        drawScaleBackground(canvas)
        drawLegend(canvas)
        drawArrow(canvas)

        drawReadings(canvas)
    }
    var objectAnimator: ObjectAnimator? = null
    private fun setSpeedAnimated(speed: Float) {
        if (objectAnimator != null) {
            objectAnimator?.cancel()
        }

        objectAnimator = ObjectAnimator.ofFloat(this, "currentSpeed", currentSpeed, speed).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }

        objectAnimator?.start()
    }

    private fun drawArrow(canvas: Canvas) {
        val limit = (currentSpeed / maxSpeed * 270 - 270)

        canvas.save()

        canvas.rotate(limit, centerX, centerY)

        val paintArrow = Paint().apply {
            color = Color.RED
            strokeWidth = (22f)
        }
        canvas.drawLine(centerX, centerY, radius * 2, radius * 2, paintArrow)

        val paintArrowHolder = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius / 7, paintArrowHolder)

        canvas.restore()
    }

    private fun drawGaugeBackground(canvas: Canvas) {
        val paintBackground = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }

        canvas.drawCircle(centerX, centerY, radius + 100f, paintBackground)
    }

    private fun drawReadings(canvas: Canvas) {
        val path = Path()
        val message = String.format("%d", currentSpeed.toInt())
        val widths = FloatArray(message.length)
        readingPaint.getTextWidths(message, widths)
        var advance = 0f
        for (width in widths) advance += width
        path.moveTo(centerX - advance / 2, centerY)
        path.lineTo(centerX + advance / 2, centerY)
        canvas.drawTextOnPath(message, path, 0f, 20f, readingPaint)
    }

    private fun drawLegend(canvas: Canvas) {
        canvas.save()

        canvas.rotate(135f, centerX, centerY)
        val circle = Path()

        val halfCircumference = radius * Math.PI * 1.5
        val increment = 10

        for (i in 0..maxSpeed.toInt() step increment) {
            circle.addCircle(centerX, centerY, radius, Path.Direction.CW)
            canvas.drawTextOnPath(
                drawDigit(i),
                circle,
                (i * halfCircumference / maxSpeed).toFloat(),
                -50f,
                scalePaint
            )
        }

        canvas.restore()
    }

    private fun drawDigit(digit: Int): String =
        if (digit % 20 == 0) {
            digit.toString()
        } else {
            "|"
        }

    private fun drawScaleBackground(canvas: Canvas?) {
        offPath.reset()

        for (i in -225..44 step 1) {
            offPath.addArc(oval, i.toFloat(), 2f)
        }
        canvas?.drawPath(offPath, offMarkPaint)
    }

    private fun setCurrentSpeed(speed: Float) {
        currentSpeed = checkLimits(speed)
        invalidate()
    }

    override fun setSpeedChanged(speed: Float) {
        setSpeedAnimated(speed)
    }

}