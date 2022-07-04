package elfak.mosis.capturetheflag.game.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentJoinGameBinding
import elfak.mosis.capturetheflag.game.viewmodel.FindGameState
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel

class JoinGameFragment : Fragment() {

    private var _binding: FragmentJoinGameBinding? = null
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
        _binding = FragmentJoinGameBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonJoinGame.setOnClickListener{
            val gameCode = binding.gameCode.text.toString()
            if(gameCode.length != 6){
                Toast.makeText(requireContext(), "Game code must be 6 characters", Toast.LENGTH_SHORT).show()
            }
            else{
                gameViewModel.getGame(gameCode)
            }
        }

        val findGameObserver = Observer<FindGameState> {state ->
            if(state is FindGameState.Success){
                findNavController().navigate(R.id.action_JoinGameFragment_to_ChooseTeamFragment)
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
            if(state is FindGameState.FindGameError){
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        gameViewModel.findGameState.observe(viewLifecycleOwner, findGameObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gameViewModel.findGameState.removeObservers(viewLifecycleOwner)
    }

}