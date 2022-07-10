package elfak.mosis.capturetheflag.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.textfield.TextInputEditText
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.model.FriendsViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import java.lang.Exception
import java.util.concurrent.Executors

class ProfileFragment : Fragment() {

    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageProfile: ImageView = requireView().findViewById<ImageView>(R.id.imageProfile)
        val username: TextView = requireView().findViewById<TextView>(R.id.textViewUsername)
        val firstAndLastName: TextView = requireView().findViewById<TextView>(R.id.textViewFirstAndLastName)
        val aboutMeDesc: EditText = requireView().findViewById<TextInputEditText>(R.id.textInputDesc)

        setFragmentResultListener("requestFriend") { _, bundle ->
            val result = bundle.get("bundleFriend")
            currentUser = friendsViewModel.friends.value?.find { friend -> friend.uid == result}

            Log.e("FRIEND", "User ${currentUser?.username} with id $result")
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            var image: Bitmap? = null

            executor.execute{
                val imageUrl = currentUser?.imgUrl
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


            username.text = currentUser?.username ?: ""
            firstAndLastName.text = "${currentUser?.firstName} ${currentUser?.lastName}"
            aboutMeDesc.setText(currentUser?.desc)


        }

    }

}