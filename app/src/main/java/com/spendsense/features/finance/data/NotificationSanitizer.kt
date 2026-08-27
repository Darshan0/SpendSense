package com.spendsense.features.finance.data

import com.spendsense.core.utils.TextSanitizer
import com.spendsense.features.finance.domain.NotificationSanitizer
import com.spendsense.features.finance.domain.RawNotification
import com.spendsense.features.finance.domain.SanitizedMessage
import java.time.Instant

class DefaultNotificationSanitizer : NotificationSanitizer {
    override fun sanitize(notification: RawNotification): SanitizedMessage {
        return SanitizedMessage(
            sourcePackage = notification.packageName,
            sender = notification.title,
            text = TextSanitizer.sanitize(
                listOf(
                    notification.title,
                    notification.text,
                    notification.bigText,
                    notification.subText,
                ),
            ),
            receivedAt = Instant.ofEpochMilli(notification.postedAt),
        )
    }
}
