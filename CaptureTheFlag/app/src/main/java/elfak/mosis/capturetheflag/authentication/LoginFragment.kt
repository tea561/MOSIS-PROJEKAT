package elfak.mosis.capturetheflag.authentication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.databinding.FragmentLoginBinding
import elfak.mosis.capturetheflag.model.AuthState
import elfak.mosis.capturetheflag.model.UserViewModel


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl("https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")
    private var _binding: FragmentLoginBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createAccButton.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_SignupFragment)
        }

        val inputUsername: EditText = requireView().findViewById<TextInputEditText>(R.id.username)
        val inputPassword: EditText = requireView().findViewById<TextInputEditText>(R.id.password)
        val loginButton: Button = requireView().findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val username: String = inputUsername.text.toString()
            val password: String = inputPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(
                    this.context,
                    "Please enter username and password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                userViewModel.loginUser(username, password)
            }
        }

        val authStateObserver = Observer<AuthState> { state ->
            if (state == AuthState.Success) {
                Toast.makeText(view.context, "User logged in! Welcome, ${userViewModel.selectedUser!!.username}", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_LoginFragment_to_MapFragment)
            }
            if (state is AuthState.AuthError) {
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
        }
        userViewModel.authState.observe(viewLifecycleOwner, authStateObserver)
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.authState.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}