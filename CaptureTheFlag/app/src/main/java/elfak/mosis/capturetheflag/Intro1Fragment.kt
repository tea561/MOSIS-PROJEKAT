package elfak.mosis.capturetheflag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.databinding.FragmentFirstBinding

import elfak.mosis.capturetheflag.databinding.FragmentIntro1Binding


class Intro1Fragment : Fragment() {

    private var _binding: FragmentIntro1Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIntro1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonNext.setOnClickListener{
            findNavController().navigate(R.id.action_Intro1Fragment_to_Intro2Fragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}