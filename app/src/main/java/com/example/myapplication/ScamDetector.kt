package com.example.myapplication

/**
 * Logic to detect potential SASSA scams based on heuristic scoring.
 */
object ScamDetector {

    private val SUSPICIOUS_KEYWORDS = listOf(
        "sassa", "grant", "pin", "verify", "expired", "bank details", 
        "r350", "payday", "update", "banking", "login", "collect",
        "srd", "gold card", "postbank", "otp", "re-register", "bonus"
    )

    private val URL_REGEX = "(https?://|www\\.)[\\n\\s]*[\\w\\d\\.\\-]+".toRegex(RegexOption.IGNORE_CASE)

    data class DetectionResult(
        val isScam: Boolean,
        val confidence: Float,
        val reason: String? = null
    )

    fun isOfficialGovLink(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.endsWith(".gov.za") || lowerUrl.contains(".gov.za/")
    }

    fun analyzeMessage(text: String): DetectionResult {
        val lowerText = text.lowercase()
        var score = 0f
        val detectedKeywords = mutableListOf<String>()

        // 1. Keyword analysis
        SUSPICIOUS_KEYWORDS.forEach { keyword ->
            if (lowerText.contains(keyword)) {
                score += 0.25f
                detectedKeywords.add(keyword)
            }
        }

        // 2. URL detection and validation
        val urlMatch = URL_REGEX.find(text)
        var hasUnsafeUrl = false
        if (urlMatch != null) {
            val url = urlMatch.value
            if (lowerText.contains("sassa") && !isOfficialGovLink(url)) {
                score += 0.6f // High risk if SASSA + non-gov link
                hasUnsafeUrl = true
            } else {
                score += 0.3f
            }
        }

        // 3. Urgent / Threatening language
        if (lowerText.contains("immediately") || lowerText.contains("cancel") || lowerText.contains("final notice")) {
            score += 0.3f
        }

        // 4. Specific SASSA trigger + Action word combo
        if (lowerText.contains("sassa") && (lowerText.contains("pin") || lowerText.contains("otp") || lowerText.contains("verify"))) {
            score += 0.4f
        }

        val isScam = score >= 0.6f
        val reason = if (isScam) {
            buildString {
                if (hasUnsafeUrl) {
                    append("Unofficial SASSA link detected. Official links always end in .gov.za. ")
                }
                append("Suspicious patterns found")
                if (detectedKeywords.isNotEmpty()) {
                    val displayKeywords = detectedKeywords.take(3).joinToString(", ")
                    append(": $displayKeywords")
                    if (detectedKeywords.size > 3) append("...")
                }
            }
        } else null

        return DetectionResult(
            isScam = isScam,
            confidence = score.coerceIn(0f, 1f),
            reason = reason
        )
    }
}
