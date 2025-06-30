package com.example.pawfectplanner.ui.gemini

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pawfectplanner.databinding.ItemChatMessageBinding
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.linkify.LinkifyPlugin

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ChatViewHolder(
            ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ChatViewHolder(private val b: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(b.root) {

        private val markwon = Markwon.builder(b.root.context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(b.root.context))
            .usePlugin(TaskListPlugin.create(b.root.context))
            .build()

        fun bind(msg: ChatMessage) {
            val markdown = msg.text.replace("\\n", "\n")
            markwon.setMarkdown(b.tvMessage, markdown)

            val lp = (b.cardBubble.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = if (msg.isUser) Gravity.END else Gravity.START
            }
            b.cardBubble.layoutParams = lp

            val bgAttr = if (msg.isUser) MaterialR.attr.colorPrimary else MaterialR.attr.colorSurface
            val fgAttr = if (msg.isUser) MaterialR.attr.colorOnPrimary else MaterialR.attr.colorOnSurface
            val bg = MaterialColors.getColor(b.cardBubble, bgAttr)
            val fg = MaterialColors.getColor(b.cardBubble, fgAttr)
            b.cardBubble.setCardBackgroundColor(bg)
            b.tvMessage.setTextColor(fg)
        }
    }

    private class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(a: ChatMessage, b: ChatMessage) =
            a.timestamp == b.timestamp

        override fun areContentsTheSame(a: ChatMessage, b: ChatMessage) =
            a == b
    }
}
