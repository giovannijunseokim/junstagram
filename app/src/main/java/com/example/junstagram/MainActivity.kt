package com.example.junstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import com.example.junstagram.databinding.ActivityMainBinding
import com.example.junstagram.navigation.AlarmFragment
import com.example.junstagram.navigation.DetailViewFragment
import com.example.junstagram.navigation.GridFragment
import com.example.junstagram.navigation.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
         when(item.itemId){
             R.id.actionHome -> {
                 var fragment = DetailViewFragment()
                 supportFragmentManager.beginTransaction().replace(R.id.mainContent,fragment).commit()
                 return true
             }
             R.id.actionAccount -> {
                 var fragment = UserFragment()
                 supportFragmentManager.beginTransaction().replace(R.id.mainContent,fragment).commit()
                 return true
             }
             R.id.actionSearch -> {
                 var fragment = GridFragment()
                 supportFragmentManager.beginTransaction().replace(R.id.mainContent,fragment).commit()
                 return true
             }
             R.id.actionAddPhoto -> {
                 return true
             }
             R.id.actionFavoriteAlarm -> {
                 var fragment = AlarmFragment()
                 supportFragmentManager.beginTransaction().replace(R.id.mainContent,fragment).commit()
                 return true
             }
         }
         return false
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)

    }
}