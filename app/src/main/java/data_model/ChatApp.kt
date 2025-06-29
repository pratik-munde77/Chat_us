package data_model

class ChatApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Image Picker
        ImagePicker.init(this)

        // Update FCM token if changed
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirestoreUtil.updateFcmToken(task.result)
            }
        }
    }
}