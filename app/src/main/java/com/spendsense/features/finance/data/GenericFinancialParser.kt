package com.spendsense.features.finance.data

import com.spendsense.core.utils.MoneyParser
import com.spendsense.features.finance.domain.ExtractedField
import com.spendsense.features.finance.domain.ExtractionSource
import com.spendsense.features.finance.domain.ParseResult
import com.spendsense.features.finance.domain.PaymentMethod
import com.spendsense.features.finance.domain.SanitizedMessage
import com.spendsense.features.finance.domain.TransactionExtraction
import com.spendsense.features.finance.domain.TransactionParser
import com.spendsense.features.finance.domain.TransactionType

class GenericFinancialParser : TransactionParser {
    private val merchantRegex = Regex("""(?i)\b(?:to|at|paid to)\s+([A-Z0-9 .&_-]{2,40})""")
    private val accountRegex = Regex("""(?i)\b(?:a/c|acct|account|card)(?:\s*(?:xx|x+|ending|no\.?)?)?\s*([0-9]{4})\b""")

    override suspend fun parse(message: SanitizedMessage): ParseResult {
        val text = message.text
        val amountMinor = MoneyParser.firstAmountMinor(text)
            ?: return ParseResult.Failed("No amount found")

        val transactionType = inferType(text)
        val merchant = merchantRegex.find(text)?.groupValues?.getOrNull(1)
            ?.let(::cleanMerchant)
            ?.takeIf { it.isNotBlank() }
        val accountLast4 = accountRegex.find(text)?.groupValues?.getOrNull(1)
        val paymentMethod = inferPaymentMethod(text)

        val extraction = TransactionExtraction(
            type = ExtractedField(transactionType, typeConfidence(transactionType), ExtractionSource.REGEX),
            amountMinor = ExtractedField(amountMinor, 1f, ExtractionSource.REGEX),
            currency = ExtractedField("INR", 0.95f, ExtractionSource.DEFAULT),
            merchantName = ExtractedField(merchant, if (merchant == null) 0f else 0.82f, ExtractionSource.REGEX),
            paymentMethod = ExtractedField(paymentMethod, if (paymentMethod == null) 0f else 0.9f, ExtractionSource.REGEX),
            accountLast4 = ExtractedField(accountLast4, if (accountLast4 == null) 0f else 0.9f, ExtractionSource.REGEX),
            confidence = calculateConfidence(
                amount = 1f,
                type = typeConfidence(transactionType),
                merchant = if (merchant == null) 0f else 0.82f,
                account = if (accountLast4 == null) 0f else 0.9f,
                date = 0f,
                paymentMethod = if (paymentMethod == null) 0f else 0.9f,
            ),
        )

        return ParseResult.Parsed(extraction)
    }

    private fun inferType(text: String): TransactionType {
        val lower = text.lowercase()
        return when {
            "failed" in lower -> TransactionType.PAYMENT_FAILED
            "reversed" in lower || "credited back" in lower -> TransactionType.REVERSAL
            "refund" in lower -> TransactionType.REFUND
            "withdrawn" in lower -> TransactionType.CASH_WITHDRAWAL
            "credited" in lower -> TransactionType.CREDIT
            "debited" in lower || "spent" in lower || "paid" in lower -> TransactionType.DEBIT
            else -> TransactionType.UNKNOWN
        }
    }

    private fun cleanMerchant(rawMerchant: String): String {
        val stopWords = listOf(" avl bal", " available balance", " balance", " on ", " ref ")
        val lower = rawMerchant.lowercase()
        val stopIndex = stopWords
            .map { lower.indexOf(it) }
            .filter { it >= 0 }
            .minOrNull()
            ?: rawMerchant.length

        return rawMerchant
            .take(stopIndex)
            .trim()
            .trimEnd('.', ',', '-')
    }

    private fun inferPaymentMethod(text: String): PaymentMethod? {
        val lower = text.lowercase()
        return when {
            "upi" in lower -> PaymentMethod.UPI
            "card" in lower -> PaymentMethod.CARD
            "netbanking" in lower || "net banking" in lower -> PaymentMethod.NET_BANKING
            "cash" in lower -> PaymentMethod.CASH
            else -> null
        }
    }

    private fun typeConfidence(type: TransactionType): Float {
        return if (type == TransactionType.UNKNOWN) 0f else 0.9f
    }

    private fun calculateConfidence(
        amount: Float,
        type: Float,
        merchant: Float,
        account: Float,
        date: Float,
        paymentMethod: Float,
    ): Float {
        return amount * 0.40f +
            type * 0.20f +
            merchant * 0.15f +
            account * 0.10f +
            date * 0.10f +
            paymentMethod * 0.05f
    }
}
