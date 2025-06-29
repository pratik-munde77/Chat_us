package adapter


import android.widget.ImageView
import com.bumptech.glide.Glide


fun ImageView.loadImage(url: String) {
    Glide.with(this.context)
        .load(url)
        .placeholder(R.drawable.ic_profile_placeholder)
        .error(R.drawable.ic_profile_placeholder)
        .into(this)
}

fun Long.toTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60 * 1000 -> "just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))
    }
}