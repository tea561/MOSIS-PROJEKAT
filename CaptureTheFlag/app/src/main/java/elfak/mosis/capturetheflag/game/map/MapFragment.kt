package elfak.mosis.capturetheflag.game.map

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.FriendsViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.userId
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment : Fragment() {

    private lateinit var mapViewModel: MapViewModel
    private lateinit var map: MapView
    private val userViewModel: UserViewModel by activityViewModels()
    private val friendsViewModel: FriendsViewModel by activityViewModels()
    private val gameViewModel: GameViewModel by activityViewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if(isGranted){
                setMyLocationOverlay()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity(),
            MapViewModelFactory(requireActivity().application, userViewModel.selectedUser!!.uid)).get(MapViewModel::class.java)
        setHasOptionsMenu(true)
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
        friendsViewModel.getFriends(userViewModel.selectedUser!!.uid)
        val fab = requireView().findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { fabOnClick() }

        val mapStateObserver = Observer<MapState> { state -> setMapStateObserver(state) }
        mapViewModel.mapState.observe(viewLifecycleOwner, mapStateObserver)

        initMapView()
        initFriendsOnMap()
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
                Log.e("MAP", "Location Service already running.")
                return true
            }
        }
        return false
    }

    private fun openSetMarkerBottomSheet(dialog: BottomSheetDialog, type: String) {
        Log.d("MAP Set Marker", "Type: $type")
        mapViewModel.setPlacingMarkerMapState(type)
        dialog.dismiss()
    }

    private fun resolveMapIcon(type: String) : Drawable? {
        if (type == "TeamBarrier") {
            return resources.getDrawable(R.drawable.ic_road_barrier_solid)
        }
        if (type == "EnemyBarrier") {
            return resources.getDrawable(R.drawable.ic_burst_solid)
        }
        if (type == "EnemyFlag") {
            return resources.getDrawable(R.drawable.ic_location_crosshairs_solid)
        }
        return null
    }
    private fun resolveIconColor(type: String) : Int {
        if (type == "TeamBarrier") {
            return R.color.blue
        }
        if (type == "EnemyBarrier" || type== "EnemyFlag") {
            return R.color.red_enemy
        }
        return R.color.black
    }

    private fun fabOnClick() {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(true)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        val btnBarrier = view.findViewById<Button>(R.id.btnBarrier)
        btnBarrier.setOnClickListener {
            openSetMarkerBottomSheet(dialog, "TeamBarrier")
        }

        val btnEnemyBarrier = view.findViewById<Button>(R.id.btnEnemyBarrier)
        btnEnemyBarrier.setOnClickListener {
            openSetMarkerBottomSheet(dialog, "EnemyBarrier")
        }

        val btnEnemyFlag = view.findViewById<Button>(R.id.btnEnemyFlag)
        btnEnemyFlag.setOnClickListener {
            openSetMarkerBottomSheet(dialog, "EnemyFlag")
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun setMapStateObserver(state: MapState) {
        val fab = requireView().findViewById<FloatingActionButton>(R.id.fab)
        if (state is MapState.ConfirmingMarker) {
            fab.show()
            mapObserverConfirmingMarker(state)
        }
        if (state is MapState.Idle) {
            fab.hide()
        }
        if (state is MapState.InGame) {
            //TODO: get info about game if not present
            fab.show()
        }
        if (state is MapState.PlacingMarker) {
            //TODO: idk dal treba nesto
            fab.show()
        }
        if (state is MapState.Cooldown) {
            //TODO: display border on map and cooldown bottom sheet
            fab.hide()
        }
    }

    private fun mapObserverConfirmingMarker(state: MapState.ConfirmingMarker) {
        val marker = Marker(map)
        marker.position = GeoPoint(state.latitude, state.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.icon = resolveMapIcon(state.type)
        map.overlays.add(marker)

        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_confirm_marker, null)
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        btnAccept.setOnClickListener {
            gameViewModel.putGameObjectToDB(gameViewModel.gameUid, state.type, gameViewModel.teamNumber, state.latitude, state.longitude)

            dialog.dismiss()
            Toast.makeText(
                requireContext(),
                "${state.type}, lat: ${state.latitude}, long: ${state.longitude}",
                Toast.LENGTH_SHORT
            ).show()
        }

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "NO.", Toast.LENGTH_SHORT).show()
            map.overlays.remove(marker)
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun initMapView() {
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
        val mapEventsOverlay = MapEventsOverlay(mapViewModel)
        map.overlays.add(mapEventsOverlay)


    }

    private fun initFriendsOnMap() {
        val friendsObserver = Observer<MutableList<User>> { state ->
            setFriendsObserver(state)
        }
        friendsViewModel.friends.observe(viewLifecycleOwner, friendsObserver)
        //friendsViewModel.getFriends(userViewModel.selectedUser!!.uid)
    }

    private fun setFriendsObserver(friendsList: MutableList<User>) {
        //TODO: draw friends on map
        if (friendsList.isEmpty()) {
            Toast.makeText(context, "No friends", Toast.LENGTH_SHORT).show()
        }
        friendsList.forEach{ friend ->
            val friendLocation = userViewModel.getLocationForUser(friend.uid)
            if (friendLocation != null) {
                val marker = Marker(map)
                marker.position = GeoPoint(friendLocation.latitude, friendLocation.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context!!.getDrawable(R.drawable.ic_person_solid)
                marker.icon.setTint(R.color.blue)
                map.overlays.add(marker)
            }
            else {
                Toast.makeText(context, "Location Null", Toast.LENGTH_SHORT).show()
            }
        }
    }
}