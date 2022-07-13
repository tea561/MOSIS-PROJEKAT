package elfak.mosis.capturetheflag.game.gameover

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentGameOverBinding
import elfak.mosis.capturetheflag.databinding.FragmentRiddleBinding
import elfak.mosis.capturetheflag.game.map.MapState
import elfak.mosis.capturetheflag.game.map.MapViewModel
import elfak.mosis.capturetheflag.game.map.MapViewModelFactory

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapViewModel: MapViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var userID = ""



        setFragmentResultListener("requestTitle"){ _, bundle ->
            binding.TextViewTittle.text = bundle.getString("bundleTitle")
            userID = bundle.getString("userID") ?: ""
            mapViewModel = ViewModelProvider(requireActivity(),
                MapViewModelFactory(requireActivity().application, userID)
            ).get(MapViewModel::class.java)
        }

        object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }
            override fun onFinish() {
                //mapViewModel.setMapState(MapState.Idle)
                findNavController().navigate(R.id.action_GameOverFragment_to_MapFragment)
            }
        }.start()

//        binding.TextViewTittle.setOnClickListener {
//            findNavController().navigate(R.id.action_GameOverFragment_to_MapFragment)
//        }
    }
}