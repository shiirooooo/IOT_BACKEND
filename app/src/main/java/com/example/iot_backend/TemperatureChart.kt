package com.example.iot_backend

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.unit.dp


@Composable
fun ShowTemperatureChart(rataSuhu: Double, suhuTertinggi: Double) {
    LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        factory = { ctx ->
            createLineChart(ctx, rataSuhu, suhuTertinggi)
        }
    )
}

fun createLineChart(context: Context, rataSuhu: Double, suhuTertinggi: Double): LineChart {
    val lineChart = LineChart(context)
    val entries = ArrayList<Entry>()

    // Data rata-rata suhu dan suhu tertinggi
    entries.add(Entry(1f, rataSuhu.toFloat()))
    entries.add(Entry(2f, suhuTertinggi.toFloat()))

    // Mengatur data dan konfigurasi chart
    val lineDataSet = LineDataSet(entries, "Temperatures")
    lineDataSet.color = Color.BLUE
    lineDataSet.valueTextColor = Color.BLACK

    val lineData = LineData(lineDataSet)
    lineChart.data = lineData
    lineChart.description = Description().apply { text = "Rata-rata dan Max Suhu" }

    lineChart.axisRight.isEnabled = false
    lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    lineChart.xAxis.setDrawGridLines(false)

    lineChart.invalidate() // Refresh chart
    return lineChart
}
