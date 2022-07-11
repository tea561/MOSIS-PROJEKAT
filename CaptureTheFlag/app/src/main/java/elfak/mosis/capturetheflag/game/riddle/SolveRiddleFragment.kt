package elfak.mosis.capturetheflag.game.riddle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentSetRiddleBinding
import elfak.mosis.capturetheflag.databinding.FragmentSolveRiddleBinding
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import java.lang.Exception
import java.util.concurrent.Executors

class SolveRiddleFragment : Fragment() {

    private val gameViewModel: GameViewModel by activityViewModels()
    private var _binding: FragmentSolveRiddleBinding? = null
    private val binding get() = _binding!!

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

        val imageView = binding.imageViewRiddle

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap? = null

        executor.execute{
            //TODO: get image
            val imageUrl = ""
            try {
                val `in` = java.net.URL(imageUrl).openStream()
                image = BitmapFactory.decodeStream(`in`)

                handler.post{
                    imageView.setImageBitmap(image)
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
        }

        val answerInput: EditText = binding.textInputAnswer

        binding.buttonSolveRiddleAccept.setOnClickListener {
            val answer: String = answerInput.text.toString()
            //TODO: check if answer is correct
        }

        binding.buttonSolveRiddleCancel.setOnClickListener {
            //TODO
        }
    }
}