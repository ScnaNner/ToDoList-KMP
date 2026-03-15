package com.example.todolist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked



@Composable
fun App() {
    TaskAppTheme {
        val scope = rememberCoroutineScope()

        // --- Core States ---
        var tasks by remember { mutableStateOf(emptyList<Task>()) }
        var showOnlyPending by remember { mutableStateOf(false) }

        // --- UX States (Loaders & Snackbars) ---
        var isLoading by remember { mutableStateOf(true) }
        val snackbarHostState = remember { SnackbarHostState() }

        // --- Dialog States (Smart Dialog for Add & Edit) ---
        var showDialog by remember { mutableStateOf(false) }
        var textInput by remember { mutableStateOf("") }
        var editingTask by remember { mutableStateOf<Task?>(null) }

        // 🎯 THE MASTER REFRESH FUNCTION
        suspend fun refreshTasks() {
            isLoading = true
            try {
                val query = if (showOnlyPending) "pending" else null
                tasks = TaskApi.getAllTasks(query)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Network Error: Cannot connect to server")
            } finally {
                isLoading = false
            }
        }

        // Fetch when app opens or switch flips
        LaunchedEffect(showOnlyPending) {
            refreshTasks()
        }

        // The Scaffold handles the layout for floating buttons and snackbars
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editingTask = null // null means we are ADDING, not editing
                    textInput = ""
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ktor Task Manager", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // The Filter Switch Card
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween // Pushes text left, switch right
                    ) {
                        Text(
                            text = "Hide Completed Tasks",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Switch(
                            checked = showOnlyPending,
                            onCheckedChange = { showOnlyPending = it },
                            // 🎨 Customizing the Colors
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            // ✅ Adding an icon INSIDE the switch thumb!
                            thumbContent = if (showOnlyPending) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ⏳ The Loading Spinner or The List
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        // 1. The 'key' parameter is crucial for animations!
                        items(items = tasks, key = { task -> task.id }) { task ->

                            // 2. The New Component: AnimatedVisibility
                            AnimatedVisibility(
                                visible = true, // LazyColumn handles the actual removal, so we keep this true
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutHorizontally() + fadeOut()
                            ) {
                                TaskItem(
                                    task = task,
                                    onDeleteClick = {
                                        scope.launch {
                                            try {
                                                TaskApi.deleteTask(task.id)
                                                refreshTasks()
                                            } catch(e: Exception) {
                                                snackbarHostState.showSnackbar("Failed to delete")
                                            }
                                        }
                                    },
                                    onToggleClick = {
                                        scope.launch {
                                            try {
                                                TaskApi.toggleTask(task.id)
                                                refreshTasks()
                                            } catch(e: Exception) {
                                                snackbarHostState.showSnackbar("Failed to update")
                                            }
                                        }
                                    },
                                    onEditClick = {
                                        editingTask = task
                                        textInput = task.title
                                        showDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🧠 The "Smart" Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(if (editingTask == null) "Create Task" else "Edit Task") },
                text = {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Task description") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (textInput.isNotBlank()) {
                            scope.launch {
                                try {
                                    if (editingTask == null) {
                                        // Add Mode
                                        TaskApi.addTask(Task(0, textInput, false))
                                    } else {
                                        // Edit Mode
                                        val updatedTask = editingTask!!.copy(title = textInput)
                                        TaskApi.updateTask(updatedTask)
                                    }
                                    refreshTasks()
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed to save task")
                                } finally {
                                    showDialog = false
                                }
                            }
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDeleteClick: () -> Unit,
    onToggleClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleClick) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = "Toggle Task",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Task",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}