package elfak.mosis.capturetheflag.intro

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.model.AuthState
import elfak.mosis.capturetheflag.model.StoreUploadState
import elfak.mosis.capturetheflag.model.UserViewModel

class Intro2ImageFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro2_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val image = requireView().findViewById<ImageView>(R.id.profileImageView)
        image.setImageBitmap(userViewModel.image.value)

        val yesButton: Button = requireView().findViewById(R.id.btnYes)
        val noButton: Button = requireView().findViewById(R.id.btnNo)

        yesButton.setOnClickListener {
            handleYes()
        }
        noButton.setOnClickListener {
            handleNo()
        }

        val uploadStateObserver = Observer<StoreUploadState> { state ->
            if (state is StoreUploadState.Success) {
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_Intro2ImageFragment_to_Intro3Fragment)
            }
            if (state is StoreUploadState.UploadError) {
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
        }
        userViewModel.uploadState.observe(viewLifecycleOwner, uploadStateObserver)
    }

    private fun handleYes() {
        userViewModel.uploadProfilePhoto()
    }

    private fun handleNo() {
        userViewModel.setImage(BitmapFactory.decodeResource(requireContext().resources,
            R.drawable.intro2))
        findNavController().navigate(R.id.action_Intro2ImageFragment_to_Intro2Fragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userViewModel.uploadState.removeObservers(viewLifecycleOwner)
    }
}