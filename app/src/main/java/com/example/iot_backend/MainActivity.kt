package com.example.iot_backend

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }

    @Composable
    fun MainContent() {
        var suhuData by remember { mutableStateOf("") }
        var maxHumidData by remember { mutableStateOf("") }
        var monthYearData by remember { mutableStateOf("") }
        var isDataVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C1C1C), Color(0xFF333333))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Text(
                text = "Informasi Cuaca",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80CBC4)
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    fetchApiData { suhu, maxHumid, monthYear ->
                        suhuData = suhu
                        maxHumidData = maxHumid
                        monthYearData = monthYear
                        isDataVisible = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Ambil Data Suhu", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isDataVisible) {
                DisplayDataCard("Suhu Maksimum, Minimum, dan Rata-rata", suhuData)
                DisplayDataCard("Data Maksimum Suhu dan Kelembaban", maxHumidData)
                DisplayDataCard("Bulan dengan Suhu Tertinggi dan Kelembaban Maksimum", monthYearData)
            }
        }
    }

    @Composable
    fun DisplayDataCard(title: String, data: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80CBC4)
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data,
                    style = TextStyle(color = Color(0xFFB0BEC5), fontSize = 16.sp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }

    private fun fetchApiData(onDataReceived: (String, String, String) -> Unit) {
        val url = "http://10.0.2.2/iot_backend/datacuaca.php"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("APIResponse", "Respons API Diterima: $response")
                val (suhuData, maxHumidData, monthYearData) = parseApiResponse(response)
                onDataReceived(suhuData, maxHumidData, monthYearData)
            },
            { error ->
                Log.e("APIError", "Gagal mengambil data dari API: ${error.message}")
                onDataReceived("Error mengambil data dari API", "", "")
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun parseApiResponse(response: JSONObject): Triple<String, String, String> {
        val suhuMax = response.optString("suhu_max", "N/A")
        val suhuMin = response.optString("suhu_min", "N/A")
        val suhuAvg = response.optString("suhu_avg", "N/A")

        val suhuData = StringBuilder()
        suhuData.append("Suhu Maksimum: $suhuMax°C\n")
        suhuData.append("Suhu Minimum: $suhuMin°C\n")
        suhuData.append("Rata-rata Suhu: $suhuAvg°C\n")

        val nilaiSuhuMaxHumidMax = response.optJSONArray("nilai_suhu_max_humid_max")
        val maxHumidData = StringBuilder()
        if (nilaiSuhuMaxHumidMax != null) {
            for (i in 0 until nilaiSuhuMaxHumidMax.length()) {
                val dataObject = nilaiSuhuMaxHumidMax.getJSONObject(i)
                val suhu = dataObject.optString("suhu", "N/A")
                val humid = dataObject.optString("humid", "N/A")
                val kecerahan = dataObject.optString("kecerahan", "N/A")
                val timestamp = dataObject.optString("timestamp", "N/A")

                maxHumidData.append("Suhu: $suhu°C\n")
                maxHumidData.append("Humid: $humid%\n")
                maxHumidData.append("Kecerahan: $kecerahan\n")
                maxHumidData.append("Waktu: $timestamp\n\n")
            }
        }

        val monthYearMax = response.optJSONArray("month_year_max")
        val monthYearData = StringBuilder()
        if (monthYearMax != null) {
            for (i in 0 until monthYearMax.length()) {
                val monthYearObject = monthYearMax.getJSONObject(i)
                val monthYear = monthYearObject.optString("month_year", "N/A")
                monthYearData.append("- $monthYear\n")
            }
        }

        return Triple(suhuData.toString(), maxHumidData.toString(), monthYearData.toString())
    }
}
