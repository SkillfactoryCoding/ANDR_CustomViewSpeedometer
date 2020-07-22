package com.amsdevelops.speedometer.presentation.customviews.speedometer

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.amsdevelops.speedometer.R
import com.amsdevelops.speedometer.constants.SpeedometerConstants
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

class SpeedometerCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs),
    SpeedChangeListener {

    private var maxSpeed = 0
    private var currentSpeed = 0f
    private val numerals = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private var objectAnimator: ObjectAnimator
    private lateinit var bitmap: Bitmap
    private lateinit var staticCanvas: Canvas

    private lateinit var dashPaintThin: Paint
    private lateinit var dashPaintWide: Paint
    private lateinit var digitsPaintBold: Paint
    private lateinit var digitsPaintThin: Paint
    private lateinit var clockPaint: Paint
    private lateinit var readingPaint: Paint
    private lateinit var paintBackground: Paint
    private lateinit var paintArrow: Paint
    private lateinit var paintArrowHolderFill: Paint
    private lateinit var paintArrowHolderStroke: Paint
    private lateinit var paintClockCircle: Paint
    private val rect = Rect()
    private var isStaticPictureDrawn = false

    private var dashColor = Color.WHITE
    private var digitColor = Color.WHITE
    private var arrowColor = Color.RED
    private var backColor = Color.DKGRAY
    private var scaleSize = 60f
    private var dashesStep = 2
    private var digitSpeedEnabled = true
    private var isHorizontalDigits = false

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    init {
        objectAnimator = ObjectAnimator.ofFloat(this, "currentSpeed", currentSpeed).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SpeedometerCustomView, 0, 0)

        try {
            maxSpeed = a.getInt(
                R.styleable.SpeedometerCustomView_maxSpeed,
                SpeedometerConstants.DEFAULT_MAX_SPEED
            )
            currentSpeed = a.getFloat(R.styleable.SpeedometerCustomView_currentSpeed, 0f)
            digitColor = a.getColor(R.styleable.SpeedometerCustomView_digitColor, digitColor)
            dashColor = a.getColor(R.styleable.SpeedometerCustomView_dashColor, dashColor)
            arrowColor = a.getColor(R.styleable.SpeedometerCustomView_arrowColor, arrowColor)
            backColor =
                a.getColor(R.styleable.SpeedometerCustomView_backgroundColor, backColor)
            scaleSize =
                a.getDimension(R.styleable.SpeedometerCustomView_scaleTextSize, scaleSize)
            digitSpeedEnabled =
                a.getBoolean(
                    R.styleable.SpeedometerCustomView_digitSpeedTextEnabled,
                    digitSpeedEnabled
                )
            isHorizontalDigits = a.getBoolean(
                R.styleable.SpeedometerCustomView_horizontalDigits,
                isHorizontalDigits
            )
            dashesStep = a.getInt(R.styleable.SpeedometerCustomView_dashesStep, dashesStep)
        } finally {
            a.recycle()
        }

        initDrawingTools()
    }

    private fun initDrawingTools() {
        dashPaintThin = Paint().apply {
            color = dashColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0.01f
            isAntiAlias = true
        }
        dashPaintWide = Paint().apply {
            color = dashColor
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 0.03f
            isAntiAlias = true
        }
        digitsPaintBold = Paint(dashPaintThin).apply {
            strokeWidth = 2f
            textSize = scaleSize
            setShadowLayer(5f, 0f, 0f, Color.RED)
            color = digitColor
            isAntiAlias = true
        }
        digitsPaintThin = Paint(dashPaintThin).apply {
            strokeWidth = 2f
            textSize = scaleSize * 0.7f
            setShadowLayer(5f, 0f, 0f, Color.RED)
            color = digitColor
            isAntiAlias = true
        }
        clockPaint = Paint(dashPaintThin).apply {
            strokeWidth = 2f
            textSize = scaleSize * 1.5f
            color = digitColor
            isAntiAlias = true
        }
        readingPaint = Paint(digitsPaintBold).apply {
            style = Paint.Style.FILL_AND_STROKE
            setShadowLayer(3f, 0f, 0f, Color.WHITE)
            textSize = scaleSize
            typeface = Typeface.SANS_SERIF
            color = Color.WHITE
            isAntiAlias = true
        }
        paintBackground = Paint().apply {
            color = backColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        paintArrow = Paint().apply {
            val arrowWidth = scaleSize / 4

            color = arrowColor
            strokeWidth = arrowWidth
            setShadowLayer(5f, 0f, 0f, arrowColor)
            isAntiAlias = true
        }
        paintArrowHolderFill = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
            setShadowLayer(5f, 0f, 0f, dashColor)
            isAntiAlias = true
        }
        paintArrowHolderStroke = Paint().apply {
            strokeWidth = 5f
            color = dashColor
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        paintClockCircle = Paint().apply {
            color = dashColor
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = if (width > height) {
            height.div(2f)
        } else {
            width.div(2f)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val chosenWidth = chooseDimension(widthMode, widthSize)
        val chosenHeight = chooseDimension(heightMode, heightSize)

        val minSide = min(chosenWidth, chosenHeight)
        centerX = minSide.div(2f)
        centerY = minSide.div(2f)

        setMeasuredDimension(minSide, minSide)
    }

    private fun chooseDimension(mode: Int, size: Int) =
        when (mode) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> size
            else -> 300
        }

    private fun drawStaticPicture() {
        bitmap = Bitmap.createBitmap(
            (centerX * 2).toInt(),
            (centerY * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )
        staticCanvas = Canvas(bitmap)

        drawGaugeBackground(staticCanvas)
        drawScaleBackground(staticCanvas)
        if (isHorizontalDigits) {
            drawLegendHorizontally(staticCanvas)
        } else {
            drawLegendAroundCircle(staticCanvas)
        }
        drawClock(staticCanvas)

        isStaticPictureDrawn = true
    }


    override fun onDraw(canvas: Canvas) {
        if (!isStaticPictureDrawn) {
            drawStaticPicture()
        }

        canvas.drawBitmap(bitmap, centerX - radius, centerY - radius, null)
        drawArrow(canvas)
        if (digitSpeedEnabled) drawReadings(canvas)
        drawHands(canvas)

        postInvalidateDelayed(500)
    }


    private fun drawGaugeBackground(canvas: Canvas) {
        canvas.drawCircle(centerX, centerY, radius, paintBackground)
    }

    private fun drawScaleBackground(canvas: Canvas) {
        canvas.save()

        canvas.translate(centerX, centerY)
        canvas.rotate(135f)
        canvas.scale(radius, radius)

        val scale = 0.93f
        val step = Math.PI * 1.5 / maxSpeed
        val threeQuartersOfCircle = Math.PI * 1.5
        val stepBetweenBigDigits = 10

        for (i in 0..maxSpeed step dashesStep) {
            val angle = threeQuartersOfCircle - (step * i)
            val x1 = (cos(angle)).toFloat()
            val y1 = (sin(angle)).toFloat()

            val x2: Float
            val y2: Float

            x2 = x1 * scale
            y2 = y1 * scale

            if (i % stepBetweenBigDigits == 0) {
                canvas.drawLine(x1, y1, x2, y2, dashPaintWide)
            } else {
                canvas.drawLine(x1, y1, x2, y2, dashPaintThin)
            }

        }

        canvas.restore()
    }

    private fun drawLegendHorizontally(canvas: Canvas) {
        val countOfDashes = maxSpeed / 10
        val space = maxSpeed / 20
        val scale = 0.8

        for (i in 0..maxSpeed step 10) {
            val tmp = i.toString()
            val digitPaint = digitThickness(i)

            digitPaint.getTextBounds(tmp, 0, tmp.length, rect)

            val angle = Math.PI * 1.5 / countOfDashes * (i / 10 + space)

            val x = (centerX + cos(angle) * radius * scale - rect.width() / 2).toFloat()
            val y = (centerY + sin(angle) * radius * scale + rect.height() / 2).toFloat()

            canvas.drawText(tmp, x, y, digitPaint)
        }
    }

    private fun drawLegendAroundCircle(canvas: Canvas) {
        canvas.save()

        canvas.rotate(135f, centerX, centerY)
        val circle = Path()
        val scale = 0.8f
        val theeForthCircumference = radius * scale * Math.PI * 1.5
        val increment = 10

        for (i in 0..maxSpeed step increment) {
            val digitText = i.toString()
            val digitPaint = digitThickness(i)

            val digitTextLength = round(digitPaint.measureText(digitText))

            circle.addCircle(centerX, centerY, radius * scale, Path.Direction.CW)
            canvas.drawTextOnPath(
                digitText,
                circle,
                ((i * theeForthCircumference / maxSpeed) - digitTextLength / 2).toFloat(),
                0f,
                digitPaint
            )
        }

        canvas.restore()
    }

    private fun digitThickness(digit: Int): Paint {
        val stepBetweenThickDashes = 20

        return if (digit % stepBetweenThickDashes == 0) {
            digitsPaintBold
        } else {
            digitsPaintThin
        }
    }



    private fun drawArrow(canvas: Canvas) {
        val limit = (currentSpeed / maxSpeed.toFloat() * 270 - 270)
        val scaleOfArrow = radius / 1.7f
        val scaleOfCircle = radius / 7

        canvas.save()

        canvas.translate(centerX, centerY)
        canvas.rotate(limit)

        canvas.drawLine(0f, 0f, scaleOfArrow, radius / scaleOfArrow, paintArrow)

        canvas.drawCircle(0f, 0f, scaleOfCircle, paintArrowHolderFill)
        canvas.drawCircle(0f, 0f, scaleOfCircle, paintArrowHolderStroke)

        canvas.restore()
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

        canvas.drawTextOnPath(message, path, 0f, scaleSize / 2.5f, readingPaint)
    }

    private fun drawClock(canvas: Canvas) {
        canvas.save()
        val canvasScale = 0.3f
        val clockScale = 0.8f

        canvas.translate(centerX, centerY)
        canvas.scale(scaleX * canvasScale, scaleY * canvasScale)

        canvas.drawCircle(0f, radius * 2f, radius, paintClockCircle)

        for (number in numerals) {
            val text = number.toString()

            clockPaint.getTextBounds(text, 0, text.length, rect)

            val angle = Math.PI / 6 * (number - 3)

            val x = (cos(angle) * radius * clockScale - rect.width() / 2).toFloat()
            val y = (radius * 2 + sin(angle) * radius * clockScale + rect.height() / 2).toFloat()

            canvas.drawText(text, x, y, clockPaint)
        }

        canvas.restore()
    }

    private fun drawHands(canvas: Canvas) {
        canvas.save()
        val canvasScale = 0.3f
        val handsPlaceholderRadius = radius / 6
        canvas.translate(centerX, centerY)

        canvas.scale(scaleX * canvasScale, scaleY * canvasScale)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)

        drawHand(
            canvas,
            ((hour + calendar.get(Calendar.MINUTE) / 60.0) * 5f),
            SpeedometerConstants.HOUR_HAND
        )
        drawHand(canvas, calendar.get(Calendar.MINUTE).toDouble(), SpeedometerConstants.MINUTE_HAND)
        drawHand(canvas, calendar.get(Calendar.SECOND).toDouble(), SpeedometerConstants.SECOND_HAND)

        canvas.drawCircle(0f, radius * 2f, handsPlaceholderRadius, paintArrowHolderFill)
        canvas.drawCircle(0f, radius * 2f, handsPlaceholderRadius, paintArrowHolderStroke)

        canvas.restore()
    }

    private fun drawHand(canvas: Canvas, loc: Double, hand: Int) {
        val paintHands = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            isAntiAlias = true


            when (hand) {
                SpeedometerConstants.HOUR_HAND -> strokeWidth = scaleSize * 0.5f
                SpeedometerConstants.MINUTE_HAND -> strokeWidth = scaleSize * 0.3f
                SpeedometerConstants.SECOND_HAND -> {
                    strokeWidth = scaleSize * 0.2f
                    color = arrowColor
                }
            }
        }
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius = if (hand == SpeedometerConstants.HOUR_HAND) {
            radius * 0.7
        } else {
            radius * 0.9
        }
        canvas.drawLine(
            0f, radius * 2,
            (cos(angle) * handRadius).toFloat(),
            (radius * 2 + sin(angle) * handRadius).toFloat(),
            paintHands
        )
    }


    override fun setSpeedChanged(speed: Float) {
        setSpeedAnimated(speed)
    }

    private fun setSpeedAnimated(speed: Float) {
        objectAnimator.cancel()
        objectAnimator.setFloatValues(speed)

        objectAnimator.start()
    }

    private fun setCurrentSpeed(speed: Float) {
        currentSpeed = checkLimits(speed)
        invalidate()
    }

    private fun checkLimits(speed: Float) = when {
        speed > maxSpeed -> {
            maxSpeed.toFloat()
        }
        speed < 0 -> {
            0f
        }
        else -> {
            speed
        }
    }

    fun getCurrentSpeed() = currentSpeed
}