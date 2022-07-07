package elfak.mosis.capturetheflag

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import elfak.mosis.capturetheflag.databinding.ActivityMainBinding
import elfak.mosis.capturetheflag.game.map.LocationService
import elfak.mosis.capturetheflag.model.MainViewModel
import elfak.mosis.capturetheflag.model.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

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

        /*binding.fab.setOnClickListener { view ->
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
            val btnBarrier = view.findViewById<Button>(R.id.btnBarrier)
            btnBarrier.setOnClickListener {
                //TODO: pull out bottom_sheet_set_marker
                //TODO: put game into barrier marker positioning state
                dialog.dismiss()
            }

            val btnEnemyBarrier = view.findViewById<Button>(R.id.btnEnemyBarrier)
            btnEnemyBarrier.setOnClickListener {
                //TODO: pull out bottom_sheet_set_marker
                //TODO: put game into enemy barrier marker positioning state
                dialog.dismiss()
            }

            val btnEnemyFlag = view.findViewById<Button>(R.id.btnEnemyFlag)
            btnEnemyFlag.setOnClickListener {
                //TODO: pull out bottom_sheet_set_marker
                //TODO: put game into enemy flag marker positioning state
                dialog.dismiss()
            }
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }*/
        binding.fab.hide()

        /*navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if ( destination.id == R.id.MapFragment)
                binding.fab.show()
        }*/
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

    override fun onDestroy() {
        super.onDestroy()
        if (!mainViewModel.keepLocationServiceAlive) {
            stopService(Intent(this, LocationService().javaClass))
        }
    }
}