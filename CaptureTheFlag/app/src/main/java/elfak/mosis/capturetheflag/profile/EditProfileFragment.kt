package elfak.mosis.capturetheflag.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentEditProfileBinding
import elfak.mosis.capturetheflag.databinding.FragmentIntro3Binding
import elfak.mosis.capturetheflag.model.UserViewModel
import java.lang.Exception
import java.util.concurrent.Executors


class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
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
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageProfile: ImageView = requireView().findViewById<ImageView>(R.id.imageViewProfile)
        val firstNameInput: EditText = requireView().findViewById(R.id.first)
        val lastNameInput: EditText = requireView().findViewById(R.id.last)
        val aboutMeInput: EditText = requireView().findViewById(R.id.aboutme)


        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap? = null

        executor.execute{
            val imageUrl = userViewModel.selectedUser?.imgUrl
            try {
                val `in` = java.net.URL(imageUrl).openStream()
                image = BitmapFactory.decodeStream(`in`)

                handler.post{
                    imageProfile.setImageBitmap(image)
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
        }


        firstNameInput.setText(userViewModel.selectedUser?.firstName)
        lastNameInput.setText(userViewModel.selectedUser?.lastName)
        aboutMeInput.setText(userViewModel.selectedUser?.desc)

        binding.buttonSave.setOnClickListener {
            val firstName: String = firstNameInput.text.toString()
            val lastName: String = lastNameInput.text.toString()
            val aboutMe: String = aboutMeInput.text.toString()

            if(firstName != userViewModel.selectedUser?.firstName)
            {
                userViewModel.updateUserData("firstName", firstName)
            }
            if(lastName != userViewModel.selectedUser?.lastName)
            {
                userViewModel.updateUserData("lastName", lastName)
            }
            if(aboutMe != userViewModel.selectedUser?.desc)
            {
                userViewModel.updateUserData("desc", aboutMe)
            }

            Toast.makeText(view.context, "User updated!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonCancel.setOnClickListener{
            findNavController().navigate(R.id.action_EditProfileFragment_to_ProfileForUserFragment)
        }
    }

}