package elfak.mosis.capturetheflag.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.activityViewModels
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentChangePasswordBinding
import elfak.mosis.capturetheflag.databinding.FragmentEditProfileBinding
import elfak.mosis.capturetheflag.model.UserViewModel

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputOldPassword: EditText = requireView().findViewById(R.id.oldPassword)
        val inputNewPassword: EditText = requireView().findViewById(R.id.newPassword)
        val inputConfirmPassword: EditText = requireView().findViewById(R.id.confirmPassword)

        binding.buttonSave.setOnClickListener {
            val oldPass: String = inputOldPassword.text.toString()
            val newPass: String = inputNewPassword.text.toString()
            val confirmPass: String = inputConfirmPassword.text.toString()

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty())
                Toast.makeText(view.context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            else{
                if (newPass != confirmPass){
                    Toast.makeText(view.context, "Passwords don't match.", Toast.LENGTH_SHORT).show()
                }
                else{

                    val email: String = "${userViewModel.selectedUser?.username}@capturetheflag.mosis"
                    val user = FirebaseAuth.getInstance().currentUser
                    val credential: AuthCredential = EmailAuthProvider.getCredential(email, oldPass)

                    var returnMessage: String = ""
                    user?.reauthenticate(credential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            user.updatePassword(newPass)
                                .addOnCompleteListener {
                                    if(!task.isSuccessful){
                                        Toast.makeText(view.context, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        Toast.makeText(view.context, "Password successfully changed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(view.context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }


                }
            }
        }
    }
}