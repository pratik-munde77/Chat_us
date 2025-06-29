package com.example.chat_us

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.chat_us.databinding.ActivityChatBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import data_model.User

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private var chatChannelId: String? = null
    private var receiverId: String? = null
    private var receiverUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiverId = intent.getStringExtra("userId")
        if (receiverId == null) {
            finish()
            return
        }

        setupRecyclerView()
        loadReceiverData()
        setupChatChannel()

        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        binding.btnAttach.setOnClickListener {
            showAttachmentOptions()
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(FirebaseAuth.getInstance().currentUser?.uid ?: "")
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun loadReceiverData() {
        FirestoreUtil.getCurrentUser { currentUser ->
            if (currentUser != null) {
                FirestoreUtil.getAllUsers { users ->
                    receiverUser = users.find { it.id == receiverId }
                    receiverUser?.let { user ->
                        binding.tvUsername.text = user.username

                        if (user.imageUrl.isNotEmpty()) {
                            Glide.with(this)
                                .load(user.imageUrl)
                                .placeholder(R.drawable.ic_profile)
                                .into(binding.ivProfile)
                        }
                    }
                }
            }
        }
    }

    private fun setupChatChannel() {
        FirestoreUtil.getCurrentUser { currentUser ->
            if (currentUser != null && receiverId != null) {
                FirestoreUtil.getOrCreateChatChannel(receiverId!!) { channelId ->
                    chatChannelId = channelId

                    FirestoreUtil.getChatMessages(channelId) { messages ->
                        messageAdapter.submitList(messages)
                        binding.recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty() || chatChannelId == null || receiverId == null) return

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val message = Chat(
            senderId = currentUserId,
            receiverId = receiverId!!,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        FirestoreUtil.sendMessage(message, chatChannelId!!)
        binding.etMessage.text.clear()

        // Send notification
        receiverUser?.fcmToken?.let { sendNotification(it, messageText) }
    }

    private fun showAttachmentOptions() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Attachment")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> takePhoto()
                options[item] == "Choose from Gallery" -> chooseFromGallery()
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun takePhoto() {
        ImagePicker.with(this)
            .cameraOnly()
            .start()
    }

    private fun chooseFromGallery() {
        ImagePicker.with(this)
            .galleryOnly()
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            uri?.let { sendImageMessage(it) }
        }
    }

    private fun sendImageMessage(imageUri: Uri) {
        if (chatChannelId == null || receiverId == null) return

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val storageRef = FirebaseStorage.getInstance().getReference(Constants.CHAT_IMAGES_FOLDER)
            .child("${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    val message = Chat(
                        senderId = currentUserId,
                        receiverId = receiverId!!,
                        imageUrl = downloadUri.toString(),
                        isImage = true,
                        timestamp = System.currentTimeMillis()
                    )

                    FirestoreUtil.sendMessage(message, chatChannelId!!)

                    // Send notification
                    receiverUser?.fcmToken?.let { sendNotification(it, "Sent an image") }
                }
            }
    }

    private fun sendNotification(fcmToken: String, message: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val username = getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_USERNAME, "") ?: ""

        val notificationData = mapOf(
            "title" to username,
            "body" to message,
            "userId" to currentUser?.uid
        )

        val apiService = Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApiService::class.java)

        val notification = FcmNotification(
            to = fcmToken,
            data = notificationData
        )

        apiService.sendNotification(notification).enqueue(object : Callback<FcmResponse> {
            override fun onResponse(call: Call<FcmResponse>, response: Response<FcmResponse>) {
                // Notification sent successfully
            }

            override fun onFailure(call: Call<FcmResponse>, t: Throwable) {
                // Failed to send notification
            }
        })
    }
}