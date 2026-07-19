package com.sitereports.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReportFormatterTest {
    private val draft = ReportDraft(
        unitId = 1,
        date = LocalDate.of(2026, 7, 19),
        blockLot = "B27L39",
        project = "Arcoe Residences",
        location = "Lipa City, Batangas",
        weather = "Sunny",
        skilledWorkers = 5,
        unskilledWorkers = 5,
        painters = 2,
        electricians = 2,
        plumbers = 2,
        foreman = 1,
        activities = "Structural Works\n\n1. Fabrication and Installation of Beam Rebars\n2. Fabrication and Installation of Column Rebars\n\nRoofing Works\n\n1. Installation of Roofing Panels",
        remarks = "1. 50 bags of cement were delivered at 9:30 AM.\n2. 10 pieces of 16 mm rebars were delivered at 1:50 PM.",
    )

    @Test
    fun formatsRequiredHeaderDateAndManpower() {
        val result = ReportFormatter.format(draft)

        assertTrue(result.startsWith("DAILY SITE REPORT\n\nDate: July 19, 2026"))
        assertTrue(result.contains("MANPOWER SUMMARY:\nTotal Manpower: 17"))
        assertTrue(result.contains("• Skilled Workers: 5"))
        assertTrue(result.endsWith(draft.remarks))
    }

    @Test
    fun preservesInternalParagraphSpacingAndLists() {
        val result = ReportFormatter.format(draft)

        assertTrue(result.contains(draft.activities))
        assertTrue(result.contains("Structural Works\n\n1. Fabrication"))
    }

    @Test
    fun calculatesTotalFromAllCategories() {
        assertEquals(17, draft.totalManpower)
    }
}
