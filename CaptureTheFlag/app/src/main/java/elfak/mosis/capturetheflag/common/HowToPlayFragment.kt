package elfak.mosis.capturetheflag.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import elfak.mosis.capturetheflag.R

class HowToPlayFragment : Fragment() {

    private val arrayDescriptions: java.util.ArrayList<String> =
        arrayListOf("Teammate", "Enemy", "Team Barrier", "Enemy Barrier", "Team Flag", "Enemy Flag")

    private val arrayIcons: java.util.ArrayList<Int> =
        arrayListOf(R.drawable.ic_person_solid, R.drawable.ic_person_solid, R.drawable.ic_road_barrier_solid, R.drawable.ic_burst_solid, R.drawable.ic_location_crosshairs_solid, R.drawable.ic_location_crosshairs_solid)

    val arrayIconTints: java.util.ArrayList<Int> = arrayListOf(R.color.blue, R.color.red_enemy, R.color.blue, R.color.red_enemy, R.color.blue, R.color.red_enemy)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_how_to_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list: ListView = view.findViewById(R.id.how_to_play_list)
        val HTPAdapter = HowToPlayAdapter(view.context, arrayDescriptions, arrayIcons, arrayIconTints)
        list.adapter = HTPAdapter
    }

}