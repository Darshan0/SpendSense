package com.spendsense.features.finance.data

import com.spendsense.features.finance.domain.LlmAvailabilityState
import com.spendsense.features.finance.domain.LlmRequest
import com.spendsense.features.finance.domain.LlmResult
import com.spendsense.features.finance.domain.LlmStatus
import com.spendsense.features.finance.domain.LocalLanguageModel

class LocalLanguageModelRouter(
    private val providers: List<LocalLanguageModel>,
    private val fallback: LocalLanguageModel,
) : LocalLanguageModel {
    override suspend fun isAvailable(): Boolean {
        return providers.any { it.isAvailable() }
    }

    override suspend fun status(): LlmStatus {
        val statuses = providers.map { it.status() }
        val ready = statuses.firstOrNull { it.state == LlmAvailabilityState.AVAILABLE }
        if (ready != null) return ready

        return LlmStatus(
            provider = "Local model router",
            state = LlmAvailabilityState.UNAVAILABLE,
            detail = statuses.joinToString(separator = " | ") { "${it.provider}: ${it.detail}" },
        )
    }

    override suspend fun prepare() {
        providers.firstOrNull { provider ->
            val status = provider.status()
            status.state == LlmAvailabilityState.AVAILABLE ||
                status.state == LlmAvailabilityState.DOWNLOADABLE
        }?.prepare()
    }

    override suspend fun generate(request: LlmRequest): LlmResult {
        providers.forEach { provider ->
            val status = provider.status()
            if (status.state == LlmAvailabilityState.AVAILABLE ||
                status.state == LlmAvailabilityState.DOWNLOADABLE
            ) {
                val result = runCatching { provider.generate(request) }.getOrNull()
                if (result != null && result.text.isNotBlank()) {
                    return result
                }
            }
        }

        return fallback.generate(request)
    }
}
