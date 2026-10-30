package com.example.data.connectors

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.model.ChannelMetricItem
import com.example.model.ChannelTelemetryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class RealCalendarTasksResult(
    val isConnected: Boolean,
    val statusLine: String,
    val eventsTodayCount: Int,
    val completedTodayCount: Int,
    val upcomingWeekCount: Int,
    val lateNightEvents7d: Int,
    val canceledOrDeclined14d: Int,
    val googleTasksSynced: Boolean,
    val googleTasksCompleted: Int,
    val googleTasksPending: Int,
    val report: ChannelTelemetryReport
)

class CalendarTasksConnector(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openCalendarInsertEvent(
        context: Context,
        title: String,
        description: String,
        startMillis: Long = System.currentTimeMillis() + 3600_000L,
        endMillis: Long = System.currentTimeMillis() + 5400_000L
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Ignore if no calendar app installed
        }
    }

    suspend fun syncCalendarAndTasks(
        context: Context,
        googleTasksOAuthToken: String = ""
    ): RealCalendarTasksResult = withContext(Dispatchers.IO) {
        val hasCalPerm = hasCalendarPermission(context)
        val hasTasksToken = googleTasksOAuthToken.isNotBlank()

        if (!hasCalPerm && !hasTasksToken) {
            return@withContext RealCalendarTasksResult(
                isConnected = false,
                statusLine = "Not connected • Grant Calendar access or add Google Tasks token",
                eventsTodayCount = 0,
                completedTodayCount = 0,
                upcomingWeekCount = 0,
                lateNightEvents7d = 0,
                canceledOrDeclined14d = 0,
                googleTasksSynced = false,
                googleTasksCompleted = 0,
                googleTasksPending = 0,
                report = ChannelTelemetryReport(
                    channelName = "Calendar & Tasks (Live)",
                    headlineStatus = "Permission Needed",
                    summary = "Grant Android Calendar permission (or provide a Google Tasks OAuth token) to read your real events, deadlines, and work hours.",
                    scorePercent = 50,
                    isAlert = false,
                    metrics = listOf(
                        ChannelMetricItem("Today's Schedule", "—", "Awaiting permission"),
                        ChannelMetricItem("After-Hours Load", "—", "Awaiting permission"),
                        ChannelMetricItem("Google Tasks", "—", "Optional OAuth")
                    ),
                    historyLog = listOf("Tap 'Setup Connections' → 'Connect Calendar & Tasks' to read live device calendar.")
                )
            )
        }

        val zone = ZoneId.systemDefault()
        val nowMs = System.currentTimeMillis()
        val today = LocalDate.now(zone)
        val startTodayMs = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val endTodayMs = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val sevenDaysAgoMs = today.minusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        val fourteenDaysAgoMs = today.minusDays(14).atStartOfDay(zone).toInstant().toEpochMilli()
        val sevenDaysAheadMs = today.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()

        var eventsToday = 0
        var completedToday = 0
        var upcomingWeek = 0
        var lateNight7d = 0
        var canceledOrDeclined14d = 0
        val recentTitles = mutableListOf<String>()
        val timeFmt = SimpleDateFormat("EEE h:mm a", Locale.getDefault())

        if (hasCalPerm) {
            try {
                val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
                ContentUris.appendId(builder, fourteenDaysAgoMs)
                ContentUris.appendId(builder, sevenDaysAheadMs)

                val projection = arrayOf(
                    CalendarContract.Instances.TITLE,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                    CalendarContract.Instances.STATUS,
                    CalendarContract.Instances.SELF_ATTENDEE_STATUS
                )

                context.contentResolver.query(
                    builder.build(),
                    projection,
                    null,
                    null,
                    "${CalendarContract.Instances.BEGIN} ASC"
                )?.use { cursor ->
                    val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                    val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                    val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
                    val allDayIdx = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                    val statusIdx = cursor.getColumnIndex(CalendarContract.Instances.STATUS)
                    val attendeeIdx = cursor.getColumnIndex(CalendarContract.Instances.SELF_ATTENDEE_STATUS)

                    while (cursor.moveToNext()) {
                        val title = (if (titleIdx >= 0) cursor.getString(titleIdx) else null)
                            ?.takeIf { it.isNotBlank() } ?: "Calendar Event"
                        val begin = if (beginIdx >= 0) cursor.getLong(beginIdx) else 0L
                        val end = if (endIdx >= 0) cursor.getLong(endIdx) else begin
                        val allDay = if (allDayIdx >= 0) cursor.getInt(allDayIdx) == 1 else false
                        val status = if (statusIdx >= 0) cursor.getInt(statusIdx) else 0
                        val selfAttendee = if (attendeeIdx >= 0) cursor.getInt(attendeeIdx) else 0

                        val isCanceled = status == CalendarContract.Events.STATUS_CANCELED ||
                            selfAttendee == CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED

                        if (isCanceled && begin in fourteenDaysAgoMs..nowMs) {
                            canceledOrDeclined14d += 1
                            continue
                        }

                        if (begin in startTodayMs until endTodayMs) {
                            eventsToday += 1
                            if (end <= nowMs) {
                                completedToday += 1
                            }
                            if (recentTitles.size < 4) {
                                recentTitles.add("Today (${timeFmt.format(Date(begin))}): $title")
                            }
                        } else if (begin in endTodayMs..sevenDaysAheadMs) {
                            upcomingWeek += 1
                            if (recentTitles.size < 4) {
                                recentTitles.add("Upcoming (${timeFmt.format(Date(begin))}): $title")
                            }
                        }

                        if (!allDay && begin in sevenDaysAgoMs..nowMs) {
                            val endHour = Instant.ofEpochMilli(end).atZone(zone).hour
                            val startHour = Instant.ofEpochMilli(begin).atZone(zone).hour
                            if (startHour >= 21 || endHour >= 22 || startHour in 0..3) {
                                lateNight7d += 1
                            }
                        }
                    }
                }
            } catch (_: SecurityException) {
                // Handled if revoked mid-query
            } catch (_: Exception) {
                // Provider query exception fallback
            }
        }

        // Optional Real Google Tasks REST API v1 fetch
        var tasksSynced = false
        var gTasksDone = 0
        var gTasksPending = 0
        var tasksError: String? = null

        if (hasTasksToken) {
            try {
                val listsReq = Request.Builder()
                    .url("https://tasks.googleapis.com/tasks/v1/users/@me/lists?maxResults=5")
                    .addHeader("Authorization", "Bearer ${googleTasksOAuthToken.trim()}")
                    .get()
                    .build()

                httpClient.newCall(listsReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        val json = JSONObject(body)
                        val items = json.optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            tasksSynced = true
                            val firstListId = items.getJSONObject(0).optString("id")
                            if (firstListId.isNotBlank()) {
                                val tasksReq = Request.Builder()
                                    .url("https://tasks.googleapis.com/tasks/v1/lists/$firstListId/tasks?showCompleted=true&showHidden=true&maxResults=50")
                                    .addHeader("Authorization", "Bearer ${googleTasksOAuthToken.trim()}")
                                    .get()
                                    .build()
                                httpClient.newCall(tasksReq).execute().use { tResp ->
                                    if (tResp.isSuccessful) {
                                        val tJson = JSONObject(tResp.body?.string().orEmpty())
                                        val tItems = tJson.optJSONArray("items")
                                        if (tItems != null) {
                                            for (i in 0 until tItems.length()) {
                                                val taskObj = tItems.getJSONObject(i)
                                                val st = taskObj.optString("status")
                                                val tTitle = taskObj.optString("title", "Task")
                                                if (st == "completed") {
                                                    gTasksDone += 1
                                                } else {
                                                    gTasksPending += 1
                                                    if (recentTitles.size < 5) {
                                                        recentTitles.add("Google Task (Open): $tTitle")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        tasksError = "Google Tasks HTTP ${resp.code}"
                    }
                }
            } catch (e: Exception) {
                tasksError = e.localizedMessage ?: "Network error"
            }
        }

        val totalActionable = eventsToday + gTasksDone + gTasksPending
        val totalDone = completedToday + gTasksDone
        val completionPct = if (totalActionable > 0) {
            ((totalDone.toFloat() / totalActionable.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        val overworkAlert = lateNight7d >= 3
        val score = when {
            overworkAlert -> 68
            totalActionable == 0 -> 88
            else -> (70 + (completionPct * 0.28f).toInt()).coerceIn(50, 99)
        }

        val statusParts = mutableListOf<String>()
        if (hasCalPerm) {
            statusParts.add("$completedToday/$eventsToday today • $upcomingWeek upcoming (7d)")
        }
        if (tasksSynced) {
            statusParts.add("Google Tasks: $gTasksDone done, $gTasksPending open")
        } else if (tasksError != null) {
            statusParts.add(tasksError!!)
        }
        val statusLine = "LIVE: " + statusParts.joinToString(" • ")

        if (recentTitles.isEmpty()) {
            recentTitles.add("Live Calendar connected — 0 events scheduled remaining today.")
        }

        RealCalendarTasksResult(
            isConnected = hasCalPerm || tasksSynced,
            statusLine = statusLine,
            eventsTodayCount = eventsToday,
            completedTodayCount = completedToday,
            upcomingWeekCount = upcomingWeek,
            lateNightEvents7d = lateNight7d,
            canceledOrDeclined14d = canceledOrDeclined14d,
            googleTasksSynced = tasksSynced,
            googleTasksCompleted = gTasksDone,
            googleTasksPending = gTasksPending,
            report = ChannelTelemetryReport(
                channelName = "Calendar & Tasks (Live)",
                headlineStatus = if (tasksSynced) {
                    "$eventsToday Events • $gTasksDone/${gTasksDone + gTasksPending} Tasks"
                } else {
                    "$eventsToday Today • $upcomingWeek Next 7d"
                },
                summary = if (overworkAlert) {
                    "Live schedule warning: Detected $lateNight7d late-night calendar blocks (after 9:30 PM) in the past 7 days."
                } else {
                    "Synced directly with your Android Calendar provider${if (tasksSynced) " & Google Tasks REST API" else ""}."
                },
                scorePercent = score,
                isAlert = overworkAlert,
                metrics = listOf(
                    ChannelMetricItem(
                        label = "Today's Events",
                        value = "$completedToday / $eventsToday",
                        trend = "$upcomingWeek upcoming (7d)",
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "Late-Night (7d)",
                        value = "$lateNight7d blocks",
                        trend = if (overworkAlert) "Past 9:30 PM!" else "Healthy cutoff",
                        isWarning = overworkAlert
                    ),
                    ChannelMetricItem(
                        label = if (tasksSynced) "Google Tasks" else "Declined (14d)",
                        value = if (tasksSynced) "$gTasksDone done" else "$canceledOrDeclined14d events",
                        trend = if (tasksSynced) "$gTasksPending open" else "Calendar RSVP",
                        isWarning = !tasksSynced && canceledOrDeclined14d >= 3
                    )
                ),
                historyLog = recentTitles
            )
        )
    }
}
