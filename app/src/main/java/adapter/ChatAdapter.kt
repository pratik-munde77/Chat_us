package adapter

package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.models.Chat
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val chatList: List<Chat>,
    private val onItemClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.ivProfile)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val lastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val time: TextView = itemView.findViewById(R.id.tvTime)
        val status: View = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]

        holder.username.text = chat.username
        holder.lastMessage.text = chat.lastMessage
        holder.time.text = getTimeAgo(chat.timestamp)

        if (chat.profileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(chat.profileImage)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.profileImage)
        }

        // Update status indicator
        holder.status.visibility = if (chat.status == "online") View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount() = chatList.size

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }
}