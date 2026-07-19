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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
    onOpenUnit: (Long) -> Unit,
) {
    val viewModel: UnitsViewModel = viewModel(factory = UnitsViewModelFactory(repository))
    val units by viewModel.units.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<SiteUnit?>(null) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "My Units",
                action = { AboutSiteRepAction() },
            )
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
                            onOpen = { onOpenUnit(unit.id) },
                            onDelete = { pendingDelete = unit },
                        )
                    }
                }
            }
        }
        if (units.isNotEmpty()) {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add unit")
            }
        }
    }

    pendingDelete?.let { unit ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${unit.blockLot}?") },
            text = { Text("This removes the unit from My Units. Saved report history will remain available.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(setOf(unit.id))
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun UnitCard(
    unit: SiteUnit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(unit.blockLot.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Text(unit.project, style = MaterialTheme.typography.bodyLarge)
                Text(unit.location, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${unit.blockLot}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnitScreen(
    repository: UnitRepository,
    onCancel: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var blockLot by rememberSaveable { mutableStateOf("") }
    var project by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Cancel") }
                    Text(
                        "ADD NEW UNIT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        },
    ) { padding ->
        Column(
                modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = blockLot,
                    onValueChange = { blockLot = it; saveError = null },
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
                    onValueChange = { project = it; saveError = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Project") },
                    isError = attemptedSave && project.isBlank(),
                    supportingText = if (attemptedSave && project.isBlank()) ({ Text("Required") }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it; saveError = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location") },
                    isError = attemptedSave && location.isBlank(),
                    supportingText = if (attemptedSave && location.isBlank()) ({ Text("Required") }) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = true,
                )
                saveError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            attemptedSave = true
                            if (blockLot.isNotBlank() && project.isNotBlank() && location.isNotBlank()) {
                                isSaving = true
                                saveError = null
                                scope.launch {
                                    try {
                                        val newUnitId = repository.save(
                                            SiteUnit(
                                                blockLot = blockLot.trim(),
                                                project = project.trim(),
                                                location = location.trim(),
                                            ),
                                        )
                                        onCreated(newUnitId)
                                    } catch (_: Exception) {
                                        saveError = "Could not add the unit. Please try again."
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (isSaving) "Adding..." else "Add") }
                }
            }
    }
}
