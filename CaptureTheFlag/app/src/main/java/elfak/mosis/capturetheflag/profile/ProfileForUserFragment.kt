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
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputEditText
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.model.UserViewModel
import java.lang.Exception
import java.util.concurrent.Executors

class ProfileForUserFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_for_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageProfile: ImageView = requireView().findViewById<ImageView>(R.id.imageProfile)
        val username: TextView = requireView().findViewById<TextView>(R.id.textViewUsername)
        val firstAndLastName: TextView = requireView().findViewById<TextView>(R.id.textViewFirstAndLastName)
        val aboutMeDesc: EditText = requireView().findViewById<TextInputEditText>(R.id.textInputDesc)

        val img = userViewModel.image.value
        val url = userViewModel.selectedUser?.imgUrl

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


    }
}