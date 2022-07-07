package elfak.mosis.capturetheflag.friends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.model.FriendsViewModel


/**
 * A fragment representing a list of Items.
 */
class FriendsFragment : Fragment() {

    private var columnCount = 1
    private val friendsViewModel: FriendsViewModel by activityViewModels()

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
        val view = inflater.inflate(elfak.mosis.capturetheflag.R.layout.fragment_item_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                val friendsAdapter = MyFriendsRecyclerViewAdapter({user -> openFriendProfile(user)}, emptyList())
                adapter = friendsAdapter
                friendsViewModel.friends.observe(viewLifecycleOwner) { newData ->
                    friendsAdapter.setData(newData)
                    Log.i("Friends observer", newData.toString())

                }

            }
        }
        return view
    }

    private fun openFriendProfile(user: User){
        setFragmentResult("requestFriend", bundleOf("bundleFriend" to user.uid))
        findNavController().navigate(R.id.action_FriendsFragment_to_ProfileFragment)
        Log.i("CLICK ON FRIEND", user.username ?: "empty")
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            FriendsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}