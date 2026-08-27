package com.spendsense.features.finance.data

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.spendsense.features.finance.domain.RawNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

open class LedgerNotificationListener : NotificationListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val rawNotification = NotificationMapper.map(sbn)
        serviceScope.launch {
            AppGraph.processNotificationUseCase.process(rawNotification)
        }
    }
}

object NotificationMapper {
    fun map(sbn: StatusBarNotification): RawNotification {
        val extras = sbn.notification.extras
        return RawNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
            subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
            postedAt = sbn.postTime,
        )
    }
}

object NotificationAccess {
    fun settingsIntent(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun isEnabled(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ).orEmpty()
        return enabledListeners.contains(context.packageName)
    }
}
