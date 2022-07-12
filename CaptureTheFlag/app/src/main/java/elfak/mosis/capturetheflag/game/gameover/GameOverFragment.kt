package elfak.mosis.capturetheflag.game.gameover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentGameOverBinding
import elfak.mosis.capturetheflag.databinding.FragmentRiddleBinding

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

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

        setFragmentResultListener("requestTitle"){ _, bundle ->
            binding.TextViewTittle.text = bundle.getString("bundleTitle")
        }

        binding.TextViewTittle.setOnClickListener {

            findNavController().navigate(R.id.action_GameOverFragment_to_MapFragment)
        }
    }
}