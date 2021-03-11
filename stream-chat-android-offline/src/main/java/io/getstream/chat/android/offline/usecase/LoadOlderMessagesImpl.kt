package io.getstream.chat.android.offline.usecase

import androidx.annotation.CheckResult
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.CoroutineCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.utils.validateCid
import io.getstream.chat.android.offline.ChatDomainImpl as NewChatDomainImpl

public interface LoadOlderMessages {
    /**
     * Loads older messages for the channel
     *
     * @param cid: the full channel id i. e. messaging:123
     * @param messageLimit: how many new messages to load
     */
    @CheckResult
    public operator fun invoke(cid: String, messageLimit: Int): Call<Channel>
}

internal class LoadOlderMessagesImpl(private val domainImpl: NewChatDomainImpl) : LoadOlderMessages {
    override operator fun invoke(cid: String, messageLimit: Int): Call<Channel> {
        validateCid(cid)

        val channelController = domainImpl.channel(cid)
        return CoroutineCall(domainImpl.scope) {
            channelController.loadOlderMessages(messageLimit)
        }
    }
}
