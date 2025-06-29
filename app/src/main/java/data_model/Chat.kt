package data_model

data class Chat(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val imageUrl: String = "",
    val isImage: Boolean = false
)