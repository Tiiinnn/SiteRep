package com.sitereports.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sitereports.app.data.ReportRepository
import com.sitereports.app.data.UnitRepository
import com.sitereports.app.domain.DailyReport
import com.sitereports.app.domain.ReportDraft
import com.sitereports.app.domain.ReportFormatter
import com.sitereports.app.domain.Unit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnitsViewModel(private val repository: UnitRepository) : ViewModel() {
    val units: StateFlow<List<Unit>> = repository.units.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun delete(ids: Set<Long>) {
        if (ids.isEmpty()) return
        viewModelScope.launch { repository.delete(ids) }
    }
}

sealed interface ReportFormEvent {
    data class Saved(val reportId: Long, val text: String) : ReportFormEvent
}

enum class UnitField { BlockLot, Project, Location }

class ReportFormViewModel(
    private val unitId: Long?,
    private val unitRepository: UnitRepository,
    private val reportRepository: ReportRepository,
    private val reportId: Long? = null,
) : ViewModel() {
    private val _draft = MutableStateFlow<ReportDraft?>(null)
    val draft: StateFlow<ReportDraft?> = _draft

    private val _events = MutableSharedFlow<ReportFormEvent>()
    val events = _events.asSharedFlow()

    private val _validationErrors = MutableStateFlow<Set<UnitField>>(emptySet())
    val validationErrors: StateFlow<Set<UnitField>> = _validationErrors

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private var reportCreatedAt: Long? = null

    init {
        viewModelScope.launch {
            if (reportId != null) {
                reportRepository.get(reportId)?.let { report ->
                    reportCreatedAt = report.createdAt
                    _draft.value = ReportDraft(
                        unitId = report.unitId,
                        date = report.date,
                        blockLot = report.blockLot,
                        project = report.project,
                        location = report.location,
                        weather = report.weather,
                        skilledWorkers = report.skilledWorkers,
                        unskilledWorkers = report.unskilledWorkers,
                        painters = report.painters,
                        electricians = report.electricians,
                        plumbers = report.plumbers,
                        foreman = report.foreman,
                        activities = report.activities,
                        remarks = report.remarks,
                    )
                } ?: run {
                    _saveError.value = "This report is no longer available."
                }
            } else {
                unitId?.let { id -> unitRepository.get(id) }?.let { unit ->
                    _draft.value = ReportDraft(unit.id, blockLot = unit.blockLot, project = unit.project, location = unit.location)
                }
            }
        }
    }

    fun update(transform: (ReportDraft) -> ReportDraft) {
        _draft.value = _draft.value?.let(transform)
        _saveError.value = null
        _draft.value?.let { current ->
            _validationErrors.value = _validationErrors.value.filterTo(mutableSetOf()) { field ->
                when (field) {
                    UnitField.BlockLot -> current.blockLot.isBlank()
                    UnitField.Project -> current.project.isBlank()
                    UnitField.Location -> current.location.isBlank()
                }
            }
        }
    }

    fun generateAndSave() {
        if (_isSubmitting.value) return
        val current = _draft.value ?: return
        val errors = buildSet {
            if (current.blockLot.isBlank()) add(UnitField.BlockLot)
            if (current.project.isBlank()) add(UnitField.Project)
            if (current.location.isBlank()) add(UnitField.Location)
        }
        _validationErrors.value = errors
        if (errors.isNotEmpty()) return

        val normalized = current.copy(
            blockLot = current.blockLot.trim(),
            project = current.project.trim(),
            location = current.location.trim(),
        )
        _isSubmitting.value = true
        viewModelScope.launch {
            _saveError.value = null
            try {
                val text = ReportFormatter.format(normalized)
                val savedReportId = if (reportId == null) {
                    unitRepository.save(
                        Unit(
                            id = normalized.unitId,
                            blockLot = normalized.blockLot,
                            project = normalized.project,
                            location = normalized.location,
                        ),
                    )
                    reportRepository.save(normalized, text)
                } else {
                    val updated = reportRepository.update(
                        reportId = reportId,
                        draft = normalized,
                        generatedText = text,
                        createdAt = reportCreatedAt ?: throw IllegalStateException("Missing report timestamp"),
                    )
                    if (!updated) throw IllegalStateException("Report was deleted")
                    reportId
                }
                _draft.value = normalized
                _events.emit(ReportFormEvent.Saved(savedReportId, text))
            } catch (_: Exception) {
                _saveError.value = if (reportId == null) {
                    "Could not save the unit and report. Please try again."
                } else {
                    "Could not update the report. Please try again."
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}

class ReportsViewModel(private val repository: ReportRepository) : ViewModel() {
    val reports: StateFlow<List<DailyReport>> = repository.reports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun delete(reportId: Long) {
        viewModelScope.launch { repository.delete(reportId) }
    }
}

class UnitsViewModelFactory(private val repository: UnitRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = UnitsViewModel(repository) as T
}

class ReportsViewModelFactory(private val repository: ReportRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportsViewModel(repository) as T
}

class ReportFormViewModelFactory(
    private val unitId: Long?,
    private val unitRepository: UnitRepository,
    private val reportRepository: ReportRepository,
    private val reportId: Long? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ReportFormViewModel(unitId, unitRepository, reportRepository, reportId) as T
}
