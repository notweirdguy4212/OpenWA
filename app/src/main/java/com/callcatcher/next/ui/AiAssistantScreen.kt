package com.callcatcher.next.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcatcher.next.ai.AiGatewayClient
import com.callcatcher.next.ai.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    modifier: Modifier = Modifier,
    endpointInitial: String,
    onSaveEndpoint: (String) -> Unit
) {
    var endpoint by remember(endpointInitial) { mutableStateOf(endpointInitial) }
    var draft by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(ChatMessage("assistant", "Hi. I can help draft replies, assess suspicious messages you paste here, and explain call-screening choices. I cannot hear or answer live calls."))) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("AI ASSISTANT", color = Neon, letterSpacing = 2.sp, fontSize = 12.sp)
        Text("Conversation engine", color = TextMain, fontSize = 28.sp)
        Text("Connect your own HTTPS AI gateway. Provider credentials belong on your server, not in this app.", color = Muted, fontSize = 13.sp)
        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("AI gateway HTTPS endpoint") },
            singleLine = true
        )
        Button(
            onClick = { onSaveEndpoint(endpoint.trim()); error = null },
            enabled = endpoint.startsWith("https://", true),
            colors = ButtonDefaults.buttonColors(containerColor = Neon, contentColor = Ink)
        ) { Text("Save gateway") }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.role == "user") Panel2 else Panel
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        message.content,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        color = TextMain
                    )
                }
            }
        }
        error?.let { Text(it, color = Danger, fontSize = 12.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                label = { Text("Message") },
                enabled = !busy
            )
            IconButton(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty() && !busy) {
                        val updated = messages + ChatMessage("user", text)
                        messages = updated
                        draft = ""
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                if (endpoint.isBlank()) error = "Save an HTTPS AI gateway endpoint first."
                                else messages = updated + ChatMessage("assistant", AiGatewayClient.reply(endpoint.trim(), updated))
                            } catch (t: Throwable) {
                                error = t.message ?: "Could not reach the AI gateway."
                            } finally {
                                busy = false
                            }
                        }
                    }
                },
                enabled = !busy
            ) { Icon(Icons.Default.Send, contentDescription = "Send", tint = Neon) }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Neon)
    }
}
