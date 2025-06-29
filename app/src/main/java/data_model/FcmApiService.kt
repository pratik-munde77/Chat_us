package data_model
interface FcmApiService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key= AIzaSyAkKpT82bu3gpHAi5vmY-6ka4OO3kPXRoM" // Replace with your Firebase server key
    )
    @POST("fcm/send")
    fun sendNotification(@Body notification: FcmNotification): Call<FcmResponse>
}

data class FcmNotification(
    val to: String,
    val data: Map<String, String>
)

data class FcmResponse(
    val success: Int,
    val failure: Int
)