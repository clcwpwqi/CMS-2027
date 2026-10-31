package com.clcwpwqi.cms2027.data

data class AppSettings(
    val themeMode: String = "system",
    val colorMode: String = "system",
    val seed: Long = 0xFF6750A4,
    val language: String = "auto",
)

data class HistoryItem(
    val id: String,
    val type: String,
    val mode: String,
    val key: String?,
    val plain: String,
    val cipher: String,
    val ts: Long,
)
