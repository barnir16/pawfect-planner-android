package com.example.pawfectplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.pawfectplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var appBarCfg: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setSupportActionBar(b.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarCfg = AppBarConfiguration(
            setOf(
                R.id.aboutFragment,
                R.id.petListFragment,
                R.id.taskListFragment,
                R.id.settingsFragment
            ),
            b.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarCfg)
        NavigationUI.setupWithNavController(b.navView, navController)

        b.navView.setNavigationItemSelectedListener { item ->
            val handled = NavigationUI.onNavDestinationSelected(item, navController)
            if (handled) b.drawerLayout.closeDrawer(GravityCompat.START)
            handled
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        NavigationUI.navigateUp(
            (supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                .navController,
            appBarCfg
        )
}
