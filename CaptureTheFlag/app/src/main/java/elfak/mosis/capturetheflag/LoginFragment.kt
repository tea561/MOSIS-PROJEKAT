package elfak.mosis.capturetheflag

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.databinding.FragmentLoginBinding
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
            val phoneNum: String = inputUsername.text.toString()
            val password: String = inputPassword.text.toString()

            if (phoneNum.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this.context,
                    "Please enter username and password",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                dbRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        //TODO("Not yet implemented")
                        Log.d("Login", "DB Login Error - onCancelled")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(phoneNum)) {
                            val dbPassword = snapshot.child(phoneNum).child("password")
                                .getValue(String::class.java)
                            if (dbPassword.equals(password)) {
                                Toast.makeText(
                                    view.context,
                                    "Successfully logged in",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val currentUser = snapshot.child(phoneNum).getValue(User::class.java)
                                currentUser?.phoneNum = phoneNum
                                Toast.makeText(
                                    view.context,
                                    "Successfully logged in",
                                    Toast.LENGTH_SHORT
                                ).show()
                                userViewModel.selectedUser = currentUser

                            } else {
                                Toast.makeText(view.context, "Wrong password", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(view.context, "User not found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
            }
        }

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
        _binding = null
    }
}