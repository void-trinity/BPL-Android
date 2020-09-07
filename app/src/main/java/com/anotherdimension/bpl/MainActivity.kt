package com.anotherdimension.bpl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var navBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, LeaderboardFragment()).commit()
    }

    private fun setListeners() {
        navBar = nav_bar
        navBar.setOnNavigationItemSelectedListener(bottomNavigationSelectedListener)
    }

    private val bottomNavigationSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener {
            var fragment: Fragment? = null
            when (it.itemId) {
                R.id.menu_leaderboard -> fragment = LeaderboardFragment()
                R.id.menu_events -> fragment = EventsFragment()
                R.id.menu_profile -> fragment = ProfileFragment()
            }

            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment!!)
                .commit()

            true
        }
}