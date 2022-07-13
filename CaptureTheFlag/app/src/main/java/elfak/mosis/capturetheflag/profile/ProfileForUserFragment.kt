package elfak.mosis.capturetheflag.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentProfileForUserBinding
import elfak.mosis.capturetheflag.game.map.LocationService
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.gameID
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.locationEnabled
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.userId
import java.util.concurrent.Executors


class ProfileForUserFragment : Fragment() {

    private var _binding: FragmentProfileForUserBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val toolbar: androidx.appcompat.widget.Toolbar = activity?.findViewById(R.id.toolbar)!!
        //(activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileForUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageProfile: ImageView = requireView().findViewById<ImageView>(R.id.imageProfile)
        val username: TextView = requireView().findViewById<TextView>(R.id.textViewUsername)
        val firstAndLastName: TextView = requireView().findViewById<TextView>(R.id.textViewFirstAndLastName)
        val aboutMeDesc: EditText = requireView().findViewById<TextInputEditText>(R.id.textInputDesc)
        val switchLocation: Switch = requireView().findViewById(R.id.switchLocation)

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

        username.text = userViewModel.selectedUser?.username ?: ""
        firstAndLastName.text = "${userViewModel.selectedUser?.firstName} ${userViewModel.selectedUser?.lastName}"
        aboutMeDesc.setText(userViewModel.selectedUser?.desc)

        binding.buttonEditYourProfile.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileForUserFragment_to_EditProfileFragment)
        }

        binding.buttonChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileForUserFragment_to_ChangePasswordFragment)
        }
        val prefs = context?.let { PreferenceHelper.customPreference(it, "User_data") }
        switchLocation.isChecked = prefs?.locationEnabled == true
        switchLocation.setOnCheckedChangeListener { _, isChecked ->
            prefs?.locationEnabled = isChecked
        }

        binding.buttonLogOut.setOnClickListener {
            prefs?.userId = ""
            prefs?.gameID = ""
            userViewModel.logoutUser()
            gameViewModel.resetGame()
            requireActivity().stopService(Intent(context, LocationService().javaClass))
            findNavController().navigate(R.id.action_ProfileForUserFragment_to_LoginFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }
}