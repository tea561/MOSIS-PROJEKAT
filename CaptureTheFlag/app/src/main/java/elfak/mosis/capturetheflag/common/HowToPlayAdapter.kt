package elfak.mosis.capturetheflag.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import elfak.mosis.capturetheflag.R
import org.w3c.dom.Text

class HowToPlayAdapter(private val context: Context, private val arrayDescriptions: java.util.ArrayList<String>,
private val arrayIcons: java.util.ArrayList<Int>, private val arrayIconTints: java.util.ArrayList<Int>) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return arrayDescriptions.size
    }

    override fun getItem(p0: Int): Any {
        return arrayIcons[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val itemView = inflater.inflate(R.layout.how_to_play_item, p2, false)
        val description: TextView = itemView.findViewById(R.id.textViewHTPDescription)
        val iconButton: MaterialButton = itemView.findViewById(R.id.buttonHTPIcon)

        description.text = arrayDescriptions[p0]
        iconButton.setIconResource(arrayIcons[p0])
        iconButton.setIconTintResource(arrayIconTints[p0])

        return itemView

    }
}