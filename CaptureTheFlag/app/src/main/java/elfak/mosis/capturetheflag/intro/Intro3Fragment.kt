package elfak.mosis.capturetheflag.intro

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.databinding.FragmentIntro3Binding
import elfak.mosis.capturetheflag.model.UserViewModel


class Intro3Fragment : Fragment() {
    private val database = Firebase.database
    private val dbRef = database.getReferenceFromUrl("https://capturetheflag-56f1c-default-rtdb.firebaseio.com/")
    private val userViewModel: UserViewModel by activityViewModels()

    private var _binding: FragmentIntro3Binding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentIntro3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editDesc: EditText = requireView().findViewById<TextInputEditText>(R.id.textInputDesc)
        binding.buttonNext3.setOnClickListener {
            val desc: String = editDesc.text.toString()

            val key = dbRef.child("users").push().key
            if (key == null) {
                Log.w(TAG, "Couldn't get push key for posts")
            }
            else {
                val id: String = userViewModel.selectedUser!!.uid
                dbRef.child("users").child(id).child("desc").setValue(desc)
                findNavController().navigate(R.id.action_Intro3Fragment_to_Intro4Fragment)
            }
        }

        binding.buttonSkip3.setOnClickListener {
            findNavController().navigate(R.id.action_Intro3Fragment_to_Intro4Fragment)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }
}