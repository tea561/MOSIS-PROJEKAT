package elfak.mosis.capturetheflag.game.map

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import elfak.mosis.capturetheflag.R

class MapFiltersDialog : DialogFragment() {

    private val markerViewModel: MarkerViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val items =  resources.getStringArray(R.array.map_filters)
            val selectedItems = markerViewModel.filters.value
            val isCheckedArray = selectedItems!!.values.toBooleanArray()
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Set Map Filters")
                .setMultiChoiceItems(R.array.map_filters, isCheckedArray
                ) { _, which, isChecked ->
                    if (isChecked) {
                        selectedItems[items[which]] = true
                    } else if (selectedItems[items[which]] == true) {
                        selectedItems[items[which]] = false
                    }
                }
                .setPositiveButton(R.string.btn_save
                ) { dialog, _ ->
                    markerViewModel.setFilters(selectedItems)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel
                ) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}