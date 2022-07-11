package elfak.mosis.capturetheflag.utils.extensions

import android.view.MotionEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.game.map.MapFragment
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class FriendMarker(private val mapView: MapView, private val user: User, private val fragment: MapFragment): Marker(mapView) {

    /*override fun onLongPress(event: MotionEvent?, mapView: MapView?): Boolean {
        fragment.openFriendProfile(user)
        return super.onLongPress(event, mapView)
    }

    override fun onDoubleTap(e: MotionEvent?, mapView: MapView?): Boolean {
        fragment.openFriendProfile(user)
        return true
    }*/
}