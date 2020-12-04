package io.getstream.chat.android.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.databinding.StreamItemMentionListBinding
import io.getstream.chat.android.ui.search.SearchResultListAdapter.MessagePreviewViewHolder
import io.getstream.chat.android.ui.search.SearchResultListView.SearchResultSelectedListener

public class SearchResultListAdapter : ListAdapter<Message, MessagePreviewViewHolder>(MessageDiffCallback) {

    private var searchResultSelectedListener: SearchResultSelectedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagePreviewViewHolder {
        return StreamItemMentionListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
            .let { binding -> MessagePreviewViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: MessagePreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    public fun setSearchResultSelectedListener(searchResultSelectedListener: SearchResultSelectedListener?) {
        this.searchResultSelectedListener = searchResultSelectedListener
    }

    public inner class MessagePreviewViewHolder(
        private val binding: StreamItemMentionListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var message: Message

        init {
            binding.root.setOnClickListener {
                searchResultSelectedListener?.onSearchResultSelected(message)
            }
        }

        internal fun bind(message: Message) {
            this.message = message
            binding.root.setMessage(message)
        }
    }

    private object MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Comparing only properties used by the ViewHolder
            return oldItem.id == newItem.id &&
                oldItem.createdAt == newItem.createdAt &&
                oldItem.createdLocallyAt == newItem.createdLocallyAt &&
                oldItem.text == newItem.text &&
                oldItem.user == newItem.user
        }
    }
}