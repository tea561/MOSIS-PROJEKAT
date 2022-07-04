package elfak.mosis.capturetheflag.game.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentChooseTeamBinding
import elfak.mosis.capturetheflag.game.viewmodel.FindGameState
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.UserViewModel

class ChooseTeamFragment : Fragment() {

    private var _binding: FragmentChooseTeamBinding? = null
    private val binding get() = _binding!!

    private val gameViewModel: GameViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChooseTeamBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonTeam1.text = gameViewModel.team1name
        binding.buttonTeam2.text = gameViewModel.team2name

        binding.buttonTeam1.setOnClickListener{
            userViewModel.selectedUser?.let { it1 -> gameViewModel.addPlayerToGame(it1.uid, 1) }
        }

        binding.buttonTeam2.setOnClickListener{
            userViewModel.selectedUser?.let { it2 -> gameViewModel.addPlayerToGame(it2.uid, 2)}
        }


        val joinGameObserver = Observer<FindGameState> { state ->
            if(state is FindGameState.Success){
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
            if(state is FindGameState.FindGameError){
                Toast.makeText(view.context, state.message, Toast.LENGTH_SHORT).show()
            }
        }

        gameViewModel.joinGameState.observe(viewLifecycleOwner, joinGameObserver)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        gameViewModel.findGameState.removeObservers(viewLifecycleOwner)
    }

}