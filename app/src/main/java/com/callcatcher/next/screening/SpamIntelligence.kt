package com.callcatcher.next.screening

data class SpamAssessment(
    val score: Int,
    val label: String,
    val reasons: List<String>,
    val shouldBlock: Boolean
)

/**
 * Conservative, on-device signals only. This is a heuristic, not a carrier
 * reputation database or a guarantee that a caller is spam.
 */
object SpamIntelligence {
    fun assess(number: String?, blocked: Set<String>, verificationFailed: Boolean = false): SpamAssessment {
        if (number.isNullOrBlank()) {
            return SpamAssessment(10, "Insufficient information", listOf("Caller ID is unavailable"), false)
        }

        val normalized = BlockRules.normalizeNumber(number)
        val reasons = mutableListOf<String>()
        var score = 0

        if (BlockRules.isBlocked(number, blocked)) {
            score = 100
            reasons += "You added this number to your blocklist"
        }
        if (verificationFailed) {
            score = maxOf(score, 75)
            reasons += "Android reports caller-number verification failed"
        }
        val digits = normalized.filter(Char::isDigit)
        if (digits.length in 1..5) {
            score = maxOf(score, 25)
            reasons += "Caller ID is unusually short"
        }
        if (digits.length >= 7 && digits.toSet().size == 1) {
            score = maxOf(score, 35)
            reasons += "Number contains a repeated-digit pattern"
        }
        if (reasons.isEmpty()) reasons += "No local risk signals were found"

        val label = when {
            score >= 75 -> "High risk"
            score >= 35 -> "Review"
            score >= 20 -> "Low signal"
            else -> "No signal"
        }
        // Only an explicit user blocklist entry triggers automatic rejection.
        return SpamAssessment(score, label, reasons, BlockRules.isBlocked(number, blocked))
    }
}
