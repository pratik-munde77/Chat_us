package data_model

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val imageUrl: String = "",
    val fcmToken: String = ""
)