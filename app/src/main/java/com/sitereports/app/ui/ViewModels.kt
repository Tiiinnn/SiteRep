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
    unitId: Long,
    private val unitRepository: UnitRepository,
    private val reportRepository: ReportRepository,
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

    init {
        viewModelScope.launch {
            unitRepository.get(unitId)?.let { unit ->
                _draft.value = ReportDraft(
                    unitId = unit.id,
                    blockLot = unit.blockLot,
                    project = unit.project,
                    location = unit.location,
                )
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
                unitRepository.save(
                    Unit(
                        id = normalized.unitId,
                        blockLot = normalized.blockLot,
                        project = normalized.project,
                        location = normalized.location,
                    ),
                )
                val text = ReportFormatter.format(normalized)
                val reportId = reportRepository.save(normalized, text)
                _draft.value = normalized
                _events.emit(ReportFormEvent.Saved(reportId, text))
            } catch (_: Exception) {
                _saveError.value = "Could not save the unit and report. Please try again."
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
    private val unitId: Long,
    private val unitRepository: UnitRepository,
    private val reportRepository: ReportRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ReportFormViewModel(unitId, unitRepository, reportRepository) as T
}
