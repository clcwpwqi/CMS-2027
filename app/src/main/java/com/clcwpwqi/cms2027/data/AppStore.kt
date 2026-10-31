package com.clcwpwqi.cms2027.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore("cms2027")

class AppStore(private val context: Context) {
    private val themeMode = stringPreferencesKey("themeMode")
    private val colorMode = stringPreferencesKey("colorMode")
    private val seed = longPreferencesKey("seed")
    private val language = stringPreferencesKey("language")
    private val history = stringPreferencesKey("history")

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[themeMode] ?: "system",
            colorMode = p[colorMode] ?: "system",
            seed = p[seed] ?: 0xFF6750A4,
            language = p[language] ?: "auto",
        )
    }

    val historyFlow: Flow<List<HistoryItem>> = context.dataStore.data.map { p ->
        parseHistory(p[history] ?: "[]")
    }

    suspend fun saveSettings(s: AppSettings) {
        context.dataStore.edit {
            it[themeMode] = s.themeMode
            it[colorMode] = s.colorMode
            it[seed] = s.seed
            it[language] = s.language
        }
    }

    suspend fun addHistory(item: HistoryItem) {
        context.dataStore.edit { prefs ->
            val list = parseHistory(prefs[history] ?: "[]").toMutableList()
            list.add(0, item)
            while (list.size > 500) list.removeAt(list.lastIndex)
            prefs[history] = serialize(list)
        }
    }

    suspend fun deleteHistory(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            val list = parseHistory(prefs[history] ?: "[]").filter { it.id !in ids }
            prefs[history] = serialize(list)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[history] = "[]" }
    }

    private fun parseHistory(raw: String): List<HistoryItem> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        HistoryItem(
                            id = o.getString("id"),
                            type = o.getString("type"),
                            mode = o.getString("mode"),
                            key = if (o.isNull("key")) null else o.optString("key"),
                            plain = o.optString("plain"),
                            cipher = o.optString("cipher"),
                            ts = o.optLong("ts"),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serialize(list: List<HistoryItem>): String {
        val arr = JSONArray()
        list.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("type", item.type)
                    put("mode", item.mode)
                    put("key", item.key ?: JSONObject.NULL)
                    put("plain", item.plain)
                    put("cipher", item.cipher)
                    put("ts", item.ts)
                },
            )
        }
        return arr.toString()
    }
}
