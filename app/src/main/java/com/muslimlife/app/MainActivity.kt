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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import java.text.SimpleDateFormat
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
                savedLat = prefs.getString("latitude", "") ?: "",
                savedLon = prefs.getString("longitude", "") ?: "",
                onSaveCount = { prefs.edit().putInt("tasbih_count", it).apply() },
                onSaveCoordinates = { lat, lon ->
                    prefs.edit().putString("latitude", lat).putString("longitude", lon).apply()
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
    onSaveCount: (Int) -> Unit,
    onSaveCoordinates: (String, String) -> Unit
) {
    var page by remember { mutableStateOf("Home") }
    var count by remember { mutableIntStateOf(savedCount) }
    var goal by remember { mutableIntStateOf(33) }
    var latText by remember { mutableStateOf(savedLat) }
    var lonText by remember { mutableStateOf(savedLon) }
    var city by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("MUSLIM_WORLD_LEAGUE") }
    var madhab by remember { mutableStateOf("HANAFI") }
    var manual by remember { mutableStateOf(false) }
    var manualFajr by remember { mutableStateOf("") }
    var manualDhuhr by remember { mutableStateOf("") }
    var manualAsr by remember { mutableStateOf("") }
    var manualMaghrib by remember { mutableStateOf("") }
    var manualIsha by remember { mutableStateOf("") }

    val lat = latText.toDoubleOrNull()
    val lon = lonText.toDoubleOrNull()
    val prayerRows = remember(lat, lon, method, madhab, manual, manualFajr, manualDhuhr, manualAsr, manualMaghrib, manualIsha) {
        if (manual) listOf(
            "फ़ज्र" to manualFajr, "सूर्योदय" to "—", "ज़ुहर" to manualDhuhr,
            "अस्र" to manualAsr, "मग़रिब / इफ्तार" to manualMaghrib, "ईशा" to manualIsha
        ) else calculatePrayerRows(lat, lon, method, madhab)
    }
    val qibla = if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) qiblaBearing(lat, lon) else null

    MaterialTheme(colorScheme = lightColorScheme(primary = androidx.compose.ui.graphics.Color(0xFF176B50))) {
        Scaffold(
            topBar = { TopAppBar(title = {
                Column {
                    Text("Muslim Life", fontWeight = FontWeight.Bold)
                    Text("आपकी इबादत का साथी", style = MaterialTheme.typography.labelSmall)
                }
            }) },
            bottomBar = {
                NavigationBar {
                    listOf("Home", "Quran", "Tasbih", "Ramadan", "More").forEach { item ->
                        NavigationBarItem(
                            selected = page == item, onClick = { page = item },
                            icon = {
                                Icon(when(item) {
                                    "Home" -> Icons.Default.Home
                                    "Quran" -> Icons.Default.MenuBook
                                    "Tasbih" -> Icons.Default.Fingerprint
                                    "Ramadan" -> Icons.Default.NightsStay
                                    else -> Icons.Default.Explore
                                }, contentDescription = item)
                            },
                            label = { Text(item) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (page) {
                    "Home" -> {
                        Text("अस्सलामु अलैकुम", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()))
                        Card {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("नमाज़ के समय", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(if (city.isBlank()) "स्थान: अक्षांश/देशांतर से" else "स्थान: $city")
                                prayerRows.forEach { PrayerRow(it.first, it.second.ifBlank { "सेट करें" }) }
                                Text(
                                    if (lat == null || lon == null) "पहले More → सेटिंग में अपने शहर के coordinates डालें।"
                                    else if (manual) "मैनुअल समय मोड चालू है।"
                                    else "गणना Adhan लाइब्रेरी से है। स्थानीय मस्जिद के समय से मिलान करें।",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        InfoCard("इशराक", "सूर्योदय के बाद, जब मकरूह समय समाप्त हो जाए। सटीक समय स्थानीय सूर्योदय से तय करें।")
                        InfoCard("तहज्जुद", "ईशा के बाद से फ़ज्र से पहले; रात के आख़िरी हिस्से में पढ़ना अफ़ज़ल माना जाता है।")
                        Button(onClick = { manual = !manual }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (manual) "ऑटोमैटिक गणना पर लौटें" else "मैनुअल नमाज़ समय दर्ज करें")
                        }
                        if (manual) {
                            TimeField("फ़ज्र", manualFajr) { manualFajr = it }
                            TimeField("ज़ुहर", manualDhuhr) { manualDhuhr = it }
                            TimeField("अस्र", manualAsr) { manualAsr = it }
                            TimeField("मग़रिब", manualMaghrib) { manualMaghrib = it }
                            TimeField("ईशा", manualIsha) { manualIsha = it }
                        }
                    }
                    "Quran" -> {
                        Text("कुरआन शरीफ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("कुरआन लाइब्रेरी", "इस ZIP में पूरा कुरआन टेक्स्ट/अनुवाद शामिल नहीं है। सही अरबी आयतें और हिंदी/उर्दू अनुवाद भरोसेमंद, अनुमति-प्राप्त स्रोत से जोड़ना ज़रूरी है; गलत धार्मिक पाठ दिखाने से बचने के लिए यह सेक्शन अभी placeholder है।")
                        Text("114 सूरह की सूची और ऑडियो अगले डेटा-इंटीग्रेशन चरण में जोड़े जाएँगे।")
                    }
                    "Tasbih" -> {
                        Text("डिजिटल तस्बीह", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Card {
                            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$count", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                                Text("लक्ष्य: $goal")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(33, 99, 100).forEach { n -> OutlinedButton(onClick = { goal = n }) { Text("$n") } }
                                }
                                Button(onClick = { count++; onSaveCount(count) }, modifier = Modifier.fillMaxWidth()) { Text("तस्बीह गिनें") }
                                OutlinedButton(onClick = { count = 0; onSaveCount(count) }, modifier = Modifier.fillMaxWidth()) { Text("रीसेट") }
                            }
                        }
                        InfoCard("ज़िक्र", "سُبْحَانَ اللّٰهِ — सुब्हानअल्लाह\nالْحَمْدُ لِلّٰهِ — अल्हम्दुलिल्लाह\nاللّٰهُ أَكْبَرُ — अल्लाहु अकबर")
                    }
                    "Ramadan" -> {
                        Text("रमज़ान मुबारक", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        InfoCard("सेहरी और इफ्तार", "सेहरी का आख़िरी समय फ़ज्र से पहले और इफ्तार का समय मग़रिब पर होता है। ऊपर दिखने वाले फ़ज्र/मग़रिब समय को अपने स्थानीय इस्लामिक सेंटर के कैलेंडर से सत्यापित करें।")
                        InfoCard("रोज़े की नीयत", "नीयत दिल के इरादे का नाम है। अलग अरबी वाक्य पढ़ना अनिवार्य नहीं।")
                        InfoCard("इफ्तार की दुआ", "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللّٰهُ\n\nज़हबज़-ज़मأु वबतल्लतिल-उरूक़ु वसबतल-अज्रु इन्शा अल्लाह।")
                        InfoCard("रमज़ान कैलेंडर", "पूरे महीने का सही timetable शहर, गणना पद्धति और स्थानीय चाँद देखने के ऐलान पर निर्भर है। मासिक कैलेंडर/नोटिफिकेशन अगले चरण में जोड़े जाएँगे।")
                    }
                    else -> {
                        Text("सेटिंग और अन्य सुविधाएँ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        OutlinedTextField(city, { city = it }, label = { Text("शहर का नाम (वैकल्पिक)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(latText, { latText = it }, label = { Text("Latitude / अक्षांश") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(lonText, { lonText = it }, label = { Text("Longitude / देशांतर") }, modifier = Modifier.fillMaxWidth())
                        Text("Calculation method")
                        DropdownChoice(method, listOf("MUSLIM_WORLD_LEAGUE", "EGYPTIAN", "KARACHI", "UMM_AL_QURA", "DUBAI", "QATAR", "KUWAIT", "SINGAPORE", "NORTH_AMERICA")) { method = it }
                        Text("Asr method")
                        DropdownChoice(madhab, listOf("HANAFI", "SHAFI")) { madhab = it }
                        Button(onClick = { onSaveCoordinates(latText, lonText) }, modifier = Modifier.fillMaxWidth()) { Text("स्थान सेव करें") }
                        InfoCard("क़िबला दिशा", qibla?.let { "काबा की दिशा: ${"%.1f".format(it)}° उत्तर से clockwise" } ?: "क़िबला दिशा के लिए सही latitude और longitude दर्ज करें।")
                        InfoCard("भाषा", "हिंदी • اردو • العربية • English — इस starter में UI अभी मुख्यतः हिंदी में है।")
                        InfoCard("हिजरी कैलेंडर", "हिजरी तारीख़ स्थानीय चाँद देखने के अनुसार बदल सकती है; प्रमाणित कैलेंडर इंटीग्रेशन बाकी है।")
                    }
                }
                Text("Muslim Life • प्रारंभिक प्रोजेक्ट", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

fun calculatePrayerRows(lat: Double?, lon: Double?, method: String, madhab: String): List<Pair<String, String>> {
    if (lat == null || lon == null || lat !in -90.0..90.0 || lon !in -180.0..180.0) {
        return listOf("फ़ज्र" to "—", "सूर्योदय" to "—", "ज़ुहर" to "—", "अस्र" to "—", "मग़रिब" to "—", "ईशा" to "—")
    }
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
        params.madhab = if (madhab == "HANAFI") Madhab.HANAFI else Madhab.SHAFI
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
    } catch (_: Exception) { listOf("समय गणना" to "जाँच आवश्यक") }
}

fun qiblaBearing(latitude: Double, longitude: Double): Double {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLon = Math.toRadians(39.8262)
    val lat = Math.toRadians(latitude)
    val lon = Math.toRadians(longitude)
    val dLon = kaabaLon - lon
    val y = sin(dLon)
    val x = cos(lat) * kotlin.math.tan(kaabaLat) - sin(lat) * cos(dLon)
    return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
}

@Composable
fun DropdownChoice(value: String, choices: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(value.replace('_', ' ')) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEach { choice ->
                DropdownMenuItem(text = { Text(choice.replace('_', ' ')) }, onClick = {
                    onChange(choice); expanded = false
                })
            }
        }
    }
}

@Composable
fun PrayerRow(name: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name); Text(time, fontWeight = FontWeight.SemiBold)
    }
}
@Composable
fun TimeField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value, onChange, label = { Text("$label (HH:MM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}
@Composable
fun InfoCard(title: String, body: String) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp)); Text(body)
        }
    }
}
