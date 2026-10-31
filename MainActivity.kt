
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DiagnosticHome()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticHome() {
    val context = LocalContext.current

    var refresh by remember { mutableIntStateOf(0) }
    var showScreenTest by remember { mutableStateOf(false) }
    var telugu by remember { mutableStateOf(false) }

    val data = remember(refresh) {
        collectDiagnostics(context)
    }

    fun tr(en: String, te: String) =
        if (telugu) te else en

    if (showScreenTest) {
        var colorIndex by remember { mutableIntStateOf(0) }

        val colors = listOf(
            Color.White,
            Color.Black,
            Color.Red,
            Color.Green,
            Color.Blue
        )

        Column(
            Modifier
                .fillMaxSize()
                .background(colors[colorIndex])
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                tr("Screen / Touch Test", "స్క్రీన్ / టచ్ పరీక్ష"),
                color = if (colorIndex == 0 || colorIndex == 3)
                    Color.Black else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = {
                    colorIndex = (colorIndex + 1) % colors.size
                }
            ) {
                Text(tr("Tap to change colour", "రంగు మార్చడానికి తాకండి"))
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = {
                showScreenTest = false
            }) {
                Text(tr("Finish test", "పరీక్ష ముగించు"))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mobile Doctor Pro",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = {
                        telugu = !telugu
                    }) {
                        Text(if (telugu) "English" else "తెలుగు")
                    }

                    TextButton(onClick = {
                        refresh++
                    }) {
                        Text(tr("Refresh", "రిఫ్రెష్"))
                    }
                }
            )
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

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F2FF)
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        tr("DEVICE OVERVIEW", "ఫోన్ వివరాలు"),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF174A7E)
                    )

                    Text(
                        data.model,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text("Android ${data.android} • ${data.manufacturer}")

                    Text(
                        "Build: ${data.build}",
                        fontSize = 12.sp
                    )
                }
            }

            // BATTERY DIAGNOSTICS

            Section(
                tr("Battery & Power", "బ్యాటరీ & పవర్")
            ) {
                Info(
                    tr("Battery level", "బ్యాటరీ స్థాయి"),
                    data.battery
                )

                Info(
                    tr("Battery temperature", "బ్యాటరీ ఉష్ణోగ్రత"),
                    data.temperature
                )

                Info(
                    tr("Charging status", "ఛార్జింగ్ స్థితి"),
                    data.charging
                )

                Info(
                    tr("Health reported by Android", "Android తెలిపిన బ్యాటరీ స్థితి"),
                    data.batteryHealth
                )

                HorizontalDivider()

                Text(
                    tr("ADVANCED BATTERY DATA", "అధునాతన బ్యాటరీ వివరాలు"),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF155E75),
                    fontSize = 13.sp
                )

                Info(
                    tr("Battery voltage", "బ్యాటరీ వోల్టేజ్"),
                    data.batteryVoltage
                )

                Info(
                    tr("Battery current", "బ్యాటరీ కరెంట్"),
                    data.batteryCurrent
                )

                Info(
                    tr("Average current", "సగటు కరెంట్"),
                    data.batteryAverageCurrent
                )

                Info(
                    tr("Remaining charge", "మిగిలిన ఛార్జ్"),
                    data.batteryCharge
                )

                Info(
                    tr("Charging source", "ఛార్జింగ్ సోర్స్"),
                    data.chargingSource
                )

                Info(
                    tr("Battery technology", "బ్యాటరీ టెక్నాలజీ"),
                    data.batteryTechnology
                )

                Note(
                    tr(
                        "Battery current may be positive or negative depending on charging or discharging. Some devices do not expose all battery properties.",
                        "ఛార్జింగ్ లేదా డిశ్చార్జింగ్ ఆధారంగా కరెంట్ విలువ ప్లస్ లేదా మైనస్‌లో ఉండవచ్చు. కొన్ని ఫోన్లు అన్ని బ్యాటరీ వివరాలను అందించవు."
                    )
                )

                Note(
                    tr(
                        "True battery capacity and health percentage are not reliably available on every phone. No percentage is guessed.",
                        "నిజమైన బ్యాటరీ సామర్థ్యం, ఆరోగ్య శాతం ప్రతి ఫోన్‌లో అందుబాటులో ఉండవు. ఊహించి శాతం చూపించము."
                    )
                )
            }

            // MEMORY AND STORAGE

            Section(
                tr("Memory & Storage", "మెమరీ & స్టోరేజ్")
            ) {
                Info("RAM ${tr("total", "మొత్తం")}", data.ramTotal)
                Info("RAM ${tr("available", "అందుబాటులో")}", data.ramFree)

                Info(
                    tr("Internal storage total", "ఇంటర్నల్ స్టోరేజ్ మొత్తం"),
                    data.storageTotal
                )

                Info(
                    tr("Internal storage free", "ఖాళీ ఇంటర్నల్ స్టోరేజ్"),
                    data.storageFree
                )
            }

            // PROCESSOR

            Section(
                tr("Processor & Software", "ప్రాసెసర్ & సాఫ్ట్‌వేర్")
            ) {
                Info("CPU ABI", data.abi)

                Info(
                    tr("Supported ABIs", "సపోర్ట్ చేసే ABIలు"),
                    data.abis
                )

                Info(
                    tr("Security patch", "సెక్యూరిటీ ప్యాచ్"),
                    data.securityPatch
                )

                Info("Kernel", data.kernel)

                Info(
                    tr("Device / product", "డివైస్ / ప్రొడక్ట్"),
                    "${data.device} / ${data.product}"
                )
            }

            // NETWORK

            Section(
                tr("Network", "నెట్‌వర్క్")
            ) {
                Info(
                    tr("Connection", "కనెక్షన్"),
                    data.network
                )

                Info(
                    tr("Wi-Fi / cellular", "వై-ఫై / మొబైల్ నెట్‌వర్క్"),
                    data.transport
                )

                Info(
                    tr("Internet validated", "ఇంటర్నెట్ నిర్ధారణ"),
                    data.internet
                )

                Note(
                    tr(
                        "Signal strength, exact 5G mode and SIM diagnostics may need extra permissions or manufacturer APIs.",
                        "సిగ్నల్ బలం, ఖచ్చితమైన 5G మోడ్, SIM పరీక్షలకు అదనపు అనుమతులు లేదా కంపెనీ APIలు అవసరం కావచ్చు."
                    )
                )
            }

            // HARDWARE

            Section(
                tr("Hardware availability", "హార్డ్‌వేర్ లభ్యత")
            ) {
                Info(
                    tr("Sensors found", "కనిపించిన సెన్సార్లు"),
                    data.sensorCount
                )

                data.sensors.forEach {
                    Info(
                        it.first,
                        it.second.replace(
                            "Available",
                            tr("Available", "అందుబాటులో")
                        )
                    )
                }

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
            }

            // MANUAL TESTS

            Section(
                tr("Quick manual tests", "త్వరిత మాన్యువల్ పరీక్షలు")
            ) {
                Button(
                    onClick = {
                        showScreenTest = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        tr(
                            "Start screen & touch test",
                            "స్క్రీన్ & టచ్ పరీక్ష ప్రారంభించు"
                        )
                    )
                }

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_DISPLAY_SETTINGS)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        tr(
                            "Open display settings",
                            "డిస్‌ప్లే సెట్టింగ్స్ తెరువు"
                        )
                    )
                }

                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        tr(
                            "Open battery settings",
                            "బ్యాటరీ సెట్టింగ్స్ తెరువు"
                        )
                    )
                }
            }

            // LIMITATIONS

            Section(
                tr("Important diagnostic limits", "డయాగ్నస్టిక్ పరిమితులు")
            ) {
                Note(
                    tr(
                        "A component being listed does not prove it works. This app reads Android-reported information and offers a basic screen test. It cannot electrically test the motherboard, detect every display defect, measure true battery capacity, or certify camera/speaker/microphone quality.",
                        "కాంపోనెంట్ జాబితాలో కనిపించడం మాత్రమే అది పనిచేస్తోందని నిరూపించదు. ఈ యాప్ Android ఇచ్చే వివరాలను చదివి ప్రాథమిక స్క్రీన్ పరీక్ష చేస్తుంది. మదర్‌బోర్డ్‌ను విద్యుత్ పరంగా పరీక్షించడం, ప్రతి డిస్‌ప్లే లోపాన్ని గుర్తించడం, నిజమైన బ్యాటరీ సామర్థ్యాన్ని కొలవడం లేదా కెమెరా/స్పీకర్/మైక్రోఫోన్ నాణ్యతను ధృవీకరించడం సాధ్యం కాదు."
                    )
                )

                Note(
                    tr(
                        "No root access, no hidden data collection, and no data is uploaded. Refresh to read the latest available values.",
                        "Root access అవసరం లేదు. రహస్యంగా డేటా సేకరించదు; ఏ డేటానూ అప్‌లోడ్ చేయదు. తాజా వివరాల కోసం Refresh నొక్కండి."
                    )
                )
            }

            Text(
                "Mobile Doctor Pro • Version 1.1",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF155E75)
            )
            content()
        }
    }
}

@Composable
private fun Info(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = Color.DarkGray,
            fontSize = 13.sp
        )

        Text(
            value,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        color = Color(0xFF6B4F00),
        fontSize = 12.sp
    )
}

// DIAGNOSTICS DATA

data class Diagnostics(
    val model: String,
    val manufacturer: String,
    val android: String,
    val build: String,

    val battery: String,
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

@Suppress("DEPRECATION")
fun collectDiagnostics(context: Context): Diagnostics {

    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    )

    val level = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_LEVEL, -1
    ) ?: -1

    val scale = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_SCALE, -1
    ) ?: -1

    val pct =
        if (level >= 0 && scale > 0)
            "${level * 100 / scale}%"
        else "Unavailable"

    val temp = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_TEMPERATURE, -1
    ) ?: -1

    val tempText =
        if (temp >= 0)
            "${DecimalFormat("0.0").format(temp / 10.0)} °C"
        else "Unavailable"

    val status = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_STATUS, -1
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
            BatteryManager.EXTRA_HEALTH, -1
        ) ?: -1
    ) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good (reported)"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    // ADVANCED BATTERY DATA

    val bm = context.getSystemService(
        Context.BATTERY_SERVICE
    ) as BatteryManager

    val voltage = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_VOLTAGE, -1
    ) ?: -1

    val voltageText =
        if (voltage > 0) "$voltage mV"
        else "Unavailable"

    fun batteryProperty(property: Int): Long? {
        val value = bm.getLongProperty(property)

        return if (value == Long.MIN_VALUE) {
            null
        } else {
            value
        }
    }

    fun currentText(value: Long?): String {
        return value?.let {
            "${DecimalFormat("0.0").format(it / 1000.0)} mA"
        } ?: "Unavailable"
    }

    val currentNow = batteryProperty(
        BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
    )

    val currentAverage = batteryProperty(
        BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE
    )

    val chargeCounter = batteryProperty(
        BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
    )

    val chargeText = chargeCounter?.let {
        "${DecimalFormat("0.0").format(it / 1000.0)} mAh"
    } ?: "Unavailable"

    val plugged = batteryIntent?.getIntExtra(
        BatteryManager.EXTRA_PLUGGED, 0
    ) ?: 0

    val chargingSource = when {
        (plugged and BatteryManager.BATTERY_PLUGGED_USB) != 0 ->
            "USB"

        (plugged and BatteryManager.BATTERY_PLUGGED_AC) != 0 ->
            "AC Adapter"

        (plugged and BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0 ->
            "Wireless"

        else -> "Not connected"
    }

    val technology = batteryIntent?.getStringExtra(
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

    val total = stat.totalBytes
    val free = stat.availableBytes

    fun gb(bytes: Long) =
        "${DecimalFormat("0.00").format(bytes / 1_073_741_824.0)} GB"

    // SENSORS

    val sm = context.getSystemService(
        Context.SENSOR_SERVICE
    ) as SensorManager

    val sensorList = sm.getSensorList(
        Sensor.TYPE_ALL
    ).map {
        it.name to "Available • ${it.vendor}"
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

    val wifi =
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

    val cell =
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

    val ethernet =
        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true

    val net = when {
        wifi -> "Connected"
        cell -> "Connected"
        ethernet -> "Connected"
        else -> "No active network"
    }

    val transport = listOfNotNull(
        if (wifi) "Wi-Fi" else null,
        if (cell) "Cellular" else null,
        if (ethernet) "Ethernet" else null
    ).ifEmpty {
        listOf("Unknown / offline")
    }.joinToString(", ")

    val internet =
        if (caps?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_VALIDATED
            ) == true
        ) "Yes" else "Not confirmed"

    // HARDWARE

    val pm = context.packageManager

    val camera =
        if (pm.hasSystemFeature("android.hardware.camera.any"))
            "Present (feature declared)"
        else "Not reported"

    val mic =
        if (pm.hasSystemFeature("android.hardware.microphone"))
            "Present (feature declared)"
        else "Not reported"

    val flash =
        if (pm.hasSystemFeature("android.hardware.camera.flash"))
            "Present (feature declared)"
        else "Not reported"

    return Diagnostics(
        model = "${Build.BRAND} ${Build.MODEL}",
        manufacturer = Build.MANUFACTURER,
        android = Build.VERSION.RELEASE ?: "Unknown",
        build = Build.DISPLAY ?: "Unknown",

        battery = pct,
        temperature = tempText,
        charging = charging,
        batteryHealth = health,

        batteryVoltage = voltageText,
        batteryCurrent = currentText(currentNow),
        batteryAverageCurrent = currentText(currentAverage),
        batteryCharge = chargeText,
        chargingSource = chargingSource,
        batteryTechnology = technology,

        ramTotal = gb(mi.totalMem),
        ramFree = gb(mi.availMem),
        storageTotal = gb(total),
        storageFree = gb(free),

        abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
        abis = Build.SUPPORTED_ABIS.joinToString(", "),
        securityPatch = if (Build.VERSION.SDK_INT >= 23)
            Build.VERSION.SECURITY_PATCH else "Not available",
        kernel = System.getProperty("os.version") ?: "Unknown",
        device = Build.DEVICE,
        product = Build.PRODUCT,

        network = net,
        transport = transport,
        internet = internet,

        sensorCount = "${sensorList.size}",
        sensors = sensorList,
        camera = camera,
        microphone = mic,
        flash = flash
    )
}
