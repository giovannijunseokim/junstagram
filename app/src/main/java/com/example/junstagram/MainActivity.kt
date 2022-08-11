package com.example.junstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.junstagram.databinding.ActivityMainBinding
import com.example.junstagram.navigation.*
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
                 if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                     PackageManager.PERMISSION_GRANTED){
                     startActivity(Intent(this, AddPhotoActivity::class.java))
                 }
                 else{
                     Toast.makeText(baseContext,
                         "갤러리 사용 권한을 허가해 주세요.",
                         Toast.LENGTH_SHORT).show()
                 }
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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        binding.bottomNavigation.selectedItemId = R.id.actionHome
    }
}