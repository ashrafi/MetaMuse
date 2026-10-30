package com.example.data.connectors

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.model.ChannelMetricItem
import com.example.model.ChannelTelemetryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class RealFinanceSyncResult(
    val isConnected: Boolean,
    val sourceName: String?,
    val statusLine: String,
    val exchangedPlaidAccessToken: String? = null,
    val claimedSimpleFinAccessUrl: String? = null,
    val totalBalanceUsd: Double?,
    val totalSpend30dUsd: Double,
    val totalInflow30dUsd: Double,
    val deliveryOrderCount: Int,
    val deliverySpendUsd: Double,
    val errorMessage: String?,
    val report: ChannelTelemetryReport
)

class BankFinanceConnector(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    private val deliveryKeywords = listOf(
        "doordash", "uber eats", "ubereats", "grubhub", "postmates",
        "instacart", "caviar", "seamless", "gopuff", "deliveroo", "dashpass"
    )

    fun unconfiguredResult(error: String? = null): RealFinanceSyncResult {
        return RealFinanceSyncResult(
            isConnected = false,
            sourceName = null,
            statusLine = error ?: "Not connected • Connect Plaid API, SimpleFIN Open Banking, or Bank CSV",
            totalBalanceUsd = null,
            totalSpend30dUsd = 0.0,
            totalInflow30dUsd = 0.0,
            deliveryOrderCount = 0,
            deliverySpendUsd = 0.0,
            errorMessage = error,
            report = ChannelTelemetryReport(
                channelName = "Finance (Bank & Budget)",
                headlineStatus = if (error != null) "Connection Error" else "Bank Link Needed",
                summary = error
                    ?: "Connect a real bank account via Plaid API (Sandbox or Production), SimpleFIN Open Banking Bridge, or import a real bank .CSV export.",
                scorePercent = 50,
                isAlert = error != null,
                metrics = listOf(
                    ChannelMetricItem("Bank Balance", "—", "Link Plaid / CSV"),
                    ChannelMetricItem("30d Outflow", "—", "No account linked"),
                    ChannelMetricItem("Delivery Spend", "—", "Pattern monitor")
                ),
                historyLog = listOf("Open 'Setup Connections' → 'Connect to Finance App' to link Plaid, SimpleFIN, or import a bank CSV.")
            )
        )
    }

    /**
     * Connects to the real Plaid REST API (`sandbox.plaid.com` or `production.plaid.com`).
     * In `sandbox` mode, if `accessToken` is blank, automatically creates a live Sandbox public_token
     * and exchanges it for a real `access_token` before pulling `/accounts/balance/get` and `/transactions/get`.
     */
    suspend fun syncViaPlaid(
        clientId: String,
        secret: String,
        environment: String = "sandbox",
        existingAccessToken: String = ""
    ): RealFinanceSyncResult = withContext(Dispatchers.IO) {
        val cleanClientId = clientId.trim()
        val cleanSecret = secret.trim()
        val envHost = if (environment.lowercase(Locale.US) == "production") {
            "https://production.plaid.com"
        } else {
            "https://sandbox.plaid.com"
        }

        if (cleanClientId.isBlank() || cleanSecret.isBlank()) {
            return@withContext unconfiguredResult("Enter both Plaid Client ID and Secret.")
        }

        val jsonMedia = "application/json; charset=utf-8".toMediaType()
        var activeToken = existingAccessToken.trim()

        try {
            // Step 1: If in Sandbox without an access_token, perform real Plaid Sandbox Link exchange!
            if (activeToken.isBlank()) {
                if (envHost.contains("production")) {
                    return@withContext unconfiguredResult("Plaid Production requires an exchanged item access_token.")
                }
                val createPubPayload = JSONObject().apply {
                    put("client_id", cleanClientId)
                    put("secret", cleanSecret)
                    put("institution_id", "ins_109508") // First Platypus Bank (Plaid official sandbox bank)
                    put("initial_products", JSONArray().put("transactions"))
                }
                val pubReq = Request.Builder()
                    .url("$envHost/sandbox/public_token/create")
                    .post(createPubPayload.toString().toRequestBody(jsonMedia))
                    .build()

                val publicToken = httpClient.newCall(pubReq).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    val obj = if (raw.isNotBlank()) JSONObject(raw) else JSONObject()
                    if (!resp.isSuccessful) {
                        val msg = obj.optString("error_message", "Plaid HTTP ${resp.code}")
                        return@withContext unconfiguredResult("Plaid error: $msg")
                    }
                    obj.optString("public_token")
                }

                val exchangePayload = JSONObject().apply {
                    put("client_id", cleanClientId)
                    put("secret", cleanSecret)
                    put("public_token", publicToken)
                }
                val exReq = Request.Builder()
                    .url("$envHost/item/public_token/exchange")
                    .post(exchangePayload.toString().toRequestBody(jsonMedia))
                    .build()

                activeToken = httpClient.newCall(exReq).execute().use { resp ->
                    val raw = resp.body?.string().orEmpty()
                    val obj = if (raw.isNotBlank()) JSONObject(raw) else JSONObject()
                    if (!resp.isSuccessful) {
                        val msg = obj.optString("error_message", "Exchange HTTP ${resp.code}")
                        return@withContext unconfiguredResult("Plaid exchange error: $msg")
                    }
                    obj.optString("access_token")
                }
            }

            // Step 2: Fetch real account balances via `/accounts/balance/get`
            val balPayload = JSONObject().apply {
                put("client_id", cleanClientId)
                put("secret", cleanSecret)
                put("access_token", activeToken)
            }
            var totalBalance = 0.0
            val accountSummaries = mutableListOf<String>()
            httpClient.newCall(
                Request.Builder()
                    .url("$envHost/accounts/balance/get")
                    .post(balPayload.toString().toRequestBody(jsonMedia))
                    .build()
            ).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                val obj = if (raw.isNotBlank()) JSONObject(raw) else JSONObject()
                if (!resp.isSuccessful) {
                    val msg = obj.optString("error_message", "Balance HTTP ${resp.code}")
                    return@withContext unconfiguredResult("Plaid balance error: $msg")
                }
                val accounts = obj.optJSONArray("accounts")
                if (accounts != null) {
                    for (i in 0 until accounts.length()) {
                        val acc = accounts.getJSONObject(i)
                        val name = acc.optString("name", "Account")
                        val subtype = acc.optString("subtype", "")
                        val bal = acc.optJSONObject("balances")?.optDouble("current", 0.0) ?: 0.0
                        if (subtype != "credit card" && subtype != "loan") {
                            totalBalance += bal
                        }
                        if (accountSummaries.size < 3) {
                            accountSummaries.add("Plaid Account: $name ($${String.format(Locale.US, "%,.2f", bal)})")
                        }
                    }
                }
            }

            // Step 3: Fetch real 30-day transactions via `/transactions/get`
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(30)
            val txPayload = JSONObject().apply {
                put("client_id", cleanClientId)
                put("secret", cleanSecret)
                put("access_token", activeToken)
                put("start_date", startDate.toString())
                put("end_date", endDate.toString())
            }

            var totalSpend = 0.0
            var totalInflow = 0.0
            var deliveryCount = 0
            var deliverySpend = 0.0
            val recentTxLogs = mutableListOf<String>()

            httpClient.newCall(
                Request.Builder()
                    .url("$envHost/transactions/get")
                    .post(txPayload.toString().toRequestBody(jsonMedia))
                    .build()
            ).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (resp.isSuccessful && raw.isNotBlank()) {
                    val obj = JSONObject(raw)
                    val txs = obj.optJSONArray("transactions")
                    if (txs != null) {
                        for (i in 0 until txs.length()) {
                            val tx = txs.getJSONObject(i)
                            // In Plaid, positive amount = money leaving account; negative = deposit/inflow
                            val amount = tx.optDouble("amount", 0.0)
                            val name = tx.optString("merchant_name").takeIf { it.isNotBlank() && it != "null" }
                                ?: tx.optString("name", "Transaction")
                            val date = tx.optString("date", "")
                            if (amount > 0) {
                                totalSpend += amount
                                val lowerName = name.lowercase(Locale.US)
                                if (deliveryKeywords.any { lowerName.contains(it) }) {
                                    deliveryCount += 1
                                    deliverySpend += amount
                                }
                                if (recentTxLogs.size < 4) {
                                    recentTxLogs.add("$date: $name (-$${String.format(Locale.US, "%.2f", amount)})")
                                }
                            } else if (amount < 0) {
                                totalInflow += abs(amount)
                            }
                        }
                    }
                }
            }

            buildSuccessResult(
                sourceName = "Plaid (${environment.replaceFirstChar { it.uppercase() }})",
                totalBalance = totalBalance,
                totalSpend = totalSpend,
                totalInflow = totalInflow,
                deliveryCount = deliveryCount,
                deliverySpend = deliverySpend,
                logEntries = accountSummaries + recentTxLogs,
                exchangedPlaidToken = activeToken
            )
        } catch (e: Exception) {
            unconfiguredResult("Plaid network error: ${e.localizedMessage ?: "unreachable"}")
        }
    }

    /**
     * Connects to a real US/CA bank via SimpleFIN Open Banking Bridge (`https://beta-bridge.simplefin.org`).
     * Accepts either a Base64 SimpleFIN Setup Token (which is claimed once via POST) or an active Access URL.
     */
    suspend fun syncViaSimpleFin(tokenOrAccessUrl: String): RealFinanceSyncResult = withContext(Dispatchers.IO) {
        val rawInput = tokenOrAccessUrl.trim()
        if (rawInput.isBlank()) {
            return@withContext unconfiguredResult("Enter a SimpleFIN Setup Token or Access URL.")
        }

        try {
            var accessUrl = rawInput
            if (!rawInput.startsWith("http://") && !rawInput.startsWith("https://")) {
                // Decode Base64 Setup Token -> Claim URL -> POST to obtain Basic-Auth Access URL
                val claimUrl = String(Base64.decode(rawInput, Base64.DEFAULT)).trim()
                val claimReq = Request.Builder()
                    .url(claimUrl)
                    .post("".toRequestBody("text/plain".toMediaType()))
                    .build()
                accessUrl = httpClient.newCall(claimReq).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        return@withContext unconfiguredResult("SimpleFIN token claim failed (HTTP ${resp.code}).")
                    }
                    resp.body?.string()?.trim().orEmpty()
                }
            }

            val parsedUri = Uri.parse(accessUrl)
            val userInfo = parsedUri.userInfo
            val cleanBaseUrl = accessUrl.replace("${userInfo}@", "").trimEnd('/')
            val thirtyDaysAgoEpoch = (System.currentTimeMillis() / 1000L) - (30L * 24L * 3600L)

            val reqBuilder = Request.Builder()
                .url("$cleanBaseUrl/accounts?start-date=$thirtyDaysAgoEpoch")
                .get()

            if (!userInfo.isNullOrBlank() && userInfo.contains(":")) {
                val parts = userInfo.split(":", limit = 2)
                reqBuilder.addHeader("Authorization", Credentials.basic(parts[0], parts[1]))
            }

            httpClient.newCall(reqBuilder.build()).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext unconfiguredResult("SimpleFIN HTTP ${resp.code}")
                }
                val body = resp.body?.string().orEmpty()
                val json = JSONObject(body)
                val accounts = json.optJSONArray("accounts") ?: JSONArray()

                var totalBalance = 0.0
                var totalSpend = 0.0
                var totalInflow = 0.0
                var deliveryCount = 0
                var deliverySpend = 0.0
                val logs = mutableListOf<String>()

                for (i in 0 until accounts.length()) {
                    val acc = accounts.getJSONObject(i)
                    val orgName = acc.optJSONObject("org")?.optString("name", "Bank") ?: "Bank"
                    val accName = acc.optString("name", "Account")
                    val bal = acc.optString("balance", "0").toDoubleOrNull() ?: 0.0
                    totalBalance += bal
                    logs.add("$orgName • $accName: $${String.format(Locale.US, "%,.2f", bal)}")

                    val txs = acc.optJSONArray("transactions")
                    if (txs != null) {
                        for (j in 0 until txs.length()) {
                            val tx = txs.getJSONObject(j)
                            val amt = tx.optString("amount", "0").toDoubleOrNull() ?: 0.0
                            val desc = tx.optString("description", "Transaction")
                            if (amt < 0) {
                                val absAmt = abs(amt)
                                totalSpend += absAmt
                                if (deliveryKeywords.any { desc.lowercase(Locale.US).contains(it) }) {
                                    deliveryCount += 1
                                    deliverySpend += absAmt
                                }
                                if (logs.size < 6) {
                                    logs.add("$desc (-$${String.format(Locale.US, "%.2f", absAmt)})")
                                }
                            } else if (amt > 0) {
                                totalInflow += amt
                            }
                        }
                    }
                }

                buildSuccessResult(
                    sourceName = "SimpleFIN Open Banking",
                    totalBalance = totalBalance,
                    totalSpend = totalSpend,
                    totalInflow = totalInflow,
                    deliveryCount = deliveryCount,
                    deliverySpend = deliverySpend,
                    logEntries = logs,
                    claimedSimpleFinUrl = accessUrl
                )
            }
        } catch (e: Exception) {
            unconfiguredResult("SimpleFIN error: ${e.localizedMessage ?: "invalid token or URL"}")
        }
    }

    /**
     * Imports and parses a real exported bank / credit card `.CSV` file from the user's device
     * (supports Chase, Bank of America, Apple Card, Amex, Wells Fargo, Capital One, Mint, etc.).
     */
    suspend fun importBankCsv(
        context: Context,
        uri: Uri
    ): Pair<RealFinanceSyncResult, String?> = withContext(Dispatchers.IO) {
        try {
            val lines = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readLines()
            } ?: return@withContext unconfiguredResult("Unable to read selected CSV file.") to null

            val nonEmpty = lines.filter { it.isNotBlank() }
            if (nonEmpty.size < 2) {
                return@withContext unconfiguredResult("CSV file has no transaction rows.") to null
            }

            val headers = splitCsvRow(nonEmpty.first()).map { it.lowercase(Locale.US).trim() }
            val dateCol = headers.indexOfFirst { it.contains("date") }
            val descCol = headers.indexOfFirst {
                it.contains("description") || it.contains("merchant") || it.contains("name") || it.contains("memo") || it.contains("payee")
            }
            val amountCol = headers.indexOfFirst { it == "amount" || it.contains("amount") }
            val debitCol = headers.indexOfFirst { it.contains("debit") || it.contains("withdrawal") }
            val creditCol = headers.indexOfFirst { it.contains("credit") || it.contains("deposit") }
            val balanceCol = headers.indexOfFirst { it.contains("balance") }

            var latestBalance: Double? = null
            var totalSpend = 0.0
            var totalInflow = 0.0
            var deliveryCount = 0
            var deliverySpend = 0.0
            val txLogs = mutableListOf<String>()
            val serializedRows = JSONArray()

            for (rowStr in nonEmpty.drop(1).take(500)) {
                val cols = splitCsvRow(rowStr)
                val date = if (dateCol in cols.indices) cols[dateCol] else ""
                val desc = if (descCol in cols.indices) cols[descCol] else cols.getOrElse(1) { "Transaction" }

                if (latestBalance == null && balanceCol in cols.indices) {
                    latestBalance = parseCurrencyDouble(cols[balanceCol])
                }

                val signedAmount: Double? = when {
                    amountCol in cols.indices -> parseCurrencyDouble(cols[amountCol])
                    debitCol in cols.indices || creditCol in cols.indices -> {
                        val deb = if (debitCol in cols.indices) abs(parseCurrencyDouble(cols[debitCol]) ?: 0.0) else 0.0
                        val cred = if (creditCol in cols.indices) abs(parseCurrencyDouble(cols[creditCol]) ?: 0.0) else 0.0
                        if (deb > 0.0) -deb else if (cred > 0.0) cred else null
                    }
                    else -> cols.mapNotNull { parseCurrencyDouble(it) }.firstOrNull()
                }

                if (signedAmount != null) {
                    // Most bank CSVs put outflows as negative (-$24.50); credit card CSVs sometimes put charges positive.
                    if (signedAmount < 0) {
                        val spend = abs(signedAmount)
                        totalSpend += spend
                        if (deliveryKeywords.any { desc.lowercase(Locale.US).contains(it) }) {
                            deliveryCount += 1
                            deliverySpend += spend
                        }
                        if (txLogs.size < 5) {
                            txLogs.add("$date: $desc (-$${String.format(Locale.US, "%.2f", spend)})")
                        }
                    } else if (signedAmount > 0) {
                        totalInflow += signedAmount
                    }

                    if (serializedRows.length() < 100) {
                        serializedRows.put(JSONObject().apply {
                            put("date", date)
                            put("desc", desc)
                            put("amount", signedAmount)
                        })
                    }
                }
            }

            // If all amounts in the CSV were positive (typical Apple Card / Amex charge-only export), treat as spend
            if (totalSpend == 0.0 && totalInflow > 0.0) {
                totalSpend = totalInflow
                totalInflow = 0.0
            }

            val summaryJson = JSONObject().apply {
                put("balance", latestBalance)
                put("totalSpend", totalSpend)
                put("totalInflow", totalInflow)
                put("deliveryCount", deliveryCount)
                put("deliverySpend", deliverySpend)
                put("logs", JSONArray(txLogs))
            }.toString()

            val result = buildSuccessResult(
                sourceName = "Imported Bank CSV (${nonEmpty.size - 1} rows)",
                totalBalance = latestBalance,
                totalSpend = totalSpend,
                totalInflow = totalInflow,
                deliveryCount = deliveryCount,
                deliverySpend = deliverySpend,
                logEntries = listOf("Parsed ${nonEmpty.size - 1} real statement rows from device CSV") + txLogs
            )
            result to summaryJson
        } catch (e: Exception) {
            unconfiguredResult("CSV parse error: ${e.localizedMessage ?: "invalid format"}") to null
        }
    }

    fun restoreCachedCsv(cachedJson: String): RealFinanceSyncResult? {
        if (cachedJson.isBlank()) return null
        return try {
            val obj = JSONObject(cachedJson)
            val bal = if (obj.has("balance") && !obj.isNull("balance")) obj.optDouble("balance") else null
            val logsArr = obj.optJSONArray("logs")
            val logs = mutableListOf<String>()
            if (logsArr != null) {
                for (i in 0 until logsArr.length()) {
                    logs.add(logsArr.optString(i))
                }
            }
            buildSuccessResult(
                sourceName = "Imported Bank CSV",
                totalBalance = bal,
                totalSpend = obj.optDouble("totalSpend", 0.0),
                totalInflow = obj.optDouble("totalInflow", 0.0),
                deliveryCount = obj.optInt("deliveryCount", 0),
                deliverySpend = obj.optDouble("deliverySpend", 0.0),
                logEntries = logs
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun buildSuccessResult(
        sourceName: String,
        totalBalance: Double?,
        totalSpend: Double,
        totalInflow: Double,
        deliveryCount: Int,
        deliverySpend: Double,
        logEntries: List<String>,
        exchangedPlaidToken: String? = null,
        claimedSimpleFinUrl: String? = null
    ): RealFinanceSyncResult {
        val deliveryLeakAlert = deliveryCount >= 5 || deliverySpend >= 150.0
        val netFlow = totalInflow - totalSpend
        val score = when {
            deliveryLeakAlert -> 48
            totalInflow > 0 && netFlow >= 0 -> 94
            else -> 82
        }

        val balFormatted = totalBalance?.let { "$${String.format(Locale.US, "%,.2f", it)}" } ?: "Synced"
        val spendFormatted = "$${String.format(Locale.US, "%,.0f", totalSpend)}"
        val statusLine = buildString {
            append("LIVE ($sourceName): ")
            if (totalBalance != null) append("Balance $balFormatted • ")
            append("30d Spend $spendFormatted")
            if (deliveryCount > 0) {
                append(" • $deliveryCount delivery orders ($${String.format(Locale.US, "%.0f", deliverySpend)})")
            }
        }

        return RealFinanceSyncResult(
            isConnected = true,
            sourceName = sourceName,
            statusLine = statusLine,
            exchangedPlaidAccessToken = exchangedPlaidToken,
            claimedSimpleFinAccessUrl = claimedSimpleFinUrl,
            totalBalanceUsd = totalBalance,
            totalSpend30dUsd = totalSpend,
            totalInflow30dUsd = totalInflow,
            deliveryOrderCount = deliveryCount,
            deliverySpendUsd = deliverySpend,
            errorMessage = null,
            report = ChannelTelemetryReport(
                channelName = "Finance ($sourceName)",
                headlineStatus = if (totalBalance != null) {
                    "$balFormatted Balance • $spendFormatted Spend"
                } else {
                    "$spendFormatted 30d Spend"
                },
                summary = if (deliveryLeakAlert) {
                    "Live finance alert: Detected $deliveryCount food-delivery orders totaling $${String.format(Locale.US, "%.2f", deliverySpend)}."
                } else {
                    "Live financial telemetry synced via $sourceName."
                },
                scorePercent = score,
                isAlert = deliveryLeakAlert,
                metrics = listOf(
                    ChannelMetricItem(
                        label = "Balance",
                        value = balFormatted,
                        trend = sourceName.substringBefore(" "),
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "30d Outflow",
                        value = spendFormatted,
                        trend = if (totalInflow > 0) "+$${String.format(Locale.US, "%,.0f", totalInflow)} in" else "30d window",
                        isWarning = false
                    ),
                    ChannelMetricItem(
                        label = "Delivery Orders",
                        value = "$deliveryCount ($${String.format(Locale.US, "%.0f", deliverySpend)})",
                        trend = if (deliveryLeakAlert) "Money leak!" else "Controlled",
                        isWarning = deliveryLeakAlert
                    )
                ),
                historyLog = if (logEntries.isNotEmpty()) logEntries else listOf("Connected to $sourceName — 0 recent transactions.")
            )
        )
    }

    private fun splitCsvRow(row: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in row) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    private fun parseCurrencyDouble(raw: String): Double? {
        val cleaned = raw.replace("$", "")
            .replace(",", "")
            .replace("USD", "", ignoreCase = true)
            .trim()
        if (cleaned.isBlank()) return null
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            return cleaned.substring(1, cleaned.length - 1).toDoubleOrNull()?.let { -it }
        }
        return cleaned.toDoubleOrNull()
    }
}
