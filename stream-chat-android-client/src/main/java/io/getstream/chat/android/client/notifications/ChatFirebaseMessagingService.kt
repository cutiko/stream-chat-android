package io.getstream.chat.android.client.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")
    private val defaultNotificationConfig = NotificationConfig()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        if (ChatClient.isValidRemoteMessage(remoteMessage, defaultNotificationConfig)) {
            syncMessageBroadcast(this)
        }
    }

    override fun onNewToken(token: String) {
        if (ChatClient.isInitialized) {
            ChatClient.instance().onNewTokenReceived(token)
        }
    }

    private fun syncMessageBroadcast(context: Context) {
        Intent(ACTION_SYNC_MESSAGES).apply {
            component = ComponentName(
                "io.getstream.chat.android.livedata.service.sync",
                "OfflineSyncFirebaseMessagingReceiver"
            )
        }.let(context::sendBroadcast)
    }

    private fun handleMessageInternalLy(message: RemoteMessage) {
        if (ChatClient.isInitialized) {
            ChatClient.instance().onMessageReceived(message)
        } else {

        }
    }

    private fun initClient(context: Context, user: User, userToken: String, apiKey: String, ): ChatClient {
        val notificationConfig = syncModule.notificationConfigStore.get()
        val notificationHandler = ChatNotificationHandler(context, notificationConfig)

        val client = ChatClient.Builder(apiKey, context.applicationContext)
            .notifications(notificationHandler)
            .build()

        client.setUserWithoutConnecting(user, userToken)

        return client
    }

    companion object {
        const val ACTION_SYNC_MESSAGES : String = "ACTION_SYNC_MESSAGES"
    }
}
