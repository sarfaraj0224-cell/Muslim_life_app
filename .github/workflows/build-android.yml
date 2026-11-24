
package com.muslimlife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("muslim_life", MODE_PRIVATE)

        setContent {
            MuslimLifeApp(
                savedCount = prefs.getInt("tasbih_count", 0),
                savedLat = prefs.getString("latitude", "25.8617") ?: "25.8617",
                savedLon = prefs.getString("longitude", "85.7784") ?: "85.7784",
                savedCity = prefs.getString("city", "समस्तीपुर, बिहार") ?: "समस्तीपुर, बिहार",
                savedManual = prefs.getBoolean("manual_mode", false),
                savedManualTimes = listOf(
                    prefs.getString("manual_fajr", "") ?: "",
                    prefs.getString("manual_dhuhr", "") ?: "",
                    prefs.getString("manual_asr", "") ?: "",
                    prefs.getString("manual_maghrib", "") ?: "",
                    prefs.getString("manual_isha", "") ?: ""
                ),
                onSaveCount = {
                    prefs.edit().putInt("tasbih_count", it).apply()
                },
                onSaveSettings = { city, lat, lon ->
                    prefs.edit()
                        .putString("city", city)
                        .putString("latitude", lat)
                        .putString("longitude", lon)
                        .apply()
                },
                onSaveManual = { enabled, times ->
                    prefs.edit()
                        .putBoolean("manual_mode", enabled)
                        .putString("manual_fajr", times[0])
                        .putString("manual_dhuhr", times[1])
                        .putString("manual_asr", times[2])
                        .putString("manual_maghrib", times[3])
                        .putString("manual_isha", times[4])
                        .apply()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuslimLifeApp(
    savedCount: Int,
    savedLat: String,
    savedLon: String,
    savedCity: String,
    savedManual: Boolean,
    savedManualTimes: List<String>,
    onSaveCount: (Int) -> Unit,
    onSaveSettings: (String, String, String) -> Unit,
    onSaveManual: (Boolean, List<String>) -> Unit
) {
    var page by remember { mutableStateOf("Home") }
    var count by remember { mutableIntStateOf(savedCount) }
    var goal by remember { mutableIntStateOf(33) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        repeat(Int.MAX_VALUE) {
