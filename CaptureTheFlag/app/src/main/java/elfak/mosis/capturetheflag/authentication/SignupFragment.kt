package elfak.mosis.capturetheflag.authentication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.databinding.FragmentSignupBinding
import elfak.mosis.capturetheflag.model.AuthState
import elfak.mosis.capturetheflag.model.UserViewModel

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SignupFragment : Fragment() {

    /*private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl("https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")*/

    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputsList: ArrayList<EditText> = ArrayList()
        val inputUsername: EditText = requireView().findViewById<TextInputEditText>(R.id.username)
        inputsList.add(inputUsername)
        val inputPassword: EditText = requireView().findViewById<TextInputEditText>(R.id.password)
        inputsList.add(inputPassword)
        val inputFirstName: EditText = requireView().findViewById<TextInputEditText>(R.id.firstname)
        inputsList.add(inputFirstName)
        val inputLastName: EditText = requireView().findViewById<TextInputEditText>(R.id.lastname)
        inputsList.add(inputLastName)
        val inputPhoneNum: EditText = requireView().findViewById<TextInputEditText>(R.id.phoneNum)
        inputsList.add(inputPhoneNum)

        val buttonSignup : Button = requireView().findViewById(R.id.buttonSignup)
        buttonSignup.isEnabled = false


        inputUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enableSignupButton(buttonSignup, inputsList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        inputPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enableSignupButton(buttonSignup, inputsList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        inputFirstName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enableSignupButton(buttonSignup, inputsList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        inputLastName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enableSignupButton(buttonSignup, inputsList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        inputPhoneNum.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                enableSignupButton(buttonSignup, inputsList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        binding.buttonSignup.setOnClickListener{
            val username: String = inputUsername.text.toString()
            val password: String = inputPassword.text.toString()
            val firstName: String = inputFirstName.text.toString()
            val lastName: String = inputLastName.text.toString()
            val phoneNum: String = inputPhoneNum.text.toString()

            val user = User(firstName, lastName, phoneNum, "", "", username, "")

            userViewModel.signupUser(user, password)
        }

        binding.buttonLogin.setOnClickListener {
            findNavController().navigate(R.id.action_SignupFragment_to_LoginFragment)
        }

        val authStateObserver = Observer<AuthState> { state ->
            if (state == AuthState.Success) {
                Toast.makeText(view.context, "User registered successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_SignupFragment_to_Intro1Fragment)
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

    private fun enableSignupButton(buttonSignup: Button, inputs: List<EditText>) {
        buttonSignup.isEnabled = inputs.fold(true){acc: Boolean, element -> acc && element.text!!.isNotEmpty()}
    }
}