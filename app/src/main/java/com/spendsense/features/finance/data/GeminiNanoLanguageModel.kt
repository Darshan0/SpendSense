package com.spendsense.features.finance.data

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Generation
import com.spendsense.features.finance.domain.LlmAvailabilityState
import com.spendsense.features.finance.domain.LlmRequest
import com.spendsense.features.finance.domain.LlmResult
import com.spendsense.features.finance.domain.LlmStatus
import com.spendsense.features.finance.domain.LocalLanguageModel
import kotlinx.coroutines.flow.first

class GeminiNanoLanguageModel : LocalLanguageModel {
    private val generativeModel by lazy { Generation.getClient() }

    override suspend fun isAvailable(): Boolean {
        return status().state == LlmAvailabilityState.AVAILABLE
    }

    override suspend fun status(): LlmStatus {
        return try {
            when (generativeModel.checkStatus()) {
                FeatureStatus.AVAILABLE -> LlmStatus(
                    provider = PROVIDER,
                    state = LlmAvailabilityState.AVAILABLE,
                    detail = "Gemini Nano is ready on this device.",
                )
                FeatureStatus.DOWNLOADABLE -> LlmStatus(
                    provider = PROVIDER,
                    state = LlmAvailabilityState.DOWNLOADABLE,
                    detail = "Gemini Nano is supported but model assets need to be downloaded.",
                )
                FeatureStatus.DOWNLOADING -> LlmStatus(
                    provider = PROVIDER,
                    state = LlmAvailabilityState.DOWNLOADING,
                    detail = "Gemini Nano model assets are downloading.",
                )
                FeatureStatus.UNAVAILABLE -> LlmStatus(
                    provider = PROVIDER,
                    state = LlmAvailabilityState.UNAVAILABLE,
                    detail = "Gemini Nano is not available on this device.",
                )
                else -> LlmStatus(
                    provider = PROVIDER,
                    state = LlmAvailabilityState.ERROR,
                    detail = "Gemini Nano returned an unknown availability state.",
                )
            }
        } catch (exception: GenAiException) {
            LlmStatus(
                provider = PROVIDER,
                state = LlmAvailabilityState.ERROR,
                detail = exception.message ?: "Gemini Nano status check failed.",
            )
        }
    }

    override suspend fun generate(request: LlmRequest): LlmResult {
        prepareIfNeeded()

        val response = generativeModel.generateContent(buildPrompt(request))
        val text = response.candidates.firstOrNull()?.text.orEmpty().trim()
        return LlmResult(
            text = text,
            provider = PROVIDER,
            usedFallback = false,
        )
    }

    override suspend fun prepare() {
        prepareIfNeeded()
    }

    private suspend fun prepareIfNeeded() {
        when (generativeModel.checkStatus()) {
            FeatureStatus.AVAILABLE -> {
                generativeModel.warmup()
            }
            FeatureStatus.DOWNLOADABLE -> {
                generativeModel.download().first { status ->
                    status is DownloadStatus.DownloadCompleted ||
                        status is DownloadStatus.DownloadFailed
                }.also { status ->
                    if (status is DownloadStatus.DownloadFailed) {
                        throw IllegalStateException("Gemini Nano download failed.")
                    }
                }
                generativeModel.warmup()
            }
            FeatureStatus.DOWNLOADING -> {
                throw IllegalStateException("Gemini Nano is still downloading.")
            }
            else -> {
                throw IllegalStateException("Gemini Nano is unavailable on this device.")
            }
        }
    }

    private fun buildPrompt(request: LlmRequest): String {
        return """
            You are SpendSense, a private on-device financial coach.

            Rules:
            - Use only the local analytics context below.
            - Rewrite the required answer facts into plain, helpful coaching.
            - Keep all amounts, categories, and merchants consistent with the context.
            - Do not invent transactions, merchants, balances, goals, or amounts.
            - Do not ask for bank credentials, OTPs, CVV, passwords, or account numbers.
            - Never tell the user to increase, maximize, or use spending to reach a goal.
            - Use 2-4 short paragraphs. No markdown tables.
            - If the data is insufficient, say what local data is missing.

            Local analytics context:
            ${request.context}

            User question:
            ${request.prompt}
        """.trimIndent()
    }

    private companion object {
        const val PROVIDER = "Gemini Nano"
    }
}
