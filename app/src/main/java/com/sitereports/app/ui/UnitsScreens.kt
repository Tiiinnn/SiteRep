package com.sitereports.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sitereports.app.data.UnitRepository
import com.sitereports.app.domain.Unit as SiteUnit
import kotlinx.coroutines.launch

@Composable
fun UnitsScreen(
    repository: UnitRepository,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onCreateReport: (Long) -> Unit,
) {
    val viewModel: UnitsViewModel = viewModel(factory = UnitsViewModelFactory(repository))
    val units by viewModel.units.collectAsStateWithLifecycle()
    var managing by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    var confirmDelete by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "My Units",
                action = if (units.isNotEmpty()) {
                    {
                        OutlinedButton(onClick = {
                            managing = !managing
                            selected = emptySet()
                        }) { Text(if (managing) "Cancel" else "Manage Units") }
                    }
                } else null,
            )
            if (managing) {
                Button(
                    onClick = { confirmDelete = true },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text("Delete Selected (${selected.size})")
                }
            }
            if (units.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(Modifier.size(16.dp))
                    Text("No units yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(
                        "Add your first property to create daily site reports.",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onAdd) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Add Unit")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(units, key = SiteUnit::id) { unit ->
                        UnitCard(
                            unit = unit,
                            managing = managing,
                            selected = unit.id in selected,
                            onSelected = { checked ->
                                selected = if (checked) selected + unit.id else selected - unit.id
                            },
                            onEdit = { onEdit(unit.id) },
                            onCreateReport = { onCreateReport(unit.id) },
                        )
                    }
                }
            }
        }
        if (!managing && units.isNotEmpty()) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add unit")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete selected units?") },
            text = { Text("Saved report history will remain available.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(selected)
                    selected = emptySet()
                    managing = false
                    confirmDelete = false
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun UnitCard(
    unit: SiteUnit,
    managing: Boolean,
    selected: Boolean,
    onSelected: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onCreateReport: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (managing) {
                Checkbox(checked = selected, onCheckedChange = onSelected)
                Spacer(Modifier.size(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(unit.blockLot.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(unit.project, style = MaterialTheme.typography.bodyLarge)
                Text(unit.location, color = MaterialTheme.colorScheme.secondary)
            }
            if (!managing) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit ${unit.blockLot}")
                }
                IconButton(onClick = onCreateReport) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = "Create report for ${unit.blockLot}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUnitScreen(
    unitId: Long,
    repository: UnitRepository,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var blockLot by rememberSaveable { mutableStateOf("") }
    var project by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(unitId == 0L) }

    LaunchedEffect(unitId) {
        if (unitId != 0L) {
            repository.get(unitId)?.let {
                blockLot = it.blockLot
                project = it.project
                location = it.location
            }
            loaded = true
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Cancel") }
                    Text(
                        if (unitId == 0L) "ADD NEW UNIT" else "EDIT UNIT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        },
    ) { padding ->
        if (loaded) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = blockLot,
                    onValueChange = { blockLot = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Block/Lot") },
                    placeholder = { Text("e.g., B25L08") },
                    isError = attemptedSave && blockLot.isBlank(),
                    supportingText = if (attemptedSave && blockLot.isBlank()) ({ Text("Required") }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = project,
                    onValueChange = { project = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Project") },
                    isError = attemptedSave && project.isBlank(),
                    supportingText = if (attemptedSave && project.isBlank()) ({ Text("Required") }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location") },
                    isError = attemptedSave && location.isBlank(),
                    supportingText = if (attemptedSave && location.isBlank()) ({ Text("Required") }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                )
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            attemptedSave = true
                            if (blockLot.isNotBlank() && project.isNotBlank() && location.isNotBlank()) {
                                scope.launch {
                                    repository.save(
                                        SiteUnit(
                                            id = unitId,
                                            blockLot = blockLot.trim(),
                                            project = project.trim(),
                                            location = location.trim(),
                                        ),
                                    )
                                    onClose()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(if (unitId == 0L) "Add" else "Update") }
                }
            }
        }
    }
}
