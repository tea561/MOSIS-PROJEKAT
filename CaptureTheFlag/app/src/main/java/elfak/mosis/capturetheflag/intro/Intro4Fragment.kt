package elfak.mosis.capturetheflag.intro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentIntro4Binding
import elfak.mosis.capturetheflag.model.UserViewModel
import java.lang.Exception
import java.util.concurrent.Executors


class Intro4Fragment : Fragment() {

    private var _binding: FragmentIntro4Binding? = null

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
        _binding = FragmentIntro4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonGetStarted: Button = requireView().findViewById(R.id.buttonGetStarted)
        buttonGetStarted.setOnClickListener {
            val navView: NavigationView? = activity?.findViewById(R.id.nav_view) ?: null

            if(navView != null) {
                val headerView: View = navView.getHeaderView(0)
                val usernameHeader: TextView = headerView.findViewById(R.id.textViewUsername)
                val nameHeader: TextView = headerView.findViewById(R.id.textViewName)
                val headerImgProfile: ImageView = headerView.findViewById(R.id.imageViewProfile)
                usernameHeader.text = userViewModel.selectedUser!!.username
                nameHeader.text = "${userViewModel.selectedUser!!.firstName} ${userViewModel.selectedUser!!.lastName}"

                val executor = Executors.newSingleThreadExecutor()
                val handler = Handler(Looper.getMainLooper())
                var image: Bitmap? = null

                executor.execute{
                    val imageUrl = userViewModel.selectedUser?.imgUrl
                    try {
                        val `in` = java.net.URL(imageUrl).openStream()
                        image = BitmapFactory.decodeStream(`in`)

                        handler.post{
                            headerImgProfile.setImageBitmap(image)
                        }
                    }
                    catch(e: Exception){
                        e.printStackTrace()
                    }
                }
            }
            findNavController().navigate(R.id.action_Intro4Fragment_to_MapFragment)
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


}