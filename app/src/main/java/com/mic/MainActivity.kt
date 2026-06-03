package com.mic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mic.databinding.ActivityMainBinding
import com.mic.libcore.utils.PermissionUtils


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        PermissionUtils.isGrantExternalRW(this, 1)
        PermissionUtils.requestPermissions(this)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNav
        bottomNavigationView.setupWithNavController(navController)
    }
}