package elfak.mosis.capturetheflag.game.gameover

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.game.gameover.placeholder.PlaceholderContent
import elfak.mosis.capturetheflag.model.FriendsViewModel
import elfak.mosis.capturetheflag.model.UserViewModel

/**
 * A fragment representing a list of Items.
 */
class RankingsFragment : Fragment() {

    private var columnCount = 1
    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rankings_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewRankings = view.findViewById<RecyclerView>(R.id.list)

        friendsViewModel.friends.observe(viewLifecycleOwner){newData ->
            if(newData.size < 1)
            {
                val noFriends: LinearLayout = view.findViewById(R.id.linearLayoutEmptyList)
                noFriends.visibility = View.VISIBLE
            }
            else{
                val noFriends: LinearLayout = view.findViewById(R.id.linearLayoutEmptyList)
                noFriends.visibility = View.GONE
                val rankingsAdapter = MyRankRecyclerViewAdapter({user -> openFriendProfile(user)}, emptyList())
                recyclerViewRankings.adapter = rankingsAdapter
                userViewModel.selectedUser?.let { newData.add(it) }
                rankingsAdapter.setData(newData)
                Log.i("Rankings observer", newData.toString())
            }
        }

        recyclerViewRankings.layoutManager = LinearLayoutManager(requireContext())


        val buttonFindFriends : Button = view.findViewById(R.id.buttonNoFriends)
        buttonFindFriends.setOnClickListener {
            findNavController().navigate(R.id.action_RankingsFragment_to_BTFragment)
        }
    }

    private fun openFriendProfile(user: User){
        setFragmentResult("requestFriend", bundleOf("bundleFriend" to user.uid))
        findNavController().navigate(R.id.action_RankingsFragment_to_ProfileFragment)
        Log.i("CLICK ON FRIEND", user.username ?: "empty")
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            RankingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}