package com.sitereports.app.ui

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sitereports.app.data.ReportRepository
import com.sitereports.app.data.UnitRepository
import com.sitereports.app.domain.DailyReport
import com.sitereports.app.domain.ReportDraft
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)

@Composable
fun ReportFormScreen(
    unitId: Long,
    unitRepository: UnitRepository,
    reportRepository: ReportRepository,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: ReportFormViewModel = viewModel(
        key = "new-report-$unitId",
        factory = ReportFormViewModelFactory(unitId, unitRepository, reportRepository),
    )
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val validationErrors by viewModel.validationErrors.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val saveError by viewModel.saveError.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ReportFormEvent.Saved -> {
                    context.copyReport(event.text)
                    Toast.makeText(context, "Report copied to clipboard.", Toast.LENGTH_SHORT).show()
                    onSaved(event.reportId)
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ReportTopBar(
                title = draft?.blockLot ?: "Daily Report",
                subtitle = draft?.project,
                onBack = onBack,
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
                saveError?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                Button(
                    onClick = viewModel::generateAndSave,
                    enabled = draft != null && !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(if (isSubmitting) "SAVING..." else "GENERATE & COPY REPORT", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        val current = draft
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ReportForm(
                draft = current,
                validationErrors = validationErrors,
                onUpdate = viewModel::update,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ReportForm(
    draft: ReportDraft,
    validationErrors: Set<UnitField>,
    onUpdate: ((ReportDraft) -> ReportDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ExpandableSection("Unit Information") {
            OutlinedTextField(
                value = draft.blockLot,
                onValueChange = { value -> onUpdate { it.copy(blockLot = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Block/Lot") },
                isError = UnitField.BlockLot in validationErrors,
                supportingText = if (UnitField.BlockLot in validationErrors) ({ Text("Required") }) else null,
                singleLine = true,
            )
            OutlinedTextField(
                value = draft.project,
                onValueChange = { value -> onUpdate { it.copy(project = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Project") },
                isError = UnitField.Project in validationErrors,
                supportingText = if (UnitField.Project in validationErrors) ({ Text("Required") }) else null,
                singleLine = true,
            )
            OutlinedTextField(
                value = draft.location,
                onValueChange = { value -> onUpdate { it.copy(location = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
                isError = UnitField.Location in validationErrors,
                supportingText = if (UnitField.Location in validationErrors) ({ Text("Required") }) else null,
                singleLine = true,
            )
            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> onUpdate { it.copy(date = LocalDate.of(year, month + 1, day)) } },
                        draft.date.year,
                        draft.date.monthValue - 1,
                        draft.date.dayOfMonth,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Date: ${draft.date.format(displayDateFormatter)}")
            }
        }

        ExpandableSection("Weather Conditions") {
            OutlinedTextField(
                value = draft.weather,
                onValueChange = { value -> onUpdate { it.copy(weather = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Weather") },
                placeholder = { Text("Describe weather conditions…") },
                minLines = 3,
            )
        }

        ExpandableSection("Manpower Summary") {
            Text("Total Manpower: ${draft.totalManpower}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ManpowerStepper("Skilled Workers", draft.skilledWorkers, { value -> onUpdate { it.copy(skilledWorkers = value) } }, Modifier.weight(1f))
                ManpowerStepper("Unskilled Workers", draft.unskilledWorkers, { value -> onUpdate { it.copy(unskilledWorkers = value) } }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ManpowerStepper("Painters", draft.painters, { value -> onUpdate { it.copy(painters = value) } }, Modifier.weight(1f))
                ManpowerStepper("Electricians", draft.electricians, { value -> onUpdate { it.copy(electricians = value) } }, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ManpowerStepper("Plumbers", draft.plumbers, { value -> onUpdate { it.copy(plumbers = value) } }, Modifier.weight(1f))
                ManpowerStepper("Foreman", draft.foreman, { value -> onUpdate { it.copy(foreman = value) } }, Modifier.weight(1f))
            }
        }

        ExpandableSection("Activities") {
            OutlinedTextField(
                value = draft.activities,
                onValueChange = { value -> onUpdate { it.copy(activities = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Activities") },
                placeholder = { Text("List construction activities, categories, bullets, and numbered items…") },
                minLines = 7,
            )
        }

        ExpandableSection("Remarks") {
            OutlinedTextField(
                value = draft.remarks,
                onValueChange = { value -> onUpdate { it.copy(remarks = value) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Remarks") },
                placeholder = { Text("Additional notes or observations…") },
                minLines = 6,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ManpowerStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, minLines = 2)
        OutlinedCard(border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = { onValueChange((value - 1).coerceAtLeast(0)) },
                    enabled = value > 0,
                    modifier = Modifier.semantics { contentDescription = "Decrease $label" },
                ) { Icon(Icons.Outlined.Remove, contentDescription = null) }
                Text(value.toString(), fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { onValueChange(value + 1) },
                    modifier = Modifier.semantics { contentDescription = "Increase $label" },
                ) { Icon(Icons.Outlined.Add, contentDescription = null) }
            }
        }
    }
}

@Composable
fun ReportsScreen(
    repository: ReportRepository,
    onOpenReport: (Long) -> Unit,
) {
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(repository))
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<DailyReport?>(null) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("My Reports", action = { AboutSiteRepAction() })
        if (reports.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No reports yet. Open a unit and create its first daily report.")
            }
        } else {
            val grouped = reports.groupBy(DailyReport::date).toSortedMap(compareByDescending { it })
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                grouped.forEach { (date, dateReports) ->
                    item(key = "date-$date") {
                        Text(
                            date.format(displayDateFormatter).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                        )
                    }
                    items(dateReports, key = DailyReport::id) { report ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenReport(report.id) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(report.blockLot.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                    Text(report.project)
                                    Text(report.location, color = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { pendingDelete = report }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete report for ${report.blockLot}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this report?") },
            text = { Text("Delete the ${report.date.format(displayDateFormatter)} report for ${report.blockLot}? This cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(report.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
fun ReportDetailsScreen(
    reportId: Long,
    repository: ReportRepository,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    var report by remember { mutableStateOf<DailyReport?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(reportId) {
        isLoading = true
        report = try {
            repository.get(reportId)
        } catch (_: Exception) {
            null
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ReportTopBar(
                title = "Report Details",
                onBack = onBack,
                action = {
                    IconButton(onClick = {
                        report?.let {
                            context.copyReport(it.generatedText)
                            Toast.makeText(context, "Report copied to clipboard.", Toast.LENGTH_SHORT).show()
                        }
                    }) { Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy report") }
                },
            )
        },
    ) { padding ->
        val current = report
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (current == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Report not found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "It may have been deleted or is no longer available.",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                )
                Button(onClick = onBack) { Text("Back to reports") }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                SelectionContainer {
                    Text(
                        text = current.generatedText,
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back") }
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                if (!subtitle.isNullOrBlank()) Text(subtitle, color = MaterialTheme.colorScheme.secondary)
            }
            action?.invoke()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

private fun Context.copyReport(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Daily Site Report", text))
}
