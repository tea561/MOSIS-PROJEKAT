package elfak.mosis.capturetheflag.game.gameover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
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
        var imgID : String? = ""
        var gameID: String? = ""
        var team : String? = ""

        arguments?.let {

            if (it.getSerializable("messageBundle") != null) {
                val msg = it.getString("messageBundle")
                binding.TextViewMessage.text = msg
            }

            if(it.getSerializable("imgIDBundle") != null){
                imgID = it.getString("imgIDBundle")
            }

            if(it.getSerializable("gameIDBundle") != null){
                gameID = it.getString("gameIDBundle")
            }

            if(it.getSerializable("teamBundle") != null){
                team = it.getString("teamBundle")
            }
        }



        binding.TextViewMessage.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("imgID", imgID)
            bundle.putString("gameIDBundle", gameID)
            bundle.putString("teamBundle", team)

            findNavController().navigate(R.id.action_RiddleFragment_to_SolveRiddleFragment, bundle)
        }


    }

}