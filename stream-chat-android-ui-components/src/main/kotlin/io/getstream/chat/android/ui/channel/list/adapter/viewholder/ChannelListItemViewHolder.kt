package io.getstream.chat.android.ui.channel.list.adapter.viewholder

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.ChannelListViewStyle
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.databinding.StreamChannelListItemForegroundViewBinding
import io.getstream.chat.android.ui.databinding.StreamChannelListItemViewBinding
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.context
import io.getstream.chat.android.ui.utils.extensions.getCurrentUser
import io.getstream.chat.android.ui.utils.extensions.getCurrentUserLastMessage
import io.getstream.chat.android.ui.utils.extensions.getCurrentUserRead
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDisplayName
import io.getstream.chat.android.ui.utils.extensions.getLastMessage
import io.getstream.chat.android.ui.utils.extensions.getLastMessageTime
import io.getstream.chat.android.ui.utils.extensions.getOrDefault
import io.getstream.chat.android.ui.utils.extensions.getPreviewText
import io.getstream.chat.android.ui.utils.extensions.getUsers
import io.getstream.chat.android.ui.utils.extensions.lastMessageByCurrentUserWasRead
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.absoluteValue

public class ChannelListItemViewHolder(itemView: View) : BaseChannelListItemViewHolder(itemView) {

    public companion object {
        @SuppressLint("ConstantLocale")
        private val DEFAULT_LOCALE: Locale = Locale.getDefault()

        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", DEFAULT_LOCALE)

        private const val OPTIONS_COUNT = 2
    }

    public val binding: StreamChannelListItemViewBinding =
        StreamChannelListItemViewBinding.bind(itemView).apply {
            configureSwipeBehavior()
        }

    public override fun bind(
        channel: Channel,
        diff: ChannelDiff?,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        channelDeleteListener: ChannelListView.ChannelClickListener,
        userClickListener: ChannelListView.UserClickListener,
        style: ChannelListViewStyle?
    ) {
        configureForeground(diff, channel, userClickListener, channelClickListener, channelLongClickListener, style)
        binding.itemBackgroundView.deleteImageView.setOnClickListener {
            channelDeleteListener.onClick(channel)
        }
    }

    private fun configureForeground(
        diff: ChannelDiff?,
        channel: Channel,
        userClickListener: ChannelListView.UserClickListener,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener,
        style: ChannelListViewStyle?
    ) {
        binding.itemForegroundView.apply {

            diff?.run {
                if (nameChanged) {
                    configureChannelNameLabel(channel)
                }

                if (avatarViewChanged) {
                    configureAvatarView(channel, userClickListener, channelClickListener)
                }

                if (lastMessageChanged) {
                    configureLastMessageLabel(channel)
                    configureLastMessageTimestamp(channel)
                    configureUnreadCountBadge(channel)
                }

                if (readStateChanged) {
                    configureCurrentUserLastMessageStatus(channel)
                }
            }

            configureClickListeners(channel, channelClickListener, channelLongClickListener)

            style?.let { applyStyle(this, it) }
        }
    }

    private fun StreamChannelListItemForegroundViewBinding.configureClickListeners(
        channel: Channel,
        channelClickListener: ChannelListView.ChannelClickListener,
        channelLongClickListener: ChannelListView.ChannelClickListener
    ) {
        root.setOnClickListener {
            channelClickListener.onClick(channel)
        }

        root.setOnLongClickListener {
            channelLongClickListener.onClick(channel)
            true
        }
    }

    private fun StreamChannelListItemForegroundViewBinding.configureChannelNameLabel(channel: Channel) {
        channelNameLabel.text = channel.getDisplayName()
    }

    private fun StreamChannelListItemForegroundViewBinding.configureAvatarView(
        channel: Channel,
        userClickListener: ChannelListView.UserClickListener,
        channelClickListener: ChannelListView.ChannelClickListener
    ) {
        avatarView.setChannelData(channel, channel.getUsers())
        avatarView.setOnClickListener {
            when (channel.getUsers().size) {
                1 -> userClickListener.onUserClick(channel.getCurrentUser())
                else -> channelClickListener.onClick(channel)
            }
        }
    }

    private fun StreamChannelListItemForegroundViewBinding.configureUnreadCountBadge(channel: Channel) {
        unreadCountBadge.isVisible = channel.messages.isNotEmpty()

        if (!unreadCountBadge.isVisible) {
            return
        }

        channel
            .getCurrentUserRead()
            ?.unreadMessages
            .getOrDefault(0)
            .let { unreadCount ->
                unreadCountBadge.apply {
                    isVisible = unreadCount > 0
                    text = unreadCount.toString()
                }
            }
    }

    private fun StreamChannelListItemForegroundViewBinding.configureLastMessageTimestamp(channel: Channel) {
        lastMessageTimeLabel.isVisible = channel.messages.isNotEmpty()

        if (!lastMessageTimeLabel.isVisible) {
            return
        }

        channel.getLastMessageTime()
            ?.let(TIME_FORMAT::format)
            ?.let { lastMessageTimeLabel.text = it }
    }

    private fun StreamChannelListItemForegroundViewBinding.configureLastMessageLabel(channel: Channel) {
        lastMessageLabel.isVisible = channel.messages.isNotEmpty()

        if (!lastMessageLabel.isVisible) {
            return
        }

        lastMessageLabel.text = channel.getLastMessage()?.getPreviewText(context) ?: String.EMPTY
    }

    private fun StreamChannelListItemForegroundViewBinding.configureCurrentUserLastMessageStatus(channel: Channel) {
        messageStatusImageView.isVisible = channel.messages.isNotEmpty()

        if (!messageStatusImageView.isVisible) {
            return
        }

        val lastMessage = channel.getLastMessage()

        /**
         * read - if the last message doesn't belong to current user, or if channel reads indicates it
         * delivered - if the last message belongs to the current user and reads indicate it wasn't read
         * pending - if the sync status says it's pending
         */

        val currentUserSentLastMessage = lastMessage == channel.getCurrentUserLastMessage()

        when {
            !currentUserSentLastMessage || channel.lastMessageByCurrentUserWasRead() ->
                messageStatusImageView.setImageResource(R.drawable.stream_ic_check_all)

            currentUserSentLastMessage && !channel.lastMessageByCurrentUserWasRead() ->
                messageStatusImageView.setImageResource(R.drawable.stream_ic_check_gray)

            else -> determineLastMessageSyncStatus(lastMessage)
        }
    }

    private fun StreamChannelListItemForegroundViewBinding.determineLastMessageSyncStatus(message: Message?) {
        message?.syncStatus?.let { sync ->
            when (sync) {
                SyncStatus.IN_PROGRESS, SyncStatus.SYNC_NEEDED -> {
                    messageStatusImageView.setImageResource(R.drawable.stream_ic_clock)
                }

                SyncStatus.COMPLETED -> {
                    messageStatusImageView.setImageResource(R.drawable.stream_ic_check_gray)
                }

                SyncStatus.FAILED_PERMANENTLY -> {
                    // no direction on this yet
                }
            }
        }
    }

    private var styleApplied = false

    private fun applyStyle(binding: StreamChannelListItemForegroundViewBinding, style: ChannelListViewStyle) {
        if (styleApplied) {
            return
        }

        binding.apply {
            val px = TypedValue.COMPLEX_UNIT_PX
            channelNameLabel.setTextSize(px, style.channelTitleText.size.toFloat())
            lastMessageLabel.setTextSize(px, style.lastMessage.size.toFloat())
            lastMessageTimeLabel.setTextSize(px, style.lastMessageDateText.size.toFloat())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun StreamChannelListItemViewBinding.configureSwipeBehavior() {
        val menuItemWidth = context.getDimension(R.dimen.stream_channel_list_item_option_icon_width).toFloat()
        val optionsMenuWidth = menuItemWidth * OPTIONS_COUNT
        var startX = 0f
        var startY = 0f
        val dragRange = -optionsMenuWidth..0f
        var swiping = false

        itemForegroundView.root.setOnTouchListener { view, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    // store our initial touch coordinate values so we can determine deltas
                    startX = event.x
                    startY = event.y

                    swiping = false

                    false // don't consume
                }

                MotionEvent.ACTION_MOVE -> {

                    // calculate the deltas
                    val deltaY = event.y - startY
                    val deltaX = event.x - startX

                    // determine if it's a swipe by comparing axis delta magnitude
                    swiping = deltaX.absoluteValue > deltaY.absoluteValue

                    when {
                        swiping -> {
                            // determine the new x value by adding the delta
                            val projectedX = view.x + deltaX
                            // clamp it and animate if necessary
                            projectedX.coerceIn(dragRange).let { clampedX ->
                                // set the new x if it's different
                                if (view.x != projectedX) {
                                    view.x = clampedX
                                }
                            }
                        }
                    }

                    swiping // consume if swiping
                }

                // determine snap and animate to it on action up
                MotionEvent.ACTION_UP -> {

                    val snap = when {
                        view.x <= -(optionsMenuWidth * .5) -> -optionsMenuWidth
                        else -> 0f
                    }

                    view.animate().x(snap).setStartDelay(0).setDuration(100).start()

                    swiping // consume if swiping
                }

                // snap closed on cancel
                MotionEvent.ACTION_CANCEL -> {

                    view.animate().x(0f).setStartDelay(0).setDuration(100).start()

                    false // don't consume
                }

                else -> false // don't consume
            }
        }
    }
}
