package elfak.mosis.capturetheflag.game.map

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.userId
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var map: MapView
    private val userViewModel: UserViewModel by activityViewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if(isGranted){
                setMyLocationOverlay()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val prefs = PreferenceHelper.customPreference(requireContext(), "User_data")
        prefs.userId = userViewModel.selectedUser!!.uid
        if(!checkLocationServiceRunning()) {
            val locationServiceIntent = Intent(this.context, LocationService().javaClass)
                .putExtra("userID", userViewModel.selectedUser!!.uid)
            requireActivity().startService(locationServiceIntent)
        }
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = requireView().findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            val btnBarrier = view.findViewById<Button>(R.id.btnBarrier)
            btnBarrier.setOnClickListener {
                openSetMarkerBottomSheet(dialog, "PlaceBarrier");
            }

            val btnEnemyBarrier = view.findViewById<Button>(R.id.btnEnemyBarrier)
            btnEnemyBarrier.setOnClickListener {
                openSetMarkerBottomSheet(dialog, "PlaceEnemyBarrier");
            }

            val btnEnemyFlag = view.findViewById<Button>(R.id.btnEnemyFlag)
            btnEnemyFlag.setOnClickListener {
                openSetMarkerBottomSheet(dialog, "PlaceEnemyFlag");
            }
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }




        val ctx: Context? = activity?.applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = requireView().findViewById(R.id.map)

        if (ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_DENIED
            && ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else {
            setMyLocationOverlay()
        }

        map.setMultiTouchControls(true)
        map.controller.setZoom(20.0)
        map.controller.setCenter(GeoPoint(43.3209, 21.8958))


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapViewModel = ViewModelProvider(this,
            MapViewModelFactory(requireActivity().application, userViewModel.selectedUser!!.uid)).get(MapViewModel::class.java)
        setHasOptionsMenu(true)


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()

    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationOverlay() {
        val locationProvider = GpsMyLocationProvider(activity)
        val myLocationOverlay = MyLocationNewOverlay(locationProvider, map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        map.overlays.add(myLocationOverlay)
    }

    private fun checkLocationServiceRunning(): Boolean {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        //FIXME: fix service starting while already running
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.simpleName == service.service.className) {
                return true
            }
        }
        return false

    }

    private fun openSetMarkerBottomSheet(dialog: BottomSheetDialog, state: String) {
        //TODO: pull out bottom_sheet_set_marker
        //TODO: put game into barrier marker positioning state
        Log.d("MAP Set Marker", "State: $state");
        dialog.dismiss()
        val view = layoutInflater.inflate(R.layout.bottom_sheet_set_marker, null)
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }
}