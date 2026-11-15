package com.callcatcher.next.screening

import android.content.Context

object BlockRules {
    private const val PREFS = "callcatcher"
    private const val KEY_BLOCKED = "blocked_numbers"

    fun getBlocked(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_BLOCKED, emptySet())?.toSet() ?: emptySet()

    fun add(context: Context, number: String) {
        val normalized = normalizeNumber(number)
        require(normalized.count(Char::isDigit) >= 5) { "Enter at least five digits." }
        val current = getBlocked(context).toMutableSet()
        current.add(normalized)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_BLOCKED, current).apply()
    }

    fun remove(context: Context, number: String) {
        val current = getBlocked(context).toMutableSet()
        current.remove(normalizeNumber(number))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putStringSet(KEY_BLOCKED, current).apply()
    }

    fun isBlocked(incoming: String?, blocked: Set<String>): Boolean {
        if (incoming.isNullOrBlank()) return false
        val candidate = normalizeNumber(incoming)
        return blocked.any { normalizeNumber(it) == candidate }
    }

    fun normalizeNumber(value: String): String {
        val cleaned = value.filter { it.isDigit() || it == '+' }
        val digits = cleaned.filter(Char::isDigit)
        return when {
            cleaned.startsWith("+") -> "+$digits"
            cleaned.startsWith("00") -> "+${digits.drop(2)}"
            else -> digits
        }
    }
}
