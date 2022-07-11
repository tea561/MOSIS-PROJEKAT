package elfak.mosis.capturetheflag.game.riddle

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentIntro2Binding
import elfak.mosis.capturetheflag.databinding.FragmentSetRiddleBinding
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.StoreUploadState

private const val REQUEST_IMAGE_CAPTURE = 1

class SetRiddleFragment : Fragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private var _binding: FragmentSetRiddleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetRiddleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val answerInput: EditText = binding.textInputAnswer
        var answer: String? = ""

        binding.buttonUploadRiddlePhoto.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                activity?.startActivityFromFragment(this, cameraIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
            }
        }

        val uploadStateObserver = Observer<StoreUploadState> { state ->
            if(state is StoreUploadState.Success){
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
                gameViewModel.putGameObjectToDB(state.message, answer!!)
                gameViewModel.resetObjectInfo()
                findNavController().navigate(R.id.action_SetRiddleFragment_to_MapFragment)
            }
            if (state is StoreUploadState.UploadError) {
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        gameViewModel.uploadState.observe(viewLifecycleOwner, uploadStateObserver)

        binding.buttonRiddleAccept.setOnClickListener {
            answer = answerInput.text.toString()
            if(answer.isNullOrEmpty() || gameViewModel.image == null){
                Toast.makeText(view.context, "You need to set riddle image and answer", Toast.LENGTH_SHORT).show()
            }
            else{
                gameViewModel.uploadRiddlePhoto()
            }
        }

        binding.buttonRiddleCancel.setOnClickListener {
            gameViewModel.resetObjectInfo()
            findNavController().navigate(R.id.action_SetRiddleFragment_to_MapFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val image = data?.extras?.get("data") as Bitmap
            gameViewModel.image = image
            binding.imageView6.setImageBitmap(image)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameViewModel.uploadState.removeObservers(viewLifecycleOwner)
    }

}