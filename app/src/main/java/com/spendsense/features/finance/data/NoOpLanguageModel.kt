package com.spendsense.features.finance.data

import com.spendsense.features.finance.domain.LlmRequest
import com.spendsense.features.finance.domain.LlmResult
import com.spendsense.features.finance.domain.LlmAvailabilityState
import com.spendsense.features.finance.domain.LlmStatus
import com.spendsense.features.finance.domain.LocalLanguageModel

class NoOpLanguageModel : LocalLanguageModel {
    override suspend fun isAvailable(): Boolean = false

    override suspend fun status(): LlmStatus {
        return LlmStatus(
            provider = "Deterministic fallback",
            state = LlmAvailabilityState.UNAVAILABLE,
            detail = "No local model provider is configured.",
        )
    }

    override suspend fun generate(request: LlmRequest): LlmResult {
        return LlmResult(
            text = "",
            provider = "Deterministic fallback",
            usedFallback = true,
        )
    }
}
