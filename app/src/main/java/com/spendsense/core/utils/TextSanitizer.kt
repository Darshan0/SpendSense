package com.spendsense.core.utils

object TextSanitizer {
    private val actionLines = listOf(
        "tap here",
        "tap to view",
        "view details",
        "open app",
    )

    fun sanitize(parts: List<String?>): String {
        return parts
            .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
            .flatMap { it.lines() }
            .map { line -> line.replace(Regex("""\s+"""), " ").trim() }
            .filter { line -> line.isNotBlank() }
            .filterNot { line -> actionLines.any { action -> line.lowercase().contains(action) } }
            .distinct()
            .joinToString(separator = "\n")
            .replace("Rs ", "Rs. ")
            .replace("INR.", "INR")
    }
}
