package com.example.snapmemo.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.snapmemo.R
import com.example.snapmemo.data.local.db.dao.DailyStat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HeatmapCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellRect = RectF()
    private val levelColors: IntArray

    private val cellSize: Float
    private val cellGap: Float
    private var dailyStats: Map<String, Int> = emptyMap()

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        val density = context.resources.displayMetrics.density
        cellSize = 12 * density
        cellGap = 2 * density

        levelColors = intArrayOf(
            ContextCompat.getColor(context, R.color.heatmap_level_0),
            ContextCompat.getColor(context, R.color.heatmap_level_1),
            ContextCompat.getColor(context, R.color.heatmap_level_2),
            ContextCompat.getColor(context, R.color.heatmap_level_3),
            ContextCompat.getColor(context, R.color.heatmap_level_4)
        )
    }

    fun setData(stats: List<DailyStat>) {
        dailyStats = stats.associate { it.day to it.count }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val today = LocalDate.now()
        val weeks = 53
        val cellStep = cellSize + cellGap

        for (w in 0 until weeks) {
            for (d in 0 until 7) {
                val daysAgo = (weeks - 1 - w) * 7 + (6 - d)
                val date = today.minusDays(daysAgo.toLong())
                val dateStr = date.format(formatter)
                val count = dailyStats[dateStr] ?: 0

                val level = when {
                    count == 0 -> 0
                    count <= 2 -> 1
                    count <= 5 -> 2
                    count <= 10 -> 3
                    else -> 4
                }

                val x = w * cellStep
                val y = d * cellStep

                paint.color = levelColors[level]
                cellRect.set(x, y, x + cellSize, y + cellSize)
                canvas.drawRoundRect(cellRect, 2f, 2f, paint)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cellStep = cellSize + cellGap
        val desiredWidth = (53 * cellStep).toInt()
        val desiredHeight = (7 * cellStep).toInt()
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }
}
