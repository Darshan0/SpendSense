package com.spendsense.features.finance.data

import com.spendsense.features.finance.domain.ClassificationResult
import com.spendsense.features.finance.domain.FinancialMessageClassifier
import com.spendsense.features.finance.domain.MessageType
import com.spendsense.features.finance.domain.SanitizedMessage

class HeuristicFinancialClassifier : FinancialMessageClassifier {
    private val transactionTerms = listOf(
        "debited",
        "credited",
        "spent",
        "paid",
        "purchase",
        "upi",
        "withdrawn",
        "refund",
        "transaction",
        "inr",
        "rs.",
        "₹",
    )
    private val balanceTerms = listOf("available balance", "avl bal", "balance")
    private val adTerms = listOf("offer", "sale", "cashback offer", "limited period")

    override suspend fun classify(message: SanitizedMessage): ClassificationResult {
        val text = message.text.lowercase()
        val transactionScore = transactionTerms.count { text.contains(it) }
        val balanceScore = balanceTerms.count { text.contains(it) }
        val adScore = adTerms.count { text.contains(it) }

        return when {
            transactionScore >= 2 -> ClassificationResult(MessageType.TRANSACTION, 0.9f)
            transactionScore == 1 && balanceScore == 0 -> ClassificationResult(MessageType.TRANSACTION, 0.72f)
            balanceScore > 0 -> ClassificationResult(MessageType.BALANCE, 0.8f)
            adScore > 0 -> ClassificationResult(MessageType.ADVERTISEMENT, 0.75f)
            else -> ClassificationResult(MessageType.NON_FINANCIAL, 0.7f)
        }
    }
}
