package com.spendsense.core.presentation

import com.spendsense.features.finance.domain.TransactionType
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

object MoneyFormatter {
    fun formatMinor(amountMinor: Long, currencyCode: String, type: TransactionType? = null): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.currency = Currency.getInstance(currencyCode)
        val amount = abs(amountMinor) / 100.0
        val prefix = when (type) {
            TransactionType.CREDIT, TransactionType.REFUND -> "+"
            TransactionType.DEBIT, TransactionType.CASH_WITHDRAWAL -> "-"
            else -> ""
        }
        return prefix + formatter.format(amount)
    }
}
