package com.sitereports.app.ui

import com.sitereports.app.data.ReportDao
import com.sitereports.app.data.ReportEntity
import com.sitereports.app.data.ReportRepository
import com.sitereports.app.data.UnitDao
import com.sitereports.app.data.UnitEntity
import com.sitereports.app.data.UnitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun generationUpdatesUnitAndIgnoresDuplicateTap() = runTest(dispatcher) {
        val unitDao = FakeUnitDao(UnitEntity(1, "OLD", "Old Project", "Old Location"))
        val reportDao = FakeReportDao()
        val viewModel = ReportFormViewModel(1, UnitRepository(unitDao), ReportRepository(reportDao))
        advanceUntilIdle()

        viewModel.update {
            it.copy(blockLot = " B01L01 ", project = " New Project ", location = " New Location ")
        }
        viewModel.generateAndSave()
        viewModel.generateAndSave()

        assertTrue(viewModel.isSubmitting.value)
        advanceUntilIdle()

        assertFalse(viewModel.isSubmitting.value)
        assertEquals(1, reportDao.insertCount)
        assertEquals("B01L01", unitDao.current.blockLot)
        assertEquals("New Project", reportDao.current?.project)
    }

    @Test
    fun blankUnitInformationBlocksGeneration() = runTest(dispatcher) {
        val unitDao = FakeUnitDao(UnitEntity(1, "B01", "Project", "Location"))
        val reportDao = FakeReportDao()
        val viewModel = ReportFormViewModel(1, UnitRepository(unitDao), ReportRepository(reportDao))
        advanceUntilIdle()

        viewModel.update { it.copy(project = "") }
        viewModel.generateAndSave()
        advanceUntilIdle()

        assertTrue(UnitField.Project in viewModel.validationErrors.value)
        assertEquals(0, reportDao.insertCount)
    }

    @Test
    fun editingReportUpdatesItsSnapshotWithoutChangingUnit() = runTest(dispatcher) {
        val unitDao = FakeUnitDao(UnitEntity(1, "UNIT", "Unit Project", "Unit Location"))
        val reportDao = FakeReportDao(
            ReportEntity(
                id = 7,
                unitId = 1,
                reportDateEpochDay = LocalDate.of(2026, 7, 19).toEpochDay(),
                blockLot = "B01",
                project = "Saved Project",
                location = "Saved Location",
                weather = "Sunny",
                skilledWorkers = 1,
                unskilledWorkers = 0,
                painters = 0,
                electricians = 0,
                plumbers = 0,
                foreman = 0,
                activities = "Initial work",
                remarks = "Initial remarks",
                generatedText = "old",
                createdAt = 1234,
            ),
        )
        val viewModel = ReportFormViewModel(1, UnitRepository(unitDao), ReportRepository(reportDao), reportId = 7)
        advanceUntilIdle()

        viewModel.update { it.copy(project = "Revised Project", weather = "Rainy") }
        viewModel.generateAndSave()
        advanceUntilIdle()

        assertEquals(0, reportDao.insertCount)
        assertEquals(1, reportDao.updateCount)
        assertEquals("Revised Project", reportDao.current?.project)
        assertEquals("Rainy", reportDao.current?.weather)
        assertEquals(1234L, reportDao.current?.createdAt)
        assertEquals("Unit Project", unitDao.current.project)
    }
}

private class FakeUnitDao(initial: UnitEntity) : UnitDao {
    private val state = MutableStateFlow(listOf(initial))
    val current: UnitEntity get() = state.value.single()

    override fun observeAll(): Flow<List<UnitEntity>> = state
    override suspend fun get(id: Long): UnitEntity? = state.value.firstOrNull { it.id == id }
    override suspend fun upsert(unit: UnitEntity): Long {
        state.value = state.value.filterNot { it.id == unit.id } + unit
        return unit.id
    }
    override suspend fun delete(ids: Set<Long>) {
        state.value = state.value.filterNot { it.id in ids }
    }
}

private class FakeReportDao(initial: ReportEntity? = null) : ReportDao {
    private val state = MutableStateFlow(initial?.let(::listOf) ?: emptyList())
    var insertCount = 0
        private set
    var updateCount = 0
        private set
    val current: ReportEntity? get() = state.value.singleOrNull()

    override fun observeAll(): Flow<List<ReportEntity>> = state
    override suspend fun get(id: Long): ReportEntity? = state.value.firstOrNull { it.id == id }
    override suspend fun insert(report: ReportEntity): Long {
        insertCount += 1
        val saved = report.copy(id = insertCount.toLong())
        state.value = state.value + saved
        return saved.id
    }
    override suspend fun update(report: ReportEntity): Int {
        val existing = state.value.any { it.id == report.id }
        if (existing) {
            updateCount += 1
            state.value = state.value.map { if (it.id == report.id) report else it }
            return 1
        }
        return 0
    }
    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }
}
