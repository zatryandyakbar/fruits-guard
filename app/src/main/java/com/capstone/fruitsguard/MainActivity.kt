package com.capstone.fruitsguard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.fruitsguard.ui.DetectFragment
import com.capstone.fruitsguard.ui.FavoriteFragment
import com.capstone.fruitsguard.ui.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DetectFragment()).commit()
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
                    true
                }
                R.id.nav_detect -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DetectFragment()).commit()
                    true
                }
                R.id.nav_favorite -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FavoriteFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }
}