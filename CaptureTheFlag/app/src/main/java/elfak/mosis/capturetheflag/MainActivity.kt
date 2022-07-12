package elfak.mosis.capturetheflag

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import elfak.mosis.capturetheflag.data.User
import elfak.mosis.capturetheflag.databinding.ActivityMainBinding
import elfak.mosis.capturetheflag.game.gameover.RiddleFragment
import elfak.mosis.capturetheflag.game.map.LocationService
import elfak.mosis.capturetheflag.game.map.MapViewModel
import elfak.mosis.capturetheflag.game.map.MapViewModelFactory
import elfak.mosis.capturetheflag.model.MainViewModel
import elfak.mosis.capturetheflag.model.UserViewModel
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.isAppActive
import elfak.mosis.capturetheflag.utils.helpers.PreferenceHelper.opposingTeam


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    private var br : BroadcastReceiver? = null

    inner class MyBroadCastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("RECEIVER", "HI")
            val msg = intent?.getStringExtra("message")
            val imgID = intent?.getStringExtra("imgID")
            val gameID = intent?.getStringExtra("gameID")
            val team = intent?.getStringExtra("team")

            startRiddleFragment(msg, imgID, gameID, team)
        }
    }

    fun startRiddleFragment(msg: String?, imgID: String?, gameID: String?, team: String?){
//        val rf = RiddleFragment()
        val bundle = Bundle()
        bundle.putString("messageBundle", msg)
        bundle.putString("imgIDBundle", imgID)
        bundle.putString("gameIDBundle", gameID)
        bundle.putString("teamBundle", team)


        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_global_test, bundle)


//        val fm = fragmentManager
//        val transaction = fm.beginTransaction()
//        transaction.replace(R.id.RiddleFragment, rf as Fragment)
//        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        appBarConfiguration = AppBarConfiguration(setOf(R.id.ProfileForUserFragment, R.id.HomeFragment, R.id.MapFragment, R.id.FriendsFragment, R.id.RankingsFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.fab.hide()
        val prefs = PreferenceHelper.customPreference(this, "User_data")

        val filter = IntentFilter()
        filter.addAction("SOME_ACTION")

        br = MyBroadCastReceiver()
        registerReceiver(br, filter)

        prefs.isAppActive = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        val prefs = PreferenceHelper.customPreference(this, "User_data")
        prefs.isAppActive = false
    }

    override fun onDestroy() {
        if (br != null) {
            unregisterReceiver(br);
            br = null;
        }
        super.onDestroy()
        val prefs = PreferenceHelper.customPreference(this, "User_data")
        prefs.isAppActive = false

        if (!mainViewModel.keepLocationServiceAlive) {
            stopService(Intent(this, LocationService().javaClass))
        }
    }
}