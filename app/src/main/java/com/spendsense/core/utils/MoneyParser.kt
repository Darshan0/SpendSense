package com.spendsense.core.utils

object MoneyParser {
    private val moneyRegex = Regex("""(?i)(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""")

    fun firstAmountMinor(text: String): Long? {
        val match = moneyRegex.find(text) ?: return null
        return amountToMinor(match.groupValues[1])
    }

    fun amountToMinor(rawAmount: String): Long? {
        val normalized = rawAmount.replace(",", "").trim()
        if (normalized.isBlank()) return null

        val parts = normalized.split(".")
        val rupees = parts.getOrNull(0)?.toLongOrNull() ?: return null
        val paise = parts.getOrNull(1)
            ?.padEnd(2, '0')
            ?.take(2)
            ?.toLongOrNull()
            ?: 0L

        return rupees * 100 + paise
    }

    fun amountAppearsInText(amountMinor: Long, text: String): Boolean {
        return moneyRegex.findAll(text).any { match ->
            amountToMinor(match.groupValues[1]) == amountMinor
        }
    }
}
