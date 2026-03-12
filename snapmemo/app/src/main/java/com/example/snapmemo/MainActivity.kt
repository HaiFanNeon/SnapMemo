package com.example.snapmemo

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.snapmemo.databinding.ActivityMainBinding
import com.example.snapmemo.ui.home.HomeViewModel
import com.example.snapmemo.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.exploreFragment, R.id.randomWalkFragment,
                R.id.aiSummaryFragment, R.id.attachmentFragment, R.id.archiveFragment,
                R.id.trashFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        setupDrawerNavigation()
        setupFab()
        observeDrawerStats()
    }

    private fun setupDrawerNavigation() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment)
                R.id.nav_explore -> navController.navigate(R.id.exploreFragment)
                R.id.nav_random_walk -> navController.navigate(R.id.randomWalkFragment)
                R.id.nav_ai_summary -> navController.navigate(R.id.aiSummaryFragment)
                R.id.nav_attachment -> navController.navigate(R.id.attachmentFragment)
                R.id.nav_archive -> navController.navigate(R.id.archiveFragment)
                R.id.nav_trash -> navController.navigate(R.id.trashFragment)
            }
            true
        }
    }

    private fun setupFab() {
        binding.fabNewMemo.setOnClickListener {
            navController.navigate(R.id.editorFragment)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.archiveFragment -> binding.fabNewMemo.show()
                else -> binding.fabNewMemo.hide()
            }
        }
    }

    private fun observeDrawerStats() {
        val headerView = binding.navView.getHeaderView(0)
        val tvNotesCount = headerView.findViewById<android.widget.TextView>(R.id.tv_notes_count)
        val tvTagsCount = headerView.findViewById<android.widget.TextView>(R.id.tv_tags_count)
        val tvDaysCount = headerView.findViewById<android.widget.TextView>(R.id.tv_days_count)

        lifecycleScope.launch {
            homeViewModel.totalCount.collectLatest { tvNotesCount?.text = it.toString() }
        }
        lifecycleScope.launch {
            homeViewModel.tagCount.collectLatest { tvTagsCount?.text = it.toString() }
        }
        lifecycleScope.launch {
            homeViewModel.activeDayCount.collectLatest { tvDaysCount?.text = it.toString() }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                navController.navigate(R.id.syncStatusFragment)
                true
            }
            R.id.action_settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
