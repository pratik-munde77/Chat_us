package fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chat_us.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatsAdapter = ChatsAdapter { chat ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("userId", chat.receiverId)
            startActivity(intent)
        }

        binding.recyclerViewChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatsAdapter
        }

        loadChats()
    }

    private fun loadChats() {
        FirestoreUtil.getCurrentUser { currentUser ->
            if (currentUser != null) {
                FirestoreUtil.getAllUsers { users ->
                    val filteredUsers = users.filter { it.id != currentUser.id }
                    chatsAdapter.submitList(filteredUsers)
                }
            }
        }
    }
}