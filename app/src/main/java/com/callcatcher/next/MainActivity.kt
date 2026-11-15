package com.callcatcher.next

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcatcher.next.screening.BlockRules
import com.callcatcher.next.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CallCatcherApp(this) }
    }
}

@Composable
private fun CallCatcherApp(activity: Activity) {
    val context = activity
    val prefs = remember { context.getSharedPreferences("callcatcher", Context.MODE_PRIVATE) }
    var onboarded by rememberSaveable { mutableStateOf(prefs.getBoolean("onboarded", false)) }
    var page by rememberSaveable { mutableStateOf("Home") }
    var screeningEnabled by remember { mutableStateOf(isScreeningRoleHeld(context)) }
    var blockedNumbers by remember { mutableStateOf(BlockRules.getBlocked(context)) }
    var showAdd by remember { mutableStateOf(false) }
    var numberInput by remember { mutableStateOf("") }
    var aiEndpoint by remember { mutableStateOf(prefs.getString("ai_gateway_endpoint", "") ?: "") }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        screeningEnabled = isScreeningRoleHeld(context)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neon,
            onPrimary = Color(0xFF061109),
            background = Ink,
            surface = Panel,
            onSurface = TextMain,
            secondary = Mint
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            if (!onboarded) {
                WelcomeScreen(
                    onContinue = { onboarded = true; prefs.edit().putBoolean("onboarded", true).apply() },
                    onEnable = {
                        val rm = context.getSystemService(RoleManager::class.java)
                        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                            roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                        }
                    },
                    screeningEnabled = screeningEnabled
                )
            } else {
                Scaffold(
                    containerColor = Ink,
                    bottomBar = {
                        NavigationBar(containerColor = Color(0xFF0D1411), contentColor = TextMain) {
                            listOf("Home", "Blocklist", "AI", "Settings").forEach { item ->
                                NavigationBarItem(
                                    selected = page == item,
                                    onClick = { page = item },
                                    icon = {
                                        Icon(
                                            when (item) {
                                                "Home" -> Icons.Default.Home
                                                "Blocklist" -> Icons.Default.Shield
                                                "AI" -> Icons.Default.Chat
                                                else -> Icons.Default.Settings
                                            }, contentDescription = item
                                        )
                                    },
                                    label = { Text(item) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Neon,
                                        selectedTextColor = Neon,
                                        indicatorColor = Color(0xFF1B3024),
                                        unselectedIconColor = Muted,
                                        unselectedTextColor = Muted
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    when (page) {
                        "Home" -> HomeScreen(
                            Modifier.padding(padding),
                            screeningEnabled,
                            onEnable = {
                                val rm = context.getSystemService(RoleManager::class.java)
                                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                                    roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                                }
                            },
                            onBlocklist = { page = "Blocklist" }
                        )
                        "Blocklist" -> BlocklistScreen(
                            Modifier.padding(padding), blockedNumbers,
                            onAdd = { showAdd = true },
                            onRemove = { number ->
                                BlockRules.remove(context, number)
                                blockedNumbers = BlockRules.getBlocked(context)
                            }
                        )
                        "AI" -> AiAssistantScreen(
                            Modifier.padding(padding),
                            aiEndpoint,
                            onSaveEndpoint = { value ->
                                aiEndpoint = value
                                prefs.edit().putString("ai_gateway_endpoint", value).apply()
                            }
                        )
                        else -> SettingsScreen(Modifier.padding(padding), screeningEnabled)
                    }
                }
            }
            if (showAdd) {
                AlertDialog(
                    onDismissRequest = { showAdd = false; numberInput = "" },
                    title = { Text("Block a number", color = TextMain) },
                    text = {
                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { numberInput = it },
                            label = { Text("Phone number") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val cleaned = numberInput.filter { it.isDigit() || it == '+' }
                            if (cleaned.count(Char::isDigit) >= 5) {
                                runCatching { BlockRules.add(context, cleaned) }
                                blockedNumbers = BlockRules.getBlocked(context)
                            }
                            showAdd = false
                            numberInput = ""
                        }) { Text("Add to blocklist", color = Neon) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdd = false; numberInput = "" }) { Text("Cancel") }
                    },
                    containerColor = Panel
                )
            }
        }
    }
}

private fun isScreeningRoleHeld(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
    val manager = context.getSystemService(RoleManager::class.java) ?: return false
    return manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}
