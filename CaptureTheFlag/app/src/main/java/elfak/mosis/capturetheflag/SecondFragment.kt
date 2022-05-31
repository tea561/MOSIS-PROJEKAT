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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl("https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")

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
        val inputPhoneNum: EditText = requireView().findViewById<TextInputEditText>(R.id.phoneNum)

        val buttonSignup : Button = requireView().findViewById<Button>(R.id.buttonSignup)
        buttonSignup.isEnabled = false
        
        inputUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
                        && (inputPhoneNum.text.length > 0)
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
                        && (inputPhoneNum.text.length > 0)
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
                        && (inputPhoneNum.text.length > 0)
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
                        && (inputPhoneNum.text.length > 0)
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        inputPhoneNum.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                buttonSignup.isEnabled = (inputUsername.text.length > 0)
                        && (inputPassword.text.length > 0)
                        && (inputFirstName.text.length > 0)
                        && (inputLastName.text.length > 0)
                        && (inputPhoneNum.text.length > 0)
            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        buttonSignup.setOnClickListener{
            val username: String = inputUsername.text.toString()
            val password: String = inputPassword.text.toString()
            val firstName: String = inputFirstName.text.toString()
            val lastName: String = inputLastName.text.toString()
            val phoneNum: String = inputPhoneNum.text.toString()

            //Toast.makeText(this.context, "Signup clicked. $username $password $firstName $lastName $phoneNum", Toast.LENGTH_SHORT).show()

            dbRef.child("users").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(phoneNum)){
                        Toast.makeText(view.context, "Phone is already registered", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        dbRef.child("users").child(phoneNum).child("firstName").setValue(firstName)
                        dbRef.child("users").child(phoneNum).child("lastName").setValue(lastName)
                        dbRef.child("users").child(phoneNum).child("username").setValue(username)
                        dbRef.child("users").child(phoneNum).child("password").setValue(password)

                        Toast.makeText(view.context, "User registered successfully", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}