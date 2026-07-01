package com.app.habit

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.app.habit.databinding.ActivityMainBinding
import com.app.habit.ui.MainViewModel
import com.app.habit.util.SessionManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        
        val startDestination = if (!sessionManager.isLoggedIn()) {
            R.id.nav_login
        } else {
            R.id.nav_dashboard
        }
        
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard, R.id.nav_habits, R.id.nav_performance, R.id.nav_milestones, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        observeLoadingState()
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_login || destination.id == R.id.nav_signup) {
                binding.toolbar.visibility = View.GONE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else {
                binding.toolbar.visibility = View.VISIBLE
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                updateNavHeader(navView)
            }
        }
    }

    private fun updateNavHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val user = sessionManager.getUser() ?: return
        
        val tvName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        val ivProfile = headerView.findViewById<ImageView>(R.id.ivUserProfile)

        tvName.text = user.name
        tvEmail.text = user.email
        user.profileImage?.let {
            Glide.with(this).load(it).circleCrop().into(ivProfile)
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
                val navController = navHostFragment?.navController
                val currentDest = navController?.currentDestination?.id
                
                val isAuthDest = currentDest == R.id.nav_login || currentDest == R.id.nav_signup
                
                if (isAuthDest) {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.mainContent.visibility = View.VISIBLE
                    return@collectLatest
                }

                if (isLoading) {
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.mainContent.visibility = View.INVISIBLE
                } else {
                    binding.loadingOverlay.visibility = View.GONE
                    binding.mainContent.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}