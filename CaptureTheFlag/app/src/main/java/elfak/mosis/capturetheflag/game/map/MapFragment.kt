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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.data.MapObject
import elfak.mosis.capturetheflag.data.UserWithLocation
import elfak.mosis.capturetheflag.game.dialogs.MapFiltersDialog
import elfak.mosis.capturetheflag.game.dialogs.PlaceFlagDialog
import elfak.mosis.capturetheflag.game.dialogs.WaitForFlagDialog
import elfak.mosis.capturetheflag.game.viewmodel.GameViewModel
import elfak.mosis.capturetheflag.model.FriendsViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.enums.MapFilters
import elfak.mosis.capturetheflag.utils.extensions.FriendInfoWindow
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.gameID
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.isAppActive
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
    private val markerViewModel: MarkerViewModel by activityViewModels()

    private val friendsMarkers = ArrayList<Marker>()
    private val playersMarkers = ArrayList<Marker>()
    private val teamBarriersMarkers = ArrayList<Marker>()
    private val enemyBarriersMarkers = ArrayList<Marker>()
    private var teamFlagMarker: Marker? = null
    private var enemyFlagMarker: Marker? = null

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
        markerViewModel.getFriendsWithLocations()
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
        val fabFilters = requireView().findViewById<FloatingActionButton>(R.id.fabFilters)
        fabFilters.setOnClickListener { fabFiltersOnClick() }

        initMapView()
        setObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        //removeObservers()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        //setObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeObservers()
    }

    fun openFriendProfile(userUid: String){
        setFragmentResult("requestFriend", bundleOf("bundleFriend" to userUid))
        findNavController().navigate(R.id.action_MapFragment_to_ProfileFragment)
        Log.i("CLICK ON FRIEND", userUid ?: "empty")
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationOverlay() {
        val locationProvider = GpsMyLocationProvider(activity)
        val myLocationOverlay = MyLocationNewOverlay(locationProvider, map)
        myLocationOverlay.enableMyLocation()

        map.overlays.add(myLocationOverlay)
        myLocationOverlay.enableFollowLocation()
        map.controller.setCenter(myLocationOverlay.myLocation)
    }

    private fun setObservers() {
        val mapStateObserver = Observer<MapState> { state -> setMapStateObserver(state) }
        mapViewModel.mapState.observe(viewLifecycleOwner, mapStateObserver)

        val friendsWithLocationsObserver = Observer<MutableMap<String, UserWithLocation>> { state ->
            setUserWithLocationsObserver(state)
        }
        markerViewModel.friendsWithLocations.observe(viewLifecycleOwner, friendsWithLocationsObserver)

        val filtersObserver = Observer<MutableMap<String, Boolean>> { state ->
            setFiltersObserver(state)
        }
        markerViewModel.filters.observe(viewLifecycleOwner, filtersObserver)
    }

    private fun removeObservers() {
        gameViewModel.gameState.removeObservers(viewLifecycleOwner)
        mapViewModel.mapState.removeObservers(viewLifecycleOwner)
        markerViewModel.friendsWithLocations.removeObservers(viewLifecycleOwner)
        markerViewModel.filters.removeObservers(viewLifecycleOwner)
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
        if (type == MapFilters.TeamBarriers.value) {
            return context!!.getDrawable(R.drawable.ic_road_barrier_solid)
        }
        if (type == MapFilters.EnemyBarriers.value) {
            return context!!.getDrawable(R.drawable.ic_burst_solid)
        }
        if (type == MapFilters.EnemyFlag.value) {
            return context!!.getDrawable(R.drawable.ic_location_crosshairs_solid)
        }
        return null
    }
    private fun resolveIconColor(type: String) : Int {
        if (type == MapFilters.TeamBarriers.value) {
            return R.color.blue
        }
        if (type == MapFilters.EnemyBarriers.value || type== MapFilters.EnemyFlag.value) {
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
            openSetMarkerBottomSheet(dialog, MapFilters.TeamBarriers.value)
        }

        val btnEnemyBarrier = view.findViewById<Button>(R.id.btnEnemyBarrier)
        btnEnemyBarrier.setOnClickListener {
            openSetMarkerBottomSheet(dialog, MapFilters.EnemyBarriers.value)
        }

        val btnEnemyFlag = view.findViewById<Button>(R.id.btnEnemyFlag)
        btnEnemyFlag.setOnClickListener {
            openSetMarkerBottomSheet(dialog, MapFilters.EnemyFlag.value)
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun fabFiltersOnClick() {
        val dialog = MapFiltersDialog()
        dialog.show(activity!!.supportFragmentManager, "MapFiltersDialog")
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

    private fun setMapStateObserver(state: MapState) {
        val fab = requireView().findViewById<FloatingActionButton>(R.id.fab)
        val fabFilters = requireView().findViewById<FloatingActionButton>(R.id.fabFilters)

        when (state) {
            is MapState.ConfirmingMarker -> {
                fab.show()
                fabFilters.hide()
                mapObserverConfirmingMarker(state)
            }
            is MapState.Idle -> {
                fab.hide()
                fabFilters.show()
            }
            is MapState.InGame -> {
                //TODO: get info about game if not present
                fab.show()
                fabFilters.show()
            }
            is MapState.PlacingMarker -> {
                //TODO: idk dal treba nesto
                fab.hide()
                fabFilters.hide()
            }
            is MapState.BeginGame -> {
                val prefs = PreferenceHelper.customPreference(context!!, "User_data")
                prefs.gameID = gameViewModel.gameUid
                    if (gameViewModel.teams[gameViewModel.team]!!.memberCount > 0) {
                        val dialog = WaitForFlagDialog()
                        dialog.show(activity!!.supportFragmentManager, "WaitForFlagDialog")
                    }
                    else {
                        val dialog = PlaceFlagDialog()
                        dialog.show(activity!!.supportFragmentManager, "PlaceFlagDialog")
                    }
            }

            is MapState.PlacingFlag -> {
                fab.hide()
                fabFilters.hide()
            }
            is MapState.ConfirmingFlag -> {
                fab.show()
                fabFilters.hide()
                mapObserverConfirmingFlag(state)
            }
            is MapState.WaitingForFlags -> {
                fab.hide()
                fabFilters.show()
            }
            else -> {
                //TODO
            }
        }
    }

    private fun mapObserverConfirmingMarker(state: MapState.ConfirmingMarker) {
        val marker = Marker(map)
        marker.position = GeoPoint(state.latitude, state.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.icon = resolveMapIcon(state.type)
        marker.setInfoWindow(null)
        map.overlays.add(marker)


        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_confirm_marker, null)
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        btnAccept.setOnClickListener {
            //gameViewModel.putGameObjectToDB(gameViewModel.gameUid, state.type, gameViewModel.team, state.latitude, state.longitude)

            mapViewModel.setMapState(MapState.InGame)
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

    private fun mapObserverConfirmingFlag(state: MapState.ConfirmingFlag) {
        val marker = Marker(map)
        marker.position = GeoPoint(state.latitude, state.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.icon = context!!.getDrawable(R.drawable.ic_flag_solid)
        marker.setInfoWindow(null)
        map.overlays.add(marker)

        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_confirm_marker, null)
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        btnAccept.setOnClickListener {
            gameViewModel.objectType = MapFilters.TeamFlag.value
            gameViewModel.objectLatitude = state.latitude
            gameViewModel.objectLongitude = state.longitude
            mapViewModel.setMapState(MapState.WaitingForFlags)
            dialog.dismiss()
            Toast.makeText(
                requireContext(),
                "flag, lat: ${state.latitude}, long: ${state.longitude}",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_MapFragment_to_SetRiddleFragment)
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

    private fun setUserWithLocationsObserver(state: MutableMap<String, UserWithLocation>) {
        if (markerViewModel.filters.value?.get(MapFilters.Friends.value) == true) {
            drawFriends(state)
        }
    }

    private fun setFiltersObserver(state: MutableMap<String, Boolean>) {
        //TODO: go through each value and redraw markers
        if (state[MapFilters.Friends.value] == true && mapViewModel.mapState.value is MapState.Idle) {
            markerViewModel.friendsWithLocations.value?.let { drawFriends(it) }
        }
        else {
            friendsMarkers.forEach { marker ->
                map.overlays.remove(marker)
            }
            friendsMarkers.clear()
        }

        if (state[MapFilters.Players.value] == true && mapViewModel.mapState.value !is MapState.Idle) {
            //TODO: draw players on map
        }
        else {
            playersMarkers.forEach { marker ->
                map.overlays.remove(marker)
            }
            playersMarkers.clear()
        }

        if (state[MapFilters.TeamBarriers.value] == true && mapViewModel.mapState.value !is MapState.Idle) {
            //TODO: draw team barriers on map
        }
        else {
            teamBarriersMarkers.forEach { marker ->
                map.overlays.remove(marker)
            }
            teamBarriersMarkers.clear()
        }

        if (state[MapFilters.EnemyBarriers.value] == true && mapViewModel.mapState.value !is MapState.Idle) {
            //TODO: draw team barriers on map
        }
        else {
            enemyBarriersMarkers.forEach { marker ->
                map.overlays.remove(marker)
            }
            enemyBarriersMarkers.clear()
        }

        if (state[MapFilters.TeamFlag.value] == true && mapViewModel.mapState.value !is MapState.Idle) {
            //TODO: draw team flag on map
        }
        else {
            if (teamFlagMarker != null) {
                map.overlays.remove(teamFlagMarker)
            }
            teamFlagMarker = null
        }

        if (state[MapFilters.EnemyFlag.value] == true && mapViewModel.mapState.value !is MapState.Idle) {
            //TODO: draw enemy flag on map
        }
        else {
            if (enemyFlagMarker != null) {
                map.overlays.remove(enemyFlagMarker)
            }
            enemyFlagMarker = null
        }
    }

    private fun drawFriends(friends: MutableMap<String, UserWithLocation>) {
        friendsMarkers.forEach { marker ->
            map.overlays.remove(marker)
        }
        friendsMarkers.clear()

        friends.forEach { (uid, user) ->
            if (user.location != null) {
                val marker = Marker(map)
                marker.position = GeoPoint(user.location!!.latitude, user.location!!.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context!!.getDrawable(R.drawable.ic_person_solid)
                marker.icon.setTint(R.color.blue)
                user.user.uid = uid
                marker.setInfoWindow(FriendInfoWindow(R.layout.bubble_friend_marker, map, this, user.user))
                friendsMarkers.add(marker)
                map.overlays.add(marker)
            }
        }
    }

    private fun drawPlayers(friends: MutableMap<String, UserWithLocation>) {
        //TODO: implement
    }

    private fun drawTeamBarriers(teamBarriers: MutableMap<String, MapObject>) {
        teamBarriersMarkers.forEach { marker ->
            map.overlays.remove(marker)
        }
        teamBarriersMarkers.clear()

        teamBarriers.forEach { (uid, barrier) ->
            if (barrier != null) {
                val marker = Marker(map)
                marker.position = GeoPoint(barrier.latitude, barrier.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context!!.getDrawable(R.drawable.ic_person_solid)
                marker.icon.setTint(R.color.blue)
                marker.setInfoWindow(null)
                teamBarriersMarkers.add(marker)
                map.overlays.add(marker)
            }
        }
    }

    private fun drawEnemyBarriers(enemyBarriers: MutableMap<String, MapObject>) {
        enemyBarriersMarkers.forEach { marker ->
            map.overlays.remove(marker)
        }
        enemyBarriersMarkers.clear()

        enemyBarriers.forEach { (uid, barrier) ->
            if (barrier != null) {
                val marker = Marker(map)
                marker.position = GeoPoint(barrier.latitude, barrier.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.icon = context!!.getDrawable(R.drawable.ic_person_solid)
                marker.icon.setTint(R.color.blue)
                marker.setInfoWindow(null)
                enemyBarriersMarkers.add(marker)
                map.overlays.add(marker)
            }
        }
    }

    private fun drawTeamFlag() {
        //TODO: implement
    }

    private fun drawEnemyFlag() {
        //TODO: implement
    }
}