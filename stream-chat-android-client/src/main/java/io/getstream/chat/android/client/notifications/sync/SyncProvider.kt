package io.getstream.chat.android.client.notifications.sync

import android.content.Context

public class SyncProvider(context: Context) {

    public val encryptedBackgroundSyncConfigStore: EncryptedBackgroundSyncConfigStore by lazy {
        EncryptedBackgroundSyncConfigStore(context)
    }

    public val notificationConfigStore: NotificationConfigStore by lazy {
        NotificationConfigStore(context)
    }
}
