package com.batterydoctor.pro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class BatterySnapshot(
    val level: Int = 0, val tempC: Float = 0f, val voltageV: Float = 0f,
    val currentMa: Int? = null, val powerW: Float? = null,
    val status: String = "Unknown", val source: String = "Unknown",
    val health: String = "Unknown", val technology: String = "Unknown",
    val plugged: Int = 0
)

class MainActivity : ComponentActivity() {
    private var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initial = readSnapshot(null)
        setContent {
            var data by remember { mutableStateOf(initial) }
            var lastFullAlert by remember { mutableIntStateOf(-1) }

            DisposableEffect(Unit) {
                val r = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        data = readSnapshot(intent)
                        if (data.level >= 100 && lastFullAlert != 100) {
                            lastFullAlert = 100
                        }
                        if (data.level < 100) lastFullAlert = -1
                    }
                }
                receiver = r
                registerReceiver(r, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                onDispose {
                    try { unregisterReceiver(r) } catch (_: Exception) {}
                    receiver = null
                }
            }

            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF63D9B5),
                    background = Color(0xFF0B1220),
                    surface = Color(0xFF151F30)
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = Color(0xFF0B1220)) {
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("BATTERY DOCTOR", color = Color(0xFF63D9B5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Professional", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Live battery & charging diagnostics", color = Color.LightGray)

                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF151F30)), shape = RoundedCornerShape(22.dp)) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BATTERY LEVEL", color = Color.LightGray, fontSize = 12.sp)
                                Text("${data.level}%", color = Color(0xFF63D9B5), fontSize = 54.sp, fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(progress = { data.level.coerceIn(0,100) / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp))
                                Spacer(Modifier.height(10.dp))
                                Text(data.status, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Section("BATTERY DIAGNOSTICS")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Metric("Temperature", "${fmt(data.tempC)} °C", Modifier.weight(1f))
                            Metric("Battery voltage", "${fmt(data.voltageV)} V", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Metric("Health status", data.health, Modifier.weight(1f))
                            Metric("Technology", data.technology, Modifier.weight(1f))
                        }

                        Section("LIVE CHARGING")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Metric("Current", data.currentMa?.let { "$it mA" } ?: "Not exposed", Modifier.weight(1f))
                            Metric("Estimated power", data.powerW?.let { "${fmt(it)} W" } ?: "Unavailable", Modifier.weight(1f))
                        }

                        Section("CHARGER INFORMATION")
                        Info("Connection", if (data.plugged == 0) "Not connected" else "Connected")
                        Info("Power source", data.source)
                        Info("Charging status", data.status)
                        Info("Phone", "${Build.MANUFACTURER} ${Build.MODEL}")
                        Info("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")

                        Section("IMPORTANT NOTES")
                        Text(
                            "Android reports the detected power-source category and battery telemetry. It does not reliably expose the adapter's exact model, cable quality, negotiated SUPERVOOC protocol, or true charger maximum wattage to ordinary apps. The power figure above is an estimate from battery voltage × reported current, not wall-input wattage.",
                            color = Color(0xFFB8C3D4), fontSize = 12.sp, lineHeight = 18.sp
                        )
                        Text(
                            "Battery health percentage and cycle count are shown only when the phone exposes reliable system data. This version does not claim to measure chemical battery capacity.",
                            color = Color(0xFFB8C3D4), fontSize = 12.sp, lineHeight = 18.sp
                        )
                        Text("Version 1.0 • Offline diagnostics", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }

    private fun readSnapshot(intent: Intent?): BatterySnapshot {
        val i = intent ?: registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (scale > 0) (level * 100 / scale) else 0
        val temp = (i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (i?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val statusCode = i?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val status = when (statusCode) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }
        val plugged = i?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val source = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery / unknown"
        }
        val healthCode = i?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        val tech = i?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val current = try {
            val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (v == Int.MIN_VALUE || v == 0) null else v / 1000
        } catch (_: Exception) { null }
        val watts = if (current != null && voltage > 0f) kotlin.math.abs(current) * voltage / 1000f else null
        return BatterySnapshot(pct, temp, voltage, current, watts, status, source, health, tech, plugged)
    }
}

@Composable
fun Section(title: String) {
    Text(title, color = Color(0xFF63D9B5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
}
@Composable
fun Metric(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF151F30)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(title, color = Color.LightGray, fontSize = 11.sp)
            Spacer(Modifier.height(7.dp))
            Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun Info(label: String, value: String) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF151F30), RoundedCornerShape(12.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray, fontSize = 13.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
fun fmt(v: Float) = String.format(Locale.US, "%.2f", v)
