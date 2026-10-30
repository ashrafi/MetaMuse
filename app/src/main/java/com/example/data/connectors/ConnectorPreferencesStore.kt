package com.example.data.connectors

import android.content.Context
import android.content.SharedPreferences

class ConnectorPreferencesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("metamuse_real_connectors_prefs", Context.MODE_PRIVATE)

    var metaAiToken: String
        get() = prefs.getString("meta_ai_token", "") ?: ""
        set(value) = prefs.edit().putString("meta_ai_token", value).apply()

    var metaAiEndpoint: String
        get() = prefs.getString("meta_ai_endpoint", "https://api.groq.com/openai/v1/chat/completions") ?: "https://api.groq.com/openai/v1/chat/completions"
        set(value) = prefs.edit().putString("meta_ai_endpoint", value).apply()

    var metaAiModel: String
        get() = prefs.getString("meta_ai_model", "llama-3.1-70b-versatile") ?: "llama-3.1-70b-versatile"
        set(value) = prefs.edit().putString("meta_ai_model", value).apply()

    var isLocalLlamaPrivacyMode: Boolean
        get() = prefs.getBoolean("is_local_llama_privacy_mode", true)
        set(value) = prefs.edit().putBoolean("is_local_llama_privacy_mode", value).apply()

    var googleTasksToken: String
        get() = prefs.getString("google_tasks_token", "") ?: ""
        set(value) = prefs.edit().putString("google_tasks_token", value).apply()

    var facebookAppId: String
        get() = prefs.getString("facebook_app_id", "") ?: ""
        set(value) = prefs.edit().putString("facebook_app_id", value).apply()

    var facebookAccessToken: String
        get() = prefs.getString("facebook_access_token", "") ?: ""
        set(value) = prefs.edit().putString("facebook_access_token", value).apply()

    var facebookUserName: String?
        get() = prefs.getString("facebook_user_name", null)
        set(value) = prefs.edit().putString("facebook_user_name", value).apply()

    var plaidClientId: String
        get() = prefs.getString("plaid_client_id", "") ?: ""
        set(value) = prefs.edit().putString("plaid_client_id", value).apply()

    var plaidSecret: String
        get() = prefs.getString("plaid_secret", "") ?: ""
        set(value) = prefs.edit().putString("plaid_secret", value).apply()

    var plaidEnvironment: String
        get() = prefs.getString("plaid_environment", "sandbox") ?: "sandbox"
        set(value) = prefs.edit().putString("plaid_environment", value).apply()

    var plaidAccessToken: String
        get() = prefs.getString("plaid_access_token", "") ?: ""
        set(value) = prefs.edit().putString("plaid_access_token", value).apply()

    var simpleFinUrlOrToken: String
        get() = prefs.getString("simplefin_url_or_token", "") ?: ""
        set(value) = prefs.edit().putString("simplefin_url_or_token", value).apply()

    var cachedCsvTransactionsJson: String
        get() = prefs.getString("cached_csv_transactions_json", "") ?: ""
        set(value) = prefs.edit().putString("cached_csv_transactions_json", value).apply()

    var cachedCsvLabel: String?
        get() = prefs.getString("cached_csv_label", null)
        set(value) = prefs.edit().putString("cached_csv_label", value).apply()
}
