package com.spendsense.features.finance.data

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.spendsense.features.finance.domain.LlmAvailabilityState
import com.spendsense.features.finance.domain.LlmRequest
import com.spendsense.features.finance.domain.LlmResult
import com.spendsense.features.finance.domain.LlmStatus
import com.spendsense.features.finance.domain.LocalLanguageModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackagedLiteRtLanguageModel(
    private val context: Context,
) : LocalLanguageModel {
    private var engine: Engine? = null

    override suspend fun isAvailable(): Boolean {
        return status().state == LlmAvailabilityState.AVAILABLE
    }

    override suspend fun status(): LlmStatus {
        val installedModel = installedModelFile()
        if (installedModel.exists()) {
            return LlmStatus(
                provider = PROVIDER,
                state = LlmAvailabilityState.AVAILABLE,
                detail = "Bundled LiteRT-LM model is installed.",
            )
        }

        return if (assetExists(MODEL_ASSET_PATH) || chunkAssetsExist()) {
            LlmStatus(
                provider = PROVIDER,
                state = LlmAvailabilityState.DOWNLOADABLE,
                detail = "Bundled Gemma model asset is present and will be installed on first use.",
            )
        } else {
            LlmStatus(
                provider = PROVIDER,
                state = LlmAvailabilityState.NOT_INSTALLED,
                detail = "Add $MODEL_ASSET_PATH to package a broad-device local model.",
            )
        }
    }

    override suspend fun generate(request: LlmRequest): LlmResult = withContext(Dispatchers.Default) {
        val modelFile = ensureModelInstalled()
        val engine = engine ?: createEngine(modelFile).also { engine = it }

        engine.createConversation().use { conversation ->
            val text = conversation.sendMessage(buildPrompt(request)).asPlainText()
            LlmResult(
                text = text,
                provider = PROVIDER,
                usedFallback = false,
            )
        }
    }

    override suspend fun prepare() {
        withContext(Dispatchers.Default) {
            val modelFile = ensureModelInstalled()
            if (engine == null) {
                engine = createEngine(modelFile)
            }
        }
    }

    private fun createEngine(modelFile: File): Engine {
        val litertCacheDir = File(context.cacheDir, "litertlm-cache").also { it.mkdirs() }
        val config = EngineConfig(
            modelPath = modelFile.absolutePath,
            backend = Backend.CPU(),
            cacheDir = litertCacheDir.absolutePath,
        )
        return Engine(config).also { it.initialize() }
    }

    private fun ensureModelInstalled(): File {
        val installedModel = installedModelFile()
        if (installedModel.exists()) return installedModel

        if (!assetExists(MODEL_ASSET_PATH) && !chunkAssetsExist()) {
            throw IllegalStateException("Bundled LiteRT-LM model asset is missing.")
        }

        installedModel.parentFile?.mkdirs()
        val tempModel = tempModelFile()
        if (tempModel.exists()) tempModel.delete()

        if (chunkAssetsExist()) {
            tempModel.outputStream().use { output ->
                MODEL_CHUNK_ASSET_PATHS.forEach { path ->
                    context.assets.open(path).use { input ->
                        input.copyTo(output)
                    }
                }
            }
            promoteInstalledModel(tempModel, installedModel)
            return installedModel
        }

        context.assets.open(MODEL_ASSET_PATH).use { input ->
            tempModel.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        promoteInstalledModel(tempModel, installedModel)
        return installedModel
    }

    private fun installedModelFile(): File {
        return File(context.filesDir, MODEL_FILE_NAME)
    }

    private fun tempModelFile(): File {
        return File(context.filesDir, "$MODEL_FILE_NAME.tmp")
    }

    private fun promoteInstalledModel(tempModel: File, installedModel: File) {
        if (installedModel.exists()) installedModel.delete()
        if (!tempModel.renameTo(installedModel)) {
            tempModel.copyTo(installedModel, overwrite = true)
            tempModel.delete()
        }
    }

    private fun assetExists(path: String): Boolean {
        return runCatching {
            context.assets.open(path).close()
            true
        }.getOrDefault(false)
    }

    private fun chunkAssetsExist(): Boolean {
        return MODEL_CHUNK_ASSET_PATHS.all(::assetExists)
    }

    private fun buildPrompt(request: LlmRequest): String {
        val history = request.conversationHistory
            .joinToString(separator = "\n") { turn -> "${turn.role}: ${turn.text}" }
            .ifBlank { "No prior conversation." }
        return """
            You are SpendSense, an offline financial coach running fully on this device.

            Current assistant intent:
            ${request.intent}

            Recent conversation:
            $history

            Use only this local analytics context:
            ${request.context}

            Answer this user question:
            ${request.prompt}

            Rules:
            - Use the intent to decide the shape of the answer.
            - For casual reflection, sound like a calm financial advisor and connect the user's feeling to local facts.
            - For exact amount questions, answer the exact amount first.
            - Rewrite the required answer facts into plain, helpful coaching.
            - Keep all amounts, categories, and merchants consistent with the context.
            - Do not invent transactions, balances, merchants, or goals.
            - Do not ask for OTP, CVV, passwords, bank credentials, or full account numbers.
            - Never tell the user to increase, maximize, or use spending to reach a goal.
            - Use 2-4 short paragraphs. No markdown tables.
        """.trimIndent()
    }

    private fun Message.asPlainText(): String {
        return contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString(separator = "") { it.text }
            .trim()
    }

    private companion object {
        const val PROVIDER = "Gemma 4 E2B LiteRT-LM"
        const val MODEL_ASSET_PATH = "models/gemma-4-E2B-it.litertlm"
        const val MODEL_FILE_NAME = "gemma-4-E2B-it.litertlm"
        val MODEL_CHUNK_ASSET_PATHS = listOf(
            "models/gemma-4-E2B-it.litertlm.part00",
            "models/gemma-4-E2B-it.litertlm.part01",
            "models/gemma-4-E2B-it.litertlm.part02",
            "models/gemma-4-E2B-it.litertlm.part03",
            "models/gemma-4-E2B-it.litertlm.part04",
        )
    }
}
