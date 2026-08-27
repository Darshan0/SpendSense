package com.spendsense.features.finance.data

import com.spendsense.features.finance.domain.ProcessingDecision
import com.spendsense.features.finance.domain.SanitizedMessage
import com.spendsense.features.finance.domain.SensitiveContent
import com.spendsense.features.finance.domain.SensitiveMessageFilter

class RegexSensitiveMessageFilter : SensitiveMessageFilter {
    private val otpRegex = Regex("""(?i)\b(otp|one[- ]time password|verification code)\b""")
    private val authRegex = Regex("""(?i)\b(login code|authorize login|authentication request)\b""")
    private val cvvRegex = Regex("""(?i)\bcvv\b""")
    private val resetRegex = Regex("""(?i)\b(password reset|reset password)\b""")

    override fun inspect(message: SanitizedMessage): ProcessingDecision {
        val text = message.text
        return when {
            otpRegex.containsMatchIn(text) -> ProcessingDecision.Ignore(SensitiveContent.OTP)
            authRegex.containsMatchIn(text) -> ProcessingDecision.Ignore(SensitiveContent.AUTHENTICATION)
            cvvRegex.containsMatchIn(text) -> ProcessingDecision.Ignore(SensitiveContent.CVV)
            resetRegex.containsMatchIn(text) -> ProcessingDecision.Ignore(SensitiveContent.PASSWORD_RESET)
            else -> ProcessingDecision.Continue
        }
    }
}
