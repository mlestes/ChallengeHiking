package com.coolcats.challengehiking.view.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.ActivityHomeBinding
import com.coolcats.challengehiking.db.UserDB.Companion.getUser
import com.coolcats.challengehiking.util.CHStatus
import com.coolcats.challengehiking.util.CHUtils.Companion.showError
import com.coolcats.challengehiking.util.Konstants
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.view.fragment.*
import com.coolcats.challengehiking.viewmod.AppViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navLayout: View
    private lateinit var currentUser: FirebaseUser
    private lateinit var fragment: Fragment
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) requestPermission()

        FirebaseAuth.getInstance().currentUser?.let {
            currentUser = it
        }

        getUser(currentUser)
        logD("current user email: ${currentUser.email}")
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.main_frame, BlankFragment())
            .commit()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        drawer = binding.drawerNavView
        toggle = setupToggle()

        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        val navItem = binding.drawerNavItemView
        setupDrawer(navItem)

        if (navItem.headerCount > 0) {
            navLayout = navItem.getHeaderView(0)
        }

        navLayout.findViewById<TextView>(R.id.user_name_header_text).text = currentUser.email

        viewModel.statusData.observe(this, {
            showStatus(it)
        })

    }

    private fun showStatus(chStatus: CHStatus) {
        when (chStatus) {
            CHStatus.LOADING -> binding.progressBar.visibility = View.VISIBLE
            CHStatus.SUCCESS -> binding.progressBar.visibility = View.GONE
            else -> {
                binding.progressBar.visibility = View.GONE
                showError(binding.root, "An Error Occurred...")
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    private fun setupToggle(): ActionBarDrawerToggle = ActionBarDrawerToggle(
        this,
        drawer,
        R.string.open_drawer,
        R.string.close_drawer
    )

    private fun selectDrawerItem(menuItem: MenuItem) {
        fragment = when (menuItem.itemId) {
            R.id.view_acc_info -> HomeFragment()
            R.id.get_hike_list -> FeedFragment()
            R.id.reset_pwd -> ResetPasswordFragment()
            R.id.del_acc -> DeleteUserFragment()
            R.id.logout -> LogOutFragment()
            R.id.settings -> SettingsFragment()
            R.id.new_hike -> HikingFragment()
            else -> BlankFragment()
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.main_frame, fragment)
            .commit()

        binding.drawerNavView.closeDrawers()
    }

    private fun setupDrawer(view: NavigationView) {
        view.setNavigationItemSelectedListener { item ->
            selectDrawerItem(item)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                Konstants.REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Konstants.REQUEST_CODE
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Konstants.REQUEST_CODE)
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    logD("Permissions granted")
                else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        requestPermission()
                    } else {
                        AlertDialog.Builder(
                            ContextThemeWrapper(
                                this,
                                R.style.ThemeOverlay_AppCompat
                            )
                        )
                            .setTitle("Permission Needed!")
                            .setMessage("Location Permission is required for this app to function! Uninstall if permissions cannot be granted.")
                            .setPositiveButton("Open Settings") { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data =
                                    Uri.fromParts("package", packageName, null)
                                startActivity(intent)
                            }.create().show()
                    }
                }

    }

}