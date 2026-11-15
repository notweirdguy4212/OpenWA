package com.callcatcher.next.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onContinue: () -> Unit, onEnable: () -> Unit, screeningEnabled: Boolean) {
    var step by remember { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxSize().background(Ink).padding(horizontal = 26.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(42.dp).background(Neon, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Call, null, tint = Ink)
                }
                Text("CALLCATCHER", color = TextMain, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(26.dp))
            Text(if (step == 0) "Calls, on
your terms." else "Protection
with permission.", color = TextMain, fontSize = 38.sp, lineHeight = 42.sp, fontWeight = FontWeight.Bold)
            Text(
                if (step == 0) "A calmer way to handle unknown callers. Keep control of every decision."
                else "CallCatcher can screen ordinary mobile calls when you choose it as your call-screening app. You can change this any time in Android settings.",
                color = Muted, fontSize = 16.sp, lineHeight = 24.sp
            )
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Neon)
                        Spacer(Modifier.width(10.dp))
                        Text("Local-first call protection", color = TextMain, fontWeight = FontWeight.SemiBold)
                    }
                    Text("• Block numbers you add yourself\n• Flag failed caller verification where Android provides it\n• No account required\n• No background recording", color = Muted, lineHeight = 23.sp)
                }
            }
            if (step == 1) {
                OutlinedButton(onClick = onEnable, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Default.Security, null); Spacer(Modifier.width(8.dp))
                    Text(if (screeningEnabled) "Call screening enabled" else "Choose CallCatcher for screening")
                }
                Text("Android will show its own system confirmation. CallCatcher cannot silently grant this role.", color = Muted, fontSize = 12.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) { Box(Modifier.size(if (step == it) 22.dp else 7.dp, 7.dp).background(if (step == it) Neon else Color(0xFF39443D), CircleShape)) }
            }
            Button(
                onClick = { if (step == 0) step = 1 else onContinue() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Ink),
                shape = RoundedCornerShape(16.dp)
            ) { Text(if (step == 0) "Get started" else "Open CallCatcher", fontWeight = FontWeight.Bold) }
            TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Skip setup for now", color = Muted) }
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier, enabled: Boolean, onEnable: () -> Unit, onBlocklist: () -> Unit) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("CALLCATCHER", color = Neon, letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Your calls.
Your control.", color = TextMain, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(22.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).background(Color(0xFF1C3525), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, null, tint = Neon)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Call screening", color = TextMain, fontWeight = FontWeight.SemiBold)
                        Text(if (enabled) "Enabled by Android" else "Not enabled yet", color = Muted, fontSize = 12.sp)
                    }
                    Box(Modifier.size(9.dp).background(if (enabled) Neon else Muted, CircleShape))
                }
                Text("Screen unknown callers and apply your blocklist to supported mobile calls.", color = Muted, lineHeight = 21.sp)
                Button(onClick = onEnable, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Ink), shape = RoundedCornerShape(14.dp)) {
                    Text(if (enabled) "Manage screening role" else "Enable call screening")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("QUICK ACCESS", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        ActionTile(Icons.Default.Block, "Manage blocked numbers", "Review or add numbers to your personal blocklist", onBlocklist)
        Spacer(Modifier.height(16.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF18221B)), shape = RoundedCornerShape(18.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, null, tint = Neon)
                Spacer(Modifier.width(10.dp))
                Text("CallCatcher does not answer calls, speak to callers, or access WhatsApp call audio. Any AI draft must be reviewed and sent by you.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun ActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Neon, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextMain, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Muted)
        }
    }
}

@Composable
fun BlocklistScreen(modifier: Modifier, numbers: Set<String>, onAdd: () -> Unit, onRemove: (String) -> Unit) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("BLOCKLIST", color = Neon, letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Blocked numbers", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Only numbers you add here are automatically blocked.", color = Muted)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Ink), shape = RoundedCornerShape(14.dp)) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add number")
        }
        Spacer(Modifier.height(16.dp))
        if (numbers.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Shield, null, tint = Neon, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Your blocklist is clear", color = TextMain, fontWeight = FontWeight.SemiBold)
                    Text("Numbers you add will appear here.", color = Muted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(numbers.toList()) { number ->
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, null, tint = Danger)
                            Spacer(Modifier.width(12.dp))
                            Text(number, color = TextMain, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onRemove(number) }) { Text("Remove", color = Muted) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(modifier: Modifier, enabled: Boolean) {
    Column(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("PREFERENCES", color = Neon, letterSpacing = 2.sp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Settings", color = TextMain, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        SettingRow("Screening role", if (enabled) "Granted by Android" else "Not enabled")
        SettingRow("Storage", "No call recordings are created")
        SettingRow("AI connection", "Optional HTTPS gateway; chat requires your backend")
        SettingRow("Privacy", "Blocklist stored locally on this device")
        SettingRow("Version", "2.1.0 • Source build")
        Text("CallCatcher is an independent project. It is not affiliated with WhatsApp or your mobile carrier.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = TextMain, fontWeight = FontWeight.SemiBold)
            Text(value, color = Muted, fontSize = 13.sp)
        }
    }
}
