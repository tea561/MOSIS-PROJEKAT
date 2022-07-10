package elfak.mosis.capturetheflag.game.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import elfak.mosis.capturetheflag.game.map.MapState
import elfak.mosis.capturetheflag.game.map.MapViewModel

class WaitForFlagDialog: DialogFragment() {
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Team Flag")
                .setMessage("Before starting the game, " +
                        "the first player to enter the team needs to set the team flag. " +
                        "That player is not you. Wait for your captain to set the flag!")
                .setPositiveButton(
                    "OK"
                ) { dialog, _ ->
                    mapViewModel.setMapState(MapState.WaitingForFlags)
                    dialog.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}