package com.sitereports.app.domain

import java.time.LocalDate

data class Unit(
    val id: Long = 0,
    val blockLot: String,
    val project: String,
    val location: String,
)

data class DailyReport(
    val id: Long = 0,
    val unitId: Long,
    val date: LocalDate,
    val blockLot: String,
    val project: String,
    val location: String,
    val weather: String,
    val skilledWorkers: Int,
    val unskilledWorkers: Int,
    val painters: Int,
    val electricians: Int,
    val plumbers: Int,
    val foreman: Int,
    val activities: String,
    val remarks: String,
    val generatedText: String,
    val createdAt: Long,
) {
    val totalManpower: Int
        get() = skilledWorkers + unskilledWorkers + painters + electricians + plumbers + foreman
}

data class ReportDraft(
    val unitId: Long,
    val date: LocalDate = LocalDate.now(),
    val blockLot: String,
    val project: String,
    val location: String,
    val weather: String = "",
    val skilledWorkers: Int = 0,
    val unskilledWorkers: Int = 0,
    val painters: Int = 0,
    val electricians: Int = 0,
    val plumbers: Int = 0,
    val foreman: Int = 0,
    val activities: String = "",
    val remarks: String = "",
) {
    val totalManpower: Int
        get() = skilledWorkers + unskilledWorkers + painters + electricians + plumbers + foreman
}
