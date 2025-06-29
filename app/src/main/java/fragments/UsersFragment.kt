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
 * Use the [UsersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersAdapter = UsersAdapter { user ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("userId", user.id)
            startActivity(intent)
        }

        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = usersAdapter
        }

        loadUsers()
    }

    private fun loadUsers() {
        FirestoreUtil.getCurrentUser { currentUser ->
            if (currentUser != null) {
                FirestoreUtil.getAllUsers { users ->
                    val filteredUsers = users.filter { it.id != currentUser.id }
                    usersAdapter.submitList(filteredUsers)
                }
            }
        }
    }
}