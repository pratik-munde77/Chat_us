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
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()

        binding.btnEditProfile.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun loadProfile() {
        FirestoreUtil.getCurrentUser { user ->
            if (user != null) {
                binding.tvUsername.text = user.username
                binding.tvEmail.text = user.email

                if (user.imageUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(user.imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(binding.ivProfile)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            uri?.let {
                binding.ivProfile.setImageURI(uri)
                uploadImage(uri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        FirestoreUtil.uploadProfileImage(imageUri) { imageUrl ->
            if (imageUrl != null) {
                FirestoreUtil.updateUserProfileImage(imageUrl) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}