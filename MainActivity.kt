
package com.mobiledoctorpro.app

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs

private val Navy = Color(0xFF101827)
private val Panel = Color(0xFF1D2B40)
private val Accent = Color(0xFF36A8F5)
private val Good = Color(0xFF16865B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiagnosticHome()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticHome() {
    val context = LocalContext.current

    var refresh by remember { mutableIntStateOf(0) }
    var telugu by remember { mutableStateOf(false) }
    var dark by remember { mutableStateOf(true) }
    var page by remember { mutableStateOf("home") }

    var screenTest by remember { mutableStateOf(false) }
    var screenColor by remember { mutableIntStateOf(0) }

    val data = remember(refresh) {
        collectDiagnostics(context)
    }

    fun tr(en: String, te: String) =
        if (telugu) te else en

    val colors = listOf(
        Color.White,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Blue
    )

    val bg = if (dark) Navy else Color(0xFFF3F6FA)
    val fg = if (dark) Color.White else Color(0xFF172033)
    val panel = if (dark) Panel else Color.White

    if (screenTest) {
        val testColor = colors[screenColor]

        Column(
            Modifier
                .fillMaxSize()
                .background(testColor)
                .clickable {
                    screenColor = (screenColor + 1) % colors.size
                }
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textColor =
                if (screenColor == 0 || screenColor == 3)
                    Color.Black
                else
                    Color.White

            Text(
                tr("Screen / Touch Test", "స్క్రీన్ / టచ్ పరీక్ష"),
                color = textColor,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            Text(
                tr(
                    "Tap anywhere to change colour",
                    "రంగు మార్చడానికి స్క్రీన్‌ను తాకండి"
                ),
                color = textColor
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    screenColor = (screenColor + 1) % colors.size
                }
            ) {
                Text(tr("Next colour", "తదుపరి రంగు"))
            }

            Button(
                onClick = {
                    screenTest = false
                    screenColor = 0
                }
            ) {
                Text(tr("Finish test", "పరీక్ష ముగించు"))
            }
        }
        return
    }

    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(
                primary = Accent,
                background = Navy,
                surface = Panel,
                onBackground = Color.White,
                onSurface = Color.White
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF1769AA),
                background = Color(0xFFF3F6FA),
                surface = Color.White,
                onBackground = Color(0xFF172033),
                onSurface = Color(0xFF172033)
            )
        }
    ) {
        Scaffold(
            containerColor = bg,

            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Mobile Doctor Pro",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "PROFESSIONAL DIAGNOSTICS",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { telugu = !telugu }
                        ) {
                            Text(if (telugu) "EN" else "తెలుగు")
                        }

                        TextButton(
                            onClick = { dark = !dark }
                        ) {
                            Text(if (dark) "☀" else "☾")
                        }

                        TextButton(
                            onClick = { refresh++ }
                        ) {
                            Text("↻")
                        }
                    }
                )
            },

            bottomBar = {
                NavigationBar(
                    containerColor = panel
                ) {
                    NavigationBarItem(
                        selected = page == "home",
                        onClick = { page = "home" },
                        icon = { Text("⌂") },
                        label = {
                            Text(tr("Home", "హోమ్"))
                        }
                    )

                    NavigationBarItem(
                        selected = page == "tests",
                        onClick = { page = "tests" },
                        icon = { Text("✓") },
                        label = {
                            Text(tr("Tests", "పరీక్షలు"))
                        }
                    )

                    NavigationBarItem(
                        selected = page == "report",
                        onClick = { page = "report" },
                        icon = { Text("▤") },
                        label = {
                            Text(tr("Report", "రిపోర్ట్"))
                        }
                    )
                }
            }
        ) { padding ->

            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                when (page) {

                    // HOME

                    "home" -> {

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = panel
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    tr(
                                        "DEVICE OVERVIEW",
                                        "ఫోన్ వివరాలు"
                                    ),
                                    color = Accent,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    data.model,
                                    color = fg,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "Android ${data.android} • ${data.manufacturer}",
                                    color = fg
                                )

                                Text(
                                    "Build: ${data.build}",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        tr(
                                            "Battery level",
                                            "బ్యాటరీ స్థాయి"
                                        ),
                                        color = fg
                                    )

                                    Text(
                                        data.battery,
                                        color = Good,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = {
                                        data.batteryPercent / 100f
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Good
                                )
                            }
                        }

                        Text(
                            tr(
                                "Diagnostic Center",
                                "డయాగ్నస్టిక్ సెంటర్"
                            ),
                            color = fg,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val items = listOf(
                            Triple(
                                "battery",
                                tr("Battery & Power", "బ్యాటరీ & పవర్"),
                                "🔋"
                            ),
                            Triple(
                                "display",
                                tr("Display & Touch", "డిస్‌ప్లే & టచ్"),
                                "▣"
                            ),
                            Triple(
                                "audio",
                                tr("Speaker & Audio", "స్పీకర్ & ఆడియో"),
                                "♫"
                            ),
                            Triple(
                                "camera",
                                tr("Camera & Flash", "కెమెరా & ఫ్లాష్"),
                                "◉"
                            ),
                            Triple(
                                "sensors",
                                tr("Sensors", "సెన్సార్లు"),
                                "⌁"
                            ),
                            Triple(
                                "network",
                                tr("Network", "నెట్‌వర్క్"),
                                "⌁"
                            ),
                            Triple(
                                "performance",
                                tr("CPU, RAM & Storage", "CPU, RAM & స్టోరేజ్"),
                                "▤"
                            )
                        )

                        items.chunked(2).forEach { pair ->
                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { item ->
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                page = item.first
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = panel
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement =
                                                Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                item.third,
                                                fontSize = 25.sp
                                            )

                                            Text(
                                                item.second,
                                                color = fg,
                                                fontWeight =
                                                    FontWeight.SemiBold
                                            )

                                            Text(
                                                tr(
                                                    "Open diagnostics",
                                                    "పరీక్ష తెరవండి"
                                                ),
                                                color = Accent,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (pair.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        Text(
                            tr("Quick actions", "త్వరిత చర్యలు"),
                            color = fg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        ActionButton(
                            tr(
                                "Start screen colour test",
                                "స్క్రీన్ రంగుల పరీక్ష"
                            )
                        ) {
                            screenTest = true
                        }

                        ActionButton(
                            tr(
                                "Open Android battery settings",
                                "బ్యాటరీ సెట్టింగ్స్"
                            )
                        ) {
                            safeStart(
                                context,
                                Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                            )
                        }
                    }

                    // BATTERY

                    "battery" -> {
                        PageTitle(
                            tr(
                                "Battery & Power Analyzer",
                                "బ్యాటరీ & పవర్ విశ్లేషణ"
                            ),
                            fg
                        )

                        MetricCard(
                            tr("Battery level", "బ్యాటరీ స్థాయి"),
                            data.battery,
                            data.temperature,
                            panel,
                            fg
                        )

                        Info(
                            tr("Charging status", "ఛార్జింగ్ స్థితి"),
                            data.charging
                        )

                        Info(
                            tr(
                                "Battery health reported by Android",
                                "Android తెలిపిన బ్యాటరీ స్థితి"
                            ),
                            data.batteryHealth
                        )

                        Info(
                            tr("Voltage", "వోల్టేజ్"),
                            data.batteryVoltage
                        )

                        Info(
                            tr("Current", "కరెంట్"),
                            data.batteryCurrent
                        )

                        Info(
                            tr("Average current", "సగటు కరెంట్"),
                            data.batteryAverageCurrent
                        )

                        Info(
                            tr("Current charge counter", "ప్రస్తుత ఛార్జ్ కౌంటర్"),
                            data.batteryCharge
                        )

                        Info(
                            tr("Charging source", "ఛార్జింగ్ సోర్స్"),
                            data.chargingSource
                        )

                        Info(
                            tr("Technology", "బ్యాటరీ టెక్నాలజీ"),
                            data.batteryTechnology
                        )

                        Note(
                            tr(
                                "Current is read from Android in microamps and converted to mA. Small readings are displayed in µA.",
                                "కరెంట్‌ను Android నుంచి microampsలో తీసుకొని mAగా మారుస్తాం. చిన్న రీడింగ్‌లను µAలో చూపిస్తాం."
                            )
                        )

                        Note(
                            tr(
                                "Invalid voltage readings are not displayed as real battery voltage.",
                                "తప్పుడు Voltage రీడింగ్‌లను నిజమైన బ్యాటరీ వోల్టేజ్‌గా చూపించము."
                            )
                        )

                        Note(
                            tr(
                                "Charge counter is not the same as full battery capacity or battery health percentage.",
                                "Charge Counter అనేది Full Battery Capacity లేదా Battery Health శాతం కాదు."
                            )
                        )
                    }

                    // DISPLAY

                    "display" -> {
                        PageTitle(
                            tr("Display & Touch Test", "డిస్‌ప్లే & టచ్ పరీక్ష"),
                            fg
                        )

                        Note(
                            tr(
                                "Use the colour test to visually inspect the screen. Dead pixels must be checked visually.",
                                "రంగుల పరీక్షతో స్క్రీన్‌ను పరిశీలించండి. Dead Pixels‌ను కళ్లతో తనిఖీ చేయాలి."
                            )
                        )

                        ActionButton(
                            tr(
                                "Start full-screen colour test",
                                "పూర్తి స్క్రీన్ రంగుల పరీక్ష"
                            )
                        ) {
                            screenTest = true
                        }

                        ActionButton(
                            tr("Open display settings", "డిస్‌ప్లే సెట్టింగ్స్")
                        ) {
                            safeStart(
                                context,
                                Intent(Settings.ACTION_DISPLAY_SETTINGS)
                            )
                        }
                    }

                    // AUDIO

                    "audio" -> {
                        PageTitle(
                            tr("Speaker & Audio Test", "స్పీకర్ & ఆడియో పరీక్ష"),
                            fg
                        )

                        Note(
                            tr(
                                "Set a safe volume before playing the test tone.",
                                "టెస్ట్ శబ్దం ప్లే చేసే ముందు సురక్షితమైన వాల్యూమ్ పెట్టండి."
                            )
                        )

                        ActionButton(
                            tr("Play speaker test tone", "స్పీకర్ టెస్ట్ శబ్దం")
                        ) {
                            try {
                                val tone = ToneGenerator(
                                    AudioManager.STREAM_MUSIC,
                                    60
                                )

                                val played = tone.startTone(
                                    ToneGenerator.TONE_PROP_BEEP,
                                    700
                                )

                                if (!played) {
                                    tone.release()
                                    throw IllegalStateException()
                                }

                                Toast.makeText(
                                    context,
                                    tr(
                                        "Test tone played",
                                        "టెస్ట్ శబ్దం ప్లే అయింది"
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()

                                android.os.Handler(
                                    android.os.Looper.getMainLooper()
                                ).postDelayed({
                                    tone.release()
                                }, 1000)

                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    tr(
                                        "Audio test unavailable",
                                        "ఆడియో పరీక్ష అందుబాటులో లేదు"
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        ActionButton(
                            tr("Open sound settings", "సౌండ్ సెట్టింగ్స్")
                        ) {
                            safeStart(
                                context,
                                Intent(Settings.ACTION_SOUND_SETTINGS)
                            )
                        }

                        Note(
                            tr(
                                "Microphone recording test is not included in this version.",
                                "ఈ వెర్షన్‌లో Microphone Recording Test లేదు."
                            )
                        )
                    }

                    // CAMERA

                    "camera" -> {
                        PageTitle(
                            tr("Camera & Flash Test", "కెమెరా & ఫ్లాష్ పరీక్ష"),
                            fg
                        )

                        Info(
                            tr("Camera feature", "కెమెరా ఫీచర్"),
                            data.camera
                        )

                        Info(
                            tr("Microphone feature", "మైక్రోఫోన్ ఫీచర్"),
                            data.microphone
                        )

                        Info(
                            tr("Flash feature", "ఫ్లాష్ ఫీచర్"),
                            data.flash
                        )

                        ActionButton(
                            tr("Open camera app", "కెమెరా తెరవండి")
                        ) {
                            safeStart(
                                context,
                                Intent("android.media.action.IMAGE_CAPTURE")
                            )
                        }

                        Note(
                            tr(
                                "Feature availability does not prove that every camera lens or flash works correctly.",
                                "Feature ఉందని చూపించడం మాత్రమే అన్ని కెమెరా లెన్స్‌లు లేదా ఫ్లాష్ పనిచేస్తున్నాయని నిరూపించదు."
                            )
                        )
                    }

                    // SENSORS

                    "sensors" -> {
                        PageTitle(
                            tr("Sensor Diagnostics", "సెన్సార్ పరీక్షలు"),
                            fg
                        )

                        Info(
                            tr("Sensors detected", "గుర్తించిన సెన్సార్లు"),
                            data.sensorCount
                        )

                        data.sensors.forEach {
                            Info(it.first, it.second)
                        }

                        Note(
                            tr(
                                "Listed sensors are detected by Android. Live sensor response testing is not included yet.",
                                "జాబితాలోని సెన్సార్లను Android గుర్తించింది. Live Sensor Response Test ఇంకా లేదు."
                            )
                        )
                    }

                    // NETWORK

                    "network" -> {
                        PageTitle(
                            tr("Network Diagnostics", "నెట్‌వర్క్ పరీక్షలు"),
                            fg
                        )

                        Info(
                            tr("Connection", "కనెక్షన్"),
                            data.network
                        )

                        Info(
                            tr("Transport", "నెట్‌వర్క్ రకం"),
                            data.transport
                        )

                        Info(
                            tr("Internet validated", "ఇంటర్నెట్ నిర్ధారణ"),
                            data.internet
                        )

                        ActionButton(
                            tr("Open Wi-Fi settings", "Wi-Fi సెట్టింగ్స్")
                        ) {
                            safeStart(
                                context,
                                Intent(Settings.ACTION_WIFI_SETTINGS)
                            )
                        }

                        ActionButton(
                            tr("Open Bluetooth settings", "Bluetooth సెట్టింగ్స్")
                        ) {
                            safeStart(
                                context,
                                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                            )
                        }

                        Note(
                            tr(
                                "Exact 5G mode and signal diagnostics may require phone permissions or manufacturer APIs.",
                                "ఖచ్చితమైన 5G మోడ్, సిగ్నల్ పరీక్షలకు ఫోన్ అనుమతులు లేదా కంపెనీ APIలు అవసరం కావచ్చు."
                            )
                        )
                    }

                    // PERFORMANCE

                    "performance" -> {
                        PageTitle(
                            tr(
                                "Performance & Device Info",
                                "పెర్ఫార్మెన్స్ & ఫోన్ వివరాలు"
                            ),
                            fg
                        )

                        Info("CPU ABI", data.abi)

                        Info(
                            tr("Supported ABIs", "సపోర్ట్ చేసే ABIలు"),
                            data.abis
                        )

                        Info(
                            tr("Total RAM", "మొత్తం RAM"),
                            data.ramTotal
                        )

                        Info(
                            tr("Available RAM", "అందుబాటులో RAM"),
                            data.ramFree
                        )

                        Info(
                            tr("Total storage", "మొత్తం స్టోరేజ్"),
                            data.storageTotal
                        )

                        Info(
                            tr("Free storage", "ఖాళీ స్టోరేజ్"),
                            data.storageFree
                        )

                        Info(
                            tr("Kernel", "కెర్నల్"),
                            data.kernel
                        )

                        Info(
                            tr("Security patch", "సెక్యూరిటీ ప్యాచ్"),
                            data.securityPatch
                        )

                        Info(
                            tr("Device / Product", "డివైస్ / ప్రొడక్ట్"),
                            "${data.device} / ${data.product}"
                        )
                    }

                    // TESTS

                    "tests" -> {
                        PageTitle(
                            tr("All Diagnostic Tests", "అన్ని డయాగ్నస్టిక్ పరీక్షలు"),
                            fg
                        )

                        ActionButton(
                            tr("Battery & Power", "బ్యాటరీ & పవర్")
                        ) {
                            page = "battery"
                        }

                        ActionButton(
                            tr("Display & Touch", "డిస్‌ప్లే & టచ్")
                        ) {
                            page = "display"
                        }

                        ActionButton(
                            tr("Speaker & Audio", "స్పీకర్ & ఆడియో")
                        ) {
                            page = "audio"
                        }

                        ActionButton(
                            tr("Camera & Flash", "కెమెరా & ఫ్లాష్")
                        ) {
                            page = "camera"
                        }

                        ActionButton(
                            tr("Sensors", "సెన్సార్లు")
                        ) {
                            page = "sensors"
                        }

                        ActionButton(
                            tr("Network", "నెట్‌వర్క్")
                        ) {
                            page = "network"
                        }

                        ActionButton(
                            tr("CPU, RAM & Storage", "CPU, RAM & స్టోరేజ్")
                        ) {
                            page = "performance"
                        }
                    }

                    // REPORT

                    "report" -> {
                        PageTitle(
                            tr("Diagnostic Summary", "డయాగ్నస్టిక్ సారాంశం"),
                            fg
                        )

                        Info(
                            tr("Device", "ఫోన్"),
                            data.model
                        )

                        Info(
                            tr("Android version", "ఆండ్రాయిడ్ వెర్షన్"),
                            data.android
                        )

                        Info(
                            tr("Battery", "బ్యాటరీ"),
                            data.battery
                        )

                        Info(
                            tr("Temperature", "ఉష్ణోగ్రత"),
                            data.temperature
                        )

                        Info(
                            tr("Charging", "ఛార్జింగ్"),
                            data.charging
                        )

                        Info(
                            tr("Voltage", "వోల్టేజ్"),
                            data.batteryVoltage
                        )

                        Info(
                            tr("Current", "కరెంట్"),
                            data.batteryCurrent
                        )

                        Info(
                            tr("Network", "నెట్‌వర్క్"),
                            data.network
                        )

                        Info(
                            tr("Sensors detected", "సెన్సార్లు"),
                            data.sensorCount
                        )

                        Info(
                            tr("RAM available", "ఖాళీ RAM"),
                            data.ramFree
                        )

                        Info(
                            tr("Storage free", "ఖాళీ స్టోరేజ్"),
                            data.storageFree
                        )

                        Note(
                            tr(
                                "This is an information summary, not a certified hardware pass/fail report.",
                                "ఇది సమాచార సారాంశం మాత్రమే; అధికారిక Hardware Pass/Fail రిపోర్ట్ కాదు."
                            )
                        )

                        ActionButton(
                            tr("Refresh diagnostic report", "రిపోర్ట్ రిఫ్రెష్")
                        ) {
                            refresh++
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Mobile Doctor Pro • Version 1.2",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// REUSABLE UI COMPONENTS

@Composable
private fun PageTitle(title: String, color: Color) {
    Text(
        title,
        color = color,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    sub: String,
    panel: Color,
    fg: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = panel
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, color = fg)

            Text(
                value,
                color = Good,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                sub,
                color = fg,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                label,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.75f
                ),
                fontSize = 13.sp
            )

            Text(
                value,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        color = Color(0xFFB58A32),
        fontSize = 12.sp
    )
}

// DIAGNOSTIC DATA MODEL

data class Diagnostics(
    val model: String,
    val manufacturer: String,
    val android: String,
    val build: String,

    val battery: String,
    val batteryPercent: Int,
    val temperature: String,
    val charging: String,
    val batteryHealth: String,
    val batteryVoltage: String,
    val batteryCurrent: String,
    val batteryAverageCurrent: String,
    val batteryCharge: String,
    val chargingSource: String,
    val batteryTechnology: String,

    val ramTotal: String,
    val ramFree: String,
    val storageTotal: String,
    val storageFree: String,

    val abi: String,
    val abis: String,
    val securityPatch: String,
    val kernel: String,
    val device: String,
    val product: String,

    val network: String,
    val transport: String,
    val internet: String,

    val sensorCount: String,
    val sensors: List<Pair<String, String>>,

    val camera: String,
    val microphone: String,
    val flash: String
)

// COLLECT DEVICE DIAGNOSTICS

@Suppress("DEPRECATION")
fun collectDiagnostics(context: Context): Diagnostics {

    // BATTERY

    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val level = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_LEVEL,
        -1
    ) ?: -1

    val scale = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_SCALE,
        -1
    ) ?: -1

    val pctValue =
        if (level >= 0 && scale > 0) {
            level * 100 / scale
        } else {
            -1
        }

    val pct = if (pctValue >= 0) {
        "$pctValue%"
    } else {
        "Unavailable"
    }

    val temp = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_TEMPERATURE,
        -1
    ) ?: -1

    val tempText =
        if (temp in 0..1000) {
            "${DecimalFormat("0.0").format(temp / 10.0)} °C"
        } else {
            "Unavailable"
        }

    val status = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_STATUS,
        -1
    ) ?: -1

    val charging = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }

    val health = when (
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            -1
        ) ?: -1
    ) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good (reported)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    val bm = context.getSystemService(
        Context.BATTERY_SERVICE
    ) as BatteryManager

    // VOLTAGE

    val voltage = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_VOLTAGE,
        -1
    ) ?: -1

    val voltageText =
        if (voltage in 2500..5000) {
            "$voltage mV"
        } else {
            "Unavailable"
        }

    // BATTERY CURRENT AND CHARGE

    fun property(id: Int): Long? {
        return try {
            bm.getLongProperty(id).takeIf {
                it != Long.MIN_VALUE
            }
        } catch (_: Exception) {
            null
        }
    }

    fun currentText(value: Long?): String {
        if (value == null) {
            return "Unavailable"
        }

        if (abs(value.toDouble()) > 10_000_000.0) {
            return "Unavailable"
        }

        return if (abs(value.toDouble()) < 1000.0) {
            "$value µA"
        } else {
            "${DecimalFormat("0.0").format(value / 1000.0)} mA"
        }
    }

    val current = property(
        BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
    )

    val average = property(
        BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE
    )

    val charge = property(
        BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
    )

    val chargeText = charge?.let {
        if (it > 0) {
            "${DecimalFormat("0.0").format(it / 1000.0)} mAh"
        } else {
            "Unavailable"
        }
    } ?: "Unavailable"

    // CHARGING SOURCE

    val plugged = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_PLUGGED,
        0
    ) ?: 0

    val source = when {
        (plugged and BatteryManager.BATTERY_PLUGGED_USB) != 0 ->
            "USB"

        (plugged and BatteryManager.BATTERY_PLUGGED_AC) != 0 ->
            "AC Adapter"

        (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0 ->
            "Wireless"

        else -> "Not connected"
    }

    val technology =
        batteryIntent?.getStringExtra(
            BatteryManager.EXTRA_TECHNOLOGY
        ) ?: "Unavailable"

    // RAM

    val am = context.getSystemService(
        Context.ACTIVITY_SERVICE
    ) as ActivityManager

    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)

    // STORAGE

    val stat = StatFs(
        Environment.getDataDirectory().path
    )

    fun gb(bytes: Long): String {
        return "${DecimalFormat("0.00").format(
            bytes / 1_073_741_824.0
        )} GB"
    }

    // SENSORS

    val sm = context.getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager

    val sensorList = sm.getSensorList(
        Sensor.TYPE_ALL
    ).map {
        it.name to "Detected • ${it.vendor}"
    }.distinctBy {
        it.first
    }

    // NETWORK

    val cm = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    val caps = cm.getNetworkCapabilities(
        cm.activeNetwork
    )

    val wifi = caps?.hasTransport(
        NetworkCapabilities.TRANSPORT_WIFI
    ) == true

    val cell = caps?.hasTransport(
        NetworkCapabilities.TRANSPORT_CELLULAR
    ) == true

    val ethernet = caps?.hasTransport(
        NetworkCapabilities.TRANSPORT_ETHERNET
    ) == true

    val net =
        if (wifi || cell || ethernet) {
            "Connected"
        } else {
            "No active network"
        }

    val transport = listOfNotNull(
        if (wifi) "Wi-Fi" else null,
        if (cell) "Cellular" else null,
        if (ethernet) "Ethernet" else null
    ).ifEmpty {
        listOf("Unknown / offline")
    }.joinToString(", ")

    val internet =
        if (
            caps?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) == true
        ) {
            "Yes"
        } else {
            "Not confirmed"
        }

    // CAMERA / MICROPHONE / FLASH

    val pm = context.packageManager

    val camera =
        if (pm.hasSystemFeature("android.hardware.camera.any")) {
            "Present (declared)"
        } else {
            "Not reported"
        }

    val mic =
        if (pm.hasSystemFeature("android.hardware.microphone")) {
            "Present (declared)"
        } else {
            "Not reported"
        }

    val flash =
        if (pm.hasSystemFeature("android.hardware.camera.flash")) {
            "Present (declared)"
        } else {
            "Not reported"
        }

    // RETURN DATA

    return Diagnostics(
        model = "${Build.BRAND} ${Build.MODEL}",
        manufacturer = Build.MANUFACTURER,
        android = Build.VERSION.RELEASE ?: "Unknown",
        build = Build.DISPLAY ?: "Unknown",

        battery = pct,
        batteryPercent = pctValue.coerceIn(0, 100),
        temperature = tempText,
        charging = charging,
        batteryHealth = health,
        batteryVoltage = voltageText,
        batteryCurrent = currentText(current),
        batteryAverageCurrent = currentText(average),
        batteryCharge = chargeText,
        chargingSource = source,
        batteryTechnology = technology,

        ramTotal = gb(mi.totalMem),
        ramFree = gb(mi.availMem),
        storageTotal = gb(stat.totalBytes),
        storageFree = gb(stat.availableBytes),

        abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
        abis = Build.SUPPORTED_ABIS.joinToString(", "),

        securityPatch =
            if (Build.VERSION.SDK_INT >= 23) {
                Build.VERSION.SECURITY_PATCH
            } else {
                "Not available"
            },

        kernel = System.getProperty("os.version") ?: "Unknown",
        device = Build.DEVICE,
        product = Build.PRODUCT,

        network = net,
        transport = transport,
        internet = internet,

        sensorCount = sensorList.size.toString(),
        sensors = sensorList,

        camera = camera,
        microphone = mic,
        flash = flash
    )
}

// SAFE ACTIVITY LAUNCHER

private fun safeStart(
    context: Context,
    intent: Intent
) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            "This setting or app is unavailable",
            Toast.LENGTH_SHORT
        ).show()
    }
}
