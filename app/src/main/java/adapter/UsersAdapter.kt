package adapter

package com.example.chatapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.models.User
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(
    private val userList: List<User>,
    private val onItemClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.ivProfile)
        val username: TextView = itemView.findViewById(R.id.tvUsername)
        val status: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.username.text = user.username
        holder.status.text = if (user.status == "online") "Online" else "Offline"

        if (user.profileImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.profileImage)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.profileImage)
        }

        holder.itemView.setOnClickListener { onItemClick(user) }
    }

    override fun getItemCount() = userList.size
}