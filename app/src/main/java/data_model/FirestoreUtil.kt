package data_model

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
            ?: throw NullPointerException("UID is null")}")

    private val storageInstance: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    fun registerNewUser(user: User, onComplete: (Boolean) -> Unit) {
        firestoreInstance.collection(Constants.USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getCurrentUser(onComplete: (User?) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java))
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun getAllUsers(onComplete: (List<User>) -> Unit) {
        firestoreInstance.collection(Constants.USERS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                val users = result.toObjects(User::class.java)
                onComplete(users)
            }
    }

    fun updateUserProfileImage(imageUrl: String, onComplete: (Boolean) -> Unit) {
        currentUserDocRef.update("imageUrl", imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun uploadProfileImage(imageUri: Uri, onComplete: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = storageInstance.getReference(Constants.PROFILE_IMAGES_FOLDER).child(userId)

        storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(task.result.toString())
                } else {
                    onComplete(null)
                }
            }
    }

    fun getOrCreateChatChannel(otherUserId: String, onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection("engagedChats")
            .document(otherUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(document["channelId"] as String)
                } else {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener

                    val newChannel = firestoreInstance.collection("chats")
                        .document()

                    newChannel.set(Chat(
                        id = newChannel.id,
                        senderId = currentUserId,
                        receiverId = otherUserId,
                        timestamp = System.currentTimeMillis()
                    ))

                    currentUserDocRef.collection("engagedChats")
                        .document(otherUserId)
                        .set(mapOf("channelId" to newChannel.id))

                    firestoreInstance.collection("users").document(otherUserId)
                        .collection("engagedChats")
                        .document(currentUserId)
                        .set(mapOf("channelId" to newChannel.id))

                    onComplete(newChannel.id)
                }
            }
    }

    fun sendMessage(message: Chat, channelId: String) {
        firestoreInstance.collection("chats")
            .document(channelId)
            .collection("messages")
            .add(message)
    }

    fun getChatMessages(channelId: String, onMessagesReceived: (List<Chat>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("chats")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Chat::class.java)
                    onMessagesReceived(messages)
                }
            }
    }

    fun updateFcmToken(token: String) {
        currentUserDocRef.update(Constants.FCM_TOKEN_KEY, token)
    }
}