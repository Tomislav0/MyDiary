package com.example.mydiary.Activities

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mydiary.*
import com.example.mydiary.Fragments.AccountFragment
import com.example.mydiary.Fragments.DiaryFragment
import com.example.mydiary.Fragments.OtherFragment
import com.example.mydiary.Fragments.StatisticsFragment
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.currUserStorageKey
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth

    lateinit var bottomNavBar : BottomNavigationView

    lateinit var diaryFragment: DiaryFragment
    lateinit var statisticsFragment: StatisticsFragment
    lateinit var accountFragment: AccountFragment
    lateinit var otherFragment: OtherFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = Firebase.auth
        diaryFragment = DiaryFragment()
        statisticsFragment = StatisticsFragment()
        accountFragment = AccountFragment()
        otherFragment = OtherFragment()

        bottomNavBar = findViewById(R.id.bottomNavigationBar)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameLayout,diaryFragment)
            commit()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, Constants.PERMISSION_CODE)
            }}


        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homeIcon ->{
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout,diaryFragment)
                        commit()
                    }
                }
                R.id.statisticsIcon ->{
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout,statisticsFragment)
                        commit()
                    }
                }
                R.id.accountIcon ->{
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout,accountFragment)
                        commit()
                    }
                }
                R.id.otherIcon ->{
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayout,otherFragment)
                        commit()
                    }
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        val prefs = getSharedPreferences(
            Constants.sharedPreferencesStorageKey,
            MODE_PRIVATE
        )
        val editor = prefs?.edit()

        if(auth.currentUser == null){
            editor?.putString(Constants.userEmailStorageKey,null)
            editor?.putString(Constants.userPasswordStorageKey,null)

            editor?.commit()
        }
    }

}