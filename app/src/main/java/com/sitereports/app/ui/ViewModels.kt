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

class ReportFormViewModel(
    unitId: Long,
    private val unitRepository: UnitRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {
    private val _draft = MutableStateFlow<ReportDraft?>(null)
    val draft: StateFlow<ReportDraft?> = _draft

    private val _events = MutableSharedFlow<ReportFormEvent>()
    val events = _events.asSharedFlow()

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
    }

    fun generateAndSave() {
        val current = _draft.value ?: return
        val text = ReportFormatter.format(current)
        viewModelScope.launch {
            val reportId = reportRepository.save(current, text)
            _events.emit(ReportFormEvent.Saved(reportId, text))
        }
    }
}

class ReportsViewModel(private val repository: ReportRepository) : ViewModel() {
    val reports: StateFlow<List<DailyReport>> = repository.reports.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )
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
