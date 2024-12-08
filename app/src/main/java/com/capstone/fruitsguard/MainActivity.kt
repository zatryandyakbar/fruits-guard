package com.capstone.fruitsguard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.capstone.fruitsguard.databinding.ActivityMainBinding
import com.capstone.fruitsguard.ui.DetectFragment
import com.capstone.fruitsguard.ui.FavoriteFragment
import com.capstone.fruitsguard.ui.HomeFragment
import com.capstone.fruitsguard.ui.ProfileActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, HomeFragment()).commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (supportFragmentManager.findFragmentByTag(HomeFragment::class.java.simpleName) == null) {
                        replaceFragment(HomeFragment())
                    }
                    true
                }
                R.id.nav_detect -> {
                    if (supportFragmentManager.findFragmentByTag(DetectFragment::class.java.simpleName) == null) {
                        replaceFragment(DetectFragment())
                    }
                    true
                }
                R.id.nav_favorite -> {
                    if (supportFragmentManager.findFragmentByTag(FavoriteFragment::class.java.simpleName) == null) {
                        replaceFragment(FavoriteFragment())
                    }
                    true
                }
                else -> false
            }
        }

        binding.toolbar.setOnMenuItemClickListener {item->
            when (item.itemId) {
                R.id.nav_profil -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment, fragment::class.java.simpleName)
        transaction.commit()
    }
}