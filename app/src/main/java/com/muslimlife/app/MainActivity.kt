
package com.muslimlife.app
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
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
                savedLat = prefs.getString("latitude", "25.86") ?: "25.86",
                savedLon = prefs.getString("longitude", "85.78") ?: "85.78",
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

    var city by remember { mutableStateOf(savedCity) }
    var latText by remember { mutableStateOf(savedLat) }
    var lonText by remember { mutableStateOf(savedLon) }

    var method by remember { mutableStateOf("MUSLIM_WORLD_LEAGUE") }
    var madhab by remember { mutableStateOf("HANAFI") }

    var manual by remember { mutableStateOf(savedManual) }
    var manualFajr by remember { mutableStateOf(savedManualTimes.getOrElse(0) { "" }) }
    var manualDhuhr by remember { mutableStateOf(savedManualTimes.getOrElse(1) { "" }) }
    var manualAsr by remember { mutableStateOf(savedManualTimes.getOrElse(2) { "" }) }
    var manualMaghrib by remember { mutableStateOf(savedManualTimes.getOrElse(3) { "" }) }
    var manualIsha by remember { mutableStateOf(savedManualTimes.getOrElse(4) { "" }) }

    val lat = latText.toDoubleOrNull()
    val lon = lonText.toDoubleOrNull()

    val validLocation = lat != null && lon != null &&
        lat in -90.0..90.0 && lon in -180.0..180.0

    val automaticTimes = remember(lat, lon, method, madhab) {
        if (validLocation) calculatePrayerRows(lat!!, lon!!, method, madhab)
        else emptyList()
    }

    val prayerRows = if (manual) {
        listOf(
            "फ़ज्र" to manualFajr,
            "ज़ुहर" to manualDhuhr,
            "अस्र" to manualAsr,
            "मग़रिब" to manualMaghrib,
            "ईशा" to manualIsha
        )
    } else automaticTimes

    val ishraq = if (!manual && validLocation) {
        automaticTimes.find { it.first == "सूर्योदय" }?.second
            ?.let { addMinutes(it, 15) } ?: "—"
    } else "ऑटो समय उपलब्ध होने पर"

    val tahajjud = if (!manual && validLocation) {
        calculateTahajjud(lat!!, lon!!, method, madhab)
    } else "—"

    val nextPrayer = if (manual) {
        nextPrayerFromManual(prayerRows)
    } else {
        nextPrayerFromRows(prayerRows)
    }

    val qibla = if (validLocation) qiblaBearing(lat!!, lon!!) else null

    MaterialTheme(
        colorScheme = lightColorScheme(primary = Color(0xFF176B50))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color(0xFF0B3D2E),
    titleContentColor = Color.White
),
                    title = {
                        Column {
                            Text(
    "Noor-e-Zindagi",
    fontWeight = FontWeight.Bold,
    color = Color.White
)
                          Text(
    "आपकी इबादत का साथी",
    style = MaterialTheme.typography.labelSmall,
    color = Color.White
)
                        }
                    }
                )
            },
            bottomBar = {
               NavigationBar(
    containerColor = Color(0xFF0B3D2E),
    contentColor = Color.White
) {
                    listOf("Home", "Quran", "Tasbih", "Ramadan", "More").forEach { item ->
                        NavigationBarItem(
                            selected = page == item,
                            colors = NavigationBarItemDefaults.colors(
    selectedIconColor = Color(0xFFD4AF37),
    selectedTextColor = Color(0xFFD4AF37),
    unselectedIconColor = Color.White,
    unselectedTextColor = Color.White,
    indicatorColor = Color(0xFF176B50)
),
                            onClick = { page = item },
                            icon = {
                                Icon(
                                    when (item) {
                                        "Home" -> Icons.Default.Home
                                        "Quran" -> Icons.Default.MenuBook
                                        "Tasbih" -> Icons.Default.Fingerprint
                                        "Ramadan" -> Icons.Default.NightsStay
                                        else -> Icons.Default.Explore
                                    },
                                    contentDescription = item
                                )
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
    Image(
        painter = painterResource(R.drawable.bg_islamic),
        contentDescription = "Islamic mosque background",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (page) {
                    "Home" -> {
                        Text(
                            "अस्सलामु अलैकुम",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(city)
                        Text(
                            SimpleDateFormat(
                                "EEEE, dd MMMM yyyy",
                                Locale.getDefault()
                            ).format(Date())
                        )

                        Card(
    colors = CardDefaults.cardColors(
    containerColor = Color(0xCC0B3D2E),
    contentColor = Color.White
),
    border = BorderStroke(1.dp, Color(0xFFD4AF37))
) {
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp)
                            ) {
                                Text(
                                    "नमाज़ के समय",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("स्थान: $city")

                                if (prayerRows.isEmpty()) {
                                    Text("समय निकालने के लिए More में स्थान जाँचें।")
                                } else {
                                    prayerRows.forEach {
                                        PrayerRow(it.first, it.second.ifBlank { "सेट करें" })
                                    }
                                }

                                HorizontalDivider()
                                Text(
                                    "अगली नमाज़: $nextPrayer",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (manual) "मैनुअल समय चालू है।"
                                    else "ऑटोमैटिक समय • स्थानीय मस्जिद के कैलेंडर से मिलान करें।",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        InfoCard(
                            "इशराक",
                            "अनुमानित समय: $ishraq\nसूर्योदय के बाद मकरूह समय समाप्त होने पर पढ़ें। स्थानीय समय की पुष्टि करें।"
                        )

                        InfoCard(
                            "तहज्जुद",
                            "आख़िरी एक-तिहाई रात की शुरुआत का अनुमान: $tahajjud\nतहज्जुद ईशा के बाद से फ़ज्र से पहले तक पढ़ी जा सकती है।"
                        )

                        Button(
                            onClick = {
                                manual = !manual
                                onSaveManual(
                                    manual,
                                    listOf(
                                        manualFajr, manualDhuhr, manualAsr,
                                        manualMaghrib, manualIsha
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (manual) "ऑटोमैटिक गणना पर लौटें"
                                else "मैनुअल नमाज़ समय दर्ज करें"
                            )
                        }

                        if (manual) {
                            TimeField("फ़ज्र", manualFajr) { manualFajr = it }
                            TimeField("ज़ुहर", manualDhuhr) { manualDhuhr = it }
                            TimeField("अस्र", manualAsr) { manualAsr = it }
                            TimeField("मग़रिब", manualMaghrib) { manualMaghrib = it }
                            TimeField("ईशा", manualIsha) { manualIsha = it }

                            Button(
                                onClick = {
                                    onSaveManual(
                                        true,
                                        listOf(
                                            manualFajr, manualDhuhr, manualAsr,
                                            manualMaghrib, manualIsha
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("मैनुअल समय सेव करें")
                            }
                        }
                    }

                    "Quran" -> {
                        Text(
                            "कुरआन शरीफ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        InfoCard(
                            "कुरआन लाइब्रेरी",
                            "पूरा कुरआन और हिंदी/उर्दू अनुवाद अभी इस ऐप में शामिल नहीं हैं। प्रमाणित पाठ और अनुमति-प्राप्त अनुवाद जोड़ना बाकी है।"
                        )
                    }

                    "Tasbih" -> {
                        Text(
                            "डिजिटल तस्बीह",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Card {
                            Column(
                                Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("लक्ष्य: $goal")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(33, 99, 100).forEach { n ->
                                        OutlinedButton(onClick = { goal = n }) {
                                            Text("$n")
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        count++
                                        onSaveCount(count)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("तस्बीह गिनें")
                                }
                                OutlinedButton(
                                    onClick = {
                                        count = 0
                                        onSaveCount(0)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("रीसेट")
                                }
                                if (count >= goal) {
                                    Text(
                                        "माशाअल्लाह! आपका लक्ष्य पूरा हुआ।",
                                        color = Color(0xFF176B50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        InfoCard(
                            "ज़िक्र",
                            "سُبْحَانَ اللّٰهِ — सुब्हानअल्लाह\n" +
                                "الْحَمْدُ لِلّٰهِ — अल्हम्दुलिल्लाह\n" +
                                "اللّٰهُ أَكْبَرُ — अल्लाहु अकबर"
                        )
                    }

                    "Ramadan" -> {
                        Text(
                            "रमज़ान मुबारक",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        InfoCard(
                            "सेहरी और इफ्तार",
                            "सेहरी का आख़िरी समय फ़ज्र से पहले और इफ्तार का समय मग़रिब पर होता है। स्थानीय मस्जिद के समय से पुष्टि करें।"
                        )
                        InfoCard(
                            "रोज़े की नीयत",
                            "नीयत दिल के इरादे का नाम है। अलग अरबी वाक्य पढ़ना अनिवार्य नहीं।"
                        )
                        InfoCard(
                            "इफ्तार की दुआ",
                            "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ"
                        )
                    }

                    else -> {
                        Text(
                            "सेटिंग और अन्य सुविधाएँ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            city,
                            { city = it },
                            label = { Text("शहर का नाम") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            latText,
                            { latText = it },
                            label = { Text("Latitude / अक्षांश") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            lonText,
                            { lonText = it },
                            label = { Text("Longitude / देशांतर") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Calculation method")
                        DropdownChoice(
                            method,
                            listOf(
                                "MUSLIM_WORLD_LEAGUE", "EGYPTIAN", "KARACHI",
                                "UMM_AL_QURA", "DUBAI", "QATAR", "KUWAIT",
                                "SINGAPORE", "NORTH_AMERICA"
                            )
                        ) { method = it }

                        Text("Asr method")
                        DropdownChoice(
                            madhab,
                            listOf("HANAFI", "SHAFI")
                        ) { madhab = it }

                        Button(
                            onClick = {
                                onSaveSettings(city, latText, lonText)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("स्थान सेव करें")
                        }

                        InfoCard(
                            "क़िबला दिशा",
                            qibla?.let {
                                "काबा की दिशा: ${"%.1f".format(it)}° उत्तर से clockwise"
                            } ?: "सही coordinates दर्ज करें।"
                        )
                        InfoCard(
                            "भाषा",
                            "हिंदी • اردو • العربية • English — अन्य भाषाओं का पूरा अनुवाद अभी जोड़ना बाकी है।"
                        )
                    }
                }

                Text(
                 "Muslim Life • प्रारंभिक संस्करण",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            }
        }
    }
}

fun calculatePrayerRows(
    lat: Double,
    lon: Double,
    method: String,
    madhab: String
): List<Pair<String, String>> {
    return try {
        val coordinates = Coordinates(lat, lon)
        val date = DateComponents.from(Date())

        val params = when (method) {
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN.parameters
            "KARACHI" -> CalculationMethod.KARACHI.parameters
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA.parameters
            "DUBAI" -> CalculationMethod.DUBAI.parameters
            "QATAR" -> CalculationMethod.QATAR.parameters
            "KUWAIT" -> CalculationMethod.KUWAIT.parameters
            "SINGAPORE" -> CalculationMethod.SINGAPORE.parameters
            "NORTH_AMERICA" -> CalculationMethod.NORTH_AMERICA.parameters
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        }

        params.madhab =
            if (madhab == "HANAFI") Madhab.HANAFI else Madhab.SHAFI

        val times = PrayerTimes(coordinates, date, params)
        val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())

        listOf(
            "फ़ज्र" to fmt.format(times.fajr),
            "सूर्योदय" to fmt.format(times.sunrise),
            "ज़ुहर" to fmt.format(times.dhuhr),
            "अस्र" to fmt.format(times.asr),
            "मग़रिब" to fmt.format(times.maghrib),
            "ईशा" to fmt.format(times.isha)
        )
    } catch (_: Exception) {
        listOf("समय गणना" to "जाँच आवश्यक")
    }
}

fun addMinutes(time: String, minutes: Int): String {
    return try {
        val input = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = input.parse(time) ?: return "—"
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutes)
        input.format(calendar.time)
    } catch (_: Exception) {
        "—"
    }
}

fun calculateTahajjud(
    lat: Double,
    lon: Double,
    method: String,
    madhab: String
): String {
    return try {
        val coordinates = Coordinates(lat, lon)
        val today = Calendar.getInstance()
        val todayDate = DateComponents.from(today.time)

        val params = when (method) {
            "EGYPTIAN" -> CalculationMethod.EGYPTIAN.parameters
            "KARACHI" -> CalculationMethod.KARACHI.parameters
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA.parameters
            "DUBAI" -> CalculationMethod.DUBAI.parameters
            "QATAR" -> CalculationMethod.QATAR.parameters
            "KUWAIT" -> CalculationMethod.KUWAIT.parameters
            "SINGAPORE" -> CalculationMethod.SINGAPORE.parameters
            "NORTH_AMERICA" -> CalculationMethod.NORTH_AMERICA.parameters
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        }
        params.madhab =
            if (madhab == "HANAFI") Madhab.HANAFI else Madhab.SHAFI

        val tonight = PrayerTimes(coordinates, todayDate, params)
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimes = PrayerTimes(
            coordinates,
            DateComponents.from(tomorrow.time),
            params
        )

        val nightStart = tonight.maghrib.time
        val nightEnd = tomorrowTimes.fajr.time
        val duration = nightEnd - nightStart
        val lastThirdStart = nightStart + (duration * 2L / 3L)

        SimpleDateFormat("hh:mm a", Locale.getDefault())
            .format(Date(lastThirdStart))
    } catch (_: Exception) {
        "—"
    }
}

fun nextPrayerFromRows(rows: List<Pair<String, String>>): String {
    if (rows.isEmpty()) return "समय उपलब्ध नहीं"
    val names = listOf("फ़ज्र", "ज़ुहर", "अस्र", "मग़रिब", "ईशा")
    val now = Calendar.getInstance()
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 +
        now.get(Calendar.MINUTE)

    val upcoming = rows.filter { it.first in names }.mapNotNull { row ->
        val minutes = parseTimeMinutes(row.second) ?: return@mapNotNull null
        Triple(row.first, minutes, minutes - currentMinutes)
    }.filter { it.third > 0 }.minByOrNull { it.third }

    return upcoming?.first ?: "कल की फ़ज्र"
}

fun nextPrayerFromManual(rows: List<Pair<String, String>>): String =
    nextPrayerFromRows(rows)

fun parseTimeMinutes(value: String): Int? {
    return try {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = format.parse(value) ?: return null
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.get(Calendar.HOUR_OF_DAY) * 60 +
            calendar.get(Calendar.MINUTE)
    } catch (_: Exception) {
        null
    }
}

fun qiblaBearing(latitude: Double, longitude: Double): Double {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)
    val lat = Math.toRadians(latitude)
    val lon = Math.toRadians(longitude)
    val dLon = kaabaLon - lon
    val y = sin(dLon)
    val x = cos(lat) * kotlin.math.tan(kaabaLat) -
        sin(lat) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

@Composable
fun DropdownChoice(
    value: String,
    choices: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(value.replace('_', ' '))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            choices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice.replace('_', ' ')) },
                    onClick = {
                        onChange(choice)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PrayerRow(name: String, time: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(name)
        Text(time, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TimeField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value,
        onChange,
        label = { Text("$label (hh:mm AM/PM)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun InfoCard(title: String, body: String) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(body)
        }
    }
}
