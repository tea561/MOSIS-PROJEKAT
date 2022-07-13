package elfak.mosis.capturetheflag.game.riddle

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.MapObject
import elfak.mosis.capturetheflag.databinding.FragmentSetRiddleBinding
import elfak.mosis.capturetheflag.databinding.FragmentSolveRiddleBinding
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import elfak.mosis.capturetheflag.utils.extensions.FirebaseLocation
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.gameID
import java.lang.Exception

import java.util.concurrent.Executors

class SolveRiddleFragment : Fragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    val userViewModel: UserViewModel by activityViewModels()
    private var _binding: FragmentSolveRiddleBinding? = null
    private val binding get() = _binding!!

    private var correctAnswer = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSolveRiddleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var imgID : String? = ""
        var gameID: String? = ""
        var team : String? = ""
        arguments?.let {
            if(it.getSerializable("imgID") != null){
                imgID = it.getString("imgID")
            }
            if(it.getSerializable("gameIDBundle") != null){
                gameID = it.getString("gameIDBundle")
            }
            if(it.getSerializable("teamBundle") != null){
                team = it.getString("teamBundle")
            }
            if (!(imgID.isNullOrEmpty() || gameID.isNullOrEmpty() || team.isNullOrEmpty())) {
                gameViewModel.getRiddlePhotoAndAnswer(imgID!!, team!!, gameID!!)
            }
        }

        val imageView = binding.imageViewRiddle

        gameViewModel.triggeredRiddleImage.observe(viewLifecycleOwner) { image ->
            if (image != null) {
                imageView.setImageBitmap(image)
            }
        }
        var correctAnswerCopy = correctAnswer
        gameViewModel.triggeredRiddleAnswer.observe(viewLifecycleOwner) { answer ->
            if (answer != null) {
                correctAnswer = answer
                correctAnswerCopy = answer
            }
        }
        val answerInput: EditText = binding.textInputAnswer


        binding.buttonSolveRiddleAccept.setOnClickListener {
            val answer: String = answerInput.text.toString()

            if (answer.lowercase() == correctAnswerCopy.lowercase()) {
                if (gameViewModel.riddleType == MapFilters.TeamFlag.value) {
                    gameViewModel.setWinner()
                    val prefs = context?.let { PreferenceHelper.customPreference(it, "User_data") }
                    prefs?.gameID = ""
                    userViewModel.updateUserRank(1000, userViewModel.selectedUser!!.uid)
                }
                else if (gameViewModel.riddleType == MapFilters.TeamBarriers.value) {
                    userViewModel.updateUserRank(500, userViewModel.selectedUser!!.uid)
                    imgID?.let { it1 -> team?.let { it2 ->
                        gameID?.let { it3 ->
                            gameViewModel.deleteRiddleFromDB(it1,
                                it2, it3
                            )
                        }
                    } }
                }
            }
            else {
                userViewModel.updateUserRank(-500, userViewModel.selectedUser!!.uid)
            }

            findNavController().navigate(R.id.action_SolveRiddleFragment_to_MapFragment)
        }

        binding.buttonSolveRiddleCancel.setOnClickListener {
            userViewModel.updateUserRank(-500, userViewModel.selectedUser!!.uid)
            findNavController().navigate(R.id.action_SolveRiddleFragment_to_MapFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameViewModel.triggeredRiddleImage.removeObservers(viewLifecycleOwner)
        gameViewModel.triggeredRiddleAnswer.removeObservers(viewLifecycleOwner)
    }
}