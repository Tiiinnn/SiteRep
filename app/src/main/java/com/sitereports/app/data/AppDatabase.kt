package com.sitereports.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.sitereports.app.domain.DailyReport
import com.sitereports.app.domain.ReportDraft
import com.sitereports.app.domain.Unit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Entity(tableName = "units")
data class UnitEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockLot: String,
    val project: String,
    val location: String,
)

@Entity(tableName = "reports")
data class ReportEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val reportDateEpochDay: Long,
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
)

@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY blockLot COLLATE NOCASE")
    fun observeAll(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): UnitEntity?

    @Upsert
    suspend fun upsert(unit: UnitEntity): Long

    @Query("DELETE FROM units WHERE id IN (:ids)")
    suspend fun delete(ids: Set<Long>)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY reportDateEpochDay DESC, createdAt DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [UnitEntity::class, ReportEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun reportDao(): ReportDao

    companion object {
        fun create(context: Context): AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "daily-site-reports.db",
        ).build()
    }
}

class UnitRepository(private val dao: UnitDao) {
    val units: Flow<List<Unit>> = dao.observeAll().map { rows -> rows.map(UnitEntity::toDomain) }

    suspend fun get(id: Long): Unit? = dao.get(id)?.toDomain()

    suspend fun save(unit: Unit): Long = dao.upsert(unit.toEntity())

    suspend fun delete(ids: Set<Long>) = dao.delete(ids)
}

class ReportRepository(private val dao: ReportDao) {
    val reports: Flow<List<DailyReport>> = dao.observeAll().map { rows -> rows.map(ReportEntity::toDomain) }

    suspend fun get(id: Long): DailyReport? = dao.get(id)?.toDomain()

    suspend fun delete(id: Long) = dao.delete(id)

    suspend fun save(draft: ReportDraft, generatedText: String): Long = dao.insert(
        ReportEntity(
            unitId = draft.unitId,
            reportDateEpochDay = draft.date.toEpochDay(),
            blockLot = draft.blockLot,
            project = draft.project,
            location = draft.location,
            weather = draft.weather,
            skilledWorkers = draft.skilledWorkers,
            unskilledWorkers = draft.unskilledWorkers,
            painters = draft.painters,
            electricians = draft.electricians,
            plumbers = draft.plumbers,
            foreman = draft.foreman,
            activities = draft.activities,
            remarks = draft.remarks,
            generatedText = generatedText,
            createdAt = System.currentTimeMillis(),
        ),
    )
}

private fun UnitEntity.toDomain() = Unit(id, blockLot, project, location)
private fun Unit.toEntity() = UnitEntity(id, blockLot, project, location)

private fun ReportEntity.toDomain() = DailyReport(
    id = id,
    unitId = unitId,
    date = LocalDate.ofEpochDay(reportDateEpochDay),
    blockLot = blockLot,
    project = project,
    location = location,
    weather = weather,
    skilledWorkers = skilledWorkers,
    unskilledWorkers = unskilledWorkers,
    painters = painters,
    electricians = electricians,
    plumbers = plumbers,
    foreman = foreman,
    activities = activities,
    remarks = remarks,
    generatedText = generatedText,
    createdAt = createdAt,
)
