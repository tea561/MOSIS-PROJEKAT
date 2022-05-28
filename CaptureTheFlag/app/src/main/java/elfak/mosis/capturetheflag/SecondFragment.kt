package elfak.mosis.capturetheflag

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
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import elfak.mosis.capturetheflag.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val inputUsername: EditText = requireView().findViewById<TextInputEditText>(R.id.username)
        val inputPassword: EditText = requireView().findViewById<TextInputEditText>(R.id.password)
        val inputFirstName: EditText = requireView().findViewById<TextInputEditText>(R.id.firstname)
        val inputLastName: EditText = requireView().findViewById<TextInputEditText>(R.id.lastname)

        val buttonSignup : Button = requireView().findViewById<Button>(R.id.buttonSignup)
        buttonSignup.isEnabled = false

        inputUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        inputUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        inputPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        inputFirstName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        inputLastName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        buttonSignup.setOnClickListener{
            Toast.makeText(this.context, "Signup clicked.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}