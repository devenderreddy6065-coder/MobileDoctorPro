package com.mobiledoctorpro.app

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Vibrator
import android.os.VibrationEffect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import kotlin.math.abs

private val Navy = Color(0xFF101827)
private val Panel = Color(0xFF1D2B40)
private val Accent = Color(0xFF36A8F5)
private val Good = Color(0xFF16865B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DiagnosticHome() }
    }
}

data class SimDetails(
    val title: String,
    val hasSim: Boolean,
    val simState: String,
    val operatorName: String,
    val networkName: String,
    val registration: String,
    val networkType: String,
    val roaming: String
)

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

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refresh++
    }

    LaunchedEffect(page) {
        if (page == "battery" || page == "charger" || page == "network") {
            while (true) {
                delay(2000)
                refresh++
            }
        }
    }

    LaunchedEffect(page) {
        if (page == "network" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val needed = listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).filter {
                context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
            if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
        }
    }

    val data = remember(refresh) { collectDiagnostics(context) }

    fun tr(en: String, te: String) = if (telugu) te else en

    val colors = listOf(Color.White, Color.Black, Color.Red, Color.Green, Color.Blue)
    val bg = if (dark) Navy else Color(0xFFF3F6FA)
    val fg = if (dark) Color.White else Color(0xFF172033)
    val panel = if (dark) Panel else Color.White

    if (screenTest) {
        val textColor = if (screenColor == 0 || screenColor == 3) Color.Black else Color.White
        Column(
            Modifier.fillMaxSize().background(colors[screenColor]).clickable {
                screenColor = (screenColor + 1) % colors.size
            }.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tr("Screen / Touch Test", "స్క్రీన్ / టచ్ పరీక్ష"), color = textColor, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Text(tr("Tap screen to change colour", "రంగు మార్చడానికి స్క్రీన్ తాకండి"), color = textColor)
            Button(onClick = { screenColor = (screenColor + 1) % colors.size }) {
                Text(tr("Next colour", "తదుపరి రంగు"))
            }
            Button(onClick = { screenTest = false; screenColor = 0 }) {
                Text(tr("Finish test", "పరీక్ష ముగించు"))
            }
        }
        return
    }

    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            primary = Accent, background = Navy, surface = Panel,
            onBackground = Color.White, onSurface = Color.White
        ) else lightColorScheme(
            primary = Color(0xFF1769AA), background = Color(0xFFF3F6FA),
            surface = Color.White, onBackground = Color(0xFF172033),
            onSurface = Color(0xFF172033)
        )
    ) {
        Scaffold(
            containerColor = bg,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Mobile Doctor Pro", fontWeight = FontWeight.Bold)
                            Text("PROFESSIONAL DIAGNOSTICS", fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    actions = {
                        TextButton(onClick = { telugu = !telugu }) { Text(if (telugu) "EN" else "తెలుగు") }
                        TextButton(onClick = { dark = !dark }) { Text(if (dark) "☀" else "☾") }
                        TextButton(onClick = { refresh++ }) { Text("↻") }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = panel) {
                    NavigationBarItem(selected = page == "home", onClick = { page = "home" }, icon = { Text("⌂") }, label = { Text(tr("Home", "హోమ్")) })
                    NavigationBarItem(selected = page == "tests", onClick = { page = "tests" }, icon = { Text("✓") }, label = { Text(tr("Tests", "పరీక్షలు")) })
                    NavigationBarItem(selected = page == "report", onClick = { page = "report" }, icon = { Text("▤") }, label = { Text(tr("Report", "రిపోర్ట్")) })
                }
            }
        ) { padding ->
            Column(
                Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (page) {
                    "home" -> {
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(20.dp)) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(tr("DEVICE OVERVIEW", "ఫోన్ వివరాలు"), color = Accent, fontWeight = FontWeight.Bold)
                                Text(data.model, color = fg, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text("Android ${data.android} • ${data.manufacturer}", color = fg)
                                Text("Build: ${data.build}", color = Color.Gray, fontSize = 11.sp)
                                Spacer(Modifier.height(8.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(tr("Battery level", "బ్యాటరీ స్థాయి"), color = fg)
                                    Text(data.battery, color = Good, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(progress = { data.batteryPercent / 100f }, modifier = Modifier.fillMaxWidth(), color = Good)
                            }
                        }
                        Text(tr("Diagnostic Center", "డయాగ్నస్టిక్ సెంటర్"), color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        val items = listOf(
                            Triple("battery", tr("Battery & Power", "బ్యాటరీ & పవర్"), "🔋"),
                            Triple("charger", tr("Charger Diagnostics", "ఛార్జర్ పరీక్షలు"), "🔌"),
                            Triple("display", tr("Display & Touch", "డిస్‌ప్లే & టచ్"), "▣"),
                            Triple("audio", tr("Speaker & Audio", "స్పీకర్ & ఆడియో"), "♫"),
                            Triple("camera", tr("Camera & Flash", "కెమెరా & ఫ్లాష్"), "◉"),
                            Triple("sensors", tr("Sensors", "సెన్సార్లు"), "⌁"),
                            Triple("network", tr("Network", "నెట్‌వర్క్"), "⌁"),
                            Triple("performance", tr("CPU, RAM & Storage", "CPU, RAM & స్టోరేజ్"), "▤"),
                            Triple("hardware", tr("Hardware Health Check", "హార్డ్‌వేర్ ఆరోగ్య పరీక్ష"), "⚙")
                        )
                        items.chunked(2).forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                pair.forEach { item ->
                                    Card(
                                        modifier = Modifier.weight(1f).clickable { page = item.first },
                                        colors = CardDefaults.cardColors(containerColor = panel),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(item.third, fontSize = 25.sp)
                                            Text(item.second, color = fg, fontWeight = FontWeight.SemiBold)
                                            Text(tr("Open diagnostics", "పరీక్ష తెరవండి"), color = Accent, fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                        Text(tr("Quick actions", "త్వరిత చర్యలు"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        ActionButton(tr("Start screen colour test", "స్క్రీన్ రంగుల పరీక్ష")) { screenTest = true }
                        ActionButton(tr("Open battery settings", "బ్యాటరీ సెట్టింగ్స్")) {
                            safeStart(context, Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                        }
                    }

                    "battery" -> {
                        PageTitle(tr("Battery & Power Analyzer", "బ్యాటరీ & పవర్ విశ్లేషణ"), fg)
                        Text(tr("LIVE • 2 SECOND REFRESH", "లైవ్ • 2 సెకన్ల రిఫ్రెష్"), color = Good, fontSize = 11.sp)
                        MetricCard(tr("Battery level", "బ్యాటరీ స్థాయి"), data.battery, data.temperature, panel, fg)
                        Info(tr("Charging status", "ఛార్జింగ్ స్థితి"), data.charging)
                        Info(tr("Battery health reported by Android", "Android తెలిపిన బ్యాటరీ స్థితి"), data.batteryHealth)
                        Info(tr("Voltage", "వోల్టేజ్"), data.batteryVoltage)
                        Info(tr("Current", "కరెంట్"), data.batteryCurrent)
                        Info(tr("Average current", "సగటు కరెంట్"), data.batteryAverageCurrent)
                        Info(tr("Charge counter", "ఛార్జ్ కౌంటర్"), data.batteryCharge)
                        Info(tr("Charging source", "ఛార్జింగ్ సోర్స్"), data.chargingSource)
                        Info(tr("Technology", "టెక్నాలజీ"), data.batteryTechnology)
                        Note(tr("Current is displayed with the original sign reported by Android.", "Android ఇచ్చిన అసలు Current signనే చూపిస్తున్నాం."))
                        Note(tr("Charge counter is not full battery capacity or battery health percentage.", "Charge Counter అనేది మొత్తం బ్యాటరీ సామర్థ్యం లేదా Battery Health శాతం కాదు."))
                    }

                    "charger" -> {
                        PageTitle(tr("Charger Diagnostics", "ఛార్జర్ పరీక్షలు"), fg)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tr("LIVE MONITORING", "లైవ్ మానిటరింగ్"), color = Good, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("2 sec refresh", color = Accent, fontSize = 11.sp)
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(tr("CHARGING TYPE", "ఛార్జింగ్ రకం"), color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(data.chargingType, color = fg, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                Text(data.charging, color = Good, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(tr("Connection Information", "కనెక్షన్ వివరాలు"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Charging Type", "ఛార్జింగ్ రకం"), data.chargingType)
                        Info(tr("Power Source", "పవర్ సోర్స్"), data.chargingSource)
                        Info(tr("Electrical Input", "ఎలక్ట్రికల్ ఇన్‌పుట్"), data.electricalInput)
                        Info(tr("Charging Status", "ఛార్జింగ్ స్థితి"), data.charging)
                        HorizontalDivider()
                        Text(tr("Battery Electrical Readings", "బ్యాటరీ ఎలక్ట్రికల్ రీడింగ్స్"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Battery Voltage", "బ్యాటరీ వోల్టేజ్"), data.batteryVoltage)
                        Info(tr("Battery Current", "బ్యాటరీ కరెంట్"), data.batteryCurrent)
                        Info(tr("Battery-side Power", "బ్యాటరీ పవర్"), data.batteryPower)
                        HorizontalDivider()
                        Text(tr("Charger Output Readings", "ఛార్జర్ అవుట్‌పుట్ రీడింగ్స్"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Charger Output Voltage", "ఛార్జర్ అవుట్‌పుట్ వోల్టేజ్"), data.chargerOutputVoltage)
                        Info(tr("Charger Output Current", "ఛార్జర్ అవుట్‌పుట్ కరెంట్"), data.chargerOutputCurrent)
                        Info(tr("Charger Output Power", "ఛార్జర్ అవుట్‌పుట్ వాట్స్"), data.chargerOutputPower)
                        Info(tr("Charging Protocol", "ఛార్జింగ్ ప్రోటోకాల్"), data.chargingProtocol)
                        HorizontalDivider()
                        Text(tr("Battery Information", "బ్యాటరీ వివరాలు"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Battery Level", "బ్యాటరీ స్థాయి"), data.battery)
                        Info(tr("Battery Temperature", "బ్యాటరీ ఉష్ణోగ్రత"), data.temperature)
                        Info(tr("Battery Technology", "బ్యాటరీ టెక్నాలజీ"), data.batteryTechnology)
                        Note(tr("Battery-side power is calculated from battery voltage and current. It is not charger output power.", "Battery Voltage, Current ఆధారంగా లెక్కించినది Battery-side Power మాత్రమే. ఇది Charger Output Power కాదు."))
                        Note(tr("Android public APIs do not expose actual VOOC/SUPERVOOC protocol or charger output readings on every device.", "ప్రతి ఫోన్‌లో VOOC/SUPERVOOC ప్రోటోకాల్ లేదా Charger Output readings Android API ద్వారా లభించవు."))
                    }

                    "display" -> {
                        PageTitle(tr("Display & Touch Test", "డిస్‌ప్లే & టచ్ పరీక్ష"), fg)
                        Note(tr("Inspect the screen using solid colours. Dead pixels must be checked visually.", "రంగుల పరీక్షతో స్క్రీన్‌ను పరిశీలించండి. Dead Pixels‌ను కళ్లతో తనిఖీ చేయాలి."))
                        ActionButton(tr("Start full-screen colour test", "పూర్తి స్క్రీన్ రంగుల పరీక్ష")) { screenTest = true }
                        ActionButton(tr("Open display settings", "డిస్‌ప్లే సెట్టింగ్స్")) { safeStart(context, Intent(Settings.ACTION_DISPLAY_SETTINGS)) }
                    }

                    "audio" -> {
                        PageTitle(tr("Speaker & Audio Test", "స్పీకర్ & ఆడియో పరీక్ష"), fg)
                        Note(tr("Set a safe volume before testing.", "పరీక్షకు ముందు సురక్షితమైన వాల్యూమ్ పెట్టండి."))
                        ActionButton(tr("Play speaker test tone", "స్పీకర్ టెస్ట్ శబ్దం")) {
                            try {
                                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
                                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 700)
                                Toast.makeText(context, tr("Test tone played", "టెస్ట్ శబ్దం ప్లే అయింది"), Toast.LENGTH_SHORT).show()
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 1000)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Audio test unavailable", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ActionButton(tr("Open sound settings", "సౌండ్ సెట్టింగ్స్")) { safeStart(context, Intent(Settings.ACTION_SOUND_SETTINGS)) }
                    }

                    "camera" -> {
                        PageTitle(tr("Camera & Flash Test", "కెమెరా & ఫ్లాష్ పరీక్ష"), fg)
                        Info(tr("Camera feature", "కెమెరా ఫీచర్"), data.camera)
                        Info(tr("Microphone feature", "మైక్రోఫోన్ ఫీచర్"), data.microphone)
                        Info(tr("Flash feature", "ఫ్లాష్ ఫీచర్"), data.flash)
                        ActionButton(tr("Open camera app", "కెమెరా తెరవండి")) { safeStart(context, Intent("android.media.action.IMAGE_CAPTURE")) }
                        Note(tr("Feature availability does not prove every camera lens or flash works.", "Feature అందుబాటులో ఉందని చూపించడం మాత్రమే అన్ని కెమెరా లెన్స్‌లు పనిచేస్తున్నాయని నిరూపించదు."))
                    }

                    "sensors" -> {
                        PageTitle(tr("Sensor Diagnostics", "సెన్సార్ పరీక్షలు"), fg)
                        Info(tr("Sensors detected", "గుర్తించిన సెన్సార్లు"), data.sensorCount)
                        data.sensors.forEach { Info(it.first, it.second) }
                        Note(tr("Sensors are detected by Android. Live sensor response testing is not included.", "సెన్సార్లను Android గుర్తించింది. Live Sensor Response Test ఇంకా లేదు."))
                    }

                    "network" -> {
                        PageTitle(tr("Network Diagnostics", "నెట్‌వర్క్ పరీక్షలు"), fg)
                        Text(tr("LIVE • 2 SECOND REFRESH", "లైవ్ • 2 సెకన్ల రిఫ్రెష్"), color = Good, fontSize = 11.sp)
                        Text(tr("Connection Overview", "కనెక్షన్ వివరాలు"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Connection", "కనెక్షన్"), data.network)
                        Info(tr("Active Transport", "యాక్టివ్ నెట్‌వర్క్"), data.transport)
                        Info(tr("Internet validated", "ఇంటర్నెట్ నిర్ధారణ"), data.internet)
                        HorizontalDivider()
                        Text(tr("SIM & Mobile Network", "SIM & మొబైల్ నెట్‌వర్క్"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Mobile Network Type", "మొబైల్ నెట్‌వర్క్ రకం"), data.mobileNetworkType)
                        Info(tr("Mobile Data Active Path", "మొబైల్ డేటా కనెక్షన్"), data.mobileConnected)
                        if (data.simPermissionMessage.isNotBlank()) {
                            Info(tr("SIM permission", "SIM అనుమతి"), data.simPermissionMessage)
                        }
                        data.sims.forEach { sim ->
                            Card(colors = CardDefaults.cardColors(containerColor = panel), shape = RoundedCornerShape(14.dp)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(sim.title, color = Accent, fontWeight = FontWeight.Bold)
                                    if (sim.hasSim) {
                                        Info(tr("SIM status", "SIM స్థితి"), sim.simState)
                                        Info(tr("Operator name", "ఆపరేటర్ పేరు"), sim.operatorName)
                                        Info(tr("Network name", "నెట్‌వర్క్ పేరు"), sim.networkName)
                                        Info(tr("Registration", "నెట్‌వర్క్ రిజిస్ట్రేషన్"), sim.registration)
                                        Info(tr("Network type", "నెట్‌వర్క్ రకం"), sim.networkType)
                                        Info(tr("Roaming", "రోమింగ్"), sim.roaming)
                                    } else {
                                        Text(tr("No SIM Inserted", "SIM పెట్టలేదు"), color = fg, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        HorizontalDivider()
                        Text(tr("Wi-Fi Diagnostics", "Wi-Fi వివరాలు"), color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Info(tr("Wi-Fi Status", "Wi-Fi స్థితి"), data.wifiStatus)
                        Info(tr("Wi-Fi Band", "Wi-Fi బ్యాండ్"), data.wifiBand)
                        Info(tr("Wi-Fi Standard", "Wi-Fi స్టాండర్డ్"), data.wifiStandard)
                        Info(tr("Frequency", "ఫ్రీక్వెన్సీ"), data.wifiFrequency)
                        Info(tr("Channel", "ఛానల్"), data.wifiChannel)
                        Info(tr("Link Speed", "లింక్ స్పీడ్"), data.wifiLinkSpeed)
                        Info(tr("Signal Strength", "సిగ్నల్ బలం"), data.wifiSignalStrength)
                        Note(tr("Wi-Fi 6E uses Wi-Fi 6 (802.11ax) on the 6 GHz band. Wi-Fi 7 is 802.11be.", "Wi-Fi 6E అనేది 6GHz బ్యాండ్‌లో Wi-Fi 6 (802.11ax). Wi-Fi 7 అనేది 802.11be."))
                        Note(tr("Android may report LTE when 5G NSA is active. Identification depends on device firmware and carrier.", "కొన్ని ఫోన్లలో 5G NSA ఉన్నా Android LTE అని చూపించవచ్చు. ఇది ఫోన్ Firmware, నెట్‌వర్క్‌పై ఆధారపడి ఉంటుంది."))
                        ActionButton(tr("Open Wi-Fi settings", "Wi-Fi సెట్టింగ్స్")) { safeStart(context, Intent(Settings.ACTION_WIFI_SETTINGS)) }
                        ActionButton(tr("Open Bluetooth settings", "Bluetooth సెట్టింగ్స్")) { safeStart(context, Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
                    }

                    "hardware" -> {
                        PageTitle(tr("Complete Hardware Inventory", "పూర్తి హార్డ్‌వేర్ వివరాలు"), fg)
                        Note(tr("This page lists hardware Android can identify. Detected means available to the system, not a guarantee that the part is physically fault-free.", "Android గుర్తించగల హార్డ్‌వేర్ వివరాలు ఇక్కడ చూపిస్తాం. Detected అంటే సిస్టమ్‌కు కనిపిస్తోంది మాత్రమే; పూర్తిగా పాడుకాలేదని హామీ కాదు."))

                        val pm = context.packageManager
                        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        val cameraIds = try { cameraManager.cameraIdList.toList() } catch (_: Exception) { emptyList() }
                        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                        val allSensors = try { sensorManager.getSensorList(Sensor.TYPE_ALL) } catch (_: Exception) { emptyList() }
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        val audioOutputs = try { audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList() } catch (_: Exception) { emptyList() }
                        val audioInputs = try { audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).toList() } catch (_: Exception) { emptyList() }
                        val displayMetrics = context.resources.displayMetrics
                        val displayWidth = displayMetrics.widthPixels
                        val displayHeight = displayMetrics.heightPixels
                        val density = DecimalFormat("0.0").format(displayMetrics.density)
                        val batteryIntentNow = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                        val batteryTechNow = batteryIntentNow?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unavailable"
                        val batteryVoltageNow = batteryIntentNow?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
                        val batteryTempNow = batteryIntentNow?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
                        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                        val bluetoothAvailable = try { btManager?.adapter != null } catch (_: Exception) { false }
                        val nfcAvailable = try { android.nfc.NfcAdapter.getDefaultAdapter(context) != null } catch (_: Exception) { false }
                        val cameraDetails = cameraIds.map { id ->
                            try {
                                val chars = cameraManager.getCameraCharacteristics(id)
                                val facing = when (chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)) {
                                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK -> "Back"
                                    android.hardware.camera2.CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                                    else -> "Unknown facing"
                                }
                                val sizes = chars.get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                    ?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                                val maxSize = sizes?.maxByOrNull { it.width.toLong() * it.height.toLong() }
                                val resolution = maxSize?.let { "${it.width} × ${it.height}" } ?: "Resolution unavailable"
                                val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                                "Camera ID $id • $facing • $resolution • Flash: ${if (hasFlash) "Yes" else "No"}"
                            } catch (_: Exception) { "Camera ID $id • Details unavailable" }
                        }

                        Text(tr("DEVICE & PROCESSING", "ఫోన్ & ప్రాసెసింగ్"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Manufacturer / Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                        Info("Device / Product", "${Build.DEVICE} / ${Build.PRODUCT}")
                        Info("CPU architecture", Build.SUPPORTED_ABIS.joinToString(", "))
                        Info("Available CPU cores", Runtime.getRuntime().availableProcessors().toString())
                        Info("GPU", tr("Android does not expose GPU model through this basic check", "ఈ ప్రాథమిక పరీక్షలో GPU మోడల్ Android ఇవ్వడం లేదు"))
                        Info("RAM", "${data.ramTotal} total • ${data.ramFree} available")
                        Info("Internal storage", "${data.storageTotal} total • ${data.storageFree} free")

                        HorizontalDivider()
                        Text(tr("DISPLAY HARDWARE", "డిస్‌ప్లే హార్డ్‌వేర్"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Display resolution", "${displayWidth} × ${displayHeight} pixels (app display metrics)")
                        Info("Display density", "$density× • ${displayMetrics.densityDpi} dpi")
                        Info("Refresh rate", try { "${context.display?.refreshRate ?: "Unavailable"} Hz" } catch (_: Exception) { "Unavailable" })
                        Info("Touchscreen", if (pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) "Detected • manual touch test recommended" else "Not reported")

                        HorizontalDivider()
                        Text(tr("CAMERA & FLASH COMPONENTS", "కెమెరా & ఫ్లాష్ భాగాలు"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Camera modules", if (cameraIds.isNotEmpty()) "${cameraIds.size} detected" else "Not detected / access unavailable")
                        if (cameraDetails.isNotEmpty()) cameraDetails.forEachIndexed { index, detail -> Info("Camera ${index + 1}", detail) }
                        Info("Camera flash", if (cameraDetails.any { it.contains("Flash: Yes") }) "Flash hardware reported" else "No flash reported")
                        ActionButton(tr("Open camera app for manual test", "కెమెరా మాన్యువల్ పరీక్ష")) { safeStart(context, Intent("android.media.action.IMAGE_CAPTURE")) }

                        HorizontalDivider()
                        Text(tr("SENSORS", "సెన్సార్లు"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Sensor count", allSensors.size.toString())
                        allSensors.forEachIndexed { index, sensor ->
                            Info("${index + 1}. ${sensor.name}", "Vendor: ${sensor.vendor} • Version: ${sensor.version} • Range: ${sensor.maximumRange} • Power: ${sensor.power} mA")
                        }

                        HorizontalDivider()
                        Text(tr("AUDIO & HAPTICS", "ఆడియో & వైబ్రేషన్"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Microphone feature", if (pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) "Detected • recording test needed" else "Not reported")
                        Info("Audio input devices", if (audioInputs.isEmpty()) "No input device listed" else audioInputs.joinToString { it.productName.toString() })
                        Info("Audio output devices", if (audioOutputs.isEmpty()) "No output device listed" else audioOutputs.joinToString { it.productName.toString() })
                        Info("Vibration motor", if (vibrator.hasVibrator()) "Detected • manual test needed" else "Not detected")
                        ActionButton(tr("Test vibration motor", "వైబ్రేషన్ మోటార్ పరీక్ష")) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE)) else vibrator.vibrate(400)
                                Toast.makeText(context, tr("Did you feel vibration?", "వైబ్రేషన్ అనిపించిందా?"), Toast.LENGTH_LONG).show()
                            } catch (_: Exception) { Toast.makeText(context, "Vibration test unavailable", Toast.LENGTH_SHORT).show() }
                        }
                        ActionButton(tr("Play speaker test tone", "స్పీకర్ పరీక్ష శబ్దం")) {
                            try {
                                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
                                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 700)
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 1000)
                            } catch (_: Exception) { Toast.makeText(context, "Audio test unavailable", Toast.LENGTH_SHORT).show() }
                        }

                        HorizontalDivider()
                        Text(tr("BATTERY & POWER", "బ్యాటరీ & పవర్"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Battery technology", batteryTechNow)
                        Info("Battery voltage", if (batteryVoltageNow > 0) "$batteryVoltageNow mV" else "Unavailable")
                        Info("Battery temperature", if (batteryTempNow >= 0) "${DecimalFormat("0.0").format(batteryTempNow / 10.0)} °C" else "Unavailable")
                        Info("Battery health (Android)", data.batteryHealth)

                        HorizontalDivider()
                        Text(tr("CONNECTIVITY & OTHER HARDWARE", "కనెక్టివిటీ & ఇతర హార్డ్‌వేర్"), color = Accent, fontWeight = FontWeight.Bold)
                        Info("Wi-Fi", if (pm.hasSystemFeature(PackageManager.FEATURE_WIFI)) "Supported" else "Not reported")
                        Info("Bluetooth", if (bluetoothAvailable) "Bluetooth adapter detected" else "Not detected / unavailable")
                        Info("NFC", if (nfcAvailable) "NFC adapter detected" else "Not detected")
                        Info("GPS / Location", if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) "GPS feature reported" else "GPS feature not reported")
                        Info("Mobile radio", if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) "Telephony hardware reported" else "Not reported")
                        Info("USB host", if (pm.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) "Supported" else "Not reported")
                        Info("Fingerprint sensor", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) "Feature reported • unlock test needed" else "Not reported")
                        Info("Flashlight", if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) "Feature reported" else "Not reported")

                        HorizontalDivider()
                        Note(tr("Detected = Android can see or report the component. A real damage diagnosis may require manual tests, permissions, manufacturer diagnostics or service equipment. Some internal chips and motherboard circuits are not exposed to ordinary Android apps.", "Detected అంటే Android ఆ భాగాన్ని గుర్తించింది. నిజమైన damage నిర్ధారణకు manual tests లేదా service equipment అవసరం కావచ్చు. కొన్ని internal chips, motherboard circuits సాధారణ Android apps‌కు కనిపించవు."))
                    }

                    "performance" -> {
                        PageTitle(tr("Performance & Device Info", "పెర్ఫార్మెన్స్ & ఫోన్ వివరాలు"), fg)
                        Info("CPU ABI", data.abi)
                        Info(tr("Supported ABIs", "సపోర్ట్ చేసే ABIలు"), data.abis)
                        Info(tr("Total RAM", "మొత్తం RAM"), data.ramTotal)
                        Info(tr("Available RAM", "అందుబాటులో RAM"), data.ramFree)
                        Info(tr("Total storage", "మొత్తం స్టోరేజ్"), data.storageTotal)
                        Info(tr("Free storage", "ఖాళీ స్టోరేజ్"), data.storageFree)
                        Info(tr("Kernel", "కెర్నల్"), data.kernel)
                        Info(tr("Security patch", "సెక్యూరిటీ ప్యాచ్"), data.securityPatch)
                        Info(tr("Device / Product", "డివైస్ / ప్రొడక్ట్"), "${data.device} / ${data.product}")
                    }

                    "tests" -> {
                        PageTitle(tr("All Diagnostic Tests", "అన్ని డయాగ్నస్టిక్ పరీక్షలు"), fg)
                        val testItems = listOf(
                            "battery" to tr("Battery & Power", "బ్యాటరీ & పవర్"),
                            "charger" to tr("Charger Diagnostics", "ఛార్జర్ పరీక్షలు"),
                            "display" to tr("Display & Touch", "డిస్‌ప్లే & టచ్"),
                            "audio" to tr("Speaker & Audio", "స్పీకర్ & ఆడియో"),
                            "camera" to tr("Camera & Flash", "కెమెరా & ఫ్లాష్"),
                            "sensors" to tr("Sensors", "సెన్సార్లు"),
                            "network" to tr("Network", "నెట్‌వర్క్"),
                            "performance" to tr("CPU, RAM & Storage", "CPU, RAM & స్టోరేజ్"),
                            "hardware" to tr("Hardware Health Check", "హార్డ్‌వేర్ ఆరోగ్య పరీక్ష")
                        )
                        testItems.forEach { item -> ActionButton(item.second) { page = item.first } }
                    }

                    "report" -> {
                        PageTitle(tr("Diagnostic Summary", "డయాగ్నస్టిక్ సారాంశం"), fg)
                        Info(tr("Device", "ఫోన్"), data.model)
                        Info(tr("Android version", "ఆండ్రాయిడ్ వెర్షన్"), data.android)
                        Info(tr("Battery", "బ్యాటరీ"), data.battery)
                        Info(tr("Temperature", "ఉష్ణోగ్రత"), data.temperature)
                        Info(tr("Charging", "ఛార్జింగ్"), data.charging)
                        Info(tr("Voltage", "వోల్టేజ్"), data.batteryVoltage)
                        Info(tr("Current", "కరెంట్"), data.batteryCurrent)
                        Info(tr("Battery Power", "బ్యాటరీ పవర్"), data.batteryPower)
                        Info(tr("Charging Type", "ఛార్జింగ్ రకం"), data.chargingType)
                        Info(tr("Power Source", "పవర్ సోర్స్"), data.chargingSource)
                        Info(tr("Charger Output Power", "ఛార్జర్ అవుట్‌పుట్ వాట్స్"), data.chargerOutputPower)
                        Info(tr("Network", "నెట్‌వర్క్"), data.network)
                        Info(tr("Mobile Network", "మొబైల్ నెట్‌వర్క్"), data.mobileNetworkType)
                        Info(tr("Wi-Fi Band", "Wi-Fi బ్యాండ్"), data.wifiBand)
                        Info(tr("Wi-Fi Standard", "Wi-Fi స్టాండర్డ్"), data.wifiStandard)
                        Info(tr("Wi-Fi Channel", "Wi-Fi ఛానల్"), data.wifiChannel)
                        Info(tr("SIM count", "SIM సంఖ్య"), data.sims.count { it.hasSim }.toString())
                        Info(tr("Sensors detected", "సెన్సార్లు"), data.sensorCount)
                        Info(tr("Available RAM", "ఖాళీ RAM"), data.ramFree)
                        Info(tr("Free storage", "ఖాళీ స్టోరేజ్"), data.storageFree)
                        Note(tr("This is an information summary, not a certified hardware pass/fail report.", "ఇది సమాచార సారాంశం మాత్రమే; అధికారిక Hardware Pass/Fail రిపోర్ట్ కాదు."))
                        ActionButton(tr("Refresh report", "రిపోర్ట్ రిఫ్రెష్")) { refresh++ }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Mobile Doctor Pro • Version 1.5.6", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally), fontSize = 11.sp)
            }
        }
    }
}

data class Diagnostics(
    val model: String, val manufacturer: String, val android: String, val build: String,
    val battery: String, val batteryPercent: Int, val temperature: String, val charging: String,
    val batteryHealth: String, val batteryVoltage: String, val batteryCurrent: String,
    val batteryAverageCurrent: String, val batteryCharge: String, val chargingSource: String,
    val batteryTechnology: String, val chargingType: String, val electricalInput: String,
    val batteryPower: String, val chargerOutputVoltage: String, val chargerOutputCurrent: String,
    val chargerOutputPower: String, val chargingProtocol: String,
    val ramTotal: String, val ramFree: String, val storageTotal: String, val storageFree: String,
    val abi: String, val abis: String, val securityPatch: String, val kernel: String,
    val device: String, val product: String, val network: String, val transport: String,
    val internet: String, val mobileNetworkType: String, val mobileConnected: String,
    val sims: List<SimDetails>, val simPermissionMessage: String,
    val wifiStatus: String, val wifiBand: String, val wifiStandard: String,
    val wifiFrequency: String, val wifiChannel: String, val wifiLinkSpeed: String,
    val wifiSignalStrength: String, val sensorCount: String,
    val sensors: List<Pair<String, String>>, val camera: String,
    val microphone: String, val flash: String
)

@Suppress("DEPRECATION")
fun collectDiagnostics(context: Context): Diagnostics {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
    val batteryText = if (pct >= 0) "$pct%" else "Unavailable"
    val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
    val tempText = if (temp in 0..1000) "${DecimalFormat("0.0").format(temp / 10.0)} °C" else "Unavailable"
    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val charging = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }
    val health = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good (reported)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
    val voltageValid = voltage in 2500..5000
    val voltageText = if (voltageValid) "$voltage mV" else "Unavailable"

    fun property(id: Int): Long? = try {
        bm.getLongProperty(id).takeIf { it != Long.MIN_VALUE }
    } catch (_: Exception) { null }

    fun currentText(value: Long?): String {
        if (value == null || abs(value.toDouble()) > 10_000_000.0) return "Unavailable"
        return if (abs(value.toDouble()) < 1000.0) "$value µA"
        else "${DecimalFormat("0.0").format(value / 1000.0)} mA"
    }

    val current = property(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    val average = property(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
    val charge = property(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val currentTextValue = currentText(current)
    val averageText = currentText(average)
    val chargeText = charge?.let {
        if (it > 0) "${DecimalFormat("0.0").format(it / 1000.0)} mAh" else "Unavailable"
    } ?: "Unavailable"

    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
    val isWireless = (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0
    val isUsb = (plugged and BatteryManager.BATTERY_PLUGGED_USB) != 0
    val isAc = (plugged and BatteryManager.BATTERY_PLUGGED_AC) != 0
    val connectedToPower = plugged != 0
    val source = when {
        !connectedToPower -> "Not connected"
        isWireless -> "Wireless"
        isAc -> "AC Adapter"
        isUsb -> "USB"
        else -> "Unknown"
    }
    val chargingType = when {
        !connectedToPower -> "Not Connected"
        isWireless -> "Wireless"
        isUsb || isAc -> "Wired"
        else -> "Unknown"
    }
    val electricalInput = when {
        !connectedToPower -> "No input"
        isWireless -> "DC (wireless receiver)"
        isUsb || isAc -> "DC (phone-side)"
        else -> "Unknown"
    }
    val batteryPower = if (voltageValid && current != null && abs(current.toDouble()) <= 10_000_000.0) {
        val watts = voltage.toDouble() * current.toDouble() / 1_000_000_000.0
        "${DecimalFormat("0.00").format(abs(watts))} W"
    } else "Unavailable"

    val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unavailable"
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val stat = StatFs(Environment.getDataDirectory().path)
    fun gb(bytes: Long) = "${DecimalFormat("0.00").format(bytes / 1_073_741_824.0)} GB"

    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensorList = sm.getSensorList(Sensor.TYPE_ALL).map { it.name to "Detected • ${it.vendor}" }.distinctBy { it.first }

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork)
    val wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val cell = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    val ethernet = caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
    val network = if (wifi || cell || ethernet) "Connected" else "No active network"
    val transport = listOfNotNull(
        if (wifi) "Wi-Fi" else null,
        if (cell) "Cellular" else null,
        if (ethernet) "Ethernet" else null
    ).ifEmpty { listOf("Unknown / offline") }.joinToString(", ")
    val internet = if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) "Yes" else "Not confirmed"

    val hasPhonePermission = context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    val mobileNetworkType: String
    val mobileConnected: String
    if (!cell) {
        mobileConnected = "Cellular is not the active internet path"
        mobileNetworkType = getDefaultNetworkType(context, hasPhonePermission)
    } else {
        mobileConnected = "Connected"
        mobileNetworkType = getDefaultNetworkType(context, hasPhonePermission)
    }

    val simResult: Pair<List<SimDetails>, String> = if (!hasPhonePermission) {
        emptyList<SimDetails>() to "Grant Phone permission to read SIM details"
    } else {
        try {
            val smgr = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val active = smgr.activeSubscriptionInfoList.orEmpty()
            val tmBase = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val slots = (0..1).map { slotIndex ->
                val info = active.firstOrNull { it.simSlotIndex == slotIndex }
                val slotState = try { tmBase.getSimState(slotIndex) } catch (_: Exception) { TelephonyManager.SIM_STATE_UNKNOWN }
                val present = info != null || slotState == TelephonyManager.SIM_STATE_READY ||
                    slotState == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                    slotState == TelephonyManager.SIM_STATE_PUK_REQUIRED ||
                    slotState == TelephonyManager.SIM_STATE_NETWORK_LOCKED
                if (!present) {
                    SimDetails("SIM SLOT ${slotIndex + 1}", false, "No SIM Inserted", "—", "—", "—", "—", "—")
                } else if (info == null) {
                    SimDetails("SIM SLOT ${slotIndex + 1}", true, "SIM detected", "Unavailable", "Unavailable", "Unknown / unavailable", "Unavailable", "Unavailable")
                } else {
                    val tm = tmBase.createForSubscriptionId(info.subscriptionId)
                    val simState = when (slotState) {
                        TelephonyManager.SIM_STATE_READY -> "Ready"
                        TelephonyManager.SIM_STATE_ABSENT -> "Absent"
                        TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN required"
                        TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK required"
                        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network locked"
                        else -> "Detected"
                    }
                    val serviceState = try { tm.serviceState?.state } catch (_: Exception) { null }
                    val registration = when (serviceState) {
                        android.telephony.ServiceState.STATE_IN_SERVICE -> "Registered"
                        android.telephony.ServiceState.STATE_OUT_OF_SERVICE -> "Not registered (out of service)"
                        android.telephony.ServiceState.STATE_EMERGENCY_ONLY -> "Emergency only"
                        android.telephony.ServiceState.STATE_POWER_OFF -> "Radio off"
                        else -> "Unknown / unavailable"
                    }
                    val type = try { networkTypeName(tm.dataNetworkType) } catch (_: Exception) { "Unavailable" }
                    SimDetails(
                        title = "SIM SLOT ${slotIndex + 1} • ${info.carrierName?.toString()?.takeIf { it.isNotBlank() } ?: "SIM"}",
                        hasSim = true,
                        simState = simState,
                        operatorName = info.carrierName?.toString()?.takeIf { it.isNotBlank() } ?: tm.simOperatorName?.takeIf { it.isNotBlank() } ?: "Unavailable",
                        networkName = tm.networkOperatorName?.takeIf { it.isNotBlank() } ?: "Unavailable",
                        registration = registration,
                        networkType = type,
                        roaming = if (tm.isNetworkRoaming) "Yes" else "No"
                    )
                }
            }
            slots to ""
        } catch (_: SecurityException) {
            emptyList<SimDetails>() to "SIM details permission unavailable"
        } catch (_: Exception) {
            emptyList<SimDetails>() to "SIM details unavailable on this device"
        }
    }
    val sims = simResult.first
    val simPermissionMessage = simResult.second

    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = try { wifiManager.connectionInfo } catch (_: Exception) { null }
    val wifiStatus = if (wifi) "Connected" else "Not connected"
    val frequency = if (wifi && wifiInfo != null) wifiInfo.frequency else 0
    val wifiBand = when {
        !wifi -> "Not connected"
        frequency in 2400..2500 -> "2.4 GHz"
        frequency in 4900..5924 -> "5 GHz"
        frequency in 5925..7125 -> "6 GHz"
        else -> "Unknown"
    }
    val wifiFrequency = if (wifi && frequency > 0) "$frequency MHz" else "Unavailable"
    val wifiChannel = if (wifi && frequency > 0) {
        when {
            frequency == 2484 -> "14"
            frequency in 2412..2472 -> ((frequency - 2407) / 5).toString()
            frequency in 5000..5924 -> ((frequency - 5000) / 5).toString()
            frequency == 5935 -> "2"
            frequency in 5955..7115 -> ((frequency - 5950) / 5).toString()
            else -> "Unknown"
        }
    } else "Unavailable"
    val wifiStandard = if (!wifi || wifiInfo == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        "Unavailable"
    } else {
        when (wifiInfo.wifiStandard) {
            1 -> "Legacy"
            4 -> "Wi-Fi 4 (802.11n)"
            5 -> "Wi-Fi 5 (802.11ac)"
            6 -> if (frequency in 5925..7125) "Wi-Fi 6E (802.11ax)" else "Wi-Fi 6 (802.11ax)"
            7 -> "WiGig (802.11ad)"
            8 -> if (Build.VERSION.SDK_INT >= 33) "Wi-Fi 7 (802.11be)" else "802.11be (Wi-Fi 7)"
            else -> "Unknown"
        }
    }
    val wifiLinkSpeed = if (wifi && wifiInfo != null && wifiInfo.linkSpeed > 0) "${wifiInfo.linkSpeed} Mbps" else "Unavailable"
    val wifiSignalStrength = if (wifi && wifiInfo != null && wifiInfo.rssi > -127) "${wifiInfo.rssi} dBm" else "Unavailable"

    val pm = context.packageManager
    val camera = if (pm.hasSystemFeature("android.hardware.camera.any")) "Present (declared)" else "Not reported"
    val microphone = if (pm.hasSystemFeature("android.hardware.microphone")) "Present (declared)" else "Not reported"
    val flash = if (pm.hasSystemFeature("android.hardware.camera.flash")) "Present (declared)" else "Not reported"

    return Diagnostics(
        model = "${Build.BRAND} ${Build.MODEL}", manufacturer = Build.MANUFACTURER,
        android = Build.VERSION.RELEASE ?: "Unknown", build = Build.DISPLAY ?: "Unknown",
        battery = batteryText, batteryPercent = pct.coerceIn(0, 100), temperature = tempText,
        charging = charging, batteryHealth = health, batteryVoltage = voltageText,
        batteryCurrent = currentTextValue, batteryAverageCurrent = averageText, batteryCharge = chargeText,
        chargingSource = source, batteryTechnology = technology, chargingType = chargingType,
        electricalInput = electricalInput, batteryPower = batteryPower,
        chargerOutputVoltage = "Unavailable", chargerOutputCurrent = "Unavailable",
        chargerOutputPower = "Unavailable", chargingProtocol = "Not detected",
        ramTotal = gb(mi.totalMem), ramFree = gb(mi.availMem),
        storageTotal = gb(stat.totalBytes), storageFree = gb(stat.availableBytes),
        abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown", abis = Build.SUPPORTED_ABIS.joinToString(", "),
        securityPatch = Build.VERSION.SECURITY_PATCH, kernel = System.getProperty("os.version") ?: "Unknown",
        device = Build.DEVICE, product = Build.PRODUCT, network = network, transport = transport,
        internet = internet, mobileNetworkType = mobileNetworkType, mobileConnected = mobileConnected,
        sims = sims, simPermissionMessage = simPermissionMessage,
        wifiStatus = wifiStatus, wifiBand = wifiBand, wifiStandard = wifiStandard,
        wifiFrequency = wifiFrequency, wifiChannel = wifiChannel, wifiLinkSpeed = wifiLinkSpeed,
        wifiSignalStrength = wifiSignalStrength, sensorCount = sensorList.size.toString(),
        sensors = sensorList, camera = camera, microphone = microphone, flash = flash
    )
}

@Suppress("DEPRECATION")
private fun getDefaultNetworkType(context: Context, hasPermission: Boolean): String {
    if (!hasPermission) return "Permission required"
    return try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        networkTypeName(tm.dataNetworkType)
    } catch (_: Exception) { "Unavailable" }
}

private fun networkTypeName(type: Int): String = when (type) {
    TelephonyManager.NETWORK_TYPE_NR -> "5G (NR)"
    TelephonyManager.NETWORK_TYPE_LTE -> "4G (LTE)"
    TelephonyManager.NETWORK_TYPE_UMTS,
    TelephonyManager.NETWORK_TYPE_HSDPA,
    TelephonyManager.NETWORK_TYPE_HSUPA,
    TelephonyManager.NETWORK_TYPE_HSPA,
    TelephonyManager.NETWORK_TYPE_HSPAP,
    TelephonyManager.NETWORK_TYPE_TD_SCDMA,
    TelephonyManager.NETWORK_TYPE_EVDO_0,
    TelephonyManager.NETWORK_TYPE_EVDO_A,
    TelephonyManager.NETWORK_TYPE_EVDO_B,
    TelephonyManager.NETWORK_TYPE_EHRPD -> "3G"
    TelephonyManager.NETWORK_TYPE_GPRS,
    TelephonyManager.NETWORK_TYPE_EDGE,
    TelephonyManager.NETWORK_TYPE_CDMA,
    TelephonyManager.NETWORK_TYPE_1xRTT,
    TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
    else -> "Unknown"
}

@Composable
private fun PageTitle(title: String, color: Color) {
    Text(title, color = color, fontSize = 21.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
private fun MetricCard(title: String, value: String, sub: String, panel: Color, fg: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = panel)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = fg)
            Text(value, color = Good, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(sub, color = fg, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), fontSize = 13.sp)
            Text(value, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Note(text: String) {
    Text(text, color = Color(0xFFB58A32), fontSize = 12.sp)
}

private fun safeStart(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "This setting or app is unavailable", Toast.LENGTH_SHORT).show()
    }
}



/*
 * V1.5.7 ADD-ON MODULE
 * Live sensor response + practical manual component tests.
 * Existing V1.5.6 diagnostic collection remains unchanged.
 */

@Composable
private fun ComponentTestsPage(
    onDisplayTest: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val sensors = remember {
        try { sensorManager.getSensorList(Sensor.TYPE_ALL) } catch (_: Exception) { emptyList() }
    }
    var selectedSensor by remember { mutableStateOf<Sensor?>(null) }
    var liveValues by remember { mutableStateOf("Select a sensor and start its live test") }
    var sensorRunning by remember { mutableStateOf(false) }
    var lastSensorTimestamp by remember { mutableStateOf("Not started") }

    DisposableEffect(selectedSensor, sensorRunning) {
        val sensor = selectedSensor
        if (sensor != null && sensorRunning) {
            val listener = object : android.hardware.SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent) {
                    liveValues = event.values.mapIndexed { index, value ->
                        "Axis ${index + 1}: ${DecimalFormat("0.000").format(value)}"
                    }.joinToString("   •   ")
                    lastSensorTimestamp = "Live response received"
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Accuracy is device/vendor dependent; the live values remain the main test.
                }
            }
            val registered = try {
                sensorManager.registerListener(
                    listener,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } catch (_: Exception) { false }

            if (!registered) {
                liveValues = "Sensor listener could not start"
                lastSensorTimestamp = "Unavailable"
            }

            onDispose {
                try { sensorManager.unregisterListener(listener) } catch (_: Exception) {}
            }
        } else {
            onDispose { }
        }
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    PageTitle("Live & Manual Component Tests", MaterialTheme.colorScheme.onBackground)
    Note("These tests check observable responses. A successful response does not certify that a component has no physical damage.")

    Text("1. SENSOR LIVE RESPONSE", color = Accent, fontWeight = FontWeight.Bold)
    Info("Detected sensors", sensors.size.toString())

    sensors.forEach { sensor ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    sensorRunning = false
                    selectedSensor = sensor
                    liveValues = "Ready to test ${sensor.name}"
                    lastSensorTimestamp = "Not started"
                },
            colors = CardDefaults.cardColors(
                containerColor = if (selectedSensor?.name == sensor.name)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text(sensor.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Vendor: ${sensor.vendor} • Type: ${sensor.type} • Range: ${sensor.maximumRange}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
                Text(
                    if (selectedSensor?.name == sensor.name) "Selected for live test" else "Tap to select",
                    color = Accent,
                    fontSize = 11.sp
                )
            }
        }
    }

    Info("Selected sensor", selectedSensor?.name ?: "None")
    Info("Live sensor values", liveValues)
    Info("Sensor response", lastSensorTimestamp)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                if (selectedSensor == null) {
                    Toast.makeText(context, "Select a sensor first", Toast.LENGTH_SHORT).show()
                } else {
                    sensorRunning = true
                    liveValues = "Waiting for sensor response…"
                    lastSensorTimestamp = "Listening"
                }
            },
            modifier = Modifier.weight(1f)
        ) { Text("Start test") }

        OutlinedButton(
            onClick = {
                sensorRunning = false
                liveValues = "Test stopped"
                lastSensorTimestamp = "Stopped"
            },
            modifier = Modifier.weight(1f)
        ) { Text("Stop test") }
    }

    HorizontalDivider()
    Text("2. DISPLAY & TOUCH", color = Accent, fontWeight = FontWeight.Bold)
    Note("Check white, black, red, green and blue screens visually. Touch every area of the screen.")
    ActionButton("Start full-screen colour / touch test", onDisplayTest)

    HorizontalDivider()
    Text("3. SPEAKER / AUDIO OUTPUT", color = Accent, fontWeight = FontWeight.Bold)
    Note("Keep volume low before starting. Listen for crackling, distortion or missing audio.")
    ActionButton("Play speaker test tone") {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 900)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                { try { tone.release() } catch (_: Exception) {} },
                1200
            )
            Toast.makeText(context, "Test tone played — check sound quality", Toast.LENGTH_LONG).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Audio test unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    Info(
        "Audio output devices",
        try {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .joinToString { it.productName?.toString() ?: "Audio device" }
                .ifBlank { "No output device listed" }
        } catch (_: Exception) { "Unavailable" }
    )

    HorizontalDivider()
    Text("4. MICROPHONE", color = Accent, fontWeight = FontWeight.Bold)
    Note("Android may require microphone permission for recording. Use a trusted recorder app and play back the recording to check microphone operation.")
    ActionButton("Open sound settings") {
        safeStart(context, Intent(Settings.ACTION_SOUND_SETTINGS))
    }
    ActionButton("Open voice recorder (if available)") {
        safeStart(context, Intent("android.provider.MediaStore.RECORD_SOUND"))
    }

    HorizontalDivider()
    Text("5. CAMERA & FLASH", color = Accent, fontWeight = FontWeight.Bold)
    Note("Open the camera and test each available lens manually. Check focus, image clarity and flash operation.")
    ActionButton("Open camera") {
        safeStart(context, Intent("android.media.action.IMAGE_CAPTURE"))
    }
    HorizontalDivider()
    Text("6. VIBRATION MOTOR", color = Accent, fontWeight = FontWeight.Bold)

    Info(
        "Vibrator hardware",
        if (vibrator.hasVibrator()) "Detected" else "Not detected"
    )

    ActionButton("Run vibration test") {
        try {
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            500,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }

                Toast.makeText(
                    context,
                    "Did you feel the vibration?",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "No vibrator reported by Android",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Vibration Error: ${e.javaClass.simpleName} - ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    HorizontalDivider()
