package elfak.mosis.capturetheflag.game.gameover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.databinding.FragmentRankingsBinding
import elfak.mosis.capturetheflag.game.gameover.placeholder.PlaceholderContent.PlaceholderItem
import org.w3c.dom.Text
import java.lang.Exception
import java.util.concurrent.Executors


/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyRankRecyclerViewAdapter(private val onClick: (User) -> Unit,
    private var values: List<User> = emptyList()
) : RecyclerView.Adapter<MyRankRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentRankingsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onClick
        )

    }

    fun setData(newData:List<User>){
        //TODO: sortiranje po ranku
        newData.sortedWith(Comparator{lhs, rhs -> if(lhs.rank > rhs.rank) -1 else 1} )
        values = newData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.currentFriend = item
        holder.idView.text = "${item.firstName} ${item.lastName}"
        holder.contentView.text = item.username
        holder.buttonRank.text = item.rank.toString()

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var image: Bitmap? = null

        executor.execute{
            val imageUrl = item.imgUrl
            try {
                val `in` = java.net.URL(imageUrl).openStream()
                image = BitmapFactory.decodeStream(`in`)

                handler.post{
                    holder.imageViewFriend.setImageBitmap(image)
                }
            }
            catch(e: Exception){
                e.printStackTrace()
            }
        }

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentRankingsBinding, val onClick: (User) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.friendName
        val contentView: TextView = binding.friendUsername
        val imageViewFriend: ImageView = binding.imageViewFriend
        val buttonRank: Button = binding.buttonRank
        var currentFriend: User? = null

        init {
            binding.root.setOnClickListener { currentFriend?.let { onClick(it) } }
        }

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}