package elfak.mosis.capturetheflag.game.setup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentCreateGameBinding
import elfak.mosis.capturetheflag.databinding.FragmentGameCodeBinding

class GameCodeFragment : Fragment() {

    private var _binding: FragmentGameCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestCode") { requestCode, bundle ->
            binding.textViewGameCode.text = bundle.getString("bundleCode")
        }

        binding.buttonStartTheGame.setOnClickListener {
            findNavController().navigate(R.id.action_GameCodeFragment_to_ChooseTeamFragment)
        }

    }



}