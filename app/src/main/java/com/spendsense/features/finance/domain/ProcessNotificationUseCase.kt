package com.spendsense.features.finance.domain

class ProcessNotificationUseCase(
    private val sanitizer: NotificationSanitizer,
    private val sensitiveMessageFilter: SensitiveMessageFilter,
    private val classifier: FinancialMessageClassifier,
    private val parser: TransactionParser,
    private val validator: ExtractionValidator,
    private val repository: TransactionRepository,
) : NotificationProcessor {
    override suspend fun process(notification: RawNotification): ProcessingResult {
        val message = sanitizer.sanitize(notification)

        when (val decision = sensitiveMessageFilter.inspect(message)) {
            ProcessingDecision.Continue -> Unit
            is ProcessingDecision.Ignore -> return ProcessingResult.Ignored(decision.reason.name)
        }

        val classification = classifier.classify(message)
        if (classification.type != MessageType.TRANSACTION) {
            return ProcessingResult.Ignored(classification.type.name)
        }

        val parseResult = parser.parse(message)
        val extraction = when (parseResult) {
            is ParseResult.Parsed -> parseResult.extraction
            is ParseResult.Failed -> return ProcessingResult.Failed(parseResult.reason)
        }

        if (!validator.validate(message, extraction)) {
            return ProcessingResult.Failed("Extraction validation failed")
        }

        if (extraction.confidence < 0.65f) {
            return ProcessingResult.NeedsReview(extraction)
        }

        val transaction = Transaction(
            type = requireNotNull(extraction.type.value),
            amountMinor = requireNotNull(extraction.amountMinor.value),
            currency = extraction.currency.value ?: "INR",
            merchantName = extraction.merchantName.value,
            paymentMethod = extraction.paymentMethod.value,
            accountLast4 = extraction.accountLast4.value,
            transactionTime = message.receivedAt,
            sourcePackage = message.sourcePackage,
            confidence = extraction.confidence,
            verificationStatus = if (extraction.confidence >= 0.9f) {
                VerificationStatus.AUTO_VERIFIED
            } else {
                VerificationStatus.NEEDS_REVIEW
            },
        )

        repository.insert(transaction)
        return ProcessingResult.Saved(transaction)
    }
}
