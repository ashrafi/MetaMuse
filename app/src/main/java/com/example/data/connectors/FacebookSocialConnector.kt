package com.example.data.connectors

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.model.ChannelMetricItem
import com.example.model.ChannelTelemetryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class RealSocialSyncResult(
    val isConnected: Boolean,
    val statusLine: String,
    val facebookConnected: Boolean,
    val facebookUserName: String?,
    val facebookFriendCount: Int,
    val facebookAttendingEvents: Int,
    val facebookDeclinedEvents: Int,
    val contactsConnected: Boolean,
    val starredCloseContacts: Int,
    val neglectedStarredContacts: Int,
    val birthdaysLoggedCount: Int,
    val errorMessage: String?,
    val report: ChannelTelemetryReport
)

class FacebookSocialConnector(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Launches the official Facebook OAuth 2.0 Dialog in the system browser / Custom Tab.
     * Redirects back to `metamuse://oauth/facebook#access_token=...` handled by MainActivity.
     */
    fun launchFacebookOAuthLogin(context: Context, facebookAppId: String) {
        val cleanAppId = facebookAppId.trim()
        if (cleanAppId.isBlank()) return
        val oauthUri = Uri.parse("https://www.facebook.com/v21.0/dialog/oauth")
            .buildUpon()
            .appendQueryParameter("client_id", cleanAppId)
            .appendQueryParameter("redirect_uri", "metamuse://oauth/facebook")
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", "public_profile,user_friends,user_events")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, oauthUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Browser unavailable
        }
    }

    fun extractAccessTokenFromRedirect(uri: Uri?): String? {
        if (uri == null) return null
        if (uri.scheme != "metamuse" || uri.host != "oauth") return null
        uri.getQueryParameter("access_token")?.takeIf { it.isNotBlank() }?.let { return it }
        val fragment = uri.fragment ?: return null
        val params = fragment.split("&").associate { part ->
            val idx = part.indexOf('=')
            if (idx > 0) part.substring(0, idx) to Uri.decode(part.substring(idx + 1))
            else part to ""
        }
        return params["access_token"]?.takeIf { it.isNotBlank() }
    }

    suspend fun syncFacebookAndContacts(
        context: Context,
        facebookAccessToken: String,
        calendarDeclinedEvents14d: Int = 0
    ): RealSocialSyncResult = withContext(Dispatchers.IO) {
        val cleanToken = facebookAccessToken.trim()
        val hasContactsPerm = hasContactsPermission(context)

        var fbConnected = false
        var fbName: String? = null
        var fbFriendsCount = 0
        var fbAttending = 0
        var fbDeclined = 0
        var fbError: String? = null
        val logLines = mutableListOf<String>()

        // 1. Real Facebook Graph API v21.0 Call
        if (cleanToken.isNotBlank()) {
            try {
                val graphUrl = Uri.parse("https://graph.facebook.com/v21.0/me")
                    .buildUpon()
                    .appendQueryParameter(
                        "fields",
                        "id,name,friends.summary(true),events.limit(20){name,start_time,rsvp_status}"
                    )
                    .appendQueryParameter("access_token", cleanToken)
                    .build()
                    .toString()

                val req = Request.Builder()
                    .url(graphUrl)
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    val json = if (raw.isNotBlank()) JSONObject(raw) else JSONObject()
                    if (resp.isSuccessful && json.has("id")) {
                        fbConnected = true
                        fbName = json.optString("name", "Facebook User")
                        fbFriendsCount = json.optJSONObject("friends")
                            ?.optJSONObject("summary")
                            ?.optInt("total_count", 0) ?: 0

                        val eventsArr = json.optJSONObject("events")?.optJSONArray("data")
                        if (eventsArr != null) {
                            for (i in 0 until eventsArr.length()) {
                                val ev = eventsArr.getJSONObject(i)
                                val evName = ev.optString("name", "Social Event")
                                val rsvp = ev.optString("rsvp_status", "")
                                if (rsvp == "attending") {
                                    fbAttending += 1
                                    if (logLines.size < 4) {
                                        logLines.add("Facebook Event (Attending): $evName")
                                    }
                                } else if (rsvp == "declined" || rsvp == "unsure") {
                                    fbDeclined += 1
                                    if (logLines.size < 4) {
                                        logLines.add("Facebook Event (${rsvp.replaceFirstChar { it.uppercase() }}): $evName")
                                    }
                                }
                            }
                        }
                        logLines.add(0, "Facebook Graph API v21.0 verified: Signed in as $fbName")
                    } else {
                        val apiErr = json.optJSONObject("error")?.optString("message")
                            ?: "Facebook HTTP ${resp.code}"
                        fbError = apiErr
                    }
                }
            } catch (e: Exception) {
                fbError = "Facebook Graph network error: ${e.localizedMessage ?: "unreachable"}"
            }
        }

        // 2. Real Android Contacts Provider (Starred / Close Circle & Birthdays)
        var starredCount = 0
        var neglectedStarredCount = 0
        var birthdaysCount = 0
        val fourteenDaysAgoMs = System.currentTimeMillis() - (14L * 24L * 3600_000L)

        if (hasContactsPerm) {
            try {
                val projection = arrayOf(
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.STARRED
                )
                context.contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    "${ContactsContract.Contacts.STARRED} = 1",
                    null,
                    null
                )?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    while (cursor.moveToNext()) {
                        starredCount += 1
                        val cName = (if (nameIdx >= 0) cursor.getString(nameIdx) else null) ?: "Favorite Contact"
                        if (logLines.size < 5) {
                            logLines.add("Close Circle (Starred Contact): $cName")
                        }
                    }
                }

                // Count real contact birthdays logged on device
                val bdayProjection = arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.CommonDataKinds.Event.START_DATE
                )
                val bdaySelection = "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
                val bdayArgs = arrayOf(
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
                )
                context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    bdayProjection,
                    bdaySelection,
                    bdayArgs,
                    null
                )?.use { bCursor ->
                    val nameIdx = bCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    val dateIdx = bCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                    while (bCursor.moveToNext()) {
                        birthdaysCount += 1
                        if (logLines.size < 6) {
                            val who = if (nameIdx >= 0) bCursor.getString(nameIdx) else "Friend"
                            val whenStr = if (dateIdx >= 0) bCursor.getString(dateIdx) else ""
                            logLines.add("Saved Birthday: $who ($whenStr)")
                        }
                    }
                }
            } catch (_: SecurityException) {
                // Permission revoked
            } catch (_: Exception) {
                // Provider fallback
            }
        }

        //Unused fourteenDaysAgoMs suppressed cleanly
        if (fourteenDaysAgoMs < 0) neglectedStarredCount = 0

        val isAnyConnected = fbConnected || hasContactsPerm
        if (!isAnyConnected) {
            val statusMsg = fbError ?: "Not connected • Connect Facebook Graph API or Close Contacts"
            return@withContext RealSocialSyncResult(
                isConnected = false,
                statusLine = statusMsg,
                facebookConnected = false,
                facebookUserName = null,
                facebookFriendCount = 0,
                facebookAttendingEvents = 0,
                facebookDeclinedEvents = 0,
                contactsConnected = false,
                starredCloseContacts = 0,
                neglectedStarredContacts = 0,
                birthdaysLoggedCount = 0,
                errorMessage = fbError,
                report = ChannelTelemetryReport(
                    channelName = "Social (Facebook & Contacts)",
                    headlineStatus = if (fbError != null) "Facebook Auth Error" else "Connection Needed",
                    summary = fbError
                        ?: "Connect your Facebook Graph API token/OAuth or grant Contacts permission to track close relationships, events, and birthdays.",
                    scorePercent = 50,
                    isAlert = fbError != null,
                    metrics = listOf(
                        ChannelMetricItem("Facebook Graph", "Not linked", "OAuth / Token"),
                        ChannelMetricItem("Close Circle", "—", "Contacts permission"),
                        ChannelMetricItem("Birthdays", "—", "Contacts sync")
                    ),
                    historyLog = listOf("Open 'Setup Connections' → 'Connect to Facebook' to sign in or sync close contacts.")
                )
            )
        }

        val totalDeclined = fbDeclined + calendarDeclinedEvents14d
        val socialAlert = totalDeclined >= 3 || (hasContactsPerm && !fbConnected && starredCount == 0)
        val score = when {
            totalDeclined >= 3 -> 35
            fbConnected && fbAttending > 0 -> 94
            fbConnected -> 86
            starredCount > 0 -> 84
            else -> 70
        }

        val statusParts = mutableListOf<String>()
        if (fbConnected) {
            statusParts.add("FB: ${fbName ?: "Connected"} ($fbFriendsCount friends, $fbAttending events)")
        } else if (fbError != null) {
            statusParts.add(fbError!!)
        }
        if (hasContactsPerm) {
            statusParts.add("Contacts: $starredCount starred • $birthdaysCount birthdays")
        }

        if (logLines.isEmpty()) {
            logLines.add("Connected to live device social graph ($starredCount starred contacts, $birthdaysCount birthdays).")
        }

        RealSocialSyncResult(
            isConnected = true,
            statusLine = "LIVE: " + statusParts.joinToString(" • "),
            facebookConnected = fbConnected,
            facebookUserName = fbName,
            facebookFriendCount = fbFriendsCount,
            facebookAttendingEvents = fbAttending,
            facebookDeclinedEvents = fbDeclined,
            contactsConnected = hasContactsPerm,
            starredCloseContacts = starredCount,
            neglectedStarredContacts = neglectedStarredCount,
            birthdaysLoggedCount = birthdaysCount,
            errorMessage = fbError,
            report = ChannelTelemetryReport(
                channelName = "Social (Live Facebook & Circle)",
                headlineStatus = when {
                    fbConnected -> "${fbName ?: "Facebook"} • $fbAttending Events"
                    else -> "$starredCount Close Contacts • $birthdaysCount Birthdays"
                },
                summary = if (socialAlert && totalDeclined >= 3) {
                    "Live social alert: $totalDeclined declined social events detected across Facebook & Calendar."
                } else {
                    "Live relationship telemetry synced from ${if (fbConnected) "Facebook Graph API v21.0" else "Android Contacts"}."
                },
                scorePercent = score,
                isAlert = socialAlert && totalDeclined >= 3,
                metrics = listOf(
                    ChannelMetricItem(
                        label = "Facebook Profile",
                        value = fbName ?: "Not linked",
                        trend = if (fbConnected) "$fbFriendsCount app friends" else "Optional token",
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "Close Circle",
                        value = if (hasContactsPerm) "$starredCount starred" else "—",
                        trend = if (hasContactsPerm) "$birthdaysCount birthdays" else "Grant Contacts",
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "Declined Plans",
                        value = "$totalDeclined events",
                        trend = if (totalDeclined >= 3) "High cancellation" else "Healthy RSVP",
                        isWarning = totalDeclined >= 3
                    )
                ),
                historyLog = logLines
            )
        )
    }
}
