package com.example.chat_us

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(userList) { user ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("receiverId", user.id)
                putExtra("receiverName", user.username)
                putExtra("receiverImage", user.profileImage)
            }
            startActivity(intent)
        }

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(this@UserListActivity, DividerItemDecoration.VERTICAL))
        }
    }

    private fun loadUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.id != currentUserId) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserListActivity, "Failed to load users: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}