package com.example.hw1

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            intent?.let { handleNotificationIntent(it, isAppRunning = false) }
        }

        setContent {
            MaterialTheme {
                Surface {
                    MainScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent, isAppRunning = true)
    }

    private fun handleNotificationIntent(intent: Intent, isAppRunning: Boolean) {
        val title = intent.getStringExtra("EXTRA_TITLE")
        val text = intent.getStringExtra("EXTRA_TEXT")
        if (title != null && text != null) {
            val message = if (isAppRunning) text else title
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        var hasPermission by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else true
            )
        }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasPermission = isGranted
        }

        LaunchedEffect(Unit) {
            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else true
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        var showSecondScreen by remember { mutableStateOf(false) }

        if (!hasPermission) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Для нормальной работы приложения нужно предоставить разрешение на показ уведомлений",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    })
                }) {
                    Text("Открыть настройки")
                }
            }
        } else {
            if (showSecondScreen) {
                SecondScreen(onBack = { showSecondScreen = false })
            } else {
                NotificationForm(onOpenSecondScreen = { showSecondScreen = true })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun NotificationForm(onOpenSecondScreen: () -> Unit) {
        val context = LocalContext.current
        val manager = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

        var title by remember { mutableStateOf("") }
        var text by remember { mutableStateOf("") }
        var isOngoing by remember { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }

        val importanceOptions = listOf(
            "Низкий" to NotificationManager.IMPORTANCE_MIN,
            "Средний" to NotificationManager.IMPORTANCE_DEFAULT,
            "Высокий" to NotificationManager.IMPORTANCE_HIGH
        )

        var selectedImportance by remember { mutableStateOf(importanceOptions[1]) }

        var activeIds by remember { mutableStateOf(getActiveIds(context, manager)) }
        var notifIdInput by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                activeIds = getActiveIds(context, manager)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок уведомления") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Текст уведомления") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isOngoing, onCheckedChange = { isOngoing = it })
                Text("Нельзя закрыть")
            }

            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedImportance.first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Важность") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    importanceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.first) },
                            onClick = { selectedImportance = option; expanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                if (title.isNotBlank() && text.isNotBlank()) {
                    var newId = 1
                    while (activeIds.contains(newId)) newId++
                    NotificationHelper.showNotification(context, newId, title, text, isOngoing, selectedImportance.second)
                    activeIds = getActiveIds(context, manager)
                    Toast.makeText(context, "Уведомление отправлено", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Отправить уведомление")
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenSecondScreen, modifier = Modifier.fillMaxWidth()) {
                Text("Перейти на второй экран")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Активные ID: ${if (activeIds.isEmpty()) "нет" else activeIds.joinToString(", ")}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notifIdInput,
                onValueChange = { if (it.all { char -> char.isDigit() }) notifIdInput = it },
                label = { Text("ID уведомления для изменения") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = activeIds.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val id = notifIdInput.toIntOrNull()
                        if (id != null && activeIds.contains(id) && title.isNotBlank() && text.isNotBlank()) {
                            NotificationHelper.showNotification(context, id, title, text, isOngoing, selectedImportance.second)
                            Toast.makeText(context, "Уведомление изменено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Введите корректный ID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = activeIds.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) { Text("Редактировать") }

                Button(
                    onClick = {
                        val id = notifIdInput.toIntOrNull()
                        if (id != null && activeIds.contains(id)) {
                            manager.cancel(id)
                            activeIds = getActiveIds(context, manager)
                            Toast.makeText(context, "Уведомление удалено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Введите корректный ID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = activeIds.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) { Text("Удалить") }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = {
                manager.cancelAll()
                activeIds = getActiveIds(context, manager)
                Toast.makeText(context, "Все уведомления удалены", Toast.LENGTH_SHORT).show()
            }, enabled = activeIds.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text("Удалить все")
            }
        }
    }

    private fun getActiveIds(context: Context, manager: NotificationManager): Set<Int> {
        return manager.activeNotifications
            .filter { it.packageName == context.packageName && it.id in 1..10000 }
            .map { it.id }
            .toSet()
    }
}