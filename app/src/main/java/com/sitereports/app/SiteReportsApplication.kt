package com.sitereports.app

import android.app.Application
import com.sitereports.app.data.AppDatabase
import com.sitereports.app.data.ReportRepository
import com.sitereports.app.data.UnitRepository

class SiteReportsApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val unitRepository: UnitRepository by lazy { UnitRepository(database.unitDao()) }
    val reportRepository: ReportRepository by lazy { ReportRepository(database.reportDao()) }
}
