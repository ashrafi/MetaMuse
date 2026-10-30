package com.example.data.connectors

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.model.ChannelMetricItem
import com.example.model.ChannelTelemetryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume

data class RealHealthSyncResult(
    val isConnected: Boolean,
    val statusLine: String,
    val stepsToday: Long,
    val avgSleepHours7d: Float,
    val priorAvgSleepHours7d: Float,
    val restingOrAvgHrBpm: Long,
    val hydrationLitersToday: Double,
    val weeklySleepHours: List<Float>,
    val report: ChannelTelemetryReport
)

class HealthConnectManager {

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class)
    )

    fun createPermissionRequestContract() =
        PermissionController.createRequestPermissionResultContract()

    fun isSdkAvailable(context: Context): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun openHealthConnectSettings(context: Context) {
        val intent = try {
            HealthConnectClient.getHealthConnectManageDataIntent(context)
        } catch (_: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.healthdata"))
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if unavailable on stripped ROM
        }
    }

    suspend fun hasPermissions(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (!isSdkAvailable(context)) return@withContext false
        return@withContext try {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            // Consider connected if at least Steps or Sleep read permission is granted
            val stepsPerm = HealthPermission.getReadPermission(StepsRecord::class)
            val sleepPerm = HealthPermission.getReadPermission(SleepSessionRecord::class)
            granted.contains(stepsPerm) || granted.contains(sleepPerm)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun syncRealHealthData(context: Context): RealHealthSyncResult = withContext(Dispatchers.IO) {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = LocalDate.now(zone)
        val startOfToday = today.atStartOfDay(zone).toInstant()
        val sevenDaysAgo = today.minusDays(6).atStartOfDay(zone).toInstant()
        val fourteenDaysAgo = today.minusDays(13).atStartOfDay(zone).toInstant()

        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        val hasActivityRecognition = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        var hcGranted = false
        var stepsToday = 0L
        var steps7dTotal = 0L
        var avgHrBpm = 0L
        var hydrationLiters = 0.0
        val dailySleepMap = mutableMapOf<LocalDate, Float>()
        var priorWeekSleepTotal = 0f
        var priorWeekNights = 0

        if (sdkStatus == HealthConnectClient.SDK_AVAILABLE) {
            try {
                val client = HealthConnectClient.getOrCreate(context)
                val granted = client.permissionController.getGrantedPermissions()
                val stepsPerm = HealthPermission.getReadPermission(StepsRecord::class)
                val sleepPerm = HealthPermission.getReadPermission(SleepSessionRecord::class)
                val hrPerm = HealthPermission.getReadPermission(HeartRateRecord::class)
                val hydPerm = HealthPermission.getReadPermission(HydrationRecord::class)

                hcGranted = granted.contains(stepsPerm) || granted.contains(sleepPerm)

                if (granted.contains(stepsPerm)) {
                    val todayAgg = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(StepsRecord.COUNT_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(startOfToday, now)
                        )
                    )
                    stepsToday = todayAgg[StepsRecord.COUNT_TOTAL] ?: 0L

                    val weekAgg = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(StepsRecord.COUNT_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(sevenDaysAgo, now)
                        )
                    )
                    steps7dTotal = weekAgg[StepsRecord.COUNT_TOTAL] ?: stepsToday
                }

                if (granted.contains(sleepPerm)) {
                    val sleepResp = client.readRecords(
                        ReadRecordsRequest(
                            recordType = SleepSessionRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(fourteenDaysAgo, now)
                        )
                    )
                    for (session in sleepResp.records) {
                        val endDate = session.endTime.atZone(zone).toLocalDate()
                        val hours = Duration.between(session.startTime, session.endTime).toMinutes() / 60f
                        if (!endDate.isBefore(today.minusDays(6))) {
                            dailySleepMap[endDate] = (dailySleepMap[endDate] ?: 0f) + hours
                        } else {
                            priorWeekSleepTotal += hours
                            priorWeekNights += 1
                        }
                    }
                }

                if (granted.contains(hrPerm)) {
                    val hrAgg = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(HeartRateRecord.BPM_AVG, HeartRateRecord.BPM_MIN),
                            timeRangeFilter = TimeRangeFilter.between(sevenDaysAgo, now)
                        )
                    )
                    avgHrBpm = hrAgg[HeartRateRecord.BPM_MIN]
                        ?: hrAgg[HeartRateRecord.BPM_AVG]
                        ?: 0L
                }

                if (granted.contains(hydPerm)) {
                    val hydAgg = client.aggregate(
                        AggregateRequest(
                            metrics = setOf(HydrationRecord.VOLUME_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(startOfToday, now)
                        )
                    )
                    hydrationLiters = hydAgg[HydrationRecord.VOLUME_TOTAL]?.inLiters ?: 0.0
                }
            } catch (_: Exception) {
                // Handled gracefully below
            }
        }

        // Supplement with real hardware step counter sensor if Health Connect steps today == 0 and sensor permission granted
        var usedHardwareSensor = false
        if (stepsToday == 0L && hasActivityRecognition) {
            val sensorSteps = readHardwareStepCounterOnce(context)
            if (sensorSteps != null && sensorSteps > 0L) {
                stepsToday = sensorSteps
                usedHardwareSensor = true
            }
        }

        val isConnected = hcGranted || usedHardwareSensor
        if (!isConnected) {
            val msg = if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                "Health Connect update required • Tap to open Play Store"
            } else {
                "Not connected • Grant Android Health Connect permission"
            }
            return@withContext RealHealthSyncResult(
                isConnected = false,
                statusLine = msg,
                stepsToday = 0L,
                avgSleepHours7d = 0f,
                priorAvgSleepHours7d = 0f,
                restingOrAvgHrBpm = 0L,
                hydrationLitersToday = 0.0,
                weeklySleepHours = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
                report = ChannelTelemetryReport(
                    channelName = "Health (Health Connect)",
                    headlineStatus = "Permission Needed",
                    summary = "Grant Android Health Connect access to read real steps, sleep sessions, heart rate, and hydration from your phone.",
                    scorePercent = 50,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Steps Today", "—", "Awaiting permission"),
                        ChannelMetricItem("7d Avg Sleep", "—", "Awaiting permission"),
                        ChannelMetricItem("Heart Rate", "—", "Awaiting permission")
                    ),
                    historyLog = listOf("Tap 'Setup Connections' → 'Connect Google Health' to authorize live biometrics.")
                )
            )
        }

        val weeklySleepList = (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            dailySleepMap[d] ?: 0f
        }
        val recordedSleepDays = weeklySleepList.filter { it > 0.1f }
        val avgSleep7d = if (recordedSleepDays.isNotEmpty()) {
            recordedSleepDays.average().toFloat()
        } else {
            0f
        }
        val priorAvgSleep = if (priorWeekNights > 0) priorWeekSleepTotal / priorWeekNights else avgSleep7d

        val lowSleepAlert = recordedSleepDays.isNotEmpty() && avgSleep7d < 6.0f
        val sleepScore = when {
            recordedSleepDays.isEmpty() -> 72 // Neutral when no sleep tracker sessions written yet
            avgSleep7d >= 7.2f -> 95
            avgSleep7d >= 6.2f -> 78
            else -> 40
        }
        val stepsScore = when {
            stepsToday >= 8000L -> 96
            stepsToday >= 4000L -> 80
            stepsToday > 0L -> 65
            else -> 58
        }
        val combinedScore = ((sleepScore + stepsScore) / 2).coerceIn(25, 99)

        val formattedSteps = String.format(Locale.US, "%,d", stepsToday)
        val formattedSleep = if (recordedSleepDays.isNotEmpty()) {
            String.format(Locale.US, "%.1fh sleep", avgSleep7d)
        } else {
            "No sleep records yet"
        }
        val formattedWater = if (hydrationLiters > 0.01) {
            String.format(Locale.US, " • %.1fL water", hydrationLiters)
        } else {
            ""
        }

        val statusLine = "LIVE: $formattedSteps steps • $formattedSleep$formattedWater"
        val sourceNote = if (hcGranted) "Android Health Connect" else "Device Step Counter Sensor"

        val historyEntries = buildList {
            add("Source: $sourceNote (Synced live from device)")
            add("Today's Steps: $formattedSteps steps (7d total: ${String.format(Locale.US, "%,d", steps7dTotal)})")
            if (recordedSleepDays.isNotEmpty()) {
                add("Recorded Sleep Nights (7d): ${recordedSleepDays.size} nights • Avg ${String.format(Locale.US, "%.1f", avgSleep7d)} hrs")
            } else {
                add("Sleep Sessions: 0 sessions recorded in Health Connect in last 7 days")
            }
            if (avgHrBpm > 0L) {
                add("Heart Rate (7d resting/avg): $avgHrBpm bpm")
            }
        }

        RealHealthSyncResult(
            isConnected = true,
            statusLine = statusLine,
            stepsToday = stepsToday,
            avgSleepHours7d = avgSleep7d,
            priorAvgSleepHours7d = priorAvgSleep,
            restingOrAvgHrBpm = avgHrBpm,
            hydrationLitersToday = hydrationLiters,
            weeklySleepHours = if (recordedSleepDays.isNotEmpty()) weeklySleepList else listOf(7f, 7f, 7f, 7f, 7f, 7f, 7f),
            report = ChannelTelemetryReport(
                channelName = "Health (Live Health Connect)",
                headlineStatus = "$formattedSteps Steps • $formattedSleep",
                summary = if (lowSleepAlert) {
                    "Live Health Connect alert: Your recorded sleep averaged ${String.format(Locale.US, "%.1f", avgSleep7d)}h over ${recordedSleepDays.size} nights."
                } else {
                    "Live telemetry read directly from $sourceNote on this device."
                },
                scorePercent = combinedScore,
                isAlert = lowSleepAlert,
                metrics = listOf(
                    ChannelMetricItem(
                        label = "Steps Today",
                        value = formattedSteps,
                        trend = if (usedHardwareSensor) "Boot sensor" else "Health Connect",
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "Avg Sleep (7d)",
                        value = if (recordedSleepDays.isNotEmpty()) String.format(Locale.US, "%.1f hrs", avgSleep7d) else "0 records",
                        trend = if (recordedSleepDays.isNotEmpty()) "${recordedSleepDays.size} nights logged" else "Log in Fit/Watch",
                        isWarning = lowSleepAlert
                    ),
                    ChannelMetricItem(
                        label = if (avgHrBpm > 0L) "Heart Rate" else "Hydration",
                        value = if (avgHrBpm > 0L) "$avgHrBpm bpm" else String.format(Locale.US, "%.1f L", hydrationLiters),
                        trend = "Live biometric",
                        isWarning = false
                    )
                ),
                historyLog = historyEntries
            )
        )
    }

    private suspend fun readHardwareStepCounterOnce(context: Context): Long? {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null
        return withTimeoutOrNull(1200L) {
            suspendCancellableCoroutine { cont ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        sensorManager.unregisterListener(this)
                        val totalSinceReboot = event?.values?.firstOrNull()?.toLong()
                        if (cont.isActive) cont.resume(totalSinceReboot)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                val registered = sensorManager.registerListener(
                    listener,
                    stepSensor,
                    SensorManager.SENSOR_DELAY_UI
                )
                if (!registered && cont.isActive) {
                    cont.resume(null)
                }
                cont.invokeOnCancellation {
                    sensorManager.unregisterListener(listener)
                }
            }
        }
    }
}
