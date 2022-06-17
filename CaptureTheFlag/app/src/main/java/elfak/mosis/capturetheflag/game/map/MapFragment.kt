package elfak.mosis.capturetheflag.game.map

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import elfak.mosis.capturetheflag.R
import elfak.mosis.capturetheflag.model.UserViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment : Fragment(), LocationListener {

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

        return inflater.inflate(R.layout.fragment_map, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx: Context? = activity?.applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        map = requireView().findViewById(R.id.map)

        if (ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_DENIED
            && ActivityCompat.checkSelfPermission(
                requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
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
        /*if(!checkLocationServiceRunning()) {
            val locationServiceIntent = Intent(this.context, LocationService().javaClass)
                .putExtra("userID", userViewModel.selectedUser!!.uid)
            requireActivity().startService(locationServiceIntent)
        }*/
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationOverlay() {
        val mLocationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, Float.MIN_VALUE, this)
        val locationProvider = GpsMyLocationProvider(activity)
        locationProvider.locationUpdateMinTime = 1000

        val myLocationOverlay = MyLocationNewOverlay(locationProvider, map)
        locationProvider.startLocationProvider(mapViewModel)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        //mapViewModel.setLocation(myLocationOverlay.myLocation)
        //mapViewModel.subscribeToLocationInDB()
        map.overlays.add(myLocationOverlay)
    }

    private fun checkLocationServiceRunning(): Boolean {
        val activityManager = requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (LocationService::class.simpleName == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onLocationChanged(location: Location) {
        Log.i("MapFragment", "onLocChanged!!!!!!!!!!!!!")
        //TODO: change location in VM
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.i("MapFragment", "onStatusChanged")
    }

}