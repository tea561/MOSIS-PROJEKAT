package elfak.mosis.capturetheflag.game.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentCreateGameBinding
import elfak.mosis.capturetheflag.databinding.FragmentEditProfileBinding
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.UserViewModel

class CreateGameFragment : Fragment() {

    private var _binding: FragmentCreateGameBinding? = null
    private val binding get() = _binding!!

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val team1Input: EditText = requireView().findViewById(R.id.textInputTeam1)
        val team2Input: EditText = requireView().findViewById(R.id.textInputTeam2)

        binding.buttonStart.setOnClickListener{
            val team1: String = team1Input.text.toString()
            val team2: String = team2Input.text.toString()

            if(team1 == "" || team2 == "")
            {
                Toast.makeText(view.context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
            else {
                gameViewModel.createGame(team1, team2)
                //TODO: nav
            }
        }

    }

}