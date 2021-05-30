package com.coolcats.challengehiking.view.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.coolcats.challengehiking.R
import com.coolcats.challengehiking.databinding.ActivityHomeBinding
import com.coolcats.challengehiking.util.Logger.Companion.logD
import com.coolcats.challengehiking.view.fragment.HomeFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().currentUser?.let {
            currentUser = it
        }

        logD("current user email: ${currentUser.email}")
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.main_frame, HomeFragment())
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
            else -> HomeFragment()
        }

        binding.mainFrame.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.main_frame, fragment)
            .commit()
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


}