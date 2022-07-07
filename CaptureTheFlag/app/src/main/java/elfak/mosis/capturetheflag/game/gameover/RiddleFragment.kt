package elfak.mosis.capturetheflag.game.gameover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentGameCodeBinding
import elfak.mosis.capturetheflag.databinding.FragmentRiddleBinding

class RiddleFragment : Fragment() {
    private var _binding: FragmentRiddleBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRiddleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestText"){ requestText, bundle ->
            binding.TextViewMessage.text = bundle.getString("bundleText")

        }
    }

}