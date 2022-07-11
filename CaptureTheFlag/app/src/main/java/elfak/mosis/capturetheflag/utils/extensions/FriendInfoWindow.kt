
package elfak.mosis.capturetheflag.utils.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.opengl.Visibility
import android.os.Handler
import android.os.Looper
import elfak.mosis.capturetheflag.R
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.game.map.MapFragment
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.lang.Exception
import java.util.concurrent.Executors


class FriendInfoWindow(
    layoutResId: Int,
    mapView: MapView,
    private val fragment: MapFragment,
    private val friend: User): MarkerInfoWindow(layoutResId, mapView) {

    override fun onOpen(item: Any?) {
        //val layout = mView.findViewById<View>(R.id.bubble_friends_layout) as LinearLayout
        val btnMoreInfo: Button = mView.findViewById<View>(R.id.bubble_moreinfo) as Button

        val imageView: ImageView = mView.findViewById(R.id.bubble_image) as ImageView

        val txtTitle = mView.findViewById<View>(R.id.bubble_title) as TextView
        val txtDescription = mView.findViewById<View>(R.id.bubble_description) as TextView
        val txtSubdescription = mView.findViewById<View>(R.id.bubble_subdescription) as TextView
        txtTitle.text = friend.username
        txtDescription.text = friend.firstName + " " + friend.lastName

        val executor = Executors.newSingleThreadExecutor()
        var image: Bitmap? = null
        executor.execute{
            val imageUrl = friend.imgUrl
            try {
                val `in` = java.net.URL(imageUrl).openStream()
                image = BitmapFactory.decodeStream(`in`)
                val handler = Handler(Looper.getMainLooper())
                handler.post{
                    imageView.setImageBitmap(image)
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
        }


        btnMoreInfo.visibility = View.VISIBLE
        btnMoreInfo.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                fragment.openFriendProfile(friend.uid)
            }
        })
    }

    override fun onClose() {

    }
}
