package com.sitereports.app.domain

import java.time.format.DateTimeFormatter
import java.util.Locale

object ReportFormatter {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

    fun format(draft: ReportDraft): String = buildString {
        appendLine("DAILY SITE REPORT")
        appendLine()
        appendLine("Date: ${draft.date.format(dateFormatter)}")
        appendLine("Block/Lot: ${draft.blockLot}")
        appendLine("Project: ${draft.project}")
        appendLine("Location: ${draft.location}")
        appendLine()
        appendLine("WEATHER CONDITIONS:")
        appendLine(content(draft.weather))
        appendLine()
        appendLine("MANPOWER SUMMARY:")
        appendLine("Total Manpower: ${draft.totalManpower}")
        appendLine("• Skilled Workers: ${draft.skilledWorkers}")
        appendLine("• Unskilled Workers: ${draft.unskilledWorkers}")
        appendLine("• Painters: ${draft.painters}")
        appendLine("• Electricians: ${draft.electricians}")
        appendLine("• Plumbers: ${draft.plumbers}")
        appendLine("• Foreman: ${draft.foreman}")
        appendLine()
        appendLine("ACTIVITIES:")
        appendLine(content(draft.activities))
        appendLine()
        appendLine("REMARKS:")
        append(content(draft.remarks))
    }

    private fun content(value: String): String = value
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .trim('\n')
}
