package com.callcatcher.next.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val role: String, val content: String)

/**
 * Calls a user-configured HTTPS backend. Keep provider API keys on that backend,
 * never inside the Android app. Contract: POST {messages:[{role,content}]}
 * and return {reply:"..."}.
 */
object AiGatewayClient {
    suspend fun reply(endpoint: String, messages: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            require(endpoint.startsWith("https://", ignoreCase = true)) {
                "Use an HTTPS AI gateway URL."
            }
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12_000
                readTimeout = 25_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }
            try {
                val messageArray = JSONArray()
                messages.forEach { message ->
                    messageArray.put(JSONObject().put("role", message.role).put("content", message.content))
                }
                val body = JSONObject().put("messages", messageArray).toString()
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (status !in 200..299) error("AI gateway returned HTTP $status.")
                val reply = JSONObject(response).optString("reply").trim()
                if (reply.isBlank()) error("AI gateway response did not contain a non-empty 'reply'.")
                reply
            } finally {
                connection.disconnect()
            }
        }
}
