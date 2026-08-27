package com.spendsense.features.finance.data

import com.spendsense.core.utils.MoneyParser
import com.spendsense.features.finance.domain.ExtractionValidator
import com.spendsense.features.finance.domain.TransactionExtraction
import com.spendsense.features.finance.domain.SanitizedMessage
import com.spendsense.features.finance.domain.TransactionType

class DefaultExtractionValidator : ExtractionValidator {
    override fun validate(message: SanitizedMessage, extraction: TransactionExtraction): Boolean {
        val amount = extraction.amountMinor.value ?: return false
        val type = extraction.type.value ?: return false

        return amount > 0 &&
            type != TransactionType.UNKNOWN &&
            MoneyParser.amountAppearsInText(amount, message.text)
    }
}
