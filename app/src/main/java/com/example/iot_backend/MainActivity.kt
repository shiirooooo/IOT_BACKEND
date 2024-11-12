package com.example.iot_backend

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.core.tween

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
        var buttonText by remember { mutableStateOf("Ambil Data Suhu") }
        var isLogoVisible by remember { mutableStateOf(true) } // State untuk visibility logo
        val primaryColor = Color(0xFF3670C0) // Warna dasar
        val lightTextColor = Color(0xFF99DDF8) // Warna biru muda untuk teks

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
            // Menampilkan logo hanya jika isLogoVisible = true
            if (isLogoVisible) {
                Image(
                    painter = painterResource(id = R.drawable.cuaca), // Gantilah 'logo' dengan nama gambar Anda
                    contentDescription = "Logo Cuaca",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(350.dp), // Sesuaikan ukuran logo sesuai kebutuhan
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (buttonText == "Ambil Data Suhu") {
                        fetchApiData { suhu, maxHumid, monthYear ->
                            suhuData = suhu
                            maxHumidData = maxHumid
                            monthYearData = monthYear
                            isDataVisible = true
                            isLogoVisible = false // Sembunyikan logo saat data mulai ditampilkan
                            buttonText = "Keluar"
                        }
                    } else {
                        isDataVisible = false
                        isLogoVisible = true // Tampilkan kembali logo saat keluar
                        buttonText = "Ambil Data Suhu"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animasi tampilkan data
            AnimatedVisibility(
                visible = isDataVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) + slideInVertically(
                    initialOffsetY = { -40 }),
                exit = fadeOut(animationSpec = tween(durationMillis = 500)) + slideOutVertically(
                    targetOffsetY = { 40 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    DisplayDataCard(
                        "Suhu Maksimum, Minimum, dan Rata-rata",
                        suhuData,
                        primaryColor,
                        lightTextColor
                    )
                    DisplayDataCard(
                        "Data Maksimum Suhu dan Kelembaban",
                        maxHumidData,
                        primaryColor,
                        lightTextColor
                    )
                    DisplayDataCard(
                        "Bulan dengan Suhu Tertinggi dan Kelembaban Maksimum",
                        monthYearData,
                        primaryColor,
                        lightTextColor
                    )
                }
            }
        }
    }

    @Composable
    fun DisplayDataCard(title: String, data: String, primaryColor: Color, textColor: Color) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor // Menggunakan warna dasar untuk judul
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Styling data text dengan warna teks #99DDf8
                Text(
                    text = data,
                    style = TextStyle(
                        color = textColor, // Menggunakan warna #99DDf8 untuk teks
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    ),
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
                val kelembaban = dataObject.optString(
                    "humid",
                    "N/A"
                ) // Mengganti nama variabel untuk konsistensi
                val kecerahan = dataObject.optString("kecerahan", "N/A")
                val timestamp = dataObject.optString("timestamp", "N/A")

                maxHumidData.append("Suhu: $suhu°C\n")
                maxHumidData.append("Kelembaban: $kelembaban%\n") // Mengubah "Humid" menjadi "Kelembaban"
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
