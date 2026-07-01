package com.app.habit.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.app.habit.R
import java.util.Calendar
import java.util.Date

class YearInPixelsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var completionDates = setOf<String>()
    
    private val emptyColor = context.getColor(R.color.fb_light_blue)
    private val completedColor = context.getColor(R.color.duo_green)
    
    private val columns = 53 // Weeks in a year
    private val rows = 7 // Days in a week
    
    // Fixed size to make them "big" and scrollable
    private val preferredCellSizeDp = 14f
    private val preferredSpacingDp = 3f
    
    private val cellSize = preferredCellSizeDp * resources.displayMetrics.density
    private val spacing = preferredSpacingDp * resources.displayMetrics.density
    private val totalCellSize = cellSize + spacing

    fun setCompletionDates(dates: List<Date>) {
        val calendar = Calendar.getInstance()
        completionDates = dates.map {
            calendar.time = it
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (totalCellSize * columns).toInt() + paddingLeft + paddingRight
        val desiredHeight = (totalCellSize * rows).toInt() + paddingTop + paddingBottom
        
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Start from the first day of the year
        calendar.set(Calendar.YEAR, currentYear)
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        
        val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        
        for (day in 1..366) {
            calendar.set(Calendar.YEAR, currentYear)
            calendar.set(Calendar.DAY_OF_YEAR, day)
            
            if (calendar.get(Calendar.YEAR) > currentYear) break
            
            val weekOfYear = (day + startDayOfWeek - 1) / 7
            val dayOfWeek = (day + startDayOfWeek - 1) % 7
            
            val left = paddingLeft + (weekOfYear * totalCellSize)
            val top = paddingTop + (dayOfWeek * totalCellSize)
            
            rect.set(left, top, left + cellSize, top + cellSize)
            
            val dateKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
            paint.color = if (completionDates.contains(dateKey)) completedColor else emptyColor
            
            canvas.drawRoundRect(rect, 6f, 6f, paint)
        }
    }
}
